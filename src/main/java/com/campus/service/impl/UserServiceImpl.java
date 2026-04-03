package com.campus.service.impl;

import com.campus.common.BusinessException;
import com.campus.common.ResultCode;
import com.campus.dto.request.UpdateUserInfoReq;
import com.campus.dto.request.WxLoginReq;
import com.campus.dto.response.LoginResp;
import com.campus.dto.response.UserInfoResp;
import com.campus.entity.User;
import com.campus.entity.UserToken;
import com.campus.mapper.UserMapper;
import com.campus.mapper.UserTokenMapper;
import com.campus.service.UserService;
import com.campus.util.TokenUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserTokenMapper userTokenMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResp wxLogin(WxLoginReq req) {
        String mockPhone = "13" + req.getCode().replaceAll("\\D", "") + "000000";
        if (mockPhone.length() > 11) {
            mockPhone = mockPhone.substring(0, 11);
        }
        while (mockPhone.length() < 11) {
            mockPhone = mockPhone + "0";
        }

        User user = userMapper.findByPhone(mockPhone);
        if (user == null) {
            user = new User();
            user.setOpenid("mock_" + UUID.randomUUID().toString().replace("-", ""));
            user.setPhone(mockPhone);
            user.setNickname("用户" + mockPhone.substring(7));
            user.setAvatarUrl("https://example.com/default-avatar.png");
            user.setRole(1);
            user.setStatus(1);
            userMapper.insert(user);
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "账号已被封禁");
        }

        String token = TokenUtil.generateToken();
        Date expireTime = buildExpireTime(7);

        UserToken oldToken = userTokenMapper.findByUserId(user.getId());
        if (oldToken == null) {
            UserToken newToken = new UserToken();
            newToken.setUserId(user.getId());
            newToken.setToken(token);
            newToken.setExpireTime(expireTime);
            userTokenMapper.insert(newToken);
        } else {
            oldToken.setToken(token);
            oldToken.setExpireTime(expireTime);
            userTokenMapper.updateByUserId(oldToken);
        }

        LoginResp resp = new LoginResp();
        resp.setToken(token);
        resp.setUser(buildUserInfo(user));
        return resp;
    }

    @Override
    public User verifyToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        UserToken userToken = userTokenMapper.findByToken(token);
        if (userToken == null || userToken.getExpireTime() == null || userToken.getExpireTime().before(new Date())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        User user = userMapper.findById(userToken.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    @Override
    public User getUserById(Integer userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(Integer userId, UpdateUserInfoReq req) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        int rows = userMapper.updateUserInfoById(userId, req.getNickname(), req.getAvatarUrl());
        if (rows <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "更新失败，请稍后重试");
        }
    }

    private UserInfoResp buildUserInfo(User user) {
        UserInfoResp userInfoResp = new UserInfoResp();
        userInfoResp.setId(user.getId());
        userInfoResp.setPhone(user.getPhone());
        userInfoResp.setNickname(user.getNickname());
        userInfoResp.setAvatarUrl(user.getAvatarUrl());
        return userInfoResp;
    }

    private Date buildExpireTime(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }
}
