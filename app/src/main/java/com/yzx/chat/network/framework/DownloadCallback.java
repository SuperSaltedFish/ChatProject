package com.yzx.chat.network.framework;

/**
 * Created by YZX on 2018年05月16日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public interface DownloadCallback extends ResponseCallback<String> {

    void onProcess(int percent);

}