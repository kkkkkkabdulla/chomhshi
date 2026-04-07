package com.campus.service.impl;

import com.campus.common.BusinessException;
import com.campus.common.ResultCode;
import com.campus.dto.request.ReportAddReq;
import com.campus.entity.Post;
import com.campus.entity.PostReport;
import com.campus.mapper.PostMapper;
import com.campus.mapper.PostReportMapper;
import com.campus.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class ReportServiceImpl implements ReportService {

    private static final int POST_STATUS_APPROVED = 1;
    private static final int POST_STATUS_OFFLINE = 3;
    private static final int REPORT_THRESHOLD = 5;

    @Resource
    private PostMapper postMapper;

    @Resource
    private PostReportMapper postReportMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addReport(Integer userId, ReportAddReq req) {
        Post post = postMapper.findById(req.getPostId());
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "帖子不存在");
        }
        if (post.getStatus() == null || post.getStatus() != POST_STATUS_APPROVED) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "该帖子当前不可举报");
        }

        PostReport report = new PostReport();
        report.setPostId(req.getPostId());
        report.setUserId(userId);
        report.setReason(req.getReason());
        report.setDescription(req.getDescription());
        report.setStatus(0);
        postReportMapper.insert(report);

        postMapper.increaseReportCount(req.getPostId());

        Post latest = postMapper.findById(req.getPostId());
        if (latest != null && latest.getReportCount() != null && latest.getReportCount() >= REPORT_THRESHOLD) {
            if (latest.getStatus() != null && latest.getStatus() != POST_STATUS_OFFLINE) {
                postMapper.updateStatus(req.getPostId(), POST_STATUS_OFFLINE);
            }
            return true;
        }
        return false;
    }
}
