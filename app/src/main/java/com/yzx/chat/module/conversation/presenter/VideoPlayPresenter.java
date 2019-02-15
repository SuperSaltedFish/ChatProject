package com.yzx.chat.module.conversation.presenter;

import android.net.Uri;
import android.text.TextUtils;

import com.yzx.chat.R;
import com.yzx.chat.module.conversation.contract.VideoPlayContract;
import com.yzx.chat.core.listener.DownloadCallback;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.core.util.LogUtil;

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
            AppClient.getInstance().getChatManager().cancelDownloadMediaContent(mCurrentDownloadMessage);
        }
    }

    @Override
    public void downloadVideo(Message message) {
        mCurrentDownloadMessage = message;
        mVideoPlayView.setEnableLoading(true);
        AppClient.getInstance().getChatManager().downloadMediaContent(message, new DownloadCallback() {
            @Override
            public void onSuccess(Message message, Uri localUri) {
                mCurrentDownloadMessage = null;
                mVideoPlayView.setEnableLoading(false);
                if (localUri != null && !TextUtils.isEmpty(localUri.getPath())) {
                    mVideoPlayView.playVideo(localUri.getPath());
                } else {
                    LogUtil.e("localUri==" + (localUri == null ? "null" : localUri.getPath()));
                    mVideoPlayView.showErrorDialog(AndroidHelper.getString(R.string.VideoPlayActivity_DownloadVideoFail));
                }
            }

            @Override
            public void onProgress(Message message, int percentage) {
                mVideoPlayView.showProcess(percentage);
            }

            @Override
            public void onError(Message message, String error) {
                LogUtil.e(error);
                mVideoPlayView.setEnableLoading(false);
                mVideoPlayView.showErrorDialog(AndroidHelper.getString(R.string.VideoPlayActivity_DownloadVideoFail));
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
