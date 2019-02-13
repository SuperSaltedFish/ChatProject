package com.yzx.chat.core.net.framework;

import android.support.annotation.Nullable;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by YZX on 2017年10月15日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public interface HttpConverter {

    @Nullable
    byte[] convertRequest(String url,Map<String, Object> requestParams);

    @Nullable
    PartContent convertMultipartRequest(String url,String partName, Map<String, Object> requestParams);

    @Nullable
    Object convertResponseBody(String url,byte[] body, Type genericType);

}
