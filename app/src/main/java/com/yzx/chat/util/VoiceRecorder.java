package com.yzx.chat.util;

import android.media.MediaRecorder;
import android.text.TextUtils;

import java.io.IOException;

/**
 * Created by YZX on 2017年12月08日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class VoiceRecorder {

    private static final int DEFAULT_MAX_DURATION = 60 * 1000;

    private String mSavePath;
    private int mMaxDuration;
    private OnRecorderStateListener mListener;

    private String initFailReason;
    private MediaRecorder mRecorder;
    private long mStartRecorderTime;

    public VoiceRecorder(String savePath) {
        this(savePath, null);
    }

    public VoiceRecorder(String savePath, OnRecorderStateListener listener) {
        this(savePath, DEFAULT_MAX_DURATION, listener);
    }

    public VoiceRecorder(String savePath, int maxDuration) {
        this(savePath, maxDuration, null);
    }

    public VoiceRecorder(String savePath, int maxDuration, OnRecorderStateListener listener) {
        mSavePath = savePath;
        mMaxDuration = maxDuration;
        mListener = listener;
    }

    private void init() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        try {
            mRecorder.setOutputFile(mSavePath);
            mRecorder.setMaxDuration(mMaxDuration);
        } catch (IllegalStateException e) {
            initFailReason = e.toString();
            return;
        }
        mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what != MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN) {
                    if (mListener != null) {
                        mListener.onComplete(mSavePath, System.currentTimeMillis() - mStartRecorderTime);
                    }
                } else {
                    mListener.onError("VoiceRecorder Error:MEDIA_RECORDER_INFO_UNKNOWN");
                }
                release();
            }
        });
        mRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                if (mListener != null) {
                    if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN) {
                        mListener.onError("VoiceRecorder Error:MEDIA_RECORDER_ERROR_UNKNOWN");
                    } else {
                        mListener.onError("VoiceRecorder Error:MEDIA_ERROR_SERVER_DIED");
                    }
                }
                release();
            }
        });
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            initFailReason = e.toString();
        }

    }

    private void release(){
        if(mRecorder!=null){
            mRecorder.reset();
            mRecorder.release();
        }
        mRecorder=null;
    }

    public void prepare(){
        release();
        init();
    }


    public void start() {
        if (!TextUtils.isEmpty(initFailReason)) {
            if (mListener != null) {
                mListener.onError(initFailReason);
            }
            return;
        }
        try {
            mRecorder.start();
            mStartRecorderTime = System.currentTimeMillis();
        } catch (IllegalStateException e) {
            if (mListener != null) {
                mListener.onError(e.toString());
            }
        }
    }

    public void stop() {
        if(mRecorder==null){
            return;
        }
        try {
            mRecorder.stop();
            if (mListener != null) {
                mListener.onComplete(mSavePath,System.currentTimeMillis() - mStartRecorderTime);
            }
        } catch (IllegalStateException e) {
            if (mListener != null) {
                mListener.onError(e.toString());
            }
        }
        release();
    }

    public void setSavePath(String savePath) {
        mSavePath = savePath;
    }

    public void setMaxDuration(int maxDuration) {
        mMaxDuration = maxDuration;
    }

    public void setOnRecorderStateListener(OnRecorderStateListener listener) {
        mListener = listener;
    }

    public interface OnRecorderStateListener {
        void onComplete(String filePath, long duration);

        void onError(String error);
    }
}
