package com.yzx.chat.util;

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
        return Base64.decode(data,Base64.DEFAULT);
    }

    public static byte[] decode(String data){
        if(data==null){
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
        if(data==null){
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

    public static String encodeTooString(String data){
        if(data==null){
            return null;
        }
        return encodeToString(data.getBytes());
    }
}
