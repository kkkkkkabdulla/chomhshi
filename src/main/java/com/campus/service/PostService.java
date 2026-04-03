package com.campus.service;

import com.campus.common.PageResult;
import com.campus.dto.request.PostPublishReq;
import com.campus.entity.Post;

public interface PostService {

    Integer publish(Integer userId, PostPublishReq req);

    PageResult<Post> myList(Integer userId, Integer page, Integer pageSize);

    PageResult<Post> list(Integer type, String keyword, String category, Integer page, Integer pageSize);

    Post detail(Integer id);
}
