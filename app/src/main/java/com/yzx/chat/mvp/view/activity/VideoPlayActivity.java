package com.yzx.chat.mvp.view.activity;

import android.os.Bundle;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;

public class VideoPlayActivity extends BaseCompatActivity {

    public static final String INTENT_EXTRA_VIDEO_URI = "VideoUri";
    public static final String INTENT_EXTRA_THUMBNAIL_URI = "ThumbnailUri";

    @Override
    protected int getLayoutID() {
        return R.layout.activity_video_play;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

    }

    @Override
    protected void setup(Bundle savedInstanceState) {

    }
}
