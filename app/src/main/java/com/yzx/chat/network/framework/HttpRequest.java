package com.yzx.chat.network.framework;


import java.util.Map;

public interface HttpRequest {

    String url();

    Map<String, Object> getParams();

    String requestMethod();

}
