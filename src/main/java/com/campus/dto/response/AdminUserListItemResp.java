package com.campus.dto.response;

import java.util.Date;
import java.util.List;

public class AdminUserListItemResp {

    private Integer id;
    private String nickname;
    private String phone;
    private String avatarUrl;
    private Date createTime;
    private Date updateTime;
    private Date lastActiveTime;
    private Integer postCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer reportCount7d;
    private Integer postCount1h;
    private Integer commentCount10m;
    private String riskLevel;
    private List<String> riskTags;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public Date getLastActiveTime() { return lastActiveTime; }
    public void setLastActiveTime(Date lastActiveTime) { this.lastActiveTime = lastActiveTime; }
    public Integer getPostCount() { return postCount; }
    public void setPostCount(Integer postCount) { this.postCount = postCount; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public Integer getCollectCount() { return collectCount; }
    public void setCollectCount(Integer collectCount) { this.collectCount = collectCount; }
    public Integer getReportCount7d() { return reportCount7d; }
    public void setReportCount7d(Integer reportCount7d) { this.reportCount7d = reportCount7d; }
    public Integer getPostCount1h() { return postCount1h; }
    public void setPostCount1h(Integer postCount1h) { this.postCount1h = postCount1h; }
    public Integer getCommentCount10m() { return commentCount10m; }
    public void setCommentCount10m(Integer commentCount10m) { this.commentCount10m = commentCount10m; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public List<String> getRiskTags() { return riskTags; }
    public void setRiskTags(List<String> riskTags) { this.riskTags = riskTags; }
}
