package com.yzx.chat.network.framework;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HttpRequestImpl implements HttpRequest {

    private String mUrl;
    private Map<String, Object> mParams;
    private String mRequestMethod;
    private Map<String, List<String>>  mUploadMap;

    @Override
    public String url() {
        return mUrl;
    }

    @Override
    public Map<String, Object> params() {
        return mParams;
    }

    @Override
    public String requestMethod() {
        return mRequestMethod;
    }

    @Override
    public Map<String, List<String>> uploadMap() {
        return mUploadMap;
    }

    public void setUploadMap(Map<String, List<String>> uploadList) {
        mUploadMap = uploadList;
    }

    public void setRequestMethod(String requestMethod) {
        mRequestMethod = requestMethod;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public void setParams(Map<String, Object> params) {
        mParams = params;
    }

}

