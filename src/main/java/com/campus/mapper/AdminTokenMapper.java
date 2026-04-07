package com.campus.mapper;

import com.campus.entity.AdminToken;
import org.apache.ibatis.annotations.Param;

public interface AdminTokenMapper {

    AdminToken findByToken(@Param("token") String token);

    AdminToken findByAdminId(@Param("adminId") Integer adminId);

    int insert(AdminToken adminToken);

    int updateByAdminId(AdminToken adminToken);
}
