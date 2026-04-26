package com.campus.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PostCollectMapper {

    Integer countByPostAndUser(@Param("postId") Integer postId,
                               @Param("userId") Integer userId);

    int insert(@Param("postId") Integer postId,
               @Param("userId") Integer userId);

    int delete(@Param("postId") Integer postId,
               @Param("userId") Integer userId);

    List<Integer> findUserCollects(@Param("userId") Integer userId);

    List<Integer> findCollectUsers(@Param("postId") Integer postId);
}