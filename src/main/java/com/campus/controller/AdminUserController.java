package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.dto.request.AdminUserStatusReq;
import com.campus.dto.response.AdminUserDetailResp;
import com.campus.dto.response.AdminUserListItemResp;
import com.campus.interceptor.AdminTokenInterceptor;
import com.campus.entity.Admin;
import com.campus.service.AdminUserService;
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
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Resource
    private AdminUserService adminUserService;

    @GetMapping("/list")
    public Result<PageResult<AdminUserListItemResp>> list(@RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) Integer status,
                                                          @RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(adminUserService.list(keyword, status, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<AdminUserDetailResp> detail(@PathVariable("id") Integer id) {
        return Result.success(adminUserService.detail(id));
    }

    @PostMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable("id") Integer id,
                                     @Valid @RequestBody AdminUserStatusReq req,
                                     HttpServletRequest request) {
        Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
        adminUserService.updateStatus(admin.getId(), id, req.getStatus(), req.getReason(), req.getRemark());
        return Result.success("操作成功", null);
    }
}
