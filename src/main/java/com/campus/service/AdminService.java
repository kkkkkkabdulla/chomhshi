package com.campus.service;

import com.campus.common.PageResult;
import com.campus.dto.request.AdminHandleReportReq;
import com.campus.dto.request.AdminLoginReq;
import com.campus.dto.request.AdminPostRejectReq;
import com.campus.dto.response.LoginResp;
import com.campus.entity.Admin;
import com.campus.entity.Post;
import com.campus.entity.PostReport;

public interface AdminService {

    LoginResp login(AdminLoginReq req);

    Admin verifyToken(String token);

    PageResult<Post> postList(Integer status, Integer page, Integer pageSize);

    void approvePost(Integer adminId, Integer postId);

    void rejectPost(Integer adminId, Integer postId, AdminPostRejectReq req);

    void deletePost(Integer adminId, Integer postId);

    PageResult<PostReport> reportList(Integer status, Integer page, Integer pageSize);

    void handleReport(Integer adminId, Integer reportId, AdminHandleReportReq req);

    void handleReportByPost(Integer adminId, Integer postId, AdminHandleReportReq req);

    void restorePost(Integer adminId, Integer postId);

    Post postDetail(Integer postId);

    Post postDetailByReport(Integer reportId);
}
