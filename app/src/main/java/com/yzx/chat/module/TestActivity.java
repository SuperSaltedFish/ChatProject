package com.yzx.chat.module;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.util.BasicCamera;
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
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCameraView.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStop() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
            mCameraView.switchCamera(BasicCamera.CAMERA_FACING_FRONT);
        } else {
            mCameraView.switchCamera(BasicCamera.CAMERA_FACING_BACK);
        }
        mCameraView.setSelected(!mCameraView.isSelected());
    }

}




