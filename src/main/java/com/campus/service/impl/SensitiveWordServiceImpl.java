package com.campus.service.impl;

import com.campus.mapper.SensitiveWordMapper;
import com.campus.service.SensitiveWordService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {

    @Resource
    private SensitiveWordMapper sensitiveWordMapper;

    private volatile List<String> words = Collections.emptyList();

    @PostConstruct
    public void init() {
        reloadWords();
    }

    @Override
    public String detectFirstHit(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String content = text.toLowerCase();
        for (String word : words) {
            if (word == null || word.trim().isEmpty()) {
                continue;
            }
            String target = word.trim().toLowerCase();
            if (content.contains(target)) {
                return word;
            }
        }
        return null;
    }

    private void reloadWords() {
        List<String> dbWords = sensitiveWordMapper.findAllWords();
        if (dbWords == null) {
            this.words = Collections.emptyList();
            return;
        }
        this.words = Collections.unmodifiableList(new ArrayList<>(dbWords));
    }
}
