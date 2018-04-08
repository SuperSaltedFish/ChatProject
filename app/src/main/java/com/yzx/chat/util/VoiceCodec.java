package com.yzx.chat.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.media.MediaSyncEvent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

/**
 * Created by YZX on 2018年04月01日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VoiceCodec {

    private static final String TAG = VoiceCodec.class.getSimpleName();

    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_BIT_RATE = 64000;


    public static VoiceCodec createEncoder(final int videoWidth, final int videoHeight) {
        HandlerThread codecThread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        codecThread.start();
        Handler codecHandler = new Handler(codecThread.getLooper());
        final CountDownLatch latch = new CountDownLatch(1);
        final MediaCodec[] videoCodec = {null};
        final MediaCodec[] audioCodec = {null};
        codecHandler.post(new Runnable() {
            @Override
            public void run() {
                videoCodec[0] = createVideoEncoder(videoWidth, videoHeight);
                if (videoCodec[0] == null) {
                    latch.countDown();
                    return;
                }
                audioCodec[0] = createAudioEncoder();
                if (audioCodec[0] == null) {
                    videoCodec[0].release();
                    videoCodec[0] = null;
                }
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (videoCodec[0] != null && audioCodec[0] != null) {
            return new VoiceCodec(videoCodec[0], audioCodec[0], codecHandler);
        } else {
            codecHandler.getLooper().quit();
            return null;
        }
    }

    private static MediaCodec createVideoEncoder(int videoWidth, int videoHeight) {
        MediaCodec videoCodec;
        try {
            videoCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, videoWidth, videoHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, videoWidth * videoHeight * 2);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000 / 30);
        videoCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        return videoCodec;
    }

    private static MediaCodec createAudioEncoder() {
        MediaCodec audioCodec;
        try {
            audioCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
        MediaFormat format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, AUDIO_SAMPLE_RATE, 1);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        format.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE);
        audioCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        return audioCodec;
    }

    private MediaCodec mVideoCodec;
    private Surface mVideoSurface;
    private MediaMuxer mMediaMuxer;

    private MediaCodec mAudioCodec;
    private AudioRecord mAudioRecord;
    private byte[] mAudioBuffer;

    private Handler mCodecHandler;

    private boolean isStarting;
    private int mVideoTrackIndex;
    private int mAudioTrackIndex;

    private VoiceCodec(MediaCodec videoCodec, MediaCodec audioCodec, Handler codecHandler) {
        mVideoCodec = videoCodec;
        mAudioCodec = audioCodec;
        mCodecHandler = codecHandler;
        mVideoCodec.setCallback(mVideoCodecCallback);
        mAudioCodec.setCallback(mAudioCodecCallback);

        int buffSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffSize);
        //mAudioRecord.setRecordPositionUpdateListener(new AudioRecordUpdateListener(), mCodecHandler);
        //  mAudioRecord.setPositionNotificationPeriod(buffSize / (2 * 16 * 1 / 8));
        mAudioBuffer = new byte[buffSize];

    }


    @Nullable
    public Surface start(String savePath) {
        if (isStarting) {
            throw new RuntimeException("The VoiceCodec is already Starting");
        }
        File file = new File(savePath);
        if (file.exists() && !file.delete()) {
            return null;
        }
        try {
            mMediaMuxer = new MediaMuxer(savePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mVideoTrackIndex = mMediaMuxer.addTrack(mVideoCodec.getOutputFormat());
            mAudioTrackIndex = mMediaMuxer.addTrack(mAudioCodec.getOutputFormat());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        mVideoSurface = mVideoCodec.createInputSurface();
        mMediaMuxer.start();
        mVideoCodec.start();
        mAudioCodec.start();
        mAudioRecord.startRecording();
        isStarting = true;
        return mVideoSurface;
    }

    public void stop() {
        if (!isStarting) {
            return;
        }
        mVideoCodec.signalEndOfInputStream();
        destroy();
    }

    private void destroyVideoCodec() {
        if (mVideoCodec != null) {
            mVideoCodec.stop();
            mVideoCodec.release();
            mVideoCodec = null;
        }
        if (mVideoSurface != null) {
            mVideoSurface.release();
            mVideoSurface = null;
        }
    }

    private void destroyAudioCodec() {
        if (mAudioCodec != null) {
            mAudioCodec.stop();
            mAudioCodec.release();
            mAudioCodec = null;
        }
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
        if (mAudioBuffer != null) {
            mAudioBuffer = null;
        }
    }

    public void destroy() {
        mCodecHandler.post(new Runnable() {
            @Override
            public void run() {
                destroyVideoCodec();
                destroyAudioCodec();
                if (mCodecHandler != null) {
                    mCodecHandler.removeCallbacksAndMessages(null);
                    mCodecHandler.getLooper().quit();
                    mCodecHandler = null;
                }
                if (mMediaMuxer != null) {
                    mMediaMuxer.stop();
                    mMediaMuxer.release();
                }
                isStarting = false;
            }
        });

    }

//    private class CollectAudioThread extends Thread {
//
//        private boolean mIsQuit;
//
//        @Override
//        public void run() {
//            super.run();
//            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
//            ByteBuffer inputBuffer;
//            long nowTime = SystemClock.elapsedRealtimeNanos() / 1000;
//            long presentationTimeUs;
//            mAudioRecord.startRecording();
//            mAudioRecord.setRecordPositionUpdateListener();
//            mAudioRecord.setPositionNotificationPeriod()
//            mAudioRecord.setRecordPositionUpdateListener(null, null);
//            while (!mIsQuit) {
//                int readLength = mAudioRecord.read(mAudioBuffer, 0, mAudioBuffer.length);
//                if (readLength > 0) {
//                    int index = mAudioCodec.dequeueInputBuffer(-1);
//                    if (index >= 0) {
//                        inputBuffer = mAudioCodec.getInputBuffer(index);
//                        if (inputBuffer != null) {
//                            inputBuffer.clear();
//                            inputBuffer.limit(mAudioBuffer.length);
//                            inputBuffer.put(mAudioBuffer);
//                            presentationTimeUs = SystemClock.elapsedRealtimeNanos() / 1000 - nowTime;
//                            mAudioCodec.queueInputBuffer(index, 0, mAudioBuffer.length, presentationTimeUs, 0);
//                        }
//                    }
//                }
//            }
//        }
//
//        public void quit() {
//            mIsQuit = true;
//            this.interrupt();
//        }
//    }

    private final SimpleCodecCallback mVideoCodecCallback = new SimpleCodecCallback() {
        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            ByteBuffer outPutByteBuffer = codec.getOutputBuffer(index);
            if (outPutByteBuffer != null && info.size > 0) {
                mMediaMuxer.writeSampleData(mVideoTrackIndex, outPutByteBuffer, info);
            }
            codec.releaseOutputBuffer(index, false);
        }

    };

    private final SimpleCodecCallback mAudioCodecCallback = new SimpleCodecCallback() {

        private long mNowTime;
        private long mPresentationTimeUs;

        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            if (mNowTime == 0) {
                mNowTime = SystemClock.elapsedRealtimeNanos() / 1000;
            }
            int readLength = 0;
            while (readLength == 0) {
                readLength = mAudioRecord.read(mAudioBuffer, 0, mAudioBuffer.length);
                if (readLength > 0) {
                    ByteBuffer inputBuffer = mAudioCodec.getInputBuffer(index);
                    if (inputBuffer != null) {
                        inputBuffer.clear();
                        inputBuffer.limit(readLength);
                        inputBuffer.put(mAudioBuffer, 0, readLength);
                        mPresentationTimeUs = SystemClock.elapsedRealtimeNanos() / 1000 - mNowTime;
                        mAudioCodec.queueInputBuffer(index, 0, readLength, mPresentationTimeUs, 0);
                    }
                    LogUtil.e("aaaaaaaaaaaaaaa");
                }
            }
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            ByteBuffer outPutByteBuffer = codec.getOutputBuffer(index);
            if (outPutByteBuffer != null && info.size > 0) {
                mMediaMuxer.writeSampleData(mAudioTrackIndex, outPutByteBuffer, info);
                LogUtil.e("mAudioCodecCallback");
            }
            codec.releaseOutputBuffer(index, false);
        }
    };

    private class AudioRecordUpdateListener implements AudioRecord.OnRecordPositionUpdateListener {
        private long mNowTime = SystemClock.elapsedRealtimeNanos() / 1000;
        private long mPresentationTimeUs;

        @Override
        public void onMarkerReached(AudioRecord recorder) {

        }

        @Override
        public void onPeriodicNotification(AudioRecord recorder) {
            if (mNowTime == 0) {
                mNowTime = SystemClock.elapsedRealtimeNanos() / 1000;
            }
            int readLength = mAudioRecord.read(mAudioBuffer, 0, mAudioBuffer.length);
            if (readLength > 0) {
                int index = mAudioCodec.dequeueInputBuffer(-1);
                if (index >= 0) {
                    ByteBuffer inputBuffer = mAudioCodec.getInputBuffer(index);
                    if (inputBuffer != null) {
                        inputBuffer.clear();
                        inputBuffer.limit(mAudioBuffer.length);
                        inputBuffer.put(mAudioBuffer);
                        mPresentationTimeUs = SystemClock.elapsedRealtimeNanos() / 1000 - mNowTime;
                        mAudioCodec.queueInputBuffer(index, 0, mAudioBuffer.length, mPresentationTimeUs, 0);
                    }
                }
            }
        }
    }

    private static class SimpleCodecCallback extends MediaCodec.Callback {

        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {

        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

        }
    }

}
