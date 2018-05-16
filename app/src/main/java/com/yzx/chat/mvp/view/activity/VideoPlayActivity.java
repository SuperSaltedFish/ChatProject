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
import com.yzx.chat.widget.view.MediaControllerView;

public class VideoPlayActivity extends BaseCompatActivity {

    public static final String INTENT_EXTRA_VIDEO_URI = "VideoUri";
    public static final String INTENT_EXTRA_THUMBNAIL_URI = "ThumbnailUri";

    public static final String TRANSITION_NAME_IMAGE = "Thumbnail";

    private ImageView mIvThumbnail;
    private VideoView mVideoView;
    private MediaControllerView mMediaControllerView;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_video_play;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mIvThumbnail = findViewById(R.id.VideoPlayActivity_mIvThumbnail);
        mVideoView = findViewById(R.id.VideoPlayActivity_mVideoView);
        ViewCompat.setTransitionName(mIvThumbnail, TRANSITION_NAME_IMAGE);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        Uri videoUri = getIntent().getParcelableExtra(INTENT_EXTRA_VIDEO_URI);
        if (videoUri == null || TextUtils.isEmpty(videoUri.getPath())) {
            finish();
            return;
        }
        Uri thumbnailUri = getIntent().getParcelableExtra(INTENT_EXTRA_THUMBNAIL_URI);
        if(thumbnailUri!=null&&!TextUtils.isEmpty(thumbnailUri.getPath())){
            GlideApp.with(this)
                    .load(thumbnailUri)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .dontAnimate()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .into(mIvThumbnail);
        }
        mMediaControllerView = new MediaControllerView(this);
        mMediaControllerView.setAnchorView(mVideoView);
        mMediaControllerView.setMediaPlayer(mVideoView);
        mVideoView.setMediaController(new MediaController(this));
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
        ViewCompat.setTransitionName(mIvThumbnail, null);
        super.onBackPressed();
    }

}
