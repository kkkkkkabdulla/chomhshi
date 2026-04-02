package com.campus.mapper;

import org.apache.ibatis.annotations.Select;

public interface HealthMapper {

    @Select("SELECT 1")
    Integer pingDb();
}
