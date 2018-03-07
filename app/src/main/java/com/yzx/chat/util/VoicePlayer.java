package com.yzx.chat.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;

import java.io.IOException;

/**
 * Created by YZX on 2017年12月19日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class VoicePlayer {

    private static VoicePlayer sVoicePlayer;

    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;
    private String mCurrentPlayPath;

    private OnPlayStateChangeListener mOnPlayStateChangeListener;

    public static VoicePlayer getInstance(Context context) {
        if (sVoicePlayer == null) {
            synchronized (VoicePlayer.class) {
                if (sVoicePlayer == null) {
                    sVoicePlayer = new VoicePlayer(context);
                }
            }
        }
        return sVoicePlayer;
    }

    private VoicePlayer(Context cxt) {
        Context baseContext = cxt.getApplicationContext();
        mAudioManager = (AudioManager) baseContext.getSystemService(Context.AUDIO_SERVICE);
        initMediaPlayer();
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mOnPlayStateChangeListener != null) {
                    mOnPlayStateChangeListener.onCompletion(mp, false);
                }
                reset();
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (mOnPlayStateChangeListener != null) {
                    mOnPlayStateChangeListener.onError("MediaPlayerError, what=" + what + ",extra=" + extra);
                }
                reset();
                return true;
            }
        });
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayer.start();
                if (mOnPlayStateChangeListener != null) {
                    mOnPlayStateChangeListener.onStartPlay();
                }
            }
        });
    }

    private void reset() {
        mMediaPlayer.reset();
        mOnPlayStateChangeListener = null;
        mCurrentPlayPath = null;
    }

    public void play(String path, OnPlayStateChangeListener listener) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        stop();
        mOnPlayStateChangeListener = listener;
        mCurrentPlayPath = path;
        setSpeaker(true);
        try {
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            if (mOnPlayStateChangeListener != null) {
                mOnPlayStateChangeListener.onError(e.toString());
                reset();
            }
        }
    }

    public void stop() {
        boolean isNeedStop = mMediaPlayer.isPlaying();
        if (isNeedStop) {
            mMediaPlayer.stop();
        }
        if (mOnPlayStateChangeListener != null) {
            mOnPlayStateChangeListener.onCompletion(mMediaPlayer, isNeedStop);
        }
        reset();
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public String getCurrentPlayPath() {
        return mCurrentPlayPath;
    }

    private void setSpeaker(boolean speakerOn) {
        if (speakerOn) {
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            mAudioManager.setSpeakerphoneOn(true);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        } else {
            mAudioManager.setSpeakerphoneOn(false);// 关闭扬声器
            // 把声音设定成Earpiece（听筒）出来，设定为正在通话中
            mAudioManager.setMode(AudioManager.MODE_IN_CALL);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        }
    }

    public interface OnPlayStateChangeListener {
        void onStartPlay();

        void onCompletion(MediaPlayer mediaPlayer, boolean isStop);

        void onError(String error);
    }
}
