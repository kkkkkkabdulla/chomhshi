package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.dto.request.AnnouncementSaveReq;
import com.campus.entity.Announcement;
import com.campus.entity.Admin;
import com.campus.interceptor.AdminTokenInterceptor;
import com.campus.service.AnnouncementService;
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
@RequestMapping("/api/admin/announcement")
public class AnnouncementController {

    @Resource
    private AnnouncementService announcementService;

    @GetMapping("/latest")
    public Result<Announcement> latest() {
        return Result.success(announcementService.getLatestEnabled());
    }

    @GetMapping("/list")
    public Result<PageResult<Announcement>> list(@RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(announcementService.publicList(page, pageSize));
    }

    @GetMapping({"/listAdmin", "/admin/list"})
    public Result<PageResult<Announcement>> listAdmin(@RequestParam(defaultValue = "1") Integer page,
                                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                                      HttpServletRequest request) {
        Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
        announcementService.checkAdmin(admin.getId());
        return Result.success(announcementService.adminList(page, pageSize));
    }

    @GetMapping({"/{id}", "/admin/{id}"})
    public Result<Announcement> detail(@PathVariable("id") Integer id,
                                       HttpServletRequest request) {
        Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
        announcementService.checkAdmin(admin.getId());
        return Result.success(announcementService.getById(id));
    }

    @PostMapping({"/save", "/admin/save"})
    public Result<Void> save(@Valid @RequestBody AnnouncementSaveReq req, HttpServletRequest request) {
        Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
        announcementService.save(admin.getId(), null, req);
        return Result.success("保存成功", null);
    }

    @PostMapping({"/update/{id}", "/admin/update/{id}"})
    public Result<Void> update(@PathVariable("id") Integer id,
                               @Valid @RequestBody AnnouncementSaveReq req,
                               HttpServletRequest request) {
        Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
        announcementService.save(admin.getId(), id, req);
        return Result.success("更新成功", null);
    }

    @DeleteMapping({"/delete/{id}", "/admin/delete/{id}"})
    public Result<Void> delete(@PathVariable("id") Integer id, HttpServletRequest request) {
        Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
        announcementService.delete(admin.getId(), id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/enable/{id}")
    public Result<Void> enable(@PathVariable("id") Integer id, HttpServletRequest request) {
        Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
        announcementService.enableAnnouncement(admin.getId(), id);
        return Result.success("已启用", null);
    }
}
