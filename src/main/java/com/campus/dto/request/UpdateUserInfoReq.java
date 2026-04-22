package com.campus.dto.request;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

public class UpdateUserInfoReq {

    @NotBlank(message = "昵称不能为空")
    @Length(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    @NotBlank(message = "头像地址不能为空")
    @Length(max = 255, message = "头像地址长度不能超过255")
    private String avatarUrl;

    @NotBlank(message = "手机号不能为空")
    @Length(max = 20, message = "手机号长度不能超过20")
    private String phone;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
