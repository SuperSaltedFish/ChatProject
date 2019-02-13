package com.yzx.chat.core.util;

import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Created by YZX on 2018年12月19日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class Sha256Util {

    public static String sha256WithSalt(String string,String hashSaly) {
        if (TextUtils.isEmpty(string)) {
            return null;
        }
        string = string +hashSaly;
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("SHA-256").digest(string.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, SHA-256 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }
}
