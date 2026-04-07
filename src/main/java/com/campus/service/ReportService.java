package com.campus.service;

import com.campus.dto.request.ReportAddReq;

public interface ReportService {

    /**
     * 返回 true 表示举报后触发了自动下架
     */
    boolean addReport(Integer userId, ReportAddReq req);
}
