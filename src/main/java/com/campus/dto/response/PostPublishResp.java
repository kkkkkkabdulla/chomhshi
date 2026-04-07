package com.campus.dto.response;

public class PostPublishResp {

    private Integer postId;
    private Integer status;
    private boolean autoApproved;
    private String reviewNote;

    public PostPublishResp() {
    }

    public PostPublishResp(Integer postId, Integer status, boolean autoApproved, String reviewNote) {
        this.postId = postId;
        this.status = status;
        this.autoApproved = autoApproved;
        this.reviewNote = reviewNote;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public boolean isAutoApproved() {
        return autoApproved;
    }

    public void setAutoApproved(boolean autoApproved) {
        this.autoApproved = autoApproved;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }
}
