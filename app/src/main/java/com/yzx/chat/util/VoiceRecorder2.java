package com.yzx.chat.util;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.Visualizer;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by YZX on 2018年07月25日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VoiceRecorder2 {

    private static final String TAG = VoiceRecorder2.class.getName();
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_RECORD_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_PLAY_CHANNEL = AudioFormat.CHANNEL_IN_STEREO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int AUDIO_RECORD_BUFF_SIZE = Math.max(AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_RECORD_CHANNEL, AUDIO_FORMAT), AudioTrack.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_PLAY_CHANNEL, AUDIO_FORMAT));
    private static final int AUDIO_PLAY_BUFF_SIZE = AUDIO_RECORD_BUFF_SIZE;

    private AudioRecord mAudioRecord;
    private AudioTrack mAudioTrack;
    private Visualizer mVisualizer;
    private byte[] mRecordBuffer;

    private FileOutputStream mFileOutputStream;
    private volatile Thread mReaderThread;
    private volatile boolean isRecording;
    private boolean isEnableVisualizer;

    public VoiceRecorder2() {
        mAudioRecord = new AudioRecord(
                AUDIO_SOURCE,
                AUDIO_SAMPLE_RATE,
                AUDIO_RECORD_CHANNEL,
                AUDIO_FORMAT,
                AUDIO_RECORD_BUFF_SIZE);
        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_RING,
                AUDIO_SAMPLE_RATE,
                AUDIO_PLAY_CHANNEL,
                AUDIO_FORMAT,
                AUDIO_PLAY_BUFF_SIZE,
                AudioTrack.MODE_STREAM);

  
        mVisualizer = new Visualizer(mAudioTrack.getAudioSessionId());
    }

    public boolean start(String savePath) {
        return start(savePath, null, false, false);
    }

    public boolean start(String savePath, @Nullable Visualizer.OnDataCaptureListener listener, boolean isEnableWaveform, boolean isEnableFFT) {
        synchronized (this) {
            if (isRecording) {
                throw new RuntimeException("VoiceRecorder already start.");
            }
            File file = new File(savePath);
            try {
                if (!file.exists() && !file.createNewFile()) {
                    LogUtil.e("create file fail:" + savePath);
                    return false;
                }
                mFileOutputStream = new FileOutputStream(file);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            try {
                mAudioRecord.startRecording();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                try {
                    mFileOutputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return false;
            }

            if (listener != null) {
                try {
                    mAudioTrack.play();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    try {
                        mFileOutputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    return false;
                }
                isEnableVisualizer = true;
                mVisualizer.setDataCaptureListener(listener, Visualizer.getMaxCaptureRate() / 2, isEnableWaveform, isEnableFFT);
                mVisualizer.setEnabled(true);
            }
            isRecording = true;
            mReaderThread = new Thread(new ReadVoiceDataRunnable(), TAG);
            mReaderThread.start();
        }
        return true;
    }

    public void stop() {
        synchronized (this) {
            stopRecorder();
        }
    }

    private void stopRecorder() {
        if (!isRecording) {
            return;
        }
        isRecording = false;
        isEnableVisualizer = false;
        mAudioRecord.stop();
        mAudioTrack.stop();
        if (mReaderThread != null) {
            mReaderThread.interrupt();
            while (mReaderThread != null) {

            }
        }
        try {
            mFileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        synchronized (this) {
            if (isRecording) {
                stopRecorder();
            }
            mAudioRecord.release();
        }
    }

    private final class ReadVoiceDataRunnable implements Runnable {

        @Override
        public void run() {
            if (mRecordBuffer == null) {
                mRecordBuffer = new byte[AUDIO_RECORD_BUFF_SIZE];
            }
            try {
                while (isRecording && !mReaderThread.isInterrupted()) {
                    int readLen = mAudioRecord.read(mRecordBuffer, 0, mRecordBuffer.length);
                    if (readLen > 0) {
                        mFileOutputStream.write(mRecordBuffer, 0, readLen);
                        if (isEnableVisualizer) {
                            mAudioTrack.write(mRecordBuffer, 0, readLen);
                        }
                    } else if (readLen < 0) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mReaderThread = null;
                if (isRecording) {
                    stop();
                }
            }

        }
    }

}
