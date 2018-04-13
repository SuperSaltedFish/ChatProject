package com.yzx.chat.network.framework;


import java.util.List;
import java.util.Map;


public interface HttpRequest {

    String url();

    Map<HttpParamsType, List<Pair<String, Object>>> params();

    String requestMethod();

    boolean isMultiParams();

}
