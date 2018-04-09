package com.yzx.chat.network.framework;


import java.util.List;
import java.util.Map;


public interface HttpRequest {

    String url();

    Map<String, Object> params();

    String requestMethod();

    Map<String, List<String>> uploadMap();

}
