package com.yzx.chat.network.framework;


import android.support.annotation.Nullable;

/**
 * Created by YZX on 2017年10月14日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class HttpResponseImpl implements HttpResponse {

    private int  mResponseCode;
    private Object mResponse;

    public void setResponseCode(int responseCode) {
        mResponseCode = responseCode;
    }

    public void setResponse(Object response) {
        mResponse = response;
    }

    @Override
    public int getResponseCode() {
        return mResponseCode;
    }

    @Nullable
    @Override
    public Object getResponse() {
        return mResponse;
    }
}
