package com.yzx.chat.base;

import android.support.annotation.NonNull;

import com.yzx.chat.R;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.framework.HttpRequest;
import com.yzx.chat.network.framework.NetworkUnavailableException;
import com.yzx.chat.network.framework.ResponseCallback;
import com.yzx.chat.network.framework.HttpResponse;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.LogUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;

/**
 * Created by YZX on 2017年10月17日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public abstract class BaseResponseCallback<T> implements ResponseCallback<JsonResponse<T>> {

    protected abstract void onSuccess(T response);

    protected abstract void onFailure(String message);


    @Override
    public void onResponse(HttpRequest request, HttpResponse<JsonResponse<T>> response) {
        if (response.responseCode() != 200) {
            LogUtil.e("ResponseCode:" + response.responseCode());
            onFailure(AndroidUtil.getString(R.string.Error_Server));
            return;
        }
        JsonResponse<T> jsonResponse = response.body();
        if (jsonResponse == null) {
            onFailure(AndroidUtil.getString(R.string.Error_Server));
        } else if (jsonResponse.getStatus() != 200) {
            onFailure(jsonResponse.getMessage());
        } else if (jsonResponse.getData() == null) {
            ParameterizedType pType = (ParameterizedType) response.getClass().getGenericInterfaces()[0];
            Type type = pType.getActualTypeArguments()[0];
            if (type != Void.class) {
                onFailure(AndroidUtil.getString(R.string.Error_Server));
            } else {
                onSuccess(null);
            }
        } else {
            onSuccess(jsonResponse.getData());
        }
    }

    @Override
    public void onError(@NonNull Throwable e) {
        e.printStackTrace();
        Class errorClass = e.getClass();
        if (errorClass == NetworkUnavailableException.class) {
            onFailure(AndroidUtil.getString(R.string.Error_NetworkUnavailable));
        } else if (errorClass == SocketTimeoutException.class) {
            onFailure(AndroidUtil.getString(R.string.Error_NetworkTimeout));
        } else {
            onFailure(AndroidUtil.getString(R.string.Error_Server));
        }
    }

    @Override
    public boolean isExecuteNextTask() {
        return false;
    }
}
