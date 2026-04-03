package com.campus.dto.response;

public class PostPublishResp {

    private Integer postId;

    public PostPublishResp() {
    }

    public PostPublishResp(Integer postId) {
        this.postId = postId;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }
}
