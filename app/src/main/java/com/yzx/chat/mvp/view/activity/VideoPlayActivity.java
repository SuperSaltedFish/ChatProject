package com.yzx.chat.mvp.view.activity;

import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
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
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.VideoDecoder;
import com.yzx.chat.widget.view.AutoFitTextureView;
import com.yzx.chat.widget.view.MediaControllerPopupWindow;
import com.yzx.chat.widget.view.ProgressDialog;

import java.util.Locale;

public class VideoPlayActivity extends BaseCompatActivity<VideoPlayContract.Presenter> implements VideoPlayContract.View {

    public static final String INTENT_EXTRA_VIDEO_URI = "VideoUri";
    public static final String INTENT_EXTRA_THUMBNAIL_URI = "ThumbnailUri";

    public static final String TRANSITION_NAME_IMAGE = "Thumbnail";

    private ImageView mIvThumbnail;
    private AutoFitTextureView mTextureView;
    private ProgressDialog mProgressDialog;
    private MediaControllerPopupWindow mMediaControllerPopupWindow;
    private VideoDecoder mVideoDecoder;

    private String mVideoPath;
    private boolean isWaitPlay;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_video_play;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mIvThumbnail = findViewById(R.id.VideoPlayActivity_mIvThumbnail);
        mTextureView = findViewById(R.id.VideoPlayActivity_mTextureView);
        mProgressDialog = new ProgressDialog(this, String.format(Locale.getDefault(), getString(R.string.ProgressHint_DownloadVideo), 0));
        mMediaControllerPopupWindow = new MediaControllerPopupWindow(this);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        Uri videoUri = getIntent().getParcelableExtra(INTENT_EXTRA_VIDEO_URI);
        if (videoUri == null || TextUtils.isEmpty(videoUri.getPath())) {
            finish();
            return;
        }
        ViewCompat.setTransitionName(mIvThumbnail, TRANSITION_NAME_IMAGE);
        Uri thumbnailUri = getIntent().getParcelableExtra(INTENT_EXTRA_THUMBNAIL_URI);
        if (thumbnailUri != null && !TextUtils.isEmpty(thumbnailUri.getPath())) {
            GlideApp.with(this)
                    .load(thumbnailUri)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .dontAnimate()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .into(mIvThumbnail);
        }

        mMediaControllerPopupWindow.setAnchorView(mTextureView);
        mMediaControllerPopupWindow.setOnCloseClickListener(mOnCloseClickListener);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);

        mPresenter.downloadVideo(videoUri);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoDecoder != null) {
            mVideoDecoder.release();
        }
    }

    @Override
    public void onBackPressed() {
        ViewCompat.setTransitionName(mIvThumbnail, null);
        ViewCompat.setTransitionName(mTextureView, TRANSITION_NAME_IMAGE);
        super.onBackPressed();
    }

    @Override
    public void playVideo(String videoPath) {
        if (mTextureView.getSurfaceTexture() == null) {
            mVideoPath = videoPath;
            isWaitPlay = true;
        } else {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            mVideoDecoder = VideoDecoder.createEncoder(videoPath, new Surface(texture));
            if (mVideoDecoder == null) {
                showToast(getString(R.string.VideoPlayActivity_PlayVideoFail));
            } else {
                mMediaControllerPopupWindow.setMediaPlayer(mVideoDecoder);
                Size resolutionRatio = mVideoDecoder.getVideoResolutionRatio();
                if (resolutionRatio != null) {
                    int width = resolutionRatio.getWidth();
                    int height = resolutionRatio.getHeight();
                    if (width > 0 && height > 0) {
                        mTextureView.setAspectRatioSize(resolutionRatio);
                        texture.setDefaultBufferSize(width, height);
                    }
                }
                mVideoDecoder.start();
            }
        }
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

    private final View.OnClickListener mOnCloseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (isWaitPlay) {
                isWaitPlay = false;
                playVideo(mVideoPath);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
}
