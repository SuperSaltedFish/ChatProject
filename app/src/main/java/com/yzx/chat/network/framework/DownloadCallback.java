package com.yzx.chat.network.framework;

import android.support.annotation.NonNull;

/**
 * Created by YZX on 2018年05月16日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public interface DownloadCallback {

    void onProcess(int percent);

    void onFinish(HttpResponse<String> httpResponse);

    void onDownloadError(@NonNull Throwable e);

    boolean isExecuteNextTask();

}