package com.yzx.chat.core.net.framework;

import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import com.yzx.chat.core.net.framework.Executor.GetRequest;
import com.yzx.chat.core.net.framework.Executor.HttpExecutor;
import com.yzx.chat.core.net.framework.Executor.HttpRequest;
import com.yzx.chat.core.net.framework.Executor.MultipartRequest;
import com.yzx.chat.core.net.framework.Executor.PostRequest;
import com.yzx.chat.core.net.framework.Executor.ResponseCallback;

import java.io.File;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;


class CallImpl<T> implements Call<T>, ResponseCallback {

    private static final HttpExecutor NETWORK_EXECUTOR = HttpExecutor.getInstance();

    private RequestParams mRequestParams;
    private Type mGenericType;
    private HttpConverter mHttpConverter;
    private Callback<T> mCallback;

    private Handler mUIHandler;

    private boolean isCancel;
    private boolean isCallbackRunOnMainThread;

    CallImpl(RequestParams requestParams, Type genericType, HttpConverter httpConverter) {
        mRequestParams = requestParams;
        mGenericType = genericType;
        mHttpConverter = httpConverter;
        mUIHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public synchronized void enqueue(Callback<T> callback) {
        enqueue(callback, true);
    }

    @Override
    public synchronized void enqueue(Callback<T> callback, boolean callbackOnMainThread) {
        mCallback = callback;
        isCallbackRunOnMainThread = callbackOnMainThread;
        try {
            NETWORK_EXECUTOR.submit(buildFrom(mRequestParams, mHttpConverter), this);
        } catch (Exception e) {
            onError(e);
        }
    }

    @Override
    public void cancel() {
        isCancel = true;
        mUIHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean isCancel() {
        return isCancel;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onResponse(int responseCode, @Nullable final byte[] body) {
        if (isCancel) {
            return;
        }
        if (responseCode != HttpURLConnection.HTTP_OK) {
            onError(new ResponseException("Http Response " + responseCode));
        } else {
            if (mCallback != null) {
                if (isCallbackRunOnMainThread) {
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mCallback.onResponse((T) mHttpConverter.convertResponseBody(mRequestParams.url(),body, mGenericType));
                            } catch (Exception e) {
                                onError(e);
                            }
                        }
                    });
                } else {
                    try {
                        mCallback.onResponse((T) mHttpConverter.convertResponseBody(mRequestParams.url(),body, mGenericType));
                    } catch (Exception e) {
                        onError(e);
                    }
                }
            }
        }
    }

    @Override
    public void onError(final Throwable error) {
        if (isCancel) {
            return;
        }
        if (mCallback != null) {
            if (isCallbackRunOnMainThread) {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCallback.onError(error);
                    }
                });
            } else {
                mCallback.onError(error);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static HttpRequest buildFrom(RequestParams params, HttpConverter converter) {
        if (HttpRequest.METHOD_GET.equalsIgnoreCase(params.method)) {
            GetRequest request = new GetRequest(params.url(), params.params);
            request.putHeaders(params.headers);
            return request;
        } else if (HttpRequest.METHOD_POST.equalsIgnoreCase(params.method)) {
            if (params.isMultipart) {
                MultipartRequest request = new MultipartRequest(params.url());
                for (Map.Entry<String, Map<String, Object>> entry : params.paramsPartMap.entrySet()) {
                    String partName = entry.getKey();
                    Map<String, Object> paramsMap = entry.getValue();
                    PartContent partContent = converter.convertMultipartRequest(request.getUrl(),partName, paramsMap);
                    if (partContent != null) {
                        request.addPart(partName, partContent.getContentType(), partContent.getContent());
                    }
                }
                for (Map.Entry<String, List<Pair<String, File>>> entry : params.filePartMap.entrySet()) {
                    String partName = entry.getKey();
                    List<Pair<String, File>> fileList = entry.getValue();
                    for (Pair<String, File> file : fileList) {
                        request.addPart(partName, file.second, file.first);
                    }
                }
                return request;
            } else {
                return new PostRequest(params.url(), converter.convertRequest(params.url(),params.params));
            }
        } else {
            throw new RuntimeException("Unknown http method：" + params.method);
        }
    }
}
