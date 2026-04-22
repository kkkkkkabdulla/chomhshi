package com.campus.service;

import com.campus.common.PageResult;
import com.campus.dto.request.AnnouncementSaveReq;
import com.campus.entity.Announcement;

public interface AnnouncementService {

    Announcement getLatestEnabled();

    PageResult<Announcement> publicList(Integer page, Integer pageSize);

    PageResult<Announcement> adminList(Integer page, Integer pageSize);

    Announcement getById(Integer id);

    void checkAdmin(Integer adminId);

    void save(Integer adminId, Integer id, AnnouncementSaveReq req);

    void delete(Integer adminId, Integer id);

    void enableAnnouncement(Integer adminId, Integer id);

    Announcement getLatestEnabledForUser();
}
