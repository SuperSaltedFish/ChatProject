package com.yzx.chat.mvp.view.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.configure.GlideApp;
import com.yzx.chat.mvp.contract.VideoPlayContract;
import com.yzx.chat.mvp.presenter.VideoPlayPresenter;
import com.yzx.chat.widget.view.MediaControllerView;
import com.yzx.chat.widget.view.ProgressDialog;

import java.util.Locale;

public class VideoPlayActivity extends BaseCompatActivity<VideoPlayContract.Presenter> implements VideoPlayContract.View {

    public static final String INTENT_EXTRA_VIDEO_URI = "VideoUri";
    public static final String INTENT_EXTRA_THUMBNAIL_URI = "ThumbnailUri";

    public static final String TRANSITION_NAME_IMAGE = "Thumbnail";

    private ImageView mIvThumbnail;
    private VideoView mVideoView;
    private MediaControllerView mMediaControllerView;
    private ProgressDialog mProgressDialog;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_video_play;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mIvThumbnail = findViewById(R.id.VideoPlayActivity_mIvThumbnail);
        mVideoView = findViewById(R.id.VideoPlayActivity_mVideoView);
        mProgressDialog = new ProgressDialog(this, String.format(Locale.getDefault(), getString(R.string.ProgressHint_DownloadVideo), 0));
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        Uri videoUri = getIntent().getParcelableExtra(INTENT_EXTRA_VIDEO_URI);
        if (videoUri == null || TextUtils.isEmpty(videoUri.getPath())) {
            finish();
            return;
        }
    //    ViewCompat.setTransitionName(mIvThumbnail, TRANSITION_NAME_IMAGE);
        Uri thumbnailUri = getIntent().getParcelableExtra(INTENT_EXTRA_THUMBNAIL_URI);
        if (thumbnailUri != null && !TextUtils.isEmpty(thumbnailUri.getPath())) {
            GlideApp.with(this)
                    .load(thumbnailUri)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .dontAnimate()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .into(mIvThumbnail);
        }
//        mMediaControllerView = new MediaControllerView(this);
//        mMediaControllerView.setAnchorView(mVideoView);
//        mMediaControllerView.setMediaPlayer(mVideoView);
        mPresenter.downloadVideo(videoUri);
        //  mVideoView.setVideoURI(Uri.parse("http://gslb.miaopai.com/stream/oxX3t3Vm5XPHKUeTS-zbXA__.mp4"));

    }


    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.pause();
    }

    @Override
    public void onBackPressed() {
       // ViewCompat.setTransitionName(mIvThumbnail, null);
        mVideoView.pause();
        super.onBackPressed();
    }

    @Override
    public void playVideo(String videoPath) {
        mVideoView.setVisibility(View.VISIBLE);
        mIvThumbnail.setVisibility(View.INVISIBLE);
        mVideoView.setVideoPath(videoPath);
        mVideoView.start();
    }

    @Override
    public void showProcess(int percent) {
        mProgressDialog.setHintText(String.format(Locale.getDefault(), getString(R.string.ProgressHint_DownloadVideo), percent));
    }

    @Override
    public void setEnableProgressDialog(boolean isEnable) {
        if (isEnable) {
            mProgressDialog.show();
        } else {
            mProgressDialog.hide();
        }
    }

    @Override
    public void showError(String error) {
        showToast(error);
    }

    @Override
    public VideoPlayContract.Presenter getPresenter() {
        return new VideoPlayPresenter();
    }
}
