package com.campus.service;

import com.campus.dto.request.UpdateUserInfoReq;
import com.campus.dto.request.WxLoginReq;
import com.campus.dto.response.LoginResp;
import com.campus.entity.User;

public interface UserService {

    LoginResp wxLogin(WxLoginReq req);

    User verifyToken(String token);

    User getUserById(Integer userId);

    void updateUserInfo(Integer userId, UpdateUserInfoReq req);
}
