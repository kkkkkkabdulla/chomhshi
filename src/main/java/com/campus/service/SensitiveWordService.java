package com.campus.service;

public interface SensitiveWordService {

    /**
     * 检测文本中是否包含敏感词，命中返回敏感词，否则返回 null
     */
    String detectFirstHit(String text);
}
