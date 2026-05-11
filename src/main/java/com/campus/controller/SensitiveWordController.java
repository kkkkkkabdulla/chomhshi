package com.campus.controller;

import com.campus.common.Result;
import com.campus.entity.SensitiveWord;
import com.campus.service.SensitiveWordService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/admin/sensitive-word")
public class SensitiveWordController {

    @Resource
    private SensitiveWordService sensitiveWordService;

    @GetMapping("/list")
    public Result<List<SensitiveWord>> list() {
        return Result.success(sensitiveWordService.listAll());
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestParam String word, @RequestParam(required = false, defaultValue = "1") Integer level) {
        sensitiveWordService.addWord(word, level);
        return Result.success("添加成功", null);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable("id") Integer id) {
        sensitiveWordService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
