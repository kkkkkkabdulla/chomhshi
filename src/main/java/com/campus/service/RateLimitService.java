package com.campus.service;

public interface RateLimitService {

    void checkPublishLimit(Integer userId);

    void checkCommentLimit(Integer userId);

    void checkReportLimit(Integer userId);
}
