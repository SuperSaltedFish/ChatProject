package com.yzx.chat.mvp.view.activity;


import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.tool.DirectoryManager;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.VoiceCodec;
import com.yzx.chat.widget.view.Camera2PreviewView;
import com.yzx.chat.widget.view.Camera2RecodeView;

import java.io.File;



public class TestActivity extends BaseCompatActivity {
    Camera2PreviewView mCamera2PreviewView;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

        mCamera2PreviewView = findViewById(R.id.ssss);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera2PreviewView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera2PreviewView.onPause();
    }

    private boolean i = true;

    public void onClick(View v) {
        Camera2RecodeView videoTextureView = findViewById(R.id.ssss);
        if (i) {
            if (new File(DirectoryManager.getPublicVideoPath() + "adw.mp4").exists()) {
                new File(DirectoryManager.getPublicVideoPath() + "adw.mp4").delete();
            }
            LogUtil.e("" + videoTextureView.startRecorder(DirectoryManager.getPublicVideoPath() + "adw.mp4"));
        } else {
            videoTextureView.stopRecorder();
        }
        i = !i;
    }

}


