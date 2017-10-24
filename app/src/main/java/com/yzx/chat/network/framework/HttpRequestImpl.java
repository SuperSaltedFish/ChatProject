package com.yzx.chat.network.framework;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestImpl implements HttpRequest {

    private String mUrl;
    private Map<String, Object> mParams;
    private String mRequestMethod;

    @Override
    public String url() {
        return mUrl;
    }

    @Override
    public Map<String, Object> getParams() {
        return mParams;
    }

    @Override
    public String requestMethod() {
        return mRequestMethod;
    }


    public void setRequestMethod(String requestMethod) {
        mRequestMethod = requestMethod;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public void setParams(HashMap<String, Object> params) {
        mParams = params;
    }

}

