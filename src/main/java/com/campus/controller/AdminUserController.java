package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.dto.response.AdminUserDetailResp;
import com.campus.dto.response.AdminUserListItemResp;
import com.campus.service.AdminUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Resource
    private AdminUserService adminUserService;

    @GetMapping("/list")
    public Result<PageResult<AdminUserListItemResp>> list(@RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) String riskType,
                                                          @RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(adminUserService.list(keyword, riskType, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<AdminUserDetailResp> detail(@PathVariable("id") Integer id) {
        return Result.success(adminUserService.detail(id));
    }
}
