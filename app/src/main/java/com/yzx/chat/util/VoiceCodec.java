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
import android.support.annotation.Nullable;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

/**
 * Created by YZX on 2018年04月01日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public abstract class VoiceCodec {

    private static final String TAG = VoiceCodec.class.getSimpleName();
    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_BIT_RATE = 64000;


    public static VoiceCodec createEncoder(final int videoWidth, final int videoHeight) {
        try {
            return new VoiceEncoder(videoWidth, videoHeight);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static MediaFormat createVideoEncoderMediaFormat(int videoWidth, int videoHeight) {
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, videoWidth, videoHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, videoWidth * videoHeight * 2);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileHigh);
        format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000 / 30);
        return format;
    }

    private static MediaFormat createAudioEncoderMediaFormat() {
        MediaFormat format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, AUDIO_SAMPLE_RATE, 1);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        format.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE);
        return format;
    }


    public abstract boolean start(String filePath);

    public abstract void stop();

    public abstract void release();

    public abstract void setOutputSurface(Surface surface);

    public abstract Surface getInputSurface();


    private static class VoiceEncoder extends VoiceCodec {

        private MediaCodec mVideoCodec;
        private MediaCodec mAudioCodec;
        private Handler mEncodeHandler;
        private AudioRecord mAudioRecord;
        private MediaMuxer mMediaMuxer;
        private Surface mInputSurface;

        private byte[] mAudioBuffer;

        private int mVideoWidth;
        private int mVideoHeight;
        private boolean isStarting;

        VoiceEncoder(int videoWidth, int videoHeight) throws IOException {
            mVideoWidth = videoWidth;
            mVideoHeight = videoHeight;
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
                throw new IOException("init VoiceEncoder fail");
            }
            reset();
        }

        @Override
        public boolean start(String filePath) {
            if (isStarting) {
                throw new RuntimeException("The VoiceCodec is already Starting");
            }
            if (mVideoCodec == null || mAudioCodec == null) {
                throw new RuntimeException("The VoiceCodec is already release");
            }
            File file = new File(filePath);
            if (file.exists() && !file.delete()) {
                return false;
            }
            try {
                mMediaMuxer = new MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            mVideoCodec.start();
            mAudioCodec.start();
            mAudioRecord.startRecording();
            isStarting = true;
            return true;
        }

        @Override
        public void stop() {
            if (isStarting) {
                mVideoCodec.signalEndOfInputStream();
                mVideoCodec.stop();
                mAudioCodec.stop();
                mMediaMuxer.stop();
                reset();
                mMediaMuxer.release();
                mMediaMuxer = null;
                mInputSurface = null;
                isStarting = false;
            }
        }

        @Override
        public void release() {
            stop();
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
        }

        @Override
        public void setOutputSurface(Surface surface) {

        }

        @Override
        public Surface getInputSurface() {
            if (mInputSurface == null && !isStarting) {
                mInputSurface = mVideoCodec.createInputSurface();
            }
            return mInputSurface;
        }

        public void reset() {
            mVideoCodec.reset();
            mAudioCodec.reset();
            mVideoCodec.configure(VoiceCodec.createVideoEncoderMediaFormat(mVideoWidth, mVideoHeight), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mAudioCodec.configure(VoiceCodec.createAudioEncoderMediaFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        }

    }

//    private final SimpleCodecCallback mVideoCodecCallback = new SimpleCodecCallback() {
//        private long mStartTime;
//
//        @Override
//        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
//            if (mVideoTrackIndex != -1 && mAudioTrackIndex != -1 && !isStartingMuxer) {
//                mMediaMuxer.start();
//                isStartingMuxer = true;
//            }
//
//            ByteBuffer outPutByteBuffer = codec.getOutputBuffer(index);
//            if (outPutByteBuffer != null && info.size > 0 && isStartingMuxer) {
//                if (mStartTime <= 0) {
//                    mStartTime = info.presentationTimeUs;
//                }
//                info.presentationTimeUs -= mStartTime;
//                mMediaMuxer.writeSampleData(mVideoTrackIndex, outPutByteBuffer, info);
//            }
//            codec.releaseOutputBuffer(index, false);
//        }
//
//        @Override
//        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
//            if (mVideoTrackIndex == -1) {
//                mVideoTrackIndex = mMediaMuxer.addTrack(format);
//            }
//        }
//    };
//
//    private final SimpleCodecCallback mAudioCodecCallback = new SimpleCodecCallback() {
//        private long mStartTime;
//
//        @Override
//        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
//            int readLength = 0;
//            while (readLength == 0) {
//                readLength = mAudioRecord.read(mAudioBuffer, 0, mAudioBuffer.length);
//                if (readLength > 0) {
//                    ByteBuffer inputBuffer = mAudioCodec.getInputBuffer(index);
//                    if (inputBuffer != null) {
//                        inputBuffer.clear();
//                        inputBuffer.limit(readLength);
//                        inputBuffer.put(mAudioBuffer, 0, readLength);
//                        mAudioCodec.queueInputBuffer(index, 0, readLength, System.nanoTime() / 1000, 0);
//                    }
//                }
//            }
//        }
//
//        @Override
//        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
//            ByteBuffer outPutByteBuffer = codec.getOutputBuffer(index);
//            if (outPutByteBuffer != null && info.size > 0 && isStartingMuxer) {
//                if (mStartTime <= 0) {
//                    mStartTime = info.presentationTimeUs;
//                }
//                info.presentationTimeUs -= mStartTime;
//                mMediaMuxer.writeSampleData(mAudioTrackIndex, outPutByteBuffer, info);
//            }
//            codec.releaseOutputBuffer(index, false);
//        }
//
//        @Override
//        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
//            if (mAudioTrackIndex == -1) {
//                mAudioTrackIndex = mMediaMuxer.addTrack(format);
//            }
//        }
//    };

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
