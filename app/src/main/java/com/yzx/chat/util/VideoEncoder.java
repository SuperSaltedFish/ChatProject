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
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import androidx.annotation.NonNull;

/**
 * Created by YZX on 2018年05月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VideoEncoder {

    private static final String TAG = VideoEncoder.class.getSimpleName();

    private static final String MIME_TYPE_VIDEO = MediaFormat.MIMETYPE_VIDEO_AVC;
    private static final String MIME_TYPE_AUDIO = MediaFormat.MIMETYPE_AUDIO_AAC;

    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_BIT_RATE = 64000;
    private static final int AUDIO_CHANNEL_COUNT = 1;
    private static final int AUDIO_BIT_PER_SAMPLE = 16;
    private static final int AUDIO_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;


    private MediaCodec mVideoCodec;
    private MediaCodec mAudioCodec;
    private Handler mEncodeHandler;
    private AudioRecord mAudioRecord;
    private MediaMuxer mMediaMuxer;

    private byte[] mAudioBuffer;

    private int mVideoTrackIndex;
    private int mAudioTrackIndex;
    private boolean isStartingMuxer;
    private boolean isStartingEncoded;

    public VideoEncoder() throws IOException, IllegalArgumentException {
        try {
            mVideoCodec = MediaCodec.createEncoderByType(MIME_TYPE_VIDEO);
            mAudioCodec = MediaCodec.createEncoderByType(MIME_TYPE_AUDIO);
            int buffSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNEL_CONFIG, AUDIO_FORMAT);
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL_CONFIG, AUDIO_FORMAT, buffSize);
            mAudioBuffer = new byte[buffSize];
        } catch (IOException | IllegalArgumentException e) {
            release();
            throw e;
        }
    }

    public MediaFormat createDefaultVideoMediaFormat(int videoWidth, int videoHeight, int frameRate) {
        MediaCodecInfo.CodecCapabilities capabilities = getVideoMediaCodecInfo().getCapabilitiesForType(MIME_TYPE_VIDEO);
        MediaCodecInfo.VideoCapabilities videoCapabilities = capabilities.getVideoCapabilities();
        MediaFormat format = capabilities.getDefaultFormat();
        if (!videoCapabilities.isSizeSupported(videoWidth, videoHeight)) {
            return null;
        } else {
            format.setInteger(MediaFormat.KEY_WIDTH, videoWidth);
            format.setInteger(MediaFormat.KEY_HEIGHT, videoHeight);
        }
        if (videoCapabilities.getSupportedFrameRatesFor(videoWidth, videoHeight).contains((double) frameRate)) {
            format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        }

        if (capabilities.getEncoderCapabilities().isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)) {
            format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
        }
        for (MediaCodecInfo.CodecProfileLevel profileLevel : capabilities.profileLevels) {
            switch (profileLevel.profile) {
                case MediaCodecInfo.CodecProfileLevel.AVCProfileHigh:
                case MediaCodecInfo.CodecProfileLevel.AVCProfileHigh10:
                case MediaCodecInfo.CodecProfileLevel.AVCProfileHigh422:
                case MediaCodecInfo.CodecProfileLevel.AVCProfileHigh444:
                    format.setInteger(MediaFormat.KEY_PROFILE, profileLevel.profile);
                    format.setInteger(MediaFormat.KEY_LEVEL, profileLevel.level);
                    break;
            }
        }
        format.setInteger(MediaFormat.KEY_BIT_RATE, videoWidth * videoHeight * 5);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, (int) Math.ceil(1000000f / frameRate));
        return format;
    }

    public MediaFormat createDefaultAudioMediaFormat() {
        MediaCodecInfo.CodecCapabilities capabilities = getAudioMediaCodecInfo().getCapabilitiesForType(MIME_TYPE_AUDIO);
        MediaCodecInfo.AudioCapabilities audioCapabilities = capabilities.getAudioCapabilities();
        MediaFormat format = capabilities.getDefaultFormat();
        for (MediaCodecInfo.CodecProfileLevel profileLevel : capabilities.profileLevels) {
            if (profileLevel.profile == MediaCodecInfo.CodecProfileLevel.AACObjectLC) {
                format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
                break;
            }
        }
        int[] supportedSampleRates = audioCapabilities.getSupportedSampleRates();
        if (supportedSampleRates == null || supportedSampleRates.length == 0) {
            return null;
        }
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, supportedSampleRates[0]);
        for (int rate : supportedSampleRates) {
            if (rate == AUDIO_SAMPLE_RATE) {
                format.setInteger(MediaFormat.KEY_SAMPLE_RATE, AUDIO_SAMPLE_RATE);
            }
        }
        if (audioCapabilities.getBitrateRange().contains(AUDIO_BIT_RATE)) {
            format.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE);
        } else {
            format.setInteger(MediaFormat.KEY_BIT_RATE, audioCapabilities.getBitrateRange().getLower());
        }
        format.setString(MediaFormat.KEY_MIME, MIME_TYPE_AUDIO);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, Math.min(AUDIO_CHANNEL_COUNT, audioCapabilities.getMaxInputChannelCount()));
        format.setInteger(MediaFormat.KEY_CHANNEL_MASK, AUDIO_CHANNEL_CONFIG);
        return format;
    }


    public Surface configureCodec(MediaFormat videoFormat, MediaFormat audioFormat) throws RuntimeException {
        synchronized (this) {
            checkIsRelease();
            reset();
            try {
                HandlerThread codecThread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                codecThread.start();
                mEncodeHandler = new Handler(codecThread.getLooper());
                mAudioCodec.setCallback(new AudioEncodeCallback(), mEncodeHandler);
                mVideoCodec.setCallback(new VideoSurfaceInputEncodeCallback(), mEncodeHandler);
                mVideoCodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                mAudioCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                return mVideoCodec.createInputSurface();
            } catch (RuntimeException e) {
                reset();
                throw e;
            }
        }
    }


    public void reset() {
        synchronized (this) {
            checkIsRelease();
            mVideoCodec.reset();
            mAudioCodec.reset();
            mVideoCodec.setCallback(null);
            mAudioCodec.setCallback(null);
            if (mEncodeHandler != null) {
                mEncodeHandler.removeCallbacksAndMessages(null);
                mEncodeHandler.getLooper().quit();
                mEncodeHandler = null;
            }
        }
    }

    public boolean start(String filePath, int videoRotationDegrees) {
        synchronized (this) {
            checkIsRelease();
            if (isStartingEncoded) {
                throw new RuntimeException("The VideoEncoder is already Starting");
            }
            File file = new File(filePath);
            try {
                if (!file.exists() && !file.createNewFile()) {
                    Log.e(TAG, "Create file fail: " + filePath);
                    return false;
                }
                mMediaMuxer = new MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                mMediaMuxer.setOrientationHint(videoRotationDegrees);
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
            checkIsRelease();
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
                        mMediaMuxer.release();
                        mMediaMuxer = null;
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

    public boolean isRunning() {
        synchronized (this) {
            return isStartingEncoded;
        }
    }

    public void release() {
        synchronized (this) {
            reset();
            if (mVideoCodec != null) {
                mVideoCodec.release();
                mVideoCodec = null;
            }
            if (mAudioCodec != null) {
                mAudioCodec.release();
                mAudioCodec = null;
            }
            if (mAudioRecord != null) {
                mAudioRecord.release();
            }
        }

    }

    public MediaCodecInfo getVideoMediaCodecInfo() {
        checkIsRelease();
        return mVideoCodec.getCodecInfo();
    }

    public MediaCodecInfo getAudioMediaCodecInfo() {
        checkIsRelease();
        return mAudioCodec.getCodecInfo();
    }

    private void checkIsRelease() {
        if (mVideoCodec == null || mAudioCodec == null) {
            throw new RuntimeException("The VideoEncoder is already release");
        }
    }


    private class VideoSurfaceInputEncodeCallback extends MediaCodec.Callback {
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
            while (readLength == 0) {
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
