package com.yzx.chat.mvp.presenter;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.yzx.chat.R;
import com.yzx.chat.mvp.contract.VideoPlayContract;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.extra.VideoMessage;
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

import io.rong.imlib.model.Message;

/**
 * Created by YZX on 2018年05月17日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VideoPlayPresenter implements VideoPlayContract.Presenter {
    private VideoPlayContract.View mVideoPlayView;
    private Message mCurrentDownloadMessage;

    @Override
    public void attachView(VideoPlayContract.View view) {
        mVideoPlayView = view;
    }

    @Override
    public void detachView() {
        mVideoPlayView = null;
        if (mCurrentDownloadMessage != null) {
            IMClient.getInstance().chatManager().cancelDownloadMediaContent(mCurrentDownloadMessage);
        }
    }

    @Override
    public void downloadVideo(Message message) {
        mCurrentDownloadMessage = message;
        IMClient.getInstance().chatManager().downloadMediaContent(message, new com.yzx.chat.network.chat.DownloadCallback() {
            @Override
            public void onSuccess(Message message, Uri localUri) {
                mCurrentDownloadMessage = null;
                mVideoPlayView.setEnableProgressDialog(false);
                if (localUri != null && !TextUtils.isEmpty(localUri.getPath())) {
                    mVideoPlayView.playVideo(localUri.getPath());
                } else {
                    mVideoPlayView.showError(AndroidUtil.getString(R.string.VideoPlayActivity_DownloadVideoFail));
                }
            }

            @Override
            public void onProgress(Message message, int percentage) {
                mVideoPlayView.setEnableProgressDialog(true);
                mVideoPlayView.showProcess(percentage);
            }

            @Override
            public void onError(Message message, String error) {
                LogUtil.e(error);
                mVideoPlayView.setEnableProgressDialog(false);
                mVideoPlayView.showError(AndroidUtil.getString(R.string.VideoPlayActivity_DownloadVideoFail));
            }

            @Override
            public void onCanceled(Message message) {
                mCurrentDownloadMessage = null;
            }
        });
    }

//    private static String getFileNameFromUri(Uri videoUri) {
//        String url = videoUri.toString();
//        if (TextUtils.isEmpty(url)) {
//            return null;
//        }
//        String fileName = null;
//        if (url.lastIndexOf('/') + 1 < url.length()) {
//            fileName = url.substring(url.lastIndexOf('/') + 1);
//        }
//        return fileName;
//    }
}
