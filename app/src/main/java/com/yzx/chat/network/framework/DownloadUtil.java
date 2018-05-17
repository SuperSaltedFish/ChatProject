package com.yzx.chat.network.framework;

import android.text.TextUtils;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by YZX on 2018年05月16日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class DownloadUtil {


    public static Call<Void> createDownloadCall(String downloadUrl, String savePath, RequestType requestType) {
        return createDownloadCall(downloadUrl, savePath, requestType, null, null);

    }

    public static Call<Void> createDownloadCall(String downloadUrl, String savePath, RequestType type, Map<String, Object> params, HttpDataFormatAdapter adapter) {
        if (type != RequestType.GET_DOWNLOAD && type != RequestType.POST_DOWNLOAD) {
            throw new IllegalArgumentException("Unsupported request type:" + type.name());
        }
        if (TextUtils.isEmpty(downloadUrl)) {
            throw new IllegalArgumentException("Download Url can't be empty");
        }

        if (TextUtils.isEmpty(savePath)) {
            throw new IllegalArgumentException("Save Path can't be empty");
        }
        HttpRequestImpl httpRequest = new HttpRequestImpl();
        if (params != null) {
            Map<HttpParamsType, Map<String, Object>> paramsTypeMap = new EnumMap<>(HttpParamsType.class);
            paramsTypeMap.put(HttpParamsType.PARAMETER_HTTP, params);
            httpRequest.setParams(paramsTypeMap);
        }
        httpRequest.setSavePath(savePath);
        httpRequest.setRequestType(type);
        httpRequest.setUrl(downloadUrl);
        return new CallImpl<>(httpRequest, adapter, Void.class);

    }

}
