package com.yzx.chat.network.framework;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface HttpRequest {

    String url();

    Map<String, Object> params();

    String requestMethod();

    HashMap<String, List<String>> uploadMap();

}
