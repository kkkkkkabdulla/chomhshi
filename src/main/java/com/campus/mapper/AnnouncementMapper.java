package com.campus.mapper;

import com.campus.entity.Announcement;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AnnouncementMapper {

    int insert(Announcement announcement);

    int updateById(Announcement announcement);

    int deleteById(@Param("id") Integer id);

    Announcement findById(@Param("id") Integer id);

    Announcement findLatestEnabled();

    List<Announcement> findEnabledList(@Param("offset") Integer offset,
                                       @Param("pageSize") Integer pageSize);

    long countEnabled();

    List<Announcement> findAdminList(@Param("offset") Integer offset,
                                     @Param("pageSize") Integer pageSize);

    long countAdmin();

    int disableAll();

    int updateStatusById(@Param("id") Integer id, @Param("status") Integer status);
}
