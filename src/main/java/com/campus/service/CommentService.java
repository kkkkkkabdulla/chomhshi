package com.campus.service;

import com.campus.common.PageResult;
import com.campus.dto.request.CommentAddReq;
import com.campus.dto.response.PostCommentResp;

public interface CommentService {

    Integer add(Integer userId, CommentAddReq req);

    PageResult<PostCommentResp> list(Integer postId, Integer page, Integer pageSize);

    void delete(Integer userId, Integer commentId);
}
