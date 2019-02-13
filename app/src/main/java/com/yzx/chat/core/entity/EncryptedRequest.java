package com.yzx.chat.core.entity;

/**
 * Created by YZX on 2018年12月17日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class EncryptedRequest {

    private String signature;
    private String secretKey;
    private String data;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
