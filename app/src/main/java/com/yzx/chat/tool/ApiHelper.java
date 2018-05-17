package com.yzx.chat.tool;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.network.api.JsonRequest;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.framework.ApiProxy;
import com.yzx.chat.network.framework.HttpDataFormatAdapter;
import com.yzx.chat.network.framework.HttpParamsType;
import com.yzx.chat.network.framework.HttpRequest;
import com.yzx.chat.network.framework.RequestType;
import com.yzx.chat.util.LogUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by YZX on 2017年10月15日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ApiHelper {

    private static Gson sGson = new GsonBuilder()
            .serializeNulls()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(String.class, new NullStringToEmptyAdapter())
            .create();

    public static Object getProxyInstance(Class<?> interfaceClass) {
        return sApiProxy.getProxyInstance(interfaceClass);
    }

    public static Gson getDefaultGsonInstance() {
        return sGson;
    }

    private static ApiProxy sApiProxy = new ApiProxy(Constants.URL_API_BASE, new HttpDataFormatAdapter() {

        @Nullable
        @Override
        public String paramsToString(HttpRequest httpRequest) {
            Map<String, Object> params = httpRequest.params().get(HttpParamsType.PARAMETER_HTTP);
            LogUtil.e("开始访问：" + httpRequest.url());
            if (params == null || params.size() == 0) {
                return null;
            }
            JsonRequest request = new JsonRequest();
            request.setParams(params);
            request.setStatus(200);
            request.setToken(IMClient.getInstance().isLogged() ? IMClient.getInstance().userManager().getToken() : null);
            String json = sGson.toJson(request);
            LogUtil.e("request: " + json);
//            if (json != null) {
//                return IdentityManager.getInstance().aesEncryptToBase64(json.getBytes());
//            }
            if (json != null) {
                return json;
            }
            return null;
        }

        @NonNull
        @Override
        public Map<HttpParamsType, Map<String, Object>> multiParamsFormat(HttpRequest httpRequest) {
            LogUtil.e("开始访问：" + httpRequest.url());
            Map<HttpParamsType, Map<String, Object>> params = httpRequest.params();
            Map<String, Object> httpParams = params.get(HttpParamsType.PARAMETER_HTTP);
            if (httpParams == null) {
                httpParams = new LinkedHashMap<>(2);
                params.put(HttpParamsType.PARAMETER_HTTP, httpParams);
            }
            JsonRequest request = new JsonRequest();
            request.setStatus(200);
            request.setToken(IMClient.getInstance().isLogged() ? IMClient.getInstance().userManager().getToken() : null);
            String json = sGson.toJson(request);
            LogUtil.e("request: " + json);
            httpParams.put("params", json);
            return params;
        }

        @Nullable
        @Override
        public Object responseToObject(String url, String httpResponse, Type genericType) {
//            byte[] data = IdentityManager.getInstance().aesDecryptFromBase64String(httpResponse);
//            if (data != null) {
//                try {
//                    String strData = new String(data);
//                    LogUtil.e("response: "+strData);
//                    return sGson.fromJson(strData, genericType);
//                } catch (JsonSyntaxException e) {
//                    e.printStackTrace();
//                }
//            }
//            return null;
            try {
                LogUtil.e("response: " + httpResponse);
                return sGson.fromJson(httpResponse, genericType);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }
    });


    private static class NullStringToEmptyAdapter extends TypeAdapter<String> {

        @Override
        public String read(JsonReader reader) throws IOException {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
                return "";
            }
            return reader.nextString();
        }

        @Override
        public void write(JsonWriter writer, String value) throws IOException {
            if (value == null) {
                writer.value("");
                return;
            }
            writer.value(value);
        }
    }

}
