package com.yzx.chat.tool;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.network.api.JsonRequest;
import com.yzx.chat.network.chat.CryptoManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.framework.ApiProxy;
import com.yzx.chat.network.framework.HttpConverter;
import com.yzx.chat.util.Base64Util;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.RSAUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
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

    public static HttpConverter getRsaHttpConverter(final String publicRsaKey) {
        if (TextUtils.isEmpty(publicRsaKey)) {
            return null;
        }
        return new HttpConverter() {
            @Nullable
            @Override
            public byte[] convertRequest(Map<String, Object> requestParams) {
                JsonRequest request = new JsonRequest();
                request.setParams(requestParams);
                request.setStatus(200);
                String json = sGson.toJson(request);
                LogUtil.e("convertRequest: " + json);
                byte[] encryptData = RSAUtil.encryptByPublicKey(json.getBytes(), Base64Util.decode(publicRsaKey.getBytes()));
                json = Base64Util.encodeToString(encryptData);
                return json == null ? null : json.getBytes(StandardCharsets.UTF_8);
            }

            @Nullable
            @Override
            public byte[] convertMultipartRequest(String partName, Object body) {
                return null;
            }

            @Nullable
            @Override
            public Object convertResponseBody(byte[] body, Type genericType) {
                if (body != null && body.length > 0) {
                    byte[] data = Base64Util.decode(new String(body));
                    if (data == null || data.length == 0) {
                        LogUtil.e("response: " + null);
                        return null;
                    }
                    data = CryptoManager.rsaDecrypt(data);
                    if (data == null) {
                        LogUtil.e("response: " + null);
                        return null;
                    }
                    String strData = new String(data);
                    LogUtil.e("convertResponseBody: " + strData);
                    return sGson.fromJson(strData, genericType);
                } else {
                    LogUtil.e("convertResponseBody fail:body == null");
                    return null;
                }
            }

        };
    }

    private static ApiProxy sApiProxy = new ApiProxy(Constants.URL_API_BASE, new HttpConverter() {

        @Nullable
        @Override
        public byte[] convertRequest(Map<String, Object> requestParams) {
            JsonRequest request = new JsonRequest();
            request.setParams(requestParams);
            request.setStatus(200);
            request.setToken(IMClient.getInstance().isLogged() ? IMClient.getInstance().getUserManager().getToken() : null);
            String json = sGson.toJson(request);
            LogUtil.e("convertRequest: " + json);
//            if (json != null) {
//                return IdentityManager.getInstance().aesEncryptToBase64(json.getBytes());
//            }
            return json == null ? null : json.getBytes(StandardCharsets.UTF_8);
        }

        @Nullable
        @Override
        public byte[] convertMultipartRequest(String partName, Object body) {
            JsonRequest request = new JsonRequest();
            request.setParams(body);
            request.setStatus(200);
            request.setToken(IMClient.getInstance().isLogged() ? IMClient.getInstance().getUserManager().getToken() : null);
            String json = sGson.toJson(request);
            LogUtil.e("request: " + json);
//            if (json != null) {
//                return IdentityManager.getInstance().aesEncryptToBase64(json.getBytes());
//            }
            return json == null ? null : json.getBytes(StandardCharsets.UTF_8);
        }

        @Nullable
        @Override
        public Object convertResponseBody(byte[] body, Type genericType) {
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
            if (body == null || body.length == 0) {
                return null;
            }
            String strBody = new String(body);
            LogUtil.e("convertResponseBody:" + strBody);
            return sGson.fromJson(new String(body), genericType);
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
