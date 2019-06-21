package com.yzx.chat.module;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;


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
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStop() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClick1(View v) {
//        mCameraView.startPreview();
//        if (mCameraView.isSelected()) {
//            mCameraView.stopRecode();
//        } else {
//            mCameraView.startRecode(DirectoryHelper.getVideoPath() + "/ddd.mp4");
//        }
//        mCameraView.setSelected(!mCameraView.isSelected());
    }

}




