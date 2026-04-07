package com.campus.dto.response;

public class CommentAddResp {

    private Integer commentId;

    public CommentAddResp() {
    }

    public CommentAddResp(Integer commentId) {
        this.commentId = commentId;
    }

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }
}
