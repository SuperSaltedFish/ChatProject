package com.yzx.chat.mvp.view.activity;


import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.tool.DirectoryManager;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.VoiceCodec;
import com.yzx.chat.widget.view.Camera2PreviewView;
import com.yzx.chat.widget.view.Camera2RecodeView;
import com.yzx.chat.widget.view.MediaControllerView;
import com.yzx.chat.widget.view.RecorderButton;

import java.io.File;


public class TestActivity extends BaseCompatActivity {

private VideoView vvv;
private MediaControllerView mMediaControllerView;
    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        vvv = findViewById(R.id.vvv);
        mMediaControllerView = new MediaControllerView(this);
        mMediaControllerView.setAnchorView(vvv);
        mMediaControllerView.setMediaPlayer(vvv);
        vvv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    public void onClick(View v) {
        mMediaControllerView.show();
    }

}


