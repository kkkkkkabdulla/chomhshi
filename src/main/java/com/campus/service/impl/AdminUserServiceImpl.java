package com.campus.service.impl;

import com.campus.common.BusinessException;
import com.campus.common.PageResult;
import com.campus.common.ResultCode;
import com.campus.dto.response.AdminUserDetailResp;
import com.campus.dto.response.AdminUserListItemResp;
import com.campus.entity.Post;
import com.campus.entity.PostComment;
import com.campus.mapper.AdminUserMapper;
import com.campus.service.AdminUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Resource
    private AdminUserMapper adminUserMapper;


    @Override
    public PageResult<AdminUserListItemResp> list(String keyword, String riskType, Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        int offset = (safePage - 1) * safePageSize;
        List<AdminUserListItemResp> list = adminUserMapper.findUsers(trimToNull(keyword), trimToNull(riskType), offset, safePageSize);
        long total = adminUserMapper.countUsers(trimToNull(keyword), trimToNull(riskType));
        return new PageResult<>(list, total, safePage, safePageSize);
    }

    @Override
    public AdminUserDetailResp detail(Integer id) {
        AdminUserDetailResp resp = adminUserMapper.findUserDetail(id);
        if (resp == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        resp.setPosts(castPosts(adminUserMapper.findUserPosts(id)));
        resp.setComments(castComments(adminUserMapper.findUserComments(id)));
        resp.setCollects(castPosts(adminUserMapper.findUserCollects(id)));
        return resp;
    }

    private String trimToNull(String str) {
        if (str == null) return null;
        String s = str.trim();
        return s.isEmpty() ? null : s;
    }

    @SuppressWarnings("unchecked")
    private List<Post> castPosts(List list) {
        if (list == null) return new ArrayList<>();
        return (List<Post>) list;
    }

    @SuppressWarnings("unchecked")
    private List<PostComment> castComments(List list) {
        if (list == null) return new ArrayList<>();
        return (List<PostComment>) list;
    }
}
