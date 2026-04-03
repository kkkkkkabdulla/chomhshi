package com.campus.dto.request;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PostPublishReq {

    @NotNull(message = "帖子类型不能为空")
    @Min(value = 1, message = "帖子类型错误")
    @Max(value = 2, message = "帖子类型错误")
    private Integer type;

    @NotBlank(message = "标题不能为空")
    @Length(max = 200, message = "标题长度不能超过200")
    private String title;

    @Length(max = 2000, message = "描述长度不能超过2000")
    private String description;

    @Length(max = 50, message = "类别长度不能超过50")
    private String category;

    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    @Length(max = 100, message = "地点长度不能超过100")
    private String location;

    private String lostFoundTime;

    @Min(value = 1, message = "失物状态错误")
    @Max(value = 2, message = "失物状态错误")
    private Integer lostStatus;

    @Length(max = 1000, message = "图片字段过长")
    private String images;

    @Length(max = 100, message = "联系方式长度不能超过100")
    private String contact;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLostFoundTime() {
        return lostFoundTime;
    }

    public void setLostFoundTime(String lostFoundTime) {
        this.lostFoundTime = lostFoundTime;
    }

    public Integer getLostStatus() {
        return lostStatus;
    }

    public void setLostStatus(Integer lostStatus) {
        this.lostStatus = lostStatus;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
