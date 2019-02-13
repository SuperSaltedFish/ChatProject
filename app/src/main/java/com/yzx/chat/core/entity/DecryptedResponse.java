package com.yzx.chat.core.entity;

/**
 * Created by YZX on 2018年12月19日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class DecryptedResponse {

    private String data;
    private String signature;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
