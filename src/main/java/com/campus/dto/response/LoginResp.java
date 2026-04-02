package com.campus.dto.response;

public class LoginResp {

    private String token;
    private UserInfoResp user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfoResp getUser() {
        return user;
    }

    public void setUser(UserInfoResp user) {
        this.user = user;
    }
}
