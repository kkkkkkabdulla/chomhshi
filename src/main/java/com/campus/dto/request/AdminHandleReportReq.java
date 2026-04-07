package com.campus.dto.request;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

public class AdminHandleReportReq {

    @NotBlank(message = "处理动作不能为空")
    private String action;

    @Length(max = 200, message = "处理备注长度不能超过200")
    private String reason;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
