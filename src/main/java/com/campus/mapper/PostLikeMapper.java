package com.campus.mapper;

import org.apache.ibatis.annotations.Param;

public interface PostLikeMapper {

    Integer countByPostAndUser(@Param("postId") Integer postId,
                               @Param("userId") Integer userId);

    int insert(@Param("postId") Integer postId,
               @Param("userId") Integer userId);

    int delete(@Param("postId") Integer postId,
               @Param("userId") Integer userId);
}
