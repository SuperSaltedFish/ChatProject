package com.yzx.chat.network.framework;

import android.support.annotation.Nullable;

public interface HttpResponse {

    boolean isSuccess();

    void setSuccess(boolean isSuccess);

    void setResponse(Object response);

    void setError(String response);

    @Nullable
    Object getResponse();

    @Nullable
    String getError();

}
