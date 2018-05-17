package com.yzx.chat.network.framework;

import java.util.Map;


class HttpRequestImpl implements HttpRequest {

    private String mUrl;
    private String mSavePath;
    private Map<HttpParamsType, Map<String, Object>> mParams;
    private RequestType mRequestType;

    @Override
    public String url() {
        return mUrl;
    }

    @Override
    public String savePath() {
        return mSavePath;
    }

    @Override
    public Map<HttpParamsType, Map<String, Object>> params() {
        return mParams;
    }

    @Override
    public RequestType requestType() {
        return mRequestType;
    }


    public void setUrl(String url) {
        mUrl = url;
    }

    public void setSavePath(String savePath) {
        mSavePath = savePath;
    }

    public void setParams(Map<HttpParamsType, Map<String, Object>> params) {
        mParams = params;
    }

    public void setRequestType(RequestType requestType) {
        mRequestType = requestType;
    }


}

