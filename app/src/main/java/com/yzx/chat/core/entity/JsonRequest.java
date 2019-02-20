package com.yzx.chat.core.entity;


/**
 * Created by YZX on 2017年10月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class JsonRequest {

    private String token;
    private String deviceID;
    private Object params;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }
}
