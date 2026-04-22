package com.campus.dto.request;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class AnnouncementSaveReq {

    @NotBlank(message = "公告标题不能为空")
    @Length(max = 200, message = "公告标题长度不能超过200")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    @Length(max = 5000, message = "公告内容长度不能超过5000")
    private String content;

    @NotNull(message = "公告状态不能为空")
    @Min(value = 0, message = "公告状态错误")
    @Max(value = 1, message = "公告状态错误")
    private Integer status;

    @NotNull(message = "置顶状态不能为空")
    @Min(value = 0, message = "置顶状态错误")
    @Max(value = 1, message = "置顶状态错误")
    private Integer isPinned;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Integer isPinned) {
        this.isPinned = isPinned;
    }
}
