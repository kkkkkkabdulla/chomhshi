package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.dto.request.CommentAddReq;
import com.campus.dto.response.CommentAddResp;
import com.campus.dto.response.PostCommentResp;
import com.campus.entity.User;
import com.campus.interceptor.TokenInterceptor;
import com.campus.service.CommentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @PostMapping("/add")
    public Result<CommentAddResp> add(@Valid @RequestBody CommentAddReq req, HttpServletRequest request) {
        User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
        Integer commentId = commentService.add(user.getId(), req);
        return Result.success("评论成功", new CommentAddResp(commentId));
    }

    @GetMapping("/list/{postId}")
    public Result<PageResult<PostCommentResp>> list(@PathVariable("postId") Integer postId,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(commentService.list(postId, page, pageSize));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable("id") Integer id, HttpServletRequest request) {
        User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
        commentService.delete(user.getId(), id);
        return Result.success("删除成功", null);
    }
}
