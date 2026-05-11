package com.campus.service.impl;

import com.campus.common.BusinessException;
import com.campus.common.PageResult;
import com.campus.common.ResultCode;
import com.campus.dto.request.CommentAddReq;
import com.campus.dto.response.CommentAddResp;
import com.campus.dto.response.PostCommentResp;
import com.campus.entity.Post;
import com.campus.entity.PostComment;
import com.campus.entity.User;
import com.campus.mapper.PostCommentMapper;
import com.campus.mapper.PostMapper;
import com.campus.mapper.UserMapper;
import com.campus.service.CommentService;
import com.campus.service.RateLimitService;
import com.campus.service.SensitiveWordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private PostCommentMapper postCommentMapper;

    @Resource
    private PostMapper postMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private SensitiveWordService sensitiveWordService;

    @Resource
    private RateLimitService rateLimitService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentAddResp add(Integer userId, CommentAddReq req) {
        ensureUserNotBlocked(userId, "评论");
        rateLimitService.checkCommentLimit(userId);

        Post post = postMapper.findById(req.getPostId());
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "帖子不存在或不可评论");
        }

        String hitWord = sensitiveWordService.detectFirstHit(req.getContent());
        if (hitWord != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "评论内容包含违规信息");
        }

        if (req.getParentId() != null && req.getParentId() > 0) {
            PostComment parent = postCommentMapper.findById(req.getParentId());
            if (parent == null || !parent.getPostId().equals(req.getPostId())) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "回复的评论不存在");
            }
        }

        PostComment comment = new PostComment();
        comment.setPostId(req.getPostId());
        comment.setUserId(userId);
        comment.setContent(req.getContent().trim());
        comment.setParentId(req.getParentId() == null ? 0 : req.getParentId());

        int rows = postCommentMapper.insert(comment);
        if (rows <= 0 || comment.getId() == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "评论失败，请稍后重试");
        }

        postMapper.increaseCommentCount(req.getPostId());
        return new CommentAddResp(comment.getId());
    }

    @Override
    public PageResult<PostCommentResp> list(Integer postId, Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);

        List<PostCommentResp> allComments = postCommentMapper.findByPostId(postId);
        long total = postCommentMapper.countByPostId(postId);

        List<PostCommentResp> tree = buildTree(allComments);

        int offset = (safePage - 1) * safePageSize;
        int end = Math.min(offset + safePageSize, tree.size());
        List<PostCommentResp> pageList = offset < tree.size() ? tree.subList(offset, end) : new ArrayList<>();

        return new PageResult<>(pageList, total, safePage, safePageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer userId, Integer commentId) {
        PostComment comment = postCommentMapper.findById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "评论不存在");
        }
        if (!userId.equals(comment.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "只能删除自己的评论");
        }

        int childCount = countChildComments(commentId, comment.getPostId());
        int deleteCount = 1 + childCount;

        deleteCommentAndChildren(commentId, comment.getPostId());

        for (int i = 0; i < deleteCount; i++) {
            postMapper.decreaseCommentCount(comment.getPostId());
        }
    }

    private List<PostCommentResp> buildTree(List<PostCommentResp> allComments) {
        Map<Integer, PostCommentResp> map = new LinkedHashMap<>();
        for (PostCommentResp c : allComments) {
            c.setReplies(new ArrayList<>());
            map.put(c.getId(), c);
        }

        Map<Integer, Integer> rootMap = new LinkedHashMap<>();
        for (PostCommentResp c : allComments) {
            if (c.getParentId() == null || c.getParentId() == 0) {
                rootMap.put(c.getId(), c.getId());
            }
        }

        for (PostCommentResp c : allComments) {
            if (c.getParentId() != null && c.getParentId() > 0) {
                Integer rootId = findRootId(c.getParentId(), rootMap, map);
                rootMap.put(c.getId(), rootId);
            }
        }

        List<PostCommentResp> roots = new ArrayList<>();
        for (PostCommentResp c : allComments) {
            if (c.getParentId() == null || c.getParentId() == 0) {
                roots.add(c);
            } else {
                Integer rootId = rootMap.get(c.getId());
                if (rootId != null) {
                    PostCommentResp root = map.get(rootId);
                    if (root != null) {
                        root.getReplies().add(c);
                        continue;
                    }
                }
                roots.add(c);
            }
        }

        List<PostCommentResp> result = new ArrayList<>();
        List<PostCommentResp> reversed = new ArrayList<>(roots);
        java.util.Collections.reverse(reversed);
        for (PostCommentResp root : reversed) {
            result.add(root);
            if (root.getReplies() != null) {
                result.addAll(root.getReplies());
            }
        }
        return result;
    }

    private Integer findRootId(Integer commentId, Map<Integer, Integer> rootMap, Map<Integer, PostCommentResp> map) {
        if (rootMap.containsKey(commentId)) {
            return rootMap.get(commentId);
        }
        PostCommentResp c = map.get(commentId);
        if (c == null || c.getParentId() == null || c.getParentId() == 0) {
            return commentId;
        }
        return findRootId(c.getParentId(), rootMap, map);
    }

    private int countChildComments(Integer parentId, Integer postId) {
        List<PostCommentResp> all = postCommentMapper.findByPostId(postId);
        return countDescendants(parentId, all);
    }

    private int countDescendants(Integer parentId, List<PostCommentResp> all) {
        int count = 0;
        for (PostCommentResp c : all) {
            if (parentId.equals(c.getParentId())) {
                count += 1 + countDescendants(c.getId(), all);
            }
        }
        return count;
    }

    private void deleteCommentAndChildren(Integer commentId, Integer postId) {
        List<PostCommentResp> all = postCommentMapper.findByPostId(postId);
        for (PostCommentResp c : all) {
            if (commentId.equals(c.getParentId())) {
                deleteCommentAndChildren(c.getId(), postId);
            }
        }
        postCommentMapper.deleteById(commentId);
    }

    private void ensureUserNotBlocked(Integer userId, String actionName) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "您已被封禁，无法" + actionName);
        }
    }
}
