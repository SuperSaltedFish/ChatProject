package com.yzx.chat.network.framework;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;

/**
 * Created by YZX on 2018年07月18日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class ResponseParams {
    int responseCode;
    byte[] body;
    Throwable throwable;

    public HttpResponse convert(HttpConverter converter, Type destType) {
        HttpResponseImpl response = new HttpResponseImpl();
        response.responseCode = responseCode;
        if(HttpURLConnection.HTTP_OK==responseCode){
            response.body = converter.convertResponseBody(body, destType);
        }
        return response;
    }
}
