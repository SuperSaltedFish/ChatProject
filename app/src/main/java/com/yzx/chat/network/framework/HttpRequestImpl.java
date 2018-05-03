package com.yzx.chat.network.framework;

import java.util.Map;


class HttpRequestImpl implements HttpRequest {

    private String mUrl;
    private  Map<HttpParamsType,Map<String, Object>> mParams;
    private String mRequestMethod;
    private boolean isMultiParams;

    @Override
    public String url() {
        return mUrl;
    }

    @Override
    public  Map<HttpParamsType,Map<String, Object>> params() {
        return mParams;
    }

    @Override
    public String requestMethod() {
        return mRequestMethod;
    }

    @Override
    public boolean isMultiParams() {
        return isMultiParams;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public void setParams( Map<HttpParamsType,Map<String, Object>> params) {
        mParams = params;
    }

    public void setRequestMethod(String requestMethod) {
        mRequestMethod = requestMethod;
    }

    public void enableMultiParams(boolean enable) {
        isMultiParams = enable;
    }
}

