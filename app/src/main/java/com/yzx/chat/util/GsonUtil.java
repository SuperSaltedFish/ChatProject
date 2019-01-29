package com.yzx.chat.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.yzx.chat.core.entity.ProvinceEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by YZX on 2018年02月18日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class GsonUtil {
    public static ArrayList<ProvinceEntity> readJsonStream(InputStream in) {
        JsonReader reader;
        try {
            reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        ArrayList<ProvinceEntity> messages = new ArrayList<>();
        try {
            reader.beginArray();
            Gson gson = new GsonBuilder().create();
            while (reader.hasNext()) {
                ProvinceEntity message = gson.fromJson(reader, ProvinceEntity.class);
                messages.add(message);
            }
            reader.endArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }
}
