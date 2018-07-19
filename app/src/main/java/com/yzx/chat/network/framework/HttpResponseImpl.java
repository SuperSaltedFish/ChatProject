package com.yzx.chat.network.framework;

/**
 * Created by YZX on 2017年10月14日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


class HttpResponseImpl implements HttpResponse {


    int responseCode;
    Object body;

    @Override
    public int responseCode() {
        return responseCode;
    }

    @Override
    public Object body() {
        return body;
    }
}
