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

    private static final String TAG = "ConcurrentMediaPlayer";

    private static VoicePlayer sVoicePlayer;

    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;

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

    public MediaPlayer getPlayer() {
        return mMediaPlayer;
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }


    public void play(String uri, OnPlayStateChangeListener listener) {
        if(TextUtils.isEmpty(uri)){
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            stop();
        } else {
            mMediaPlayer.reset();
        }

        mOnPlayStateChangeListener = listener;

        try {
            setSpeaker(true);
            mMediaPlayer.setDataSource(uri);
            mMediaPlayer.prepare();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stop();

                    mOnPlayStateChangeListener = null;
                }
            });
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            if (mOnPlayStateChangeListener != null) {
                mOnPlayStateChangeListener.onError();
            }
        }
    }

    public void stop() {
        mMediaPlayer.stop();
        mMediaPlayer.reset();

        if (mOnPlayStateChangeListener != null) {
            mOnPlayStateChangeListener.onCompletion(mMediaPlayer);
        }
    }

    private VoicePlayer(Context cxt) {
        Context baseContext = cxt.getApplicationContext();
        mAudioManager = (AudioManager) baseContext.getSystemService(Context.AUDIO_SERVICE);
        mMediaPlayer = new MediaPlayer();
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
        void onCompletion(MediaPlayer mediaPlayer);

        void onError();
    }
}
