package com.campus.service.impl;

import com.campus.common.BusinessException;
import com.campus.common.PageResult;
import com.campus.common.ResultCode;
import com.campus.dto.request.AnnouncementSaveReq;
import com.campus.entity.Announcement;
import com.campus.mapper.AnnouncementMapper;
import com.campus.mapper.AdminMapper;
import com.campus.service.AnnouncementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Resource
    private AnnouncementMapper announcementMapper;

    @Resource
    private AdminMapper adminMapper;

    @Override
    public Announcement getLatestEnabled() {
        return announcementMapper.findLatestEnabled();
    }

    @Override
    public Announcement getLatestEnabledForUser() {
        return announcementMapper.findLatestEnabled();
    }

    @Override
    public PageResult<Announcement> publicList(Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        int offset = (safePage - 1) * safePageSize;
        List<Announcement> list = announcementMapper.findEnabledList(offset, safePageSize);
        long total = announcementMapper.countEnabled();
        return new PageResult<>(list, total, safePage, safePageSize);
    }

    @Override
    public PageResult<Announcement> adminList(Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        int offset = (safePage - 1) * safePageSize;
        List<Announcement> list = announcementMapper.findAdminList(offset, safePageSize);
        long total = announcementMapper.countAdmin();
        return new PageResult<>(list, total, safePage, safePageSize);
    }

    @Override
    public Announcement getById(Integer id) {
        Announcement ann = announcementMapper.findById(id);
        if (ann == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "公告不存在");
        }
        return ann;
    }

    @Override
    public void checkAdmin(Integer adminId) {
        if (adminMapper.findById(adminId) == null) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "管理员不存在或登录过期");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Integer adminId, Integer id, AnnouncementSaveReq req) {
        if (adminMapper.findById(adminId) == null) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "管理员不存在");
        }
        if (Integer.valueOf(1).equals(req.getStatus())) {
            announcementMapper.disableAll();
        }
        Announcement ann = new Announcement();
        ann.setTitle(req.getTitle());
        ann.setContent(req.getContent());
        ann.setStatus(req.getStatus());
        ann.setIsPinned(req.getIsPinned());
        ann.setCreatedBy(adminId);
        if (id == null || id <= 0) {
            int rows = announcementMapper.insert(ann);
            if (rows <= 0) throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "保存失败");
        } else {
            ann.setId(id);
            int rows = announcementMapper.updateById(ann);
            if (rows <= 0) throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "更新失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer adminId, Integer id) {
        if (announcementMapper.deleteById(id) <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "删除失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableAnnouncement(Integer adminId, Integer id) {
        if (adminMapper.findById(adminId) == null) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "管理员不存在");
        }
        if (announcementMapper.findById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "公告不存在");
        }
        announcementMapper.disableAll();
        announcementMapper.updateStatusById(id, 1);
    }
}
