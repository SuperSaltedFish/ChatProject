package com.yzx.chat.core.net.framework.Executor;


import android.text.TextUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public abstract class HttpRequest {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    private String mUrl;
    private String mMethod;
    private Map<String, String> mHeaders;

    public HttpRequest(String url, String method) {
        mUrl = url;
        mMethod = method;
        mHeaders = Collections.synchronizedMap(new LinkedHashMap<String, String>());
    }

    public void putHeader(String key, String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }
        mHeaders.put(key, value);
    }

    public void putHeaders(Map<String, String> headers) {
        if (headers == null || headers.size() == 0) {
            return;
        }
        mHeaders.putAll(headers);
    }

    public String getUrl() {
        return mUrl;
    }

    public String getMethod() {
        return mMethod;
    }

    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    public abstract boolean hasBody();

    public abstract void writeBodyTo(OutputStream outputStream) throws IOException;


}
