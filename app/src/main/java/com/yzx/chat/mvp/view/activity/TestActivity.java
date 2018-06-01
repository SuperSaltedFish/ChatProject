package com.yzx.chat.mvp.view.activity;


import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.configure.GlideApp;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.widget.view.GlideSemicircleTransform;


public class TestActivity extends BaseCompatActivity {

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
//        ImageView imageView = findViewById(R.id.StrangerProfileActivity_mIvPicture);
//        GlideApp.with(this)
//                .load(R.drawable.src_sex_man)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .transforms(new GlideSemicircleTransform(AndroidUtil.dip2px(40), Color.WHITE))
//                .into(imageView);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS);
    }

    public void onClick(View v) {

    }

}


