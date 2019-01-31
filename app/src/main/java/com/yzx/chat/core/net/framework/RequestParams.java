package com.yzx.chat.core.net.framework;

import android.util.Pair;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class RequestParams {

    String baseUrl;
    String relativeUrl;
    String method;
    public Map<String, String> headers;
    public Map<String, Object> params;
    public Map<String, Map<String, Object>> paramsPartMap;
    public Map<String, List<Pair<String, File>>> filePartMap;

    public boolean isMultipart;

    public RequestParams() {
        headers = new LinkedHashMap<>();
        params = new LinkedHashMap<>();
        paramsPartMap = new LinkedHashMap<>();
        filePartMap = new LinkedHashMap<>();
    }

    public String url() {
        return baseUrl + relativeUrl;
    }

    public String method() {
        return method.toUpperCase(Locale.US);
    }


}

