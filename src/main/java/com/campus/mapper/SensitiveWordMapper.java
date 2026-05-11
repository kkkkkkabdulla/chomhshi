package com.campus.mapper;

import com.campus.entity.SensitiveWord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SensitiveWordMapper {

    @Select("SELECT id, word, level, create_time FROM sensitive_word ORDER BY id DESC")
    List<SensitiveWord> findAll();

    @Select("SELECT word FROM sensitive_word")
    List<String> findAllWords();

    @Insert("INSERT INTO sensitive_word (word, level) VALUES (#{word}, #{level})")
    int insert(@Param("word") String word, @Param("level") Integer level);

    @Select("SELECT COUNT(1) FROM sensitive_word WHERE word = #{word}")
    int countByWord(@Param("word") String word);

    @Delete("DELETE FROM sensitive_word WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);
}
