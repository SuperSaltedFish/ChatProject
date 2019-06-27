package com.yzx.chat.module;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.widget.view.RecodeView;


public class TestActivity extends BaseCompatActivity {

private RecodeView mCameraView;

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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mCameraView.onResume();
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

    }

}




