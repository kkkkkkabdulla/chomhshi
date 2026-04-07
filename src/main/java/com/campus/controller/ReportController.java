package com.campus.controller;

import com.campus.common.Result;
import com.campus.dto.request.ReportAddReq;
import com.campus.entity.User;
import com.campus.interceptor.TokenInterceptor;
import com.campus.service.ReportService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Resource
    private ReportService reportService;

    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody ReportAddReq req, HttpServletRequest request) {
        User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
        boolean offline = reportService.addReport(user.getId(), req);
        if (offline) {
            return Result.success("举报成功，该帖子已被下架", null);
        }
        return Result.success("举报成功", null);
    }
}
