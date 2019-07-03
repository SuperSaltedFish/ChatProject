package com.yzx.chat.module.common.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.tool.DirectoryHelper;
import com.yzx.chat.util.BasicCamera;
import com.yzx.chat.widget.view.RecordView;
import com.yzx.chat.widget.view.RecorderButton;
import com.yzx.chat.widget.view.VideoSurfaceView;

import java.io.File;


/**
 * Created by YZX on 2018年05月08日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VideoRecorderActivity extends BaseCompatActivity {

    public static final String INTENT_EXTRA_SAVE_PATH = "SavePath";
    public static final int RESULT_CODE = VideoRecorderActivity.class.hashCode();

    private static final int CURRENT_STATE_PREVIEW = 1;
    private static final int CURRENT_STATE_RECORDER = 2;
    private static final int CURRENT_STATE_PLAY = 3;

    private static final int MAX_RECORDER_DURATION = Constants.MAX_VIDEO_RECORDER_DURATION;
    private static final int MIN_TRIGGER_RECORDER_TIME = 400;

    private VideoSurfaceView mSvVideoPlay;
    private RecordView mRecordView;
    private ImageView mIvClose;
    private ImageView mIvFlash;
    private ImageView mIvSwitchCamera;
    private RecorderButton mRecorderButton;
    private ImageView mIvConfirm;
    private ImageView mIvRestart;

    private String mCurrentVideoPath;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_video_recorder;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mSvVideoPlay = findViewById(R.id.mSvVideoPlay);
        mRecordView = findViewById(R.id.mRecordView);
        mIvClose = findViewById(R.id.mIvClose);
        mIvFlash = findViewById(R.id.mIvFlash);
        mIvSwitchCamera = findViewById(R.id.mIvSwitchCamera);
        mRecorderButton = findViewById(R.id.mRecorderButton);
        mIvConfirm = findViewById(R.id.mIvConfirm);
        mIvRestart = findViewById(R.id.mIvRestart);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawable(null);
        setSystemUiMode(SYSTEM_UI_MODE_FULLSCREEN);
        setBrightness(0.85f);

        mIvFlash.setOnClickListener(mOnViewClick);
        mIvClose.setOnClickListener(mOnViewClick);
        mIvConfirm.setOnClickListener(mOnViewClick);
        mIvRestart.setOnClickListener(mOnViewClick);
        mIvSwitchCamera.setOnClickListener(mOnViewClick);

        mRecorderButton.setDuration(MAX_RECORDER_DURATION);
        mRecorderButton.setOnRecorderTouchListener(mOnRecorderTouchListener);

        setCurrentState(CURRENT_STATE_PREVIEW);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setCurrentState(int state) {
        switch (state) {
            case CURRENT_STATE_PREVIEW:
                mIvFlash.setSelected(false);
                mIvClose.setVisibility(View.VISIBLE);
                mIvFlash.setVisibility(View.VISIBLE);
                mIvConfirm.setVisibility(View.INVISIBLE);
                mIvRestart.setVisibility(View.INVISIBLE);
                mSvVideoPlay.setVisibility(View.INVISIBLE);
                mIvSwitchCamera.setVisibility(View.VISIBLE);
                mRecorderButton.setVisibility(View.VISIBLE);
                mRecordView.setVisibility(View.VISIBLE);
                break;
            case CURRENT_STATE_RECORDER:
                mIvClose.setVisibility(View.INVISIBLE);
                mIvFlash.setVisibility(View.VISIBLE);
                mIvConfirm.setVisibility(View.INVISIBLE);
                mIvRestart.setVisibility(View.INVISIBLE);
                mSvVideoPlay.setVisibility(View.INVISIBLE);
                mIvSwitchCamera.setVisibility(View.INVISIBLE);
                mRecorderButton.setVisibility(View.VISIBLE);
                mRecordView.setVisibility(View.VISIBLE);
                break;
            case CURRENT_STATE_PLAY:
                mIvClose.setVisibility(View.VISIBLE);
                mIvFlash.setVisibility(View.INVISIBLE);
                mIvConfirm.setVisibility(View.VISIBLE);
                mIvRestart.setVisibility(View.VISIBLE);
                mSvVideoPlay.setVisibility(View.VISIBLE);
                mIvSwitchCamera.setVisibility(View.INVISIBLE);
                mRecorderButton.setVisibility(View.INVISIBLE);
                mRecordView.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private final View.OnClickListener mOnViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.mIvClose:
                    finish();
                    break;
                case R.id.mIvFlash:
                    mIvFlash.setSelected(!mIvFlash.isSelected());
                    mRecordView.setEnableFlash(mIvFlash.isSelected());
                    break;
                case R.id.mIvSwitchCamera:
                    mIvSwitchCamera.setSelected(!mIvSwitchCamera.isSelected());
                    if (mIvSwitchCamera.isSelected()) {
                        mRecordView.switchCamera(BasicCamera.CAMERA_FACING_FRONT);
                    } else {
                        mRecordView.switchCamera(BasicCamera.CAMERA_FACING_BACK);
                    }
                    mIvFlash.setSelected(false);
                    break;
                case R.id.mIvRestart:
                    mSvVideoPlay.stopPlay();
                    restartPreview();
                    break;
                case R.id.mIvConfirm:
                    confirmVideo();
                    break;
            }
        }
    };

    private final RecorderButton.OnRecorderTouchListener mOnRecorderTouchListener = new RecorderButton.OnRecorderTouchListener() {
        private long mStartTime;

        @Override
        public void onStart() {
            if (startRecord()) {
                mStartTime = System.currentTimeMillis();
            } else {
                mRecorderButton.reset();
            }
        }

        @Override
        public void onFinish() {
            if (System.currentTimeMillis()-mStartTime < MIN_TRIGGER_RECORDER_TIME) {
                stopRecord(false);
            } else {
                stopRecord(true);
                playVideo();
            }
        }

        @Override
        public void onCancel() {
            stopRecord(false);
            restartPreview();
        }
    };

    private boolean startRecord() {
//        mCurrentVideoPath = DirectoryHelper.getVideoPath() + MD5Util.encrypt16(String.valueOf(System.currentTimeMillis())) + ".mp4";
        mCurrentVideoPath = DirectoryHelper.getVideoPath() + "/fff.mp4";
        if (mRecordView.startRecode(mCurrentVideoPath)) {
            setCurrentState(CURRENT_STATE_RECORDER);
            return true;
        } else {
            mCurrentVideoPath = null;
            showToast(getString(R.string.VideoRecorderActivity_RecorderFail));
            return false;
        }
    }

    private void stopRecord(boolean isNeedSave) {
        mRecordView.stopRecode();
        if (!isNeedSave && !TextUtils.isEmpty(mCurrentVideoPath)) {
            File file = new File(mCurrentVideoPath);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
        }
    }

    private void playVideo() {
        setCurrentState(CURRENT_STATE_PLAY);
        mSvVideoPlay.setVideoPath(mCurrentVideoPath, new VideoSurfaceView.OnPlayStateListener() {
            @Override
            public void onPlaySuccessful() {
//                setCurrentState(CURRENT_STATE_PLAY);
            }

            @Override
            public void onPlayFailure() {
                showLongToast(getString(R.string.VideoRecorderActivity_PlayVideoError));
                restartPreview();
            }
        });
    }

    private void restartPreview() {
        mCurrentVideoPath = null;
        setCurrentState(CURRENT_STATE_PREVIEW);
    }

    private void confirmVideo() {
        if (!TextUtils.isEmpty(mCurrentVideoPath)) {
            Intent intent = new Intent();
            intent.putExtra(INTENT_EXTRA_SAVE_PATH, mCurrentVideoPath);
            setResult(RESULT_CODE, intent);
        }
        finish();
    }


}
