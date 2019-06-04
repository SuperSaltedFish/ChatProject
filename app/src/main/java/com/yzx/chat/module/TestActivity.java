package com.yzx.chat.module;

import android.os.Bundle;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
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
//        mCameraView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mCameraView.onDestroy();
    }

    public void onClick1(View v) {
//        switch (mCameraView.getCameraFacingType()) {
//            case BasicCamera.CAMERA_FACING_FRONT:
//                mCameraView.switchCamera(BasicCamera.CAMERA_FACING_BACK);
//                break;
//            case BasicCamera.CAMERA_FACING_BACK:
//                mCameraView.switchCamera(BasicCamera.CAMERA_FACING_FRONT);
//                break;
//        }

    }



}




