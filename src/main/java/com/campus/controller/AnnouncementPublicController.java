package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.entity.Announcement;
import com.campus.service.AnnouncementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/announcement")
public class AnnouncementPublicController {

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
}
