package com.yzx.chat.util;

import android.media.MediaRecorder;
import android.os.Handler;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by YZX on 2017年12月08日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class VoiceRecorder {

    public static final int MAX_AMPLITUDE = 32767;

    private static final int DEFAULT_MAX_DURATION = 60 * 1000;
    private static final int DEFAULT_AMPLITUDE_REFRESH_INTERVAL = 1000 / 16;

    private String mSavePath;
    private int mMaxDuration;
    private OnRecorderStateListener mOnRecorderStateListener;
    private OnAmplitudeChange mOnAmplitudeChangeListener;

    private Handler mAmplitudeChangeHandler;
    private String initFailReason;
    private MediaRecorder mRecorder;
    private long mStartRecorderTime;
    private int mGetAmplitudeInterval = DEFAULT_AMPLITUDE_REFRESH_INTERVAL;
    private int mCurrentAmplitude;

    private final Object mRecorderLock = new Object();

    public VoiceRecorder() {
        this(null);
    }

    public VoiceRecorder(String savePath) {
        this(savePath, null);
    }

    public VoiceRecorder(String savePath, OnRecorderStateListener onRecorderStateListener) {
        this(savePath, DEFAULT_MAX_DURATION, onRecorderStateListener);
    }

    public VoiceRecorder(String savePath, int maxDuration) {
        this(savePath, maxDuration, null);
    }

    public VoiceRecorder(String savePath, int maxDuration, OnRecorderStateListener onRecorderStateListener) {
        mSavePath = savePath;
        mMaxDuration = maxDuration;
        mOnRecorderStateListener = onRecorderStateListener;
    }

    private void init() {
        mRecorder = new MediaRecorder();
        try {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(mSavePath);
            mRecorder.setMaxDuration(mMaxDuration);
        } catch (RuntimeException e) {
            initFailReason = e.toString();
            return;
        }
        mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                release();
                if (what != MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN) {
                    if (mOnRecorderStateListener != null) {
                        mOnRecorderStateListener.onComplete(mSavePath, System.currentTimeMillis() - mStartRecorderTime);
                    }
                } else {
                    mOnRecorderStateListener.onError("VoiceRecorder Error:MEDIA_RECORDER_INFO_UNKNOWN");
                }
            }
        });
        mRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                release();
                if (mOnRecorderStateListener != null) {
                    if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN) {
                        mOnRecorderStateListener.onError("VoiceRecorder Error:MEDIA_RECORDER_ERROR_UNKNOWN");
                    } else {
                        mOnRecorderStateListener.onError("VoiceRecorder Error:MEDIA_ERROR_SERVER_DIED");
                    }
                }
            }
        });
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            initFailReason = e.toString();
        }

    }

    private void release() {
        synchronized (mRecorderLock) {
            if (mAmplitudeChangeHandler != null) {
                mAmplitudeChangeHandler.removeCallbacks(mGetAmplitudeRunnable);
            }
            if (mRecorder != null) {
                mRecorder.reset();
                mRecorder.release();
            }
            mCurrentAmplitude = 0;
            mRecorder = null;
        }
    }

    private void updateAmplitude() {
        if (mOnAmplitudeChangeListener == null) {
            return;
        }
        if (mAmplitudeChangeHandler == null) {
            mAmplitudeChangeHandler = new Handler();
        }
        mAmplitudeChangeHandler.post(mGetAmplitudeRunnable);
    }

    private final Runnable mGetAmplitudeRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mRecorderLock) {
                if (mRecorder == null || mOnAmplitudeChangeListener == null) {
                    return;
                }
                int nowAmplitude;
                try {
                    nowAmplitude = mRecorder.getMaxAmplitude();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    return;
                }
                if (nowAmplitude != mCurrentAmplitude) {
                    mOnAmplitudeChangeListener.onAmplitudeChange(nowAmplitude);
                    mCurrentAmplitude = nowAmplitude;
                }
                mAmplitudeChangeHandler.postDelayed(this, mGetAmplitudeInterval);
            }
        }
    };

    public void prepare() {
        release();
        init();
    }


    public void start() {
        if (!TextUtils.isEmpty(initFailReason)) {
            if (mOnRecorderStateListener != null) {
                mOnRecorderStateListener.onError(initFailReason);
            }
            return;
        }
        try {
            mRecorder.start();
            mStartRecorderTime = System.currentTimeMillis();
            updateAmplitude();
        } catch (RuntimeException e) {
            if (mOnRecorderStateListener != null) {
                mOnRecorderStateListener.onError(e.toString());
            }
        }
    }

    public void stop() {
        if (mRecorder == null) {
            return;
        }
        String error = null;
        try {
            mRecorder.stop();
        } catch (RuntimeException e) {
            error = e.toString();
        }
        release();
        if (TextUtils.isEmpty(error)) {
            if (mOnRecorderStateListener != null) {
                mOnRecorderStateListener.onComplete(mSavePath, System.currentTimeMillis() - mStartRecorderTime);
            }
        } else {
            if (mOnRecorderStateListener != null) {
                mOnRecorderStateListener.onError(error);
            }
        }
    }

    public boolean cancelAndDelete() {
        try {
            mRecorder.stop();
        } catch (RuntimeException ignored) {
        }
        release();
        File file = new File(mSavePath);
        return !file.exists() || file.delete();
    }

    public void setSavePath(String savePath) {
        mSavePath = savePath;
    }

    public void setMaxDuration(int maxDuration) {
        mMaxDuration = maxDuration;
    }

    public void setOnRecorderStateListener(OnRecorderStateListener listener) {
        mOnRecorderStateListener = listener;
    }

    public void setGetAmplitudeInterval(int ms) {
        mGetAmplitudeInterval = ms;
    }

    public void setOnAmplitudeChange(OnAmplitudeChange onAmplitudeChange, Handler handler) {
        setAmplitudeChangeHandler(handler);
        mOnAmplitudeChangeListener = onAmplitudeChange;
    }

    public void setAmplitudeChangeHandler(Handler handler) {
        if (mAmplitudeChangeHandler != null) {
            mAmplitudeChangeHandler.removeCallbacks(mGetAmplitudeRunnable);
        }
        mAmplitudeChangeHandler = handler;
    }

    public Handler getAmplitudeChangeHandler() {
        return mAmplitudeChangeHandler;
    }

    public interface OnRecorderStateListener {
        void onComplete(String filePath, long duration);

        void onError(String error);
    }

    public interface OnAmplitudeChange {
        void onAmplitudeChange(int amplitude);
    }
}
