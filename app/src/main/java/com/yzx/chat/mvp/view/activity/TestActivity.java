package com.yzx.chat.mvp.view.activity;


import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.widget.view.CropImageView;
import com.yzx.chat.widget.view.VisualizerView;


public class TestActivity extends BaseCompatActivity {

    private CropImageView mCropImageView;
    private ImageView mImageView;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mCropImageView = findViewById(R.id.aaa);
        mImageView = findViewById(R.id.bbb);
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                mImageView.setImageBitmap(mCropImageView.crop());
                return false;
            }
        });
    }

    @Override
    protected void setup(Bundle savedInstanceState) {

    }


    public void onClick(View v) {

    }

    public void onClick1(View v) {

    }
}


