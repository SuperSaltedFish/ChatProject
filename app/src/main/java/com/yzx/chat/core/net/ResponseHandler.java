package com.yzx.chat.core.net;

import com.yzx.chat.R;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.entity.JsonResponse;
import com.yzx.chat.core.listener.ResultCallback;
import com.yzx.chat.core.net.framework.Callback;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by YZX on 2019年01月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
class ResponseHandler<T> implements Callback<JsonResponse<T>>, ResultCallback<T> {

    private static final int STATUS_CODE_SUCCESSFUL = 200;
    private static final int STATUS_CODE_UNKNOWN_RESPONSE_DATA = 0;

    public static final int NETWORK_ERROR_CODE_UNKNOWN = -1;
    public static final int NETWORK_ERROR_CODE_TIMEOUT = -2;
    public static final int NETWORK_ERROR_CODE_UNAVAILABLE = -3;

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
                    onFailure(STATUS_CODE_UNKNOWN_RESPONSE_DATA, AppClient.getInstance().getAppContext().getString(R.string.Error_Server3));
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
            mResultCallback.onFailure(NETWORK_ERROR_CODE_TIMEOUT, AppClient.getInstance().getAppContext().getString(R.string.Error_Server6));
        } else if (t instanceof ConnectException) {
            mResultCallback.onFailure(NETWORK_ERROR_CODE_TIMEOUT, AppClient.getInstance().getAppContext().getString(R.string.Error_Server7));
        } else if (t instanceof UnknownHostException) {
            mResultCallback.onFailure(NETWORK_ERROR_CODE_UNAVAILABLE, AppClient.getInstance().getAppContext().getString(R.string.Error_Server4));
        } else {
            mResultCallback.onFailure(NETWORK_ERROR_CODE_UNKNOWN, AppClient.getInstance().getAppContext().getString(R.string.Error_Server3));
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