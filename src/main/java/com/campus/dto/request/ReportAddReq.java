package com.campus.dto.request;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

public class ReportAddReq {

    @NotNull(message = "帖子ID不能为空")
    private Integer postId;

    @NotNull(message = "举报原因类型不能为空")
    private Integer reasonType;

    @Length(max = 500, message = "举报补充描述长度不能超过500")
    private String reasonDesc;

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Integer getReasonType() {
        return reasonType;
    }

    public void setReasonType(Integer reasonType) {
        this.reasonType = reasonType;
    }

    public String getReasonDesc() {
        return reasonDesc;
    }

    public void setReasonDesc(String reasonDesc) {
        this.reasonDesc = reasonDesc;
    }
}
