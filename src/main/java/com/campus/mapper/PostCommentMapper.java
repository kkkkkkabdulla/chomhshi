package com.campus.mapper;

import com.campus.dto.response.PostCommentResp;
import com.campus.entity.PostComment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PostCommentMapper {

    int insert(PostComment postComment);

    List<PostCommentResp> findByPostId(@Param("postId") Integer postId);

    long countByPostId(@Param("postId") Integer postId);

    PostComment findById(@Param("id") Integer id);

    int deleteById(@Param("id") Integer id);
}
