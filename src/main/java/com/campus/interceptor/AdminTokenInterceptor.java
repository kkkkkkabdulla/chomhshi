package com.campus.interceptor;

import com.campus.common.ResultCode;
import com.campus.entity.Admin;
import com.campus.service.AdminService;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AdminTokenInterceptor implements HandlerInterceptor {

    public static final String CURRENT_ADMIN_ATTR = "currentAdmin";

    @Resource
    private AdminService adminService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("token");
        if (token == null || token.trim().isEmpty()) {
            response.setStatus(ResultCode.UNAUTHORIZED.getCode());
            return false;
        }
        Admin admin = adminService.verifyToken(token);
        request.setAttribute(CURRENT_ADMIN_ATTR, admin);
        return true;
    }
}
