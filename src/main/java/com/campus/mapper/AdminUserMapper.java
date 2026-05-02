package com.campus.mapper;

import com.campus.dto.response.AdminUserDetailResp;
import com.campus.dto.response.AdminUserListItemResp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminUserMapper {

    List<AdminUserListItemResp> findUsers(@Param("keyword") String keyword,
                                          @Param("status") Integer status,
                                          @Param("offset") Integer offset,
                                          @Param("pageSize") Integer pageSize);

    long countUsers(@Param("keyword") String keyword,
                    @Param("status") Integer status);

    AdminUserDetailResp findUserDetail(@Param("id") Integer id);

    List findUserPosts(@Param("id") Integer id);

    List findUserComments(@Param("id") Integer id);

    List findUserCollects(@Param("id") Integer id);
}
