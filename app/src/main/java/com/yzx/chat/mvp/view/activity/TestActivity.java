package com.yzx.chat.mvp.view.activity;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.tool.DirectoryManager;
import com.yzx.chat.util.Camera2Helper;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.RSAUtil;
import com.yzx.chat.util.VideoRecorder;
import com.yzx.chat.widget.view.Camera2PreviewView;
import com.yzx.chat.widget.view.Camera2RecodeView;
import com.yzx.chat.widget.view.VideoTextureView;

import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.List;


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


