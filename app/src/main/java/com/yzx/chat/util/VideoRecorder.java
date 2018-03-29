package com.yzx.chat.util;

import android.media.MediaRecorder;
import android.view.Surface;

import java.io.File;
import java.io.IOException;

/**
 * Created by YZX on 2018年03月28日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VideoRecorder {

    private OnRecorderStateListener mOnRecorderStateListener;

    private String mSavePath;

    private MediaRecorder mRecorder;
    private long mStartRecorderTime;


    private boolean tryInit(final String savePath, int videoWidth, int videoHeight, int maxDuration, int orientationHint) {
        mRecorder = new MediaRecorder();
        try {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setVideoEncodingBitRate(videoWidth * videoHeight*3);
            mRecorder.setVideoFrameRate(30);
            mRecorder.setVideoSize(videoWidth, videoHeight);
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setOutputFile(savePath);
            mRecorder.setMaxDuration(maxDuration);
            mRecorder.setOrientationHint(orientationHint);
        } catch (RuntimeException e) {
            e.printStackTrace();
            release();
            return false;
        }
        mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                release();
                if (what != MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN) {
                    if (mOnRecorderStateListener != null) {
                        mOnRecorderStateListener.onComplete(savePath, System.currentTimeMillis() - mStartRecorderTime);
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
                        mOnRecorderStateListener.onError("VideoRecorder Error:MEDIA_RECORDER_ERROR_UNKNOWN");
                    } else {
                        mOnRecorderStateListener.onError("VideoRecorder Error:MEDIA_ERROR_SERVER_DIED");
                    }
                }
            }
        });
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            release();
            return false;
        }
        return true;

    }

    private void release() {
        mSavePath = null;
        if (mRecorder != null) {
            mRecorder.reset();
            mRecorder.release();
        }
        mRecorder = null;
    }


    public Surface prepare(String savePath, int videoWidth, int videoHeight, int maxDuration) {
        return prepare(savePath, videoWidth, videoHeight, maxDuration, 0);
    }

    public Surface prepare(String savePath, int videoWidth, int videoHeight, int maxDuration, int orientationHint) {
        release();
        if (tryInit(savePath, videoWidth, videoHeight, maxDuration, orientationHint)) {
            mSavePath = savePath;
            return mRecorder.getSurface();
        }
        return null;
    }


    public void start() {
        if (mRecorder == null) {
            throw new IllegalStateException("The VideoRecorder is not prepare");
        }
        try {
            mRecorder.start();
            mStartRecorderTime = System.currentTimeMillis();
        } catch (RuntimeException e) {
            if (mOnRecorderStateListener != null) {
                mOnRecorderStateListener.onError(e.toString());
            }
        }
    }

    public String stop() {
        if (mRecorder == null) {
            return null;
        }
        try {
            mRecorder.stop();
            if (mOnRecorderStateListener != null) {
                mOnRecorderStateListener.onComplete(mSavePath, System.currentTimeMillis() - mStartRecorderTime);
            }
            return mSavePath;
        } catch (RuntimeException e) {
            if (mOnRecorderStateListener != null) {
                mOnRecorderStateListener.onError(e.toString());
            }
            return null;
        } finally {
            release();
        }

    }


    public void setOnRecorderStateListener(OnRecorderStateListener listener) {
        mOnRecorderStateListener = listener;
    }

    public interface OnRecorderStateListener {
        void onComplete(String filePath, long duration);

        void onError(String error);
    }

}
