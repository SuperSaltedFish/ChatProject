package com.yzx.chat.mvp.view.activity;


import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.view.View;
import android.view.WindowManager;
import android.widget.VideoView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.widget.view.MediaControllerPopupWindow;



public class TestActivity extends BaseCompatActivity {

    private VideoView vvv;

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

//        Call<Void> call = DownloadUtil.createDownloadCall("http://gdown.baidu.com/data/wisegame/51ec7c88b5bfa985/tielu12306_150.apk", DirectoryManager.getPublicThumbnailPath(), RequestType.GET_DOWNLOAD);
//        call.setDownloadCallback(new DownloadCallback() {
//            @Override
//            public void onProcess(int process) {
//                Log.e("ss", String.valueOf(process));
//            }
//
//            @Override
//            public void onFinish(HttpResponse<String> httpResponse) {
//                Log.e("ss", String.valueOf(1000));
//
//            }
//
//            @Override
//            public void onDownloadError(@NonNull Throwable e) {
//                LogUtil.e("onDownloadError");
//            }
//
//            @Override
//            public boolean isExecuteNextTask() {
//                return false;
//            }
//        });
//        NetworkExecutor.getInstance().submit(call);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    public void onClick(View v) {
        MediaControllerPopupWindow mediaControllerPopupWindow = new MediaControllerPopupWindow(TestActivity.this);
        mediaControllerPopupWindow.setAnchorView(getWindow().getDecorView());
        mediaControllerPopupWindow.show();
    }

}


