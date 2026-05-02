package com.campus.service.impl;

import com.campus.common.BusinessException;
import com.campus.common.ResultCode;
import com.campus.service.RateLimitService;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitServiceImpl implements RateLimitService {

    private static final int PUBLISH_PER_MINUTE = 3;
    private static final int PUBLISH_PER_DAY = 20;
    private static final int COMMENT_PER_MINUTE = 3;
    private static final int REPORT_PER_DAY = 10;

    private static final long MILLIS_PER_MINUTE = 60_000L;
    private static final long MILLIS_PER_DAY = 24 * 60 * 60_000L;

    private final Map<Integer, LinkedList<Long>> publishMinuteMap = new ConcurrentHashMap<>();
    private final Map<Integer, LinkedList<Long>> publishDayMap = new ConcurrentHashMap<>();
    private final Map<Integer, LinkedList<Long>> commentMinuteMap = new ConcurrentHashMap<>();
    private final Map<Integer, LinkedList<Long>> reportDayMap = new ConcurrentHashMap<>();

    @Override
    public void checkPublishLimit(Integer userId) {
        long now = System.currentTimeMillis();
        checkWindow(publishMinuteMap, userId, now, MILLIS_PER_MINUTE, PUBLISH_PER_MINUTE,
                "发帖过于频繁，每分钟最多" + PUBLISH_PER_MINUTE + "条");
        checkWindow(publishDayMap, userId, now, MILLIS_PER_DAY, PUBLISH_PER_DAY,
                "今日发帖已达上限（" + PUBLISH_PER_DAY + "条），请明天再试");
    }

    @Override
    public void checkCommentLimit(Integer userId) {
        long now = System.currentTimeMillis();
        checkWindow(commentMinuteMap, userId, now, MILLIS_PER_MINUTE, COMMENT_PER_MINUTE,
                "评论过于频繁，每分钟最多" + COMMENT_PER_MINUTE + "条");
    }

    @Override
    public void checkReportLimit(Integer userId) {
        long now = System.currentTimeMillis();
        checkWindow(reportDayMap, userId, now, MILLIS_PER_DAY, REPORT_PER_DAY,
                "今日举报已达上限（" + REPORT_PER_DAY + "条），请明天再试");
    }

    private void checkWindow(Map<Integer, LinkedList<Long>> map, Integer userId,
                             long now, long windowMillis, int maxCount, String message) {
        LinkedList<Long> timestamps = map.computeIfAbsent(userId, k -> new LinkedList<>());

        synchronized (timestamps) {
            Iterator<Long> it = timestamps.iterator();
            while (it.hasNext()) {
                if (now - it.next() > windowMillis) {
                    it.remove();
                } else {
                    break;
                }
            }

            if (timestamps.size() >= maxCount) {
                throw new BusinessException(ResultCode.FORBIDDEN.getCode(), message);
            }

            timestamps.addLast(now);
        }
    }
}
