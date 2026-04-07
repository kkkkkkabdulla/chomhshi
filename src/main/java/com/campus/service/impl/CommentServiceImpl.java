package com.campus.service.impl;

import com.campus.common.BusinessException;
import com.campus.common.PageResult;
import com.campus.common.ResultCode;
import com.campus.dto.request.CommentAddReq;
import com.campus.dto.response.PostCommentResp;
import com.campus.entity.Post;
import com.campus.entity.PostComment;
import com.campus.mapper.PostCommentMapper;
import com.campus.mapper.PostMapper;
import com.campus.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private PostMapper postMapper;

    @Resource
    private PostCommentMapper postCommentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer add(Integer userId, CommentAddReq req) {
        Post post = postMapper.findById(req.getPostId());
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "帖子不存在或不可评论");
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
        return comment.getId();
    }

    @Override
    public PageResult<PostCommentResp> list(Integer postId, Integer page, Integer pageSize) {
        Post post = postMapper.findById(postId);
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "帖子不存在或不可查看评论");
        }

        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        int offset = (safePage - 1) * safePageSize;

        List<PostCommentResp> list = postCommentMapper.findByPostId(postId, offset, safePageSize);
        long total = postCommentMapper.countByPostId(postId);
        return new PageResult<>(list, total, safePage, safePageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer userId, Integer commentId) {
        PostComment comment = postCommentMapper.findById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "评论不存在");
        }
        if (!userId.equals(comment.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权删除他人评论");
        }

        int rows = postCommentMapper.deleteById(commentId);
        if (rows <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "删除评论失败");
        }
        postMapper.decreaseCommentCount(comment.getPostId());
    }
}
