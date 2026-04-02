package com.campus.controller;

import com.campus.common.Result;
import com.campus.dto.request.WxLoginReq;
import com.campus.dto.response.LoginResp;
import com.campus.dto.response.UserInfoResp;
import com.campus.entity.User;
import com.campus.interceptor.TokenInterceptor;
import com.campus.service.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/wxLogin")
    public Result<LoginResp> wxLogin(@Valid @RequestBody WxLoginReq req) {
        return Result.success("登录成功", userService.wxLogin(req));
    }

    @GetMapping("/verifyToken")
    public Result<UserInfoResp> verifyToken(HttpServletRequest request) {
        User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
        UserInfoResp resp = convert(user);
        return Result.success("token有效", resp);
    }

    @GetMapping("/info")
    public Result<UserInfoResp> info(HttpServletRequest request) {
        User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
        UserInfoResp resp = convert(userService.getUserById(user.getId()));
        return Result.success(resp);
    }

    private UserInfoResp convert(User user) {
        UserInfoResp resp = new UserInfoResp();
        resp.setId(user.getId());
        resp.setPhone(user.getPhone());
        resp.setNickname(user.getNickname());
        resp.setAvatarUrl(user.getAvatarUrl());
        return resp;
    }
}
