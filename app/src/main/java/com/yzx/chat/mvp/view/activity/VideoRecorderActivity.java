package com.yzx.chat.mvp.view.activity;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.tool.DirectoryManager;
import com.yzx.chat.util.MD5Util;
import com.yzx.chat.util.VideoDecoder;
import com.yzx.chat.widget.view.Camera2RecodeView;
import com.yzx.chat.widget.view.RecorderButton;


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

    private static final int MAX_RECORDER_DURATION = 10 * 1000 + 300;
    private static final int MIN_TRIGGER_RECORDER_TIME = 300;

    private ImageView mIvClose;
    private ImageView mIvFlash;
    private ImageView mIvSwitchCamera;
    private ImageView mIvRestart;
    private ImageView mIvConfirm;
    private Camera2RecodeView mCamera2RecodeView;
    private TextureView mVideoTextureView;
    private RecorderButton mRecorderButton;
    private Handler mHandler;
    private VideoDecoder mVideoDecoder;

    private String mCurrentVideoPath;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_video_recorder;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mIvClose = findViewById(R.id.VideoRecorderActivity_mIvClose);
        mIvFlash = findViewById(R.id.VideoRecorderActivity_mIvFlash);
        mIvSwitchCamera = findViewById(R.id.VideoRecorderActivity_mIvSwitchCamera);
        mIvRestart = findViewById(R.id.VideoRecorderActivity_mIvRestart);
        mIvConfirm = findViewById(R.id.VideoRecorderActivity_mIvConfirm);
        mCamera2RecodeView = findViewById(R.id.VideoRecorderActivity_mCamera2RecodeView);
        mRecorderButton = findViewById(R.id.VideoRecorderActivity_mRecorderButton);
        mVideoTextureView = findViewById(R.id.VideoRecorderActivity_mVideoTextureView);
        mHandler = new Handler();
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        setSystemUiMode(SYSTEM_UI_MODE_FULLSCREEN);
        mIvFlash.setOnClickListener(mOnViewClick);
        mIvClose.setOnClickListener(mOnViewClick);
        mIvConfirm.setOnClickListener(mOnViewClick);
        mIvRestart.setOnClickListener(mOnViewClick);
        mIvSwitchCamera.setOnClickListener(mOnViewClick);
        mRecorderButton.setOnRecorderTouchListener(mOnRecorderTouchListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera2RecodeView.onResume();
        if (mVideoDecoder!=null&&!mVideoDecoder.isPlaying()) {
            mVideoDecoder.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera2RecodeView.onPause();
        if (mVideoDecoder!=null&&mVideoDecoder.isPlaying()) {
            mVideoDecoder.pause();
        } else {
            resetAndTryPlayRecorderContent();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mCamera2RecodeView.closeCamera();
        if (mVideoDecoder != null) {
            mVideoDecoder.release();
        }
    }

                private void resetAndTryPlayRecorderContent() {
                    mHandler.removeCallbacksAndMessages(null);
                    mRecorderButton.reset();
                    mRecorderButton.animate().scaleX(1f).scaleY(1f).setListener(null).start();
                    if (mCamera2RecodeView.isRecording()) {
                        mCamera2RecodeView.stopRecorder();
                        if (mVideoDecoder != null) {
                            mVideoDecoder.release();
                        }
                        mVideoDecoder = VideoDecoder.createEncoder(mCurrentVideoPath, new Surface(mVideoTextureView.getSurfaceTexture()));
                        if (mVideoDecoder != null) {
                            mVideoDecoder.start();
                            setCurrentState(CURRENT_STATE_PLAY);
            } else {
                showLongToast(getString(R.string.VideoRecorderActivity_PlayVideoError));
                restartPreview();
            }
        } else {
            setCurrentState(CURRENT_STATE_PREVIEW);
        }
    }

    private void setCurrentState(int state) {
        switch (state) {
            case CURRENT_STATE_PREVIEW:
                mIvClose.setVisibility(View.VISIBLE);
                mIvFlash.setVisibility(View.VISIBLE);
                mIvConfirm.setVisibility(View.INVISIBLE);
                mIvRestart.setVisibility(View.INVISIBLE);
                mVideoTextureView.setVisibility(View.INVISIBLE);
                mIvSwitchCamera.setVisibility(View.VISIBLE);
                mRecorderButton.setVisibility(View.VISIBLE);
                mCamera2RecodeView.setVisibility(View.VISIBLE);
                break;
            case CURRENT_STATE_RECORDER:
                mIvClose.setVisibility(View.INVISIBLE);
                mIvFlash.setVisibility(View.VISIBLE);
                mIvConfirm.setVisibility(View.INVISIBLE);
                mIvRestart.setVisibility(View.INVISIBLE);
                mVideoTextureView.setVisibility(View.INVISIBLE);
                mIvSwitchCamera.setVisibility(View.INVISIBLE);
                mRecorderButton.setVisibility(View.VISIBLE);
                mCamera2RecodeView.setVisibility(View.VISIBLE);
                break;
            case CURRENT_STATE_PLAY:
                mIvClose.setVisibility(View.VISIBLE);
                mIvFlash.setVisibility(View.INVISIBLE);
                mIvConfirm.setVisibility(View.VISIBLE);
                mIvRestart.setVisibility(View.VISIBLE);
                mVideoTextureView.setVisibility(View.VISIBLE);
                mIvSwitchCamera.setVisibility(View.INVISIBLE);
                mRecorderButton.setVisibility(View.INVISIBLE);
                mCamera2RecodeView.setVisibility(View.INVISIBLE);
                break;
        }
    }


    private void restartPreview() {
        mCamera2RecodeView.restartPreview();
        setCurrentState(CURRENT_STATE_PREVIEW);
        mCurrentVideoPath = null;
    }

    private void confirmVideo() {
        if (!TextUtils.isEmpty(mCurrentVideoPath)) {
            Intent intent = new Intent();
            intent.putExtra(INTENT_EXTRA_SAVE_PATH, mCurrentVideoPath);
            setResult(RESULT_CODE, intent);
        }
        finish();
    }

    private final View.OnClickListener mOnViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.VideoRecorderActivity_mIvClose:
                    finish();
                    break;
                case R.id.VideoRecorderActivity_mIvFlash:
                    mIvFlash.setSelected(!mIvFlash.isSelected());
                    mCamera2RecodeView.setEnableFlash(mIvFlash.isSelected());
                    break;
                case R.id.VideoRecorderActivity_mIvSwitchCamera:
                    mIvSwitchCamera.setSelected(!mIvSwitchCamera.isSelected());
                    if (mIvSwitchCamera.isSelected()) {
                        mCamera2RecodeView.switchCamera(Camera2RecodeView.CAMERA_TYPE_FRONT);
                    } else {
                        mCamera2RecodeView.switchCamera(Camera2RecodeView.CAMERA_TYPE_BACK);
                    }
                    break;
                case R.id.VideoRecorderActivity_mIvRestart:
                    if (mVideoDecoder != null) {
                        mVideoDecoder.release();
                    }
                    restartPreview();
                    break;
                case R.id.VideoRecorderActivity_mIvConfirm:
                    confirmVideo();
                    break;
            }
        }
    };


    private final RecorderButton.OnRecorderTouchListener mOnRecorderTouchListener = new RecorderButton.OnRecorderTouchListener() {

        @Override
        public void onDown() {
            mRecorderButton
                    .animate()
                    .scaleX(1.3f)
                    .scaleY(1.3f)
                    .setDuration(MIN_TRIGGER_RECORDER_TIME)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mCurrentVideoPath = DirectoryManager.getUserVideoPath() + MD5Util.encrypt16(String.valueOf(System.currentTimeMillis())) + ".mp4";
                            if (!mCamera2RecodeView.startRecorder(mCurrentVideoPath)) {
                                showToast(getString(R.string.VideoRecorderActivity_RecorderFail));
                                resetAndTryPlayRecorderContent();
                            } else {
                                mRecorderButton.startRecorderAnimation(MAX_RECORDER_DURATION, null);
                                setCurrentState(CURRENT_STATE_RECORDER);
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        resetAndTryPlayRecorderContent();
                                    }
                                }, MAX_RECORDER_DURATION);
                            }
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            resetAndTryPlayRecorderContent();
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }).start();
        }

        @Override
        public void onUp() {
            resetAndTryPlayRecorderContent();
        }

        @Override
        public void onOutOfBoundsChange(boolean isOutOfBounds) {

        }

        @Override
        public void onCancel() {
            resetAndTryPlayRecorderContent();
        }
    };
}
