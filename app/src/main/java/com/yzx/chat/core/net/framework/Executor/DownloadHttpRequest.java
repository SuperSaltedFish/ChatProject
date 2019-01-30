package com.yzx.chat.core.net.framework.Executor;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by YZX on 2019年01月30日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class DownloadHttpRequest extends HttpRequest {

    private boolean isCancel;
    private String mSavePath;
    private HttpRequest mHttpRequest;

    public DownloadHttpRequest(@NonNull HttpRequest request, String savePath) {
        super(request.getUrl(), request.getMethod());
        mHttpRequest = request;
        mSavePath = savePath;
    }

    public void cancel() {
        isCancel = true;
    }

    public boolean isCancel() {
        return isCancel;
    }

    public String getSavePath() {
        return mSavePath;
    }

    @Override
    public boolean hasBody() {
        return mHttpRequest.hasBody();
    }

    @Override
    public void writeBodyTo(OutputStream outputStream) throws IOException {
        mHttpRequest.writeBodyTo(outputStream);
    }
}
