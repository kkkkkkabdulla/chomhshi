package com.campus.dto.request;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

public class AdminLoginReq {

    @NotBlank(message = "账号不能为空")
    @Length(max = 50, message = "账号长度不能超过50")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Length(max = 100, message = "密码长度不能超过100")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
