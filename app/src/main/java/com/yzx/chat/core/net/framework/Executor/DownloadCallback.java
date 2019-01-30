package com.yzx.chat.core.net.framework.Executor;

/**
 * Created by YZX on 2019年01月29日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public interface DownloadCallback {
    void onProcess(int percent);

    void onComplete(String filePath);

    void onError(Throwable error);
}
