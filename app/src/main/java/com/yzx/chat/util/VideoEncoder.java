package com.yzx.chat.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

/**
 * Created by YZX on 2018年05月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VideoEncoder {

    private static final String TAG = VideoEncoder.class.getSimpleName();

    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_BIT_RATE = 64000;
    private static final int AUDIO_CHANNEL_COUNT = 1;
    private static final int AUDIO_BIT_PER_SAMPLE = 16;

    private static final int VIDEO_FRAME_RATE = 30;

    public static VideoEncoder createVideoEncoder(final int videoWidth, final int videoHeight, final int videoRotation) {
        try {
            return new VideoEncoder(videoWidth, videoHeight, videoRotation);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static MediaFormat createVideoEncoderMediaFormat(int videoWidth, int videoHeight) {
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, videoWidth, videoHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, videoWidth * videoHeight * 2);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000 / VIDEO_FRAME_RATE);
        format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileHigh);
        format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);

        return format;
    }

    private static MediaFormat createAudioEncoderMediaFormat() {
        MediaFormat format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL_COUNT);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        format.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE);
        return format;
    }

    private int mVideoWidth;
    private int mVideoHeight;
    private int mVideoRotation;

    private MediaCodec mVideoCodec;
    private MediaCodec mAudioCodec;
    private Handler mEncodeHandler;
    private AudioRecord mAudioRecord;
    private MediaMuxer mMediaMuxer;
    private Surface mInputSurface;


    private byte[] mAudioBuffer;

    private int mVideoTrackIndex;
    private int mAudioTrackIndex;
    private boolean isStartingMuxer;
    private boolean isStartingEncoded;

    private VideoEncoder(int videoWidth, int videoHeight, int videoRotation) throws IOException {
        mVideoWidth = videoWidth;
        mVideoHeight = videoHeight;
        mVideoRotation = videoRotation;
        HandlerThread codecThread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        codecThread.start();
        mEncodeHandler = new Handler(codecThread.getLooper());
        final CountDownLatch latch = new CountDownLatch(1);
        mEncodeHandler.post(new Runnable() {
            @Override
            public void run() {
                do {
                    try {
                        mVideoCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
                    } catch (IOException | IllegalArgumentException e) {
                        e.printStackTrace();
                        break;
                    }
                    try {
                        mAudioCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
                    } catch (IOException | IllegalArgumentException e) {
                        e.printStackTrace();
                        break;
                    }
                    try {
                        int buffSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffSize);
                        mAudioBuffer = new byte[buffSize];
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        break;
                    }
                    configureCodec();
                } while (false);
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mVideoCodec == null || mAudioCodec == null || mAudioRecord == null) {
            release();
            throw new IOException("init VoiceEncoder fail");
        }
    }

    private void configureCodec() {
        mVideoCodec.reset();
        mAudioCodec.reset();
        try {
            mVideoCodec.configure(createVideoEncoderMediaFormat(mVideoWidth, mVideoHeight), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mAudioCodec.configure(createAudioEncoderMediaFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mVideoCodec.setCallback(new VideoEncodeCallback());
            mAudioCodec.setCallback(new AudioEncodeCallback());
        } catch (RuntimeException e) {
            e.printStackTrace();
            release();
        }
    }

    public boolean start(String filePath) {
        synchronized (this) {
            if (isStartingEncoded) {
                throw new RuntimeException("The VoiceCodec is already Starting");
            }
            if (mVideoCodec == null || mAudioCodec == null) {
                throw new RuntimeException("The VoiceCodec is already release");
            }
            try {
                mMediaMuxer = new MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                mMediaMuxer.setOrientationHint(mVideoRotation);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            mVideoTrackIndex = -1;
            mAudioTrackIndex = -1;
            isStartingMuxer = false;
            mAudioRecord.startRecording();
            mVideoCodec.start();
            mAudioCodec.start();
            isStartingEncoded = true;
        }
        return true;
    }

    public void stop() {
        synchronized (this) {
            try {
                if (!isStartingEncoded) {
                    return;
                }
                final CountDownLatch latch = new CountDownLatch(1);
                mEncodeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAudioRecord.stop();
                        if (mVideoTrackIndex != -1) {
                            mVideoCodec.signalEndOfInputStream();
                        }
                        mVideoCodec.stop();
                        mAudioCodec.stop();
                        if (isStartingMuxer) {
                            mMediaMuxer.stop();
                        }
                        configureCodec();
                        mMediaMuxer.release();
                        mMediaMuxer = null;
                        mInputSurface = null;
                        isStartingEncoded = false;
                        latch.countDown();
                    }
                });
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
            }
        }

    }

    public void release() {
        synchronized (this) {
            if (mVideoCodec != null) {
                mVideoCodec.release();
                mVideoCodec = null;
            }
            if (mAudioCodec != null) {
                mAudioCodec.release();
                mAudioCodec = null;
            }
            if (mEncodeHandler != null) {
                mEncodeHandler.getLooper().quitSafely();
                mEncodeHandler = null;
            }
            if (mAudioRecord != null) {
                mAudioRecord.release();
            }
        }

    }

    public boolean isRunning() {
        synchronized (this) {
            return isStartingEncoded;
        }
    }


    public Surface getInputSurface() {
        synchronized (this) {
            if (mInputSurface == null && !isStartingEncoded) {
                mInputSurface = mVideoCodec.createInputSurface();
            }
        }
        return mInputSurface;
    }

    private class VideoEncodeCallback extends MediaCodec.Callback {
        private long mStartPresentationTime;

        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            if (mVideoTrackIndex != -1 && mAudioTrackIndex != -1 && !isStartingMuxer) {
                mMediaMuxer.start();
                isStartingMuxer = true;
            }
            ByteBuffer outPutByteBuffer = codec.getOutputBuffer(index);
            if (outPutByteBuffer != null && info.size > 0 && isStartingMuxer) {
                if (mStartPresentationTime <= 0) {
                    mStartPresentationTime = info.presentationTimeUs;
                }
                info.presentationTimeUs -= mStartPresentationTime;
                mMediaMuxer.writeSampleData(mVideoTrackIndex, outPutByteBuffer, info);
            }
            codec.releaseOutputBuffer(index, false);
        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
            if (mVideoTrackIndex == -1) {
                mVideoTrackIndex = mMediaMuxer.addTrack(format);
            }
        }
    }

    private class AudioEncodeCallback extends MediaCodec.Callback {
        private long mStartPresentationTime;
        private final int mSingleDataSize;

        AudioEncodeCallback() {
            mSingleDataSize = Math.min(6 * (AUDIO_SAMPLE_RATE / 1000) * (AUDIO_BIT_PER_SAMPLE / 8 * AUDIO_CHANNEL_COUNT), mAudioBuffer.length);
        }

        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            int readLength = 0;
            int i = 0;
            while (readLength == 0) {
                i++;
                long s = System.currentTimeMillis();
                readLength = mAudioRecord.read(mAudioBuffer, 0, mSingleDataSize);
                if (readLength > 0) {
                    ByteBuffer inputBuffer = mAudioCodec.getInputBuffer(index);
                    if (inputBuffer != null) {
                        inputBuffer.clear();
                        inputBuffer.limit(readLength);
                        inputBuffer.put(mAudioBuffer, 0, readLength);
                        mAudioCodec.queueInputBuffer(index, 0, readLength, System.nanoTime() / 1000, 0);
                    }
                }
            }
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            ByteBuffer outPutByteBuffer = codec.getOutputBuffer(index);
            if (outPutByteBuffer != null && info.size > 0 && isStartingMuxer) {
                if (mStartPresentationTime <= 0) {
                    mStartPresentationTime = info.presentationTimeUs;
                }
                info.presentationTimeUs -= mStartPresentationTime;
                mMediaMuxer.writeSampleData(mAudioTrackIndex, outPutByteBuffer, info);
            }
            codec.releaseOutputBuffer(index, false);
        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
            if (mAudioTrackIndex == -1) {
                mAudioTrackIndex = mMediaMuxer.addTrack(format);
            }
        }
    }
}
