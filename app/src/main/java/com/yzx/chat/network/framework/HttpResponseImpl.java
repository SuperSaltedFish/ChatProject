package com.yzx.chat.network.framework;


/**
 * Created by YZX on 2017年10月14日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class HttpResponseImpl implements HttpResponse {

    private boolean isSuccess;
    private Object mResponse;
    private String mError;

    @Override
    public boolean isSuccess() {
        return isSuccess;
    }

    @Override
    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    @Override
    public Object getResponse() {
        return mResponse;
    }

    @Override
    public void setResponse(Object response) {
        mResponse = response;
    }

    @Override
    public String getError() {
        return mError;
    }

    public void setError(String error) {
        mError = error;
    }
}
