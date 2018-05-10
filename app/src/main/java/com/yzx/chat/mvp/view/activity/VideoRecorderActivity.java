package com.yzx.chat.mvp.view.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.constraint.Group;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.tool.DirectoryManager;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.MD5Util;
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

    private static final int MAX_RECORDER_DURATION = 12 * 1000;
    private static final int MIN_TRIGGER_RECORDER_TIME = 300;
    private ImageView mIvClose;
    private ImageView mIvFlash;
    private ImageView mIvSwitchCamera;
    private ImageView mIvRestart;
    private ImageView mIvConfirm;
    private Camera2RecodeView mCamera2RecodeView;
    private RecorderButton mRecorderButton;
    private VideoView mVideoView;

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
        mVideoView = findViewById(R.id.VideoRecorderActivity_mVideoView);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        mIvFlash.setOnClickListener(mOnViewClick);
        mIvClose.setOnClickListener(mOnViewClick);
        mIvConfirm.setOnClickListener(mOnViewClick);
        mIvRestart.setOnClickListener(mOnViewClick);
        mIvSwitchCamera.setOnClickListener(mOnViewClick);
        mRecorderButton.setOnRecorderTouchListener(mOnRecorderTouchListener);
        mRecorderButton.setOnRecorderAnimationListener(mRecorderAnimationListener);
        mVideoView.setOnPreparedListener(mOnVideoPreparedListener);
        mVideoView.setOnErrorListener(mOnPlayVideoErrorListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera2RecodeView.onResume();
        mVideoView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera2RecodeView.onPause();
        mVideoView.pause();
        reset();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera2RecodeView.closeCamera();
    }

    private void reset() {
        mRecorderButton.reset();
        if (mCamera2RecodeView.isRecording()) {
            mCamera2RecodeView.stopRecorder();
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
                mVideoView.setVisibility(View.INVISIBLE);
                mIvSwitchCamera.setVisibility(View.VISIBLE);
                mRecorderButton.setVisibility(View.VISIBLE);
                mCamera2RecodeView.setVisibility(View.VISIBLE);
                break;
            case CURRENT_STATE_RECORDER:
                mIvClose.setVisibility(View.INVISIBLE);
                mIvFlash.setVisibility(View.VISIBLE);
                mIvConfirm.setVisibility(View.INVISIBLE);
                mIvRestart.setVisibility(View.INVISIBLE);
                mVideoView.setVisibility(View.INVISIBLE);
                mIvSwitchCamera.setVisibility(View.INVISIBLE);
                mRecorderButton.setVisibility(View.VISIBLE);
                mCamera2RecodeView.setVisibility(View.VISIBLE);
                break;
            case CURRENT_STATE_PLAY:
                mIvClose.setVisibility(View.VISIBLE);
                mIvFlash.setVisibility(View.INVISIBLE);
                mIvConfirm.setVisibility(View.VISIBLE);
                mIvRestart.setVisibility(View.VISIBLE);
                mVideoView.setVisibility(View.VISIBLE);
                mIvSwitchCamera.setVisibility(View.INVISIBLE);
                mRecorderButton.setVisibility(View.INVISIBLE);
                mCamera2RecodeView.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void playVideo() {
        setCurrentState(CURRENT_STATE_PLAY);
        mVideoView.setVideoPath(mCurrentVideoPath);
        mVideoView.start();
    }

    private void restartPreview() {
        mVideoView.stopPlayback();
        setCurrentState(CURRENT_STATE_PREVIEW);
    }

    private void confirmVideo() {
        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_SAVE_PATH, mCurrentVideoPath);
        setResult(RESULT_CODE, intent);
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
            mRecorderButton.startRecorderAnimation(MIN_TRIGGER_RECORDER_TIME, MAX_RECORDER_DURATION);
        }

        @Override
        public void onUp() {
            reset();
            playVideo();
        }

        @Override
        public void onOutOfBoundsChange(boolean isOutOfBounds) {

        }

        @Override
        public void onCancel() {
            reset();
        }
    };

    private final RecorderButton.OnRecorderAnimationListener mRecorderAnimationListener = new RecorderButton.OnRecorderAnimationListener() {
        @Override
        public void onPrepareAnimationStart() {
            setCurrentState(CURRENT_STATE_RECORDER);
        }

        @Override
        public void onPrepareAnimationEnd() {

        }

        @Override
        public void onProgressAnimationStart() {
            mCurrentVideoPath = DirectoryManager.getPublicVideoPath() + MD5Util.encrypt16(String.valueOf(System.currentTimeMillis())) + ".mp4";
            if (!mCamera2RecodeView.startRecorder(mCurrentVideoPath)) {
                showToast(getString(R.string.VideoRecorderActivity_RecorderFail));
                mRecorderButton.reset();
            }
        }

        @Override
        public void onProgressAnimationEnd() {
            reset();
            playVideo();
        }
    };

    private final MediaPlayer.OnPreparedListener mOnVideoPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.setLooping(true);
        }
    };

    private final MediaPlayer.OnErrorListener mOnPlayVideoErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            setCurrentState(CURRENT_STATE_PREVIEW);
            showToast(getString(R.string.VideoRecorderActivity_PlayVideoError));
            return false;
        }
    };
}
