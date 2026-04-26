package com.campus.dto.response;

public class PostCollectResp {

    private boolean collected;
    private Integer collectCount;

    public PostCollectResp() {
    }

    public PostCollectResp(boolean collected, Integer collectCount) {
        this.collected = collected;
        this.collectCount = collectCount;
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    public Integer getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(Integer collectCount) {
        this.collectCount = collectCount;
    }
}