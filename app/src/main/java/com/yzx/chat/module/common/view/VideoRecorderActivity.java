package com.yzx.chat.module.common.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.WindowManager;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.util.VideoEncoder;
import com.yzx.chat.widget.view.CameraView;
import com.yzx.chat.widget.view.RecodeView;


/**
 * Created by YZX on 2018年05月08日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VideoRecorderActivity extends BaseCompatActivity {

    private static final String TAG = VideoRecorderActivity.class.getName();
    public static final String INTENT_EXTRA_SAVE_PATH = "SavePath";
    public static final int RESULT_CODE = VideoRecorderActivity.class.hashCode();

    private static final int CURRENT_STATE_PREVIEW = 1;
    private static final int CURRENT_STATE_RECORDER = 2;
    private static final int CURRENT_STATE_PLAY = 3;

    private static final int MAX_RECORDER_DURATION = Constants.MAX_VIDEO_RECORDER_DURATION;
    private static final int MIN_TRIGGER_RECORDER_TIME = 300;


    private RecodeView mRecodeView;
    private Handler mWorkHandle;
    private VideoEncoder mVideoEncoder;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_video_recorder;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mRecodeView = findViewById(R.id.mRecodeView);

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mWorkHandle = new Handler(handlerThread.getLooper());
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setSystemUiMode(SYSTEM_UI_MODE_FULLSCREEN);
        setBrightness(0.9f);

//        mRecodeView.setCaptureCallback(mCaptureCallback,mWorkHandle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRecodeView.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRecodeView.stopPreview();
    }

    @Override
    protected void onDestroy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }

    private CameraView.CaptureCallback mCaptureCallback = new CameraView.CaptureCallback() {
        @Override
        public void onCapture(byte[] yuv, int width, int height, int orientation) {

        }
    };

}
