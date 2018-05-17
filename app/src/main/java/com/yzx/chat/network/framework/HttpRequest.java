package com.yzx.chat.network.framework;


import java.util.Map;


public interface HttpRequest {

    String url();

    String savePath();

    Map<HttpParamsType,Map<String, Object>> params();

    RequestType requestType();

}
