package com.campus.mapper;

import com.campus.entity.PostReport;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PostReportMapper {

    int insert(PostReport report);

    List<PostReport> findByStatus(@Param("status") Integer status,
                                  @Param("offset") Integer offset,
                                  @Param("pageSize") Integer pageSize);

    long countByStatus(@Param("status") Integer status);

    PostReport findById(@Param("id") Integer id);

    int updateHandle(@Param("id") Integer id,
                     @Param("status") Integer status,
                     @Param("adminId") Integer adminId);
}
