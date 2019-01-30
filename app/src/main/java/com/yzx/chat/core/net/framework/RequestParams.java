package com.yzx.chat.core.net.framework;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;


public class RequestParams {

    public String baseUrl;
    public String relativeUrl;
    public String method;
    public Map<String, String> headers;
    public Map<String, Object> params;
    public Map<String, Map<String,Object>> paramsPartMap;
    public Map<String, Object> filePart;

    public boolean isMultipart;

    public RequestParams() {
        headers = new LinkedHashMap<>();
        params = new LinkedHashMap<>();
        paramsPartMap = new LinkedHashMap<>();
        filePart = new LinkedHashMap<>();
    }

    public String url() {
        return baseUrl + relativeUrl;
    }

    public String method() {
        return method.toUpperCase(Locale.US);
    }


}

