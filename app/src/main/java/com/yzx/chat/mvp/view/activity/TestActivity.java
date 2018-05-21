package com.yzx.chat.mvp.view.activity;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.DownloadCallback;
import com.yzx.chat.network.framework.DownloadUtil;
import com.yzx.chat.network.framework.HttpResponse;
import com.yzx.chat.network.framework.NetworkExecutor;
import com.yzx.chat.network.framework.RequestType;
import com.yzx.chat.tool.DirectoryManager;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.widget.view.MediaControllerView;



public class TestActivity extends BaseCompatActivity {

    private VideoView vvv;
    private MediaControllerView mMediaControllerView;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
//        vvv = findViewById(R.id.vvv);
//        mMediaControllerView = new MediaControllerView(this);
//        mMediaControllerView.setAnchorView(vvv);
//        mMediaControllerView.setMediaPlayer(vvv);
//        vvv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        Call<Void> call = DownloadUtil.createDownloadCall("http://gdown.baidu.com/data/wisegame/51ec7c88b5bfa985/tielu12306_150.apk", DirectoryManager.getPublicThumbnailPath(), RequestType.GET_DOWNLOAD);
        call.setDownloadCallback(new DownloadCallback() {
            @Override
            public void onProcess(int process) {
                Log.e("ss", String.valueOf(process));
            }

            @Override
            public void onFinish(HttpResponse<String> httpResponse) {
                Log.e("ss", String.valueOf(1000));

            }

            @Override
            public void onDownloadError(@NonNull Throwable e) {
                LogUtil.e("onDownloadError");
            }

            @Override
            public boolean isExecuteNextTask() {
                return false;
            }
        });
        NetworkExecutor.getInstance().submit(call);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    public void onClick(View v) {
        mMediaControllerView.show();
    }

}


