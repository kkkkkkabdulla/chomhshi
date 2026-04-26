package com.campus.service.impl;

import com.campus.common.BusinessException;
import com.campus.common.PageResult;
import com.campus.common.ResultCode;
import com.campus.dto.request.PostPublishReq;
import com.campus.dto.response.PostCollectResp;
import com.campus.dto.response.PostLikeResp;
import com.campus.dto.response.PostPublishResp;
import com.campus.entity.Post;
import com.campus.mapper.PostCollectMapper;
import com.campus.mapper.PostLikeMapper;
import com.campus.mapper.PostMapper;
import com.campus.service.PostService;
import com.campus.service.SensitiveWordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private static final int POST_TYPE_LOST_FOUND = 1;
    private static final int POST_TYPE_SECOND_HAND = 2;
    private static final int POST_TYPE_ANNOUNCEMENT = 3;

    @Resource
    private PostMapper postMapper;

    @Resource
    private SensitiveWordService sensitiveWordService;

    @Resource
    private PostLikeMapper postLikeMapper;

    @Resource
    private PostCollectMapper postCollectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostPublishResp publish(Integer userId, PostPublishReq req) {
        validateTypeSpecificFields(req);

        String hitWord = detectSensitiveContent(req);
        boolean autoApproved = (hitWord == null);
        int status = autoApproved ? 1 : 0;

        Post post = new Post();
        post.setUserId(userId);
        post.setType(req.getType());
        post.setTitle(req.getTitle());
        post.setDescription(req.getDescription());
        post.setCategory(req.getCategory());
        post.setPrice(req.getPrice());
        post.setLocation(req.getLocation());
        post.setLostFoundTime(parseLostFoundTime(req.getLostFoundTime()));
        post.setLostStatus(req.getLostStatus());
        post.setImages(req.getImages());
        post.setContact(req.getContact());

        // 自动审核逻辑：未命中敏感词 -> 直接通过(status=1)，命中 -> 进入人工审核(status=0)
        post.setStatus(status);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setReportCount(0);

        int rows = postMapper.insert(post);
        if (rows <= 0 || post.getId() == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "发布失败，请稍后重试");
        }

        String reviewNote = autoApproved ? "自动审核通过，已上架" : ("命中敏感词「" + hitWord + "」，已进入人工审核队列");
        return new PostPublishResp(post.getId(), status, autoApproved, reviewNote);
    }

    @Override
    public PageResult<Post> myList(Integer userId, Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        int offset = (safePage - 1) * safePageSize;

        List<Post> list = postMapper.findMyPosts(userId, offset, safePageSize);
        long total = postMapper.countMyPosts(userId);
        return new PageResult<>(list, total, safePage, safePageSize);
    }

    @Override
    public PageResult<Post> list(Integer type, String keyword, String category, Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        int offset = (safePage - 1) * safePageSize;

        List<Post> list = postMapper.findApprovedPosts(type, trimToNull(keyword), trimToNull(category), offset, safePageSize);
        long total = postMapper.countApprovedPosts(type, trimToNull(keyword), trimToNull(category));
        return new PageResult<>(list, total, safePage, safePageSize);
    }

    @Override
    public Post detail(Integer id) {
        Post post = postMapper.findApprovedDetailById(id);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "帖子不存在或未通过审核");
        }
        return post;
    }

    @Override
    public Post detailForOwner(Integer userId, Integer id) {
        Post post = postMapper.findById(id);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "帖子不存在");
        }
        if (!userId.equals(post.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权查看他人帖子");
        }
        return post;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Integer userId, Integer postId, PostPublishReq req) {
        Post post = requireOwnedPost(userId, postId);
        if (post.getStatus() != null && post.getStatus() != 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "仅待审核帖子允许编辑");
        }

        validateTypeSpecificFields(req);
        checkSensitiveContentForUpdate(req);

        post.setTitle(req.getTitle());
        post.setDescription(req.getDescription());
        post.setCategory(req.getCategory());
        post.setPrice(req.getPrice());
        post.setLocation(req.getLocation());
        post.setLostFoundTime(parseLostFoundTime(req.getLostFoundTime()));
        post.setLostStatus(req.getLostStatus());
        post.setImages(req.getImages());
        post.setContact(req.getContact());

        int rows = postMapper.updateById(post);
        if (rows <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "编辑失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer userId, Integer postId) {
        Post post = requireOwnedPost(userId, postId);
        int rows = postMapper.deleteById(postId);
        if (rows <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "删除失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostLikeResp toggleLike(Integer userId, Integer postId) {
        Post post = postMapper.findById(postId);
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "帖子不存在或不可点赞");
        }

        Integer count = postLikeMapper.countByPostAndUser(postId, userId);
        boolean currentlyLiked = count != null && count > 0;

        if (currentlyLiked) {
            postLikeMapper.delete(postId, userId);
            postMapper.decreaseLikeCount(postId);
        } else {
            postLikeMapper.insert(postId, userId);
            postMapper.increaseLikeCount(postId);
        }

        Post latest = postMapper.findById(postId);
        return new PostLikeResp(!currentlyLiked, latest == null ? 0 : latest.getLikeCount());
    }

    @Override
    public boolean isLiked(Integer userId, Integer postId) {
        Integer count = postLikeMapper.countByPostAndUser(postId, userId);
        return count != null && count > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostCollectResp toggleCollect(Integer userId, Integer postId) {
        Post post = postMapper.findById(postId);
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "帖子不存在或不可收藏");
        }

        Integer count = postCollectMapper.countByPostAndUser(postId, userId);
        boolean currentlyCollected = count != null && count > 0;

        if (currentlyCollected) {
            postCollectMapper.delete(postId, userId);
            postMapper.decreaseCollectCount(postId);
        } else {
            postCollectMapper.insert(postId, userId);
            postMapper.increaseCollectCount(postId);
        }

        Post latest = postMapper.findById(postId);
        return new PostCollectResp(!currentlyCollected, latest == null ? 0 : latest.getCollectCount());
    }

    @Override
    public boolean isCollected(Integer userId, Integer postId) {
        Integer count = postCollectMapper.countByPostAndUser(postId, userId);
        return count != null && count > 0;
    }

    @Override
    public PageResult<Post> myCollects(Integer userId, Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        int offset = (safePage - 1) * safePageSize;

        List<Integer> collectPostIds = postCollectMapper.findUserCollects(userId);
        if (collectPostIds == null || collectPostIds.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0, safePage, safePageSize);
        }

        List<Post> list = postMapper.findPostsByIds(collectPostIds, offset, safePageSize);
        long total = collectPostIds.size();
        return new PageResult<>(list, total, safePage, safePageSize);
    }

    private void validateTypeSpecificFields(PostPublishReq req) {
        if (req.getType() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "帖子类型不能为空");
        }

        if (req.getType() == POST_TYPE_ANNOUNCEMENT) {
            if (!"公告".equals(req.getCategory())) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "公告类型的分类必须为公告");
            }
            return;
        }

        if (isBlank(req.getContact())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "联系方式不能为空");
        }

        if (POST_TYPE_SECOND_HAND == req.getType() && "二手物品".equals(req.getCategory())) {
            if (req.getPrice() == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "二手物品价格不能为空");
            }
        }
    }

    private String detectSensitiveContent(PostPublishReq req) {
        String hitInTitle = sensitiveWordService.detectFirstHit(req.getTitle());
        if (hitInTitle != null) {
            return hitInTitle;
        }
        return sensitiveWordService.detectFirstHit(req.getDescription());
    }

    private void checkSensitiveContentForUpdate(PostPublishReq req) {
        String hitWord = detectSensitiveContent(req);
        if (hitWord != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "内容包含敏感词：" + hitWord);
        }
    }

    private Date parseLostFoundTime(String time) {
        if (isBlank(time)) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time.trim());
        } catch (ParseException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "lostFoundTime 格式应为 yyyy-MM-dd HH:mm:ss");
        }
    }

    private Post requireOwnedPost(Integer userId, Integer postId) {
        Post post = postMapper.findById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "帖子不存在");
        }
        if (!userId.equals(post.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作他人帖子");
        }
        return post;
    }

    private String trimToNull(String str) {
        if (str == null) {
            return null;
        }
        String val = str.trim();
        return val.isEmpty() ? null : val;
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
