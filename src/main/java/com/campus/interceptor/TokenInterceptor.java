package com.campus.interceptor;

import com.campus.common.ResultCode;
import com.campus.entity.User;
import com.campus.service.UserService;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TokenInterceptor implements HandlerInterceptor {

    public static final String CURRENT_USER_ATTR = "currentUser";

    @Resource
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("token");
        if (token == null || token.trim().isEmpty()) {
            response.setStatus(ResultCode.UNAUTHORIZED.getCode());
            return false;
        }
        User user = userService.verifyToken(token);
        request.setAttribute(CURRENT_USER_ATTR, user);
        return true;
    }
}
