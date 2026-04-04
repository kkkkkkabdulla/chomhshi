package com.campus.dto.response;

public class UploadImageResp {

    private String url;

    public UploadImageResp() {
    }

    public UploadImageResp(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
