package com.campus.mapper;

import com.campus.entity.Post;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PostMapper {

    int insert(Post post);

    List<Post> findMyPosts(@Param("userId") Integer userId,
                           @Param("offset") Integer offset,
                           @Param("pageSize") Integer pageSize);

    long countMyPosts(@Param("userId") Integer userId);

    List<Post> findApprovedPosts(@Param("type") Integer type,
                                 @Param("keyword") String keyword,
                                 @Param("category") String category,
                                 @Param("offset") Integer offset,
                                 @Param("pageSize") Integer pageSize);

    long countApprovedPosts(@Param("type") Integer type,
                            @Param("keyword") String keyword,
                            @Param("category") String category);

    Post findApprovedDetailById(@Param("id") Integer id);

    Post findById(@Param("id") Integer id);

    int updateById(Post post);

    int deleteById(@Param("id") Integer id);
}
