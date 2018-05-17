package com.yzx.chat.mvp.presenter;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.yzx.chat.R;
import com.yzx.chat.mvp.contract.VideoPlayContract;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.DownloadCallback;
import com.yzx.chat.network.framework.DownloadUtil;
import com.yzx.chat.network.framework.HttpResponse;
import com.yzx.chat.network.framework.RequestType;
import com.yzx.chat.tool.DirectoryManager;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.LogUtil;

import java.io.File;

/**
 * Created by YZX on 2018年05月17日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VideoPlayPresenter implements VideoPlayContract.Presenter {
    private VideoPlayContract.View mVideoPlayView;
    private Call<Void> mDownloadVideoCall;

    @Override
    public void attachView(VideoPlayContract.View view) {
        mVideoPlayView = view;
    }

    @Override
    public void detachView() {
        mVideoPlayView = null;
        AsyncUtil.cancelCall(mDownloadVideoCall);
    }

    @Override
    public void downloadVideo(Uri videoUri) {
        if (videoUri == null || TextUtils.isEmpty(videoUri.toString())) {
            mVideoPlayView.showError(AndroidUtil.getString(R.string.ChatActivity_VideoAlreadyOverdue));
            return;
        }
        String videoPath = null;
        String savePath = DirectoryManager.getUserVideoPath();
        if ("file".equals(videoUri.getScheme())) {
            videoPath = videoUri.getPath();
        } else {
            String videoName = getFileNameFromUri(videoUri);
            if (!TextUtils.isEmpty(videoName)) {
                videoPath = savePath + videoName;
            }
        }
        if (!TextUtils.isEmpty(videoPath)) {
            if (new File(videoPath).exists()) {
                mVideoPlayView.playVideo(videoPath);
                return;
            }
        }
        AsyncUtil.cancelCall(mDownloadVideoCall);
        mDownloadVideoCall = DownloadUtil.createDownloadCall(videoUri.toString(), savePath, RequestType.GET_DOWNLOAD);
        mDownloadVideoCall.setDownloadCallback(new DownloadCallback() {
            @Override
            public void onProcess(int percent) {
                mVideoPlayView.setEnableProgressDialog(true);
                mVideoPlayView.showProcess(percent);
            }

            @Override
            public void onFinish(HttpResponse<String> httpResponse) {
                mVideoPlayView.setEnableProgressDialog(false);
                if (httpResponse.getResponseCode() == 200 || !TextUtils.isEmpty(httpResponse.getResponse())) {
                    mVideoPlayView.playVideo(httpResponse.getResponse());
                } else {
                    mVideoPlayView.showError(AndroidUtil.getString(R.string.VideoPlayActivity_DownloadVideoFail));
                }
            }

            @Override
            public void onDownloadError(@NonNull Throwable e) {
                LogUtil.e(e.toString());
                mVideoPlayView.setEnableProgressDialog(false);
                mVideoPlayView.showError(AndroidUtil.getString(R.string.VideoPlayActivity_DownloadVideoFail));
            }

            @Override
            public boolean isExecuteNextTask() {
                return false;
            }
        });
        sHttpExecutor.submit(mDownloadVideoCall);
    }

    private static String getFileNameFromUri(Uri videoUri) {
        String url = videoUri.toString();
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String fileName = null;
        if (url.lastIndexOf('/') + 1 < url.length()) {
            fileName = url.substring(url.lastIndexOf('/') + 1);
        }
        return fileName;
    }
}
