package com.campus.service;

import com.campus.common.PageResult;
import com.campus.dto.response.AdminUserDetailResp;
import com.campus.dto.response.AdminUserListItemResp;

public interface AdminUserService {

    PageResult<AdminUserListItemResp> list(String keyword, Integer status, Integer page, Integer pageSize);

    AdminUserDetailResp detail(Integer id);

    void updateStatus(Integer adminId, Integer id, Integer status, String reason, String remark);
}
