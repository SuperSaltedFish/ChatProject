package com.yzx.chat.core.net;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.core.net.framework.ApiProxy;
import com.yzx.chat.core.net.framework.HttpConverter;
import com.yzx.chat.core.net.framework.PartContent;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by YZX on 2017年10月15日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ApiHelper {

    public static final Gson GSON;
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
            public PartContent convertMultipartRequest(String partName, Map<String, Object> requestParams) {
                return null;
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
