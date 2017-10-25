package com.yzx.chat.base;

import android.support.annotation.Nullable;

import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.framework.HttpCallback;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by YZX on 2017年10月17日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public abstract class BaseHttpCallback<T> implements HttpCallback<JsonResponse<T>> {

    protected abstract void onSuccess(T response);

    protected abstract void onFailure(String message);

    @Override
    public void onResponse(@Nullable JsonResponse<T> response) {
        if (response == null) {
            onFailure(null);
          //  onSuccess(null);
        } else if (response.getStatus() != 200) {
            onFailure(response.getMessage());
        } else if (response.getData() == null) {
            ParameterizedType pType = (ParameterizedType) response.getClass().getGenericInterfaces()[0];
            Type type = pType.getActualTypeArguments()[0];
            if (type != Void.class) {
                onFailure(null);
            } else {
                onSuccess(null);
            }
        } else {
            onSuccess(response.getData());
        }
    }

    @Override
    public void onError(@Nullable String error) {
        onFailure(error);
    }

    @Override
    public boolean isComplete() {
        return true;
    }
}
