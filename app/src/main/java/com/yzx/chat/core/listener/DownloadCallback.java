package com.yzx.chat.core.listener;

import android.net.Uri;

import io.rong.imlib.model.Message;

/**
 * Created by YZX on 2018年05月25日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public interface DownloadCallback {
    void onSuccess(Message message,Uri localUri);

    void onProgress(Message message, int percentage);

    void onError(Message message, String error);

    void onCanceled(Message message);
}
