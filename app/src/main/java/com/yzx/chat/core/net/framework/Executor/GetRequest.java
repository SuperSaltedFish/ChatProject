package com.yzx.chat.core.net.framework.Executor;


import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import androidx.annotation.Nullable;

/**
 * Created by YZX on 2019年01月30日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class GetRequest extends HttpRequest {

    public GetRequest(String url,@Nullable Map<String, Object> paramsMap) {
        super(toFormFormat(url, paramsMap), METHOD_GET);
    }

    @Override
    public boolean hasBody() {
        return false;
    }

    @Override
    public void writeBodyTo(OutputStream outputStream) throws IOException {

    }

    private static String toFormFormat(String url, Map<String, Object> paramsMap) {
        if (paramsMap != null && paramsMap.size() > 0) {
            StringBuilder newUrl = new StringBuilder(128);
            newUrl.append(url).append("?");
            boolean isFirst = true;
            for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    newUrl.append("&");
                }
                newUrl.append(entry.getKey()).append("=").append(entry.getValue());
            }
            return newUrl.toString();
        } else {
            return url;
        }
    }

}
