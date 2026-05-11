package com.campus.service;

import com.campus.entity.SensitiveWord;

import java.util.List;

public interface SensitiveWordService {

    String detectFirstHit(String text);

    List<SensitiveWord> listAll();

    void addWord(String word, Integer level);

    void deleteById(Integer id);
}
