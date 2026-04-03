package com.campus.mapper;

import com.campus.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    User findByPhone(@Param("phone") String phone);

    User findById(@Param("id") Integer id);

    int insert(User user);

    int updateUserInfoById(@Param("id") Integer id,
                           @Param("nickname") String nickname,
                           @Param("avatarUrl") String avatarUrl);
}
