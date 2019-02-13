package com.yzx.chat.core.net;

import com.yzx.chat.R;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.entity.JsonResponse;
import com.yzx.chat.core.listener.ResultCallback;
import com.yzx.chat.core.net.framework.Callback;
import com.yzx.chat.core.util.ResourcesHelper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by YZX on 2019年01月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class ResponseHandler<T> implements Callback<JsonResponse<T>>, ResultCallback<T> {

    private static final int STATUS_CODE_SUCCESSFUL = 200;
    private static final int STATUS_CODE_UNKNOWN_RESPONSE_DATA = 0;

    public static final int ERROR_CODE_UNKNOWN = -1;
    public static final int ERROR_CODE_NOT_LOGGED_IN = -2;
    public static final int ERROR_CODE_NETWORK_UNKNOWN = -200;
    public static final int ERROR_CODE_NETWORK_TIMEOUT = -300;
    public static final int ERROR_CODE_NETWORK_UNAVAILABLE = -400;


    private ResultCallback<T> mResultCallback;

    public ResponseHandler(ResultCallback<T> resultCallback) {
        mResultCallback = resultCallback;
    }

    @Override
    public void onResponse(JsonResponse<T> response) {
        if (mResultCallback == null) {
            return;
        }
        if (response.getStatus() == STATUS_CODE_SUCCESSFUL) {
            if (response.getData() == null) {
                ParameterizedType pType = (ParameterizedType) this.getClass().getGenericSuperclass();
                Type type = pType.getActualTypeArguments()[0];
                if (type != Void.class) {
                    onFailure(STATUS_CODE_UNKNOWN_RESPONSE_DATA, ResourcesHelper.getString(R.string.Error_Server3));
                } else {
                    onResult(null);
                }
            } else {
                onResult(response.getData());
            }
            onResult(response.getData());
        } else {
            onFailure(response.getStatus(), response.getMessage());
        }
    }

    @Override
    public void onError(Throwable t) {
        if (mResultCallback == null) {
            return;
        }
        if (t instanceof SocketTimeoutException) {
            mResultCallback.onFailure(ERROR_CODE_NETWORK_TIMEOUT, ResourcesHelper.getString(R.string.Error_Server6));
        } else if (t instanceof ConnectException) {
            mResultCallback.onFailure(ERROR_CODE_NETWORK_TIMEOUT, ResourcesHelper.getString(R.string.Error_Server7));
        } else if (t instanceof UnknownHostException) {
            mResultCallback.onFailure(ERROR_CODE_NETWORK_UNAVAILABLE, ResourcesHelper.getString(R.string.Error_Server4));
        } else {
            mResultCallback.onFailure(ERROR_CODE_NETWORK_UNKNOWN, ResourcesHelper.getString(R.string.Error_Server3));
        }
    }

    @Override
    public void onResult(T result) {
        if (mResultCallback != null) {
            mResultCallback.onResult(result);
        }
    }

    @Override
    public void onFailure(int code, String error) {
        if (mResultCallback != null) {
            mResultCallback.onFailure(code, error);
        }
    }

}
