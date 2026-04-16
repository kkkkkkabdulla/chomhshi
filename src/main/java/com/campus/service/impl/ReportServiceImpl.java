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

    private static final int POST_STATUS_NORMAL = 1;

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
        if (post.getUserId() != null && post.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "不能举报自己的帖子");
        }
        if (post.getStatus() == null || post.getStatus() != POST_STATUS_NORMAL) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "该帖子当前不可举报");
        }

        int pendingCount = postReportMapper.countByPostAndUser(req.getPostId(), userId);
        if (pendingCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "您已举报过该帖子，请等待处理");
        }

        PostReport report = new PostReport();
        report.setReporterId(userId);
        report.setPostId(req.getPostId());
        report.setReasonType(req.getReasonType());
        report.setReasonDesc(req.getReasonDesc());
        report.setStatus(0);
        postReportMapper.insert(report);

        return false;
    }
}
