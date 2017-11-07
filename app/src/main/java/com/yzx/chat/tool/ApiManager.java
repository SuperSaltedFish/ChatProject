package com.yzx.chat.tool;

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
import com.yzx.chat.network.framework.ApiProxy;
import com.yzx.chat.network.framework.HttpDataFormatAdapter;
import com.yzx.chat.util.LogUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by YZX on 2017年10月15日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ApiManager {

    private static Gson sGson = new GsonBuilder()
            .serializeNulls()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(String.class,new NullStringToEmptyAdapter())
            .create();

    private static ApiProxy sApiProxy = new ApiProxy(Constants.URL_API_BASE, new HttpDataFormatAdapter() {
        @Nullable
        @Override
        public String requestToString(String url, Map<String, Object> params, String requestMethod) {
            JsonRequest request = new JsonRequest();
            request.setParams(params);
            request.setStatus(200);
            request.setToken(IdentityManager.getInstance().getToken());
            String json = sGson.toJson(request);
            LogUtil.e("request: "+json);
            if (json != null) {
                return IdentityManager.getInstance().aesEncryptToBase64(json.getBytes());
            }
            return null;
        }

        @Nullable
        @Override
        public Object responseToObject(String url, String httpResponse, Type genericType) {
            byte[] data = IdentityManager.getInstance().aesDecryptFromBase64String(httpResponse);
            if (data != null) {
                try {
                    String strData = new String(data);
                    LogUtil.e("request: "+strData);
                    return sGson.fromJson(strData, genericType);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    });


    public static Object getProxyInstance(Class<?> interfaceClass) {
        return sApiProxy.getProxyInstance(interfaceClass);
    }

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
