package com.campus.dto.request;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

public class AdminPostRejectReq {

    @NotBlank(message = "拒绝原因不能为空")
    @Length(max = 200, message = "拒绝原因长度不能超过200")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
