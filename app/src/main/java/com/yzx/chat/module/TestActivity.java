package com.yzx.chat.module;

import android.os.Bundle;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.tool.DirectoryHelper;
import com.yzx.chat.widget.view.RecodeView;


public class TestActivity extends BaseCompatActivity {

    RecodeView mCameraView;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mCameraView = findViewById(R.id.mCameraView);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mCameraView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCameraView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraView.onDestroy();
    }

    public void onClick1(View v) {
//        mCameraView.startPreview();
        if (mCameraView.isSelected()) {
            mCameraView.stopRecode();
        } else {
            mCameraView.startRecode(DirectoryHelper.getVideoPath() + "/ddd.mp4");
        }
        mCameraView.setSelected(!mCameraView.isSelected());
    }

}




