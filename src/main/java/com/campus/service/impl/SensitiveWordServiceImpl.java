package com.campus.service.impl;

import com.campus.common.BusinessException;
import com.campus.common.ResultCode;
import com.campus.entity.SensitiveWord;
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

    @Override
    public List<SensitiveWord> listAll() {
        return sensitiveWordMapper.findAll();
    }

    @Override
    public void addWord(String word, Integer level) {
        if (word == null || word.trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "敏感词不能为空");
        }
        String trimmed = word.trim();
        if (sensitiveWordMapper.countByWord(trimmed) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "该敏感词已存在");
        }
        if (level == null || (level != 1 && level != 2)) {
            level = 1;
        }
        sensitiveWordMapper.insert(trimmed, level);
        reloadWords();
    }

    @Override
    public void deleteById(Integer id) {
        if (id == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "ID不能为空");
        }
        sensitiveWordMapper.deleteById(id);
        reloadWords();
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
