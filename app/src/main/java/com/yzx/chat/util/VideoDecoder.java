package com.yzx.chat.util;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

/**
 * Created by YZX on 2018年05月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VideoDecoder {

    private static final String TAG = VideoDecoder.class.getSimpleName();

    private static final int AUDIO_BIT_PER_SAMPLE = AudioFormat.ENCODING_PCM_16BIT;

    public static VideoDecoder createEncoder(final String videoPath, final Surface outputSurface) {
        try {
            return new VideoDecoder(videoPath, outputSurface);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private MediaCodec mVideoCodec;
    private MediaCodec mAudioCodec;
    private Handler mDecodeHandler;
    private MediaExtractor mVideoExtractor;
    private MediaExtractor mAudioExtractor;
    private AudioTrack mAudioTrack;
    private Surface mOutputSurface;

    private String mVideoPath;
    private int mVideoTrackIndex;
    private int mAudioTrackIndex;
    private boolean isStartingEncoded;
    private boolean isPause;

    private VideoDecoder(String videoPath, Surface outputSurface) throws IOException {
        mVideoPath = videoPath;
        mOutputSurface = outputSurface;
        HandlerThread codecThread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        codecThread.start();
        mDecodeHandler = new Handler(codecThread.getLooper());
        final CountDownLatch latch = new CountDownLatch(1);
        mDecodeHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!tryInitVideoDecode()) {
                    release();
                }
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mVideoCodec == null || mAudioCodec == null || mAudioTrack == null) {
            throw new IOException("init VoiceDecoder fail");
        }
    }

    private boolean tryInitVideoDecode() {
        mVideoExtractor = new MediaExtractor();
        mAudioExtractor = new MediaExtractor();
        try {
            mVideoExtractor.setDataSource(mVideoPath);
            mAudioExtractor.setDataSource(mVideoPath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        mVideoTrackIndex = getMediaTrackIndexByMimeType(mVideoExtractor, "video/");
        mAudioTrackIndex = getMediaTrackIndexByMimeType(mAudioExtractor, "audio/");
        if (mVideoTrackIndex == -1 || mAudioTrackIndex == -1) {
            return false;
        }
        mVideoExtractor.selectTrack(mVideoTrackIndex);
        mAudioExtractor.selectTrack(mAudioTrackIndex);
        MediaFormat videoFormat = mVideoExtractor.getTrackFormat(mVideoTrackIndex);
        MediaFormat audioFormat = mVideoExtractor.getTrackFormat(mAudioTrackIndex);
        try {
            long videoDuration = videoFormat.getLong(MediaFormat.KEY_DURATION);
            mVideoCodec = MediaCodec.createDecoderByType(videoFormat.getString(MediaFormat.KEY_MIME));
            mVideoCodec.configure(videoFormat, mOutputSurface, null, 0);
            mVideoCodec.setCallback(new VideoDecodeCallback());

            int audioChannels = audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            int audioSampleRate = audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            int audioMaxInputSize = audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
            mAudioCodec = MediaCodec.createDecoderByType(audioFormat.getString(MediaFormat.KEY_MIME));
            mAudioCodec.configure(audioFormat, null, null, 0);
            mAudioCodec.setCallback(new AudioDecodeCallback());
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, audioSampleRate, (audioChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO), AUDIO_BIT_PER_SAMPLE, audioMaxInputSize, AudioTrack.MODE_STREAM);
            return true;
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean start() {
        synchronized (this) {
            if (isStartingEncoded) {
                throw new RuntimeException("The VoiceCodec is already Starting");
            }
            if (mVideoCodec == null || mAudioCodec == null) {
                throw new RuntimeException("The VoiceCodec is already release");
            }
            mAudioTrack.play();
            mVideoCodec.start();
            //     mAudioCodec.start();
            isStartingEncoded = true;
        }
        return true;
    }

    public void resume() {
        synchronized (this) {
            if (!isPause) {
                return;
            }
            if (mVideoCodec == null || mAudioCodec == null) {
                throw new RuntimeException("The VoiceCodec is already release");
            }
            mAudioTrack.play();
            mVideoCodec.start();
            mAudioCodec.start();
        }
        isPause = false;
    }

    public void pause() {
        synchronized (this) {
            if (isPause) {
                return;
            }
            if (mVideoCodec == null || mAudioCodec == null) {
                throw new RuntimeException("The VoiceCodec is already release");
            }
            isPause = true;
            mAudioCodec.flush();
            mVideoCodec.flush();
            mAudioTrack.pause();
        }
    }

    private void decodeComplete() {
        mAudioCodec.flush();
        mVideoCodec.flush();
        mAudioTrack.pause();
        mAudioTrack.flush();
        mVideoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        mAudioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
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
            if (mDecodeHandler != null) {
                mDecodeHandler.getLooper().quitSafely();
                mDecodeHandler = null;
            }
            if (mAudioTrack != null) {
                mAudioTrack.release();
                mAudioTrack = null;
            }
            if (mVideoExtractor != null) {
                mVideoExtractor.release();
            }
            if (mAudioExtractor != null) {
                mAudioExtractor.release();
            }
        }
    }

    public boolean isRunning() {
        synchronized (this) {
            return isStartingEncoded;
        }
    }

    private static int getMediaTrackIndexByMimeType(MediaExtractor mediaExtractor, String mimeType) {
        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith(mimeType)) {
                return i;
            }
        }
        return -1;
    }


    private class VideoDecodeCallback extends DelayCodecCallback {

        @Override
        protected void playFrame(MediaCodec codec, int index, int dataSize) {
            codec.releaseOutputBuffer(index, true);
        }

        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            ByteBuffer inputBuffer = codec.getInputBuffer(index);
            if (inputBuffer != null) {
                inputBuffer.clear();
                int readLength = mVideoExtractor.readSampleData(inputBuffer, 0);
                if (readLength > 0) {
                    codec.queueInputBuffer(index, 0, readLength, mVideoExtractor.getSampleTime(), 0);
                    mVideoExtractor.advance();
                } else {
                    codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                }
            }
        }
    }

    private class AudioDecodeCallback extends DelayCodecCallback {

        @Override
        protected void playFrame(MediaCodec codec, int index, int dataSize) {
            ByteBuffer outputBuffer = codec.getOutputBuffer(index);
            if (outputBuffer != null && dataSize > 0) {
                mAudioTrack.write(outputBuffer, dataSize, AudioTrack.WRITE_NON_BLOCKING);
            }
            codec.releaseOutputBuffer(index, false);
        }

        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            ByteBuffer inputBuffer = codec.getInputBuffer(index);
            if (inputBuffer != null) {
                inputBuffer.clear();
                int readLength = mAudioExtractor.readSampleData(inputBuffer, 0);
                if (readLength > 0) {
                    codec.queueInputBuffer(index, 0, readLength, mAudioExtractor.getSampleTime(), 0);
                    mVideoExtractor.advance();
                } else {
                    codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                }
            }
        }
    }


    private abstract class DelayCodecCallback extends MediaCodec.Callback {
        private long mStartTimeUs = -1;

        protected abstract void playFrame(MediaCodec codec, int index, int dataSize);

        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

        }

        @Override
        public void onOutputBufferAvailable(@NonNull final MediaCodec codec, final int index, @NonNull MediaCodec.BufferInfo info) {
            if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                decodeComplete();
                return;
            }
            if (mStartTimeUs == -1) {
                mStartTimeUs = SystemClock.elapsedRealtimeNanos() / 1000;
            }
            long nextFramePlayTime = (SystemClock.elapsedRealtimeNanos() / 1000 - mStartTimeUs - info.presentationTimeUs) / 1000;
            final int dataSize = info.size;
            if (nextFramePlayTime <= 0 || dataSize == 0) {
                playFrame(codec, index, dataSize);
            } else {
                mDecodeHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playFrame(codec, index, dataSize);
                    }
                }, nextFramePlayTime);
            }
        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

        }
    }

}
