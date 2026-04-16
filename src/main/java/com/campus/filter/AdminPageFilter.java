package com.campus.filter;

import com.campus.service.AdminService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 管理后台页面鉴权过滤器
 * 保护所有 /admin/* 页面,未登录或 token 无效用户重定向到登录页
 */
public class AdminPageFilter implements Filter {

    private AdminService adminService;

    @Override
    public void init(FilterConfig filterConfig) {
        WebApplicationContext context = WebApplicationContextUtils
                .getWebApplicationContext(filterConfig.getServletContext());
        if (context != null) {
            this.adminService = context.getBean(AdminService.class);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String ctx = httpRequest.getContextPath();
        String path = uri.substring(ctx.length());

        // 放行登录页，避免死循环重定向
        if ("/admin/login".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        String token = httpRequest.getParameter("t");

        if (isBlank(token)) {
            token = httpRequest.getHeader("token");
        }

        if (isBlank(token)) {
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("adminToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (isBlank(token)) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/admin/login");
            return;
        }

        // token 必须可被后端校验通过，才能访问后台页面
        if (adminService == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/admin/login");
            return;
        }

        try {
            adminService.verifyToken(token);
        } catch (Exception ex) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/admin/login");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Override
    public void destroy() {
    }
}
