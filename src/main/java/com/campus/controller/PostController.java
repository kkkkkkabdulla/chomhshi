package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.dto.request.PostPublishReq;
import com.campus.dto.response.PostPublishResp;
import com.campus.entity.Post;
import com.campus.entity.User;
import com.campus.interceptor.TokenInterceptor;
import com.campus.service.PostService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/post")
public class PostController {

    @Resource
    private PostService postService;

    @PostMapping("/publish")
    public Result<PostPublishResp> publish(@Valid @RequestBody PostPublishReq req, HttpServletRequest request) {
        User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
        Integer postId = postService.publish(user.getId(), req);
        return Result.success("发布成功，等待审核", new PostPublishResp(postId));
    }

    @GetMapping("/myList")
    public Result<PageResult<Post>> myList(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           HttpServletRequest request) {
        User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
        return Result.success(postService.myList(user.getId(), page, pageSize));
    }

    @GetMapping("/list")
    public Result<PageResult<Post>> list(@RequestParam(required = false) Integer type,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String category,
                                         @RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(postService.list(type, keyword, category, page, pageSize));
    }

    @GetMapping("/detail/{id}")
    public Result<Post> detail(@PathVariable("id") Integer id) {
        return Result.success(postService.detail(id));
    }
}
