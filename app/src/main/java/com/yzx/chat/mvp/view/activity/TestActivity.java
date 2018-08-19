package com.yzx.chat.mvp.view.activity;

import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.view.View;

import com.amap.api.maps.MapView;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.tool.DirectoryHelper;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.view.CarouselView;
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
        MaskImageView maskImageView = findViewById(R.id.MaskImageView);
        //maskImageView.setCurrentMode(MaskImageView.MASK_MODE_LINEAR);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    public void onClick(View v) {


    }

    public void onClick1(View v) {

    }
}


