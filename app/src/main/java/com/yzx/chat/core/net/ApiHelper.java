package com.yzx.chat.core.net;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.core.manager.CryptoManager;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.entity.JsonRequest;
import com.yzx.chat.core.net.framework.ApiProxy;
import com.yzx.chat.core.net.framework.HttpConverter;
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

    private static final Gson GSON;
    private static final ApiProxy API_PROXY;

    static {
        GSON = new GsonBuilder()
                .serializeNulls()
                .enableComplexMapKeySerialization()
                .disableHtmlEscaping()
                .registerTypeAdapter(String.class, new TypeAdapter<String>() {
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
                })
                .create();

        API_PROXY = new ApiProxy(Constants.URL_API_BASE, new HttpConverter() {
            @Nullable
            @Override
            public byte[] convertRequest(Map<String, Object> requestParams) {
                return new byte[0];
            }

            @Nullable
            @Override
            public byte[] convertMultipartRequest(String partName, Object body) {
                return new byte[0];
            }

            @Nullable
            @Override
            public Object convertResponseBody(byte[] body, Type genericType) {
                return null;
            }
        });
    }

    public static <T> T getProxyInstance(Class<T> interfaceClass) {
        return API_PROXY.getProxyInstance(interfaceClass);
    }


}
