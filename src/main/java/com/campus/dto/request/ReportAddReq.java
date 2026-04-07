package com.campus.dto.request;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ReportAddReq {

    @NotNull(message = "帖子ID不能为空")
    private Integer postId;

    @NotBlank(message = "举报原因不能为空")
    @Length(max = 100, message = "举报原因长度不能超过100")
    private String reason;

    @Length(max = 500, message = "举报描述长度不能超过500")
    private String description;

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
