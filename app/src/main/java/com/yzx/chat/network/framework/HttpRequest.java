package com.yzx.chat.network.framework;


import java.util.Map;


public interface HttpRequest {

    String url();

    Map<HttpParamsType,Map<String, Object>> params();

    String requestMethod();

    boolean isMultiParams();

}
