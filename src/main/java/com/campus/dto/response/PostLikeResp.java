package com.campus.dto.response;

public class PostLikeResp {

    private boolean liked;
    private Integer likeCount;

    public PostLikeResp() {
    }

    public PostLikeResp(boolean liked, Integer likeCount) {
        this.liked = liked;
        this.likeCount = likeCount;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
}
