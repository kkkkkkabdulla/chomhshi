package com.campus.controller;

import com.campus.common.Result;
import com.campus.common.PageResult;
import com.campus.dto.request.AdminHandleReportReq;
import com.campus.dto.request.AdminLoginReq;
import com.campus.dto.request.AdminPostRejectReq;
import com.campus.dto.response.LoginResp;
import com.campus.entity.Admin;
import com.campus.entity.Post;
import com.campus.entity.PostReport;
import com.campus.interceptor.AdminTokenInterceptor;
import com.campus.service.AdminService;
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
import java.util.HashMap;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Resource
    private AdminService adminService;

    @PostMapping("/login")
    public Result<LoginResp> login(@Valid @RequestBody AdminLoginReq req) {
        return Result.success("登录成功", adminService.login(req));
    }

    @GetMapping("/profile")
    public Result<Map<String, Object>> profile(HttpServletRequest request) {
        Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
        Map<String, Object> data = new HashMap<>();
        data.put("id", admin.getId());
        data.put("username", admin.getUsername());
        return Result.success(data);
    }

    @GetMapping("/post/list")
    public Result<PageResult<Post>> postList(@RequestParam(required = false) Integer status,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(adminService.postList(status, page, pageSize));
    }

    @PostMapping("/post/approve/{id}")
    public Result<Void> approve(@PathVariable("id") Integer id, HttpServletRequest request) {
        Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
        adminService.approvePost(admin.getId(), id);
        return Result.success("审核通过", null);
    }

    @PostMapping("/post/reject/{id}")
    public Result<Void> reject(@PathVariable("id") Integer id,
                               @Valid @RequestBody AdminPostRejectReq req,
                               HttpServletRequest request) {
        Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
        adminService.rejectPost(admin.getId(), id, req);
        return Result.success("已拒绝", null);
    }

    @DeleteMapping("/post/delete/{id}")
    public Result<Void> deletePost(@PathVariable("id") Integer id, HttpServletRequest request) {
        Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
        adminService.deletePost(admin.getId(), id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/post/reportList")
    public Result<PageResult<PostReport>> reportList(@RequestParam(required = false) Integer status,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(adminService.reportList(status, page, pageSize));
    }

    @PostMapping("/post/handleReport/{id}")
    public Result<Void> handleReport(@PathVariable("id") Integer id,
                                     @Valid @RequestBody AdminHandleReportReq req,
                                     HttpServletRequest request) {
        Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
        adminService.handleReport(admin.getId(), id, req);
        return Result.success("处理成功", null);
    }

    @PostMapping("/post/restore/{id}")
    public Result<Void> restore(@PathVariable("id") Integer id, HttpServletRequest request) {
        Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
        adminService.restorePost(admin.getId(), id);
        return Result.success("已恢复", null);
    }
}
