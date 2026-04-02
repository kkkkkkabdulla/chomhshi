package com.campus.mapper;

import com.campus.entity.UserToken;
import org.apache.ibatis.annotations.Param;

public interface UserTokenMapper {

    UserToken findByToken(@Param("token") String token);

    UserToken findByUserId(@Param("userId") Integer userId);

    int insert(UserToken userToken);

    int updateByUserId(UserToken userToken);
}
