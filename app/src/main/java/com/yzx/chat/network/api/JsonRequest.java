package com.yzx.chat.network.api;

import java.util.Map;

/**
 * Created by YZX on 2017年10月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class JsonRequest {

    private String token;
    private int state = 200;
    private Map<String, Object> param;


    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Map<String, Object> getParam() {
        return param;
    }

    public void setParam(Map<String, Object> param) {
        this.param = param;
    }
}
