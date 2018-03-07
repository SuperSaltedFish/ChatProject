package com.yzx.chat.base;

import android.support.annotation.NonNull;

import com.yzx.chat.R;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.framework.HttpCallback;
import com.yzx.chat.network.framework.HttpResponse;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.LogUtil;

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
    public void onResponse(HttpResponse<JsonResponse<T>> response) {
        if(response.getResponseCode()!=200){
            LogUtil.e("ResponseCode:"+response.getResponseCode());
            onFailure(AndroidUtil.getString(R.string.Server_Error));
            return;
        }
        JsonResponse<T> jsonResponse = response.getResponse();
        if (jsonResponse == null) {
            onFailure(AndroidUtil.getString(R.string.Server_Error));
        } else if (jsonResponse.getStatus() != 200) {
            onFailure(jsonResponse.getMessage());
        } else if (jsonResponse.getData() == null) {
            ParameterizedType pType = (ParameterizedType) response.getClass().getGenericInterfaces()[0];
            Type type = pType.getActualTypeArguments()[0];
            if (type != Void.class) {
                onFailure("数据解析失败，请稍后再试！");
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
        onFailure("数据解析失败，请稍后再试！");
    }

    @Override
    public boolean isExecuteNextTask() {
        return false;
    }
}
