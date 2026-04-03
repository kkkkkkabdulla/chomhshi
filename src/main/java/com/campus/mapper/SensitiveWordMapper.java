package com.campus.mapper;

import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SensitiveWordMapper {

    @Select("SELECT word FROM sensitive_word")
    List<String> findAllWords();
}
