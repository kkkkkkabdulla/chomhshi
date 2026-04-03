package com.campus.service.impl;

import com.campus.common.BusinessException;
import com.campus.common.PageResult;
import com.campus.common.ResultCode;
import com.campus.dto.request.PostPublishReq;
import com.campus.entity.Post;
import com.campus.mapper.PostMapper;
import com.campus.service.PostService;
import com.campus.service.SensitiveWordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private static final int POST_TYPE_LOST_FOUND = 1;
    private static final int POST_TYPE_SECOND_HAND = 2;

    @Resource
    private PostMapper postMapper;

    @Resource
    private SensitiveWordService sensitiveWordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer publish(Integer userId, PostPublishReq req) {
        validateTypeSpecificFields(req);
        checkSensitiveContent(req);

        Post post = new Post();
        post.setUserId(userId);
        post.setType(req.getType());
        post.setTitle(req.getTitle());
        post.setDescription(req.getDescription());
        post.setCategory(req.getCategory());
        post.setPrice(req.getPrice());
        post.setLocation(req.getLocation());
        post.setLostFoundTime(parseLostFoundTime(req.getLostFoundTime()));
        post.setLostStatus(req.getLostStatus());
        post.setImages(req.getImages());
        post.setContact(req.getContact());

        // 发布默认状态：待审核
        post.setStatus(0);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setReportCount(0);

        int rows = postMapper.insert(post);
        if (rows <= 0 || post.getId() == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "发布失败，请稍后重试");
        }
        return post.getId();
    }

    @Override
    public PageResult<Post> myList(Integer userId, Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        int offset = (safePage - 1) * safePageSize;

        List<Post> list = postMapper.findMyPosts(userId, offset, safePageSize);
        long total = postMapper.countMyPosts(userId);
        return new PageResult<>(list, total, safePage, safePageSize);
    }

    @Override
    public PageResult<Post> list(Integer type, String keyword, String category, Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        int offset = (safePage - 1) * safePageSize;

        List<Post> list = postMapper.findApprovedPosts(type, trimToNull(keyword), trimToNull(category), offset, safePageSize);
        long total = postMapper.countApprovedPosts(type, trimToNull(keyword), trimToNull(category));
        return new PageResult<>(list, total, safePage, safePageSize);
    }

    @Override
    public Post detail(Integer id) {
        Post post = postMapper.findApprovedDetailById(id);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "帖子不存在或未通过审核");
        }
        return post;
    }

    private void validateTypeSpecificFields(PostPublishReq req) {
        if (req.getType() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "帖子类型不能为空");
        }

        if (POST_TYPE_SECOND_HAND == req.getType()) {
            if (req.getPrice() == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "二手物品价格不能为空");
            }
            if (isBlank(req.getCategory())) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "二手物品类别不能为空");
            }
        }

        if (POST_TYPE_LOST_FOUND == req.getType()) {
            if (isBlank(req.getLocation())) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "失物招领地点不能为空");
            }
            if (req.getLostStatus() == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "失物状态不能为空");
            }
        }
    }

    private void checkSensitiveContent(PostPublishReq req) {
        String hitInTitle = sensitiveWordService.detectFirstHit(req.getTitle());
        if (hitInTitle != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "标题包含敏感词：" + hitInTitle);
        }

        String hitInDesc = sensitiveWordService.detectFirstHit(req.getDescription());
        if (hitInDesc != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "描述包含敏感词：" + hitInDesc);
        }
    }

    private Date parseLostFoundTime(String time) {
        if (isBlank(time)) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time.trim());
        } catch (ParseException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "lostFoundTime 格式应为 yyyy-MM-dd HH:mm:ss");
        }
    }

    private String trimToNull(String str) {
        if (str == null) {
            return null;
        }
        String val = str.trim();
        return val.isEmpty() ? null : val;
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
