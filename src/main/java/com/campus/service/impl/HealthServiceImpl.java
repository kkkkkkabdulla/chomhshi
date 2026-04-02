package com.campus.service.impl;

import com.campus.mapper.HealthMapper;
import com.campus.service.HealthService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class HealthServiceImpl implements HealthService {

    @Resource
    private HealthMapper healthMapper;

    @Override
    public Integer pingDb() {
        return healthMapper.pingDb();
    }
}
