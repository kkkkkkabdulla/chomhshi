package com.campus.dto.request;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CommentAddReq {

    @NotNull(message = "postId不能为空")
    @Min(value = 1, message = "postId非法")
    private Integer postId;

    @NotBlank(message = "评论内容不能为空")
    @Length(max = 500, message = "评论内容最多500字")
    private String content;

    @Min(value = 0, message = "parentId非法")
    private Integer parentId = 0;

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }
}
