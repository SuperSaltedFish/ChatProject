package com.yzx.chat.mvp.view.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.view.View;
import android.widget.ImageView;

import com.amap.api.maps.MapView;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.configure.GlideApp;
import com.yzx.chat.configure.GlideRequest;
import com.yzx.chat.tool.DirectoryHelper;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.widget.view.CarouselView;
import com.yzx.chat.widget.view.GlideHexagonTransform;
import com.yzx.chat.widget.view.MaskImageView;
import com.yzx.chat.widget.view.RoundLinearLayout;

import java.util.ArrayList;
import java.util.List;


public class TestActivity extends BaseCompatActivity {


    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        ImageView s = findViewById(R.id.sss);
        GlideApp.with(this)
                .load(R.drawable.temp_head_image)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .dontAnimate()
                .format(DecodeFormat.PREFER_RGB_565)
                .transform(new GlideHexagonTransform())
                .error(R.drawable.ic_avatar_default)
                .into(s);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    public void onClick1(View v) {

    }
}


