package com.yzx.chat.core.util;

import android.text.TextUtils;
import android.util.Base64;

/**
 * Created by YZX on 2017年10月25日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class Base64Util {

    public static byte[] decode(byte[] data){
        if(data==null){
            return null;
        }
        return Base64.decode(data,Base64.NO_WRAP);
    }

    public static byte[] decode(String data){
        if(TextUtils.isEmpty(data)){
            return null;
        }
        return Base64.decode(data,Base64.DEFAULT);
    }

    public static byte[] encode(byte[] data){
        if(data==null){
            return null;
        }
        return Base64.encode(data,Base64.DEFAULT);
    }

    public static byte[] encode(String data){
        if(TextUtils.isEmpty(data)){
            return null;
        }
        return encode(data.getBytes());
    }

    public static String encodeToString(byte[] data){
        if(data==null){
            return null;
        }
        return Base64.encodeToString(data,Base64.DEFAULT);
    }

    public static String encodeToString(String data){
        if(TextUtils.isEmpty(data)){
            return null;
        }
        return encodeToString(data.getBytes());
    }
}
