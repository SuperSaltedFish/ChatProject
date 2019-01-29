package com.yzx.chat.core.net.framework;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by YZX on 2018年07月17日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public interface DownloadCall extends Call {


    void setSavePath(@NonNull String savePath);

    void setDownloadCallback(@Nullable DownloadCallback callback);

    void setDownloadCallback(@Nullable DownloadCallback callback, boolean runOnMainThread);

    String getSavePath();

    DownloadCallback getDownloadCallback();

}
