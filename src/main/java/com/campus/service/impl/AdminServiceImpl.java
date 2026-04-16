package com.campus.service.impl;

import com.campus.common.BusinessException;
import com.campus.common.PageResult;
import com.campus.common.ResultCode;
import com.campus.dto.request.AdminHandleReportReq;
import com.campus.dto.request.AdminLoginReq;
import com.campus.dto.request.AdminPostRejectReq;
import com.campus.dto.response.LoginResp;
import com.campus.dto.response.UserInfoResp;
import com.campus.entity.Admin;
import com.campus.entity.AdminToken;
import com.campus.entity.Post;
import com.campus.entity.PostReport;
import com.campus.mapper.AdminMapper;
import com.campus.mapper.AdminTokenMapper;
import com.campus.mapper.PostMapper;
import com.campus.mapper.PostReportMapper;
import com.campus.service.AdminService;
import com.campus.util.TokenUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private AdminTokenMapper adminTokenMapper;

    @Resource
    private PostMapper postMapper;

    @Resource
    private PostReportMapper postReportMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResp login(AdminLoginReq req) {
        Admin admin = adminMapper.findByUsername(req.getUsername());
        if (admin == null || !req.getPassword().equals(admin.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "账号或密码错误");
        }
        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "管理员账号已禁用");
        }

        String token = TokenUtil.generateToken();
        Date expireTime = buildExpireTime(7);

        AdminToken old = adminTokenMapper.findByAdminId(admin.getId());
        if (old == null) {
            AdminToken add = new AdminToken();
            add.setAdminId(admin.getId());
            add.setToken(token);
            add.setExpireTime(expireTime);
            adminTokenMapper.insert(add);
        } else {
            old.setToken(token);
            old.setExpireTime(expireTime);
            adminTokenMapper.updateByAdminId(old);
        }

        LoginResp resp = new LoginResp();
        resp.setToken(token);

        UserInfoResp user = new UserInfoResp();
        user.setId(admin.getId());
        user.setNickname(admin.getUsername());
        resp.setUser(user);

        return resp;
    }

    @Override
    public Admin verifyToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        AdminToken adminToken = adminTokenMapper.findByToken(token);
        if (adminToken == null || adminToken.getExpireTime() == null || adminToken.getExpireTime().before(new Date())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Admin admin = adminMapper.findById(adminToken.getAdminId());
        if (admin == null || admin.getStatus() == null || admin.getStatus() != 1) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return admin;
    }

    @Override
    public PageResult<Post> postList(Integer status, Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        int offset = (safePage - 1) * safePageSize;

        List<Post> list = postMapper.findByStatus(status, offset, safePageSize);
        long total = postMapper.countByStatus(status);
        return new PageResult<>(list, total, safePage, safePageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approvePost(Integer adminId, Integer postId) {
        Post post = requirePost(postId);
        if (post.getStatus() != null && post.getStatus() == 1) {
            return;
        }
        postMapper.updateStatus(postId, 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectPost(Integer adminId, Integer postId, AdminPostRejectReq req) {
        Post post = requirePost(postId);
        if (post.getStatus() != null && post.getStatus() == 2) {
            return;
        }
        postMapper.updateStatus(postId, 2);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Integer adminId, Integer postId) {
        requirePost(postId);
        int rows = postMapper.deleteById(postId);
        if (rows <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "删除失败");
        }
    }

    @Override
    public PageResult<PostReport> reportList(Integer status, Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        int offset = (safePage - 1) * safePageSize;

        List<PostReport> list = postReportMapper.findByStatus(status, offset, safePageSize);
        long total = postReportMapper.countByStatus(status);
        return new PageResult<>(list, total, safePage, safePageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleReport(Integer adminId, Integer reportId, AdminHandleReportReq req) {
        PostReport report = postReportMapper.findById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "举报记录不存在");
        }
        if (report.getStatus() != null && report.getStatus() != 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "该举报已处理");
        }
        handleReportByPost(adminId, report.getPostId(), req);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleReportByPost(Integer adminId, Integer postId, AdminHandleReportReq req) {
        Post post = requirePost(postId);
        List<PostReport> pendingReports = postReportMapper.findPendingByPostId(postId);
        if (pendingReports == null || pendingReports.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "该帖子暂无待处理举报");
        }

        String action = req.getAction() == null ? "" : req.getAction().trim();
        Integer handleStatus;
        if ("违规，下架".equals(action)) {
            handleStatus = 1;
        } else if ("不违规，驳回".equals(action)) {
            handleStatus = 2;
        } else {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "action 仅支持：违规，下架/不违规，驳回");
        }

        for (PostReport report : pendingReports) {
            int rows = postReportMapper.updateHandle(report.getId(), handleStatus, req.getAdminRemark(), adminId);
            if (rows <= 0) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "处理失败");
            }
        }

        if (handleStatus == 1 && (post.getStatus() == null || post.getStatus() != 3)) {
            postMapper.updateStatus(post.getId(), 3);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restorePost(Integer adminId, Integer postId) {
        requirePost(postId);
        postMapper.updateStatus(postId, 1);
    }

    @Override
    public Post postDetail(Integer postId) {
        return requirePost(postId);
    }

    @Override
    public Post postDetailByReport(Integer reportId) {
        PostReport report = postReportMapper.findById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "举报记录不存在");
        }
        if (report.getPostId() == null || report.getPostId() <= 0) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "举报记录缺少有效帖子ID");
        }
        return requirePost(report.getPostId());
    }

    private Post requirePost(Integer postId) {
        Post post = postMapper.findById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "帖子不存在");
        }
        return post;
    }

    private Date buildExpireTime(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }
}
