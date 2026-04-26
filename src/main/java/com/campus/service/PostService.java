package com.campus.service;

import com.campus.common.PageResult;
import com.campus.dto.request.PostPublishReq;
import com.campus.dto.response.PostLikeResp;
import com.campus.dto.response.PostCollectResp;
import com.campus.dto.response.PostPublishResp;
import com.campus.entity.Post;

public interface PostService {

    PostPublishResp publish(Integer userId, PostPublishReq req);

    PageResult<Post> myList(Integer userId, Integer page, Integer pageSize);

    PageResult<Post> list(Integer type, String keyword, String category, Integer page, Integer pageSize);

    Post detail(Integer id);

    Post detailForOwner(Integer userId, Integer id);

    void update(Integer userId, Integer postId, PostPublishReq req);

    void delete(Integer userId, Integer postId);

    PostLikeResp toggleLike(Integer userId, Integer postId);

    boolean isLiked(Integer userId, Integer postId);

    PostCollectResp toggleCollect(Integer userId, Integer postId);

    boolean isCollected(Integer userId, Integer postId);

    PageResult<Post> myCollects(Integer userId, Integer page, Integer pageSize);
}
