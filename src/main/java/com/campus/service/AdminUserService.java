package com.campus.service;

import com.campus.common.PageResult;
import com.campus.dto.response.AdminUserDetailResp;
import com.campus.dto.response.AdminUserListItemResp;

public interface AdminUserService {

    PageResult<AdminUserListItemResp> list(String keyword, String riskType, Integer page, Integer pageSize);

    AdminUserDetailResp detail(Integer id);
}
