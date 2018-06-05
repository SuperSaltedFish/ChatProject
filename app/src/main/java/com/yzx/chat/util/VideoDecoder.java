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
import android.util.Size;
import android.view.Surface;
import android.widget.MediaController;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

/**
 * Created by YZX on 2018年05月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VideoDecoder implements MediaController.MediaPlayerControl {

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
    private Size mVideoResolutionRatio;
    private long mVideoDurationMs;
    private long mStartTimeUsVideo;
    private long mStartTimeUsAudio;
    private long mLastPlayFramePresentationTimeUsVideo;
    private long mLastPlayFramePresentationTimeUsAudio;
    private boolean isPauseVideo;
    private boolean isPauseAudio;
    private boolean isVideoEndOfStream;
    private boolean isAudioEndOfStream;

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
        int videoTrackIndex = getMediaTrackIndexByMimeType(mVideoExtractor, "video/");
        int audioTrackIndex = getMediaTrackIndexByMimeType(mAudioExtractor, "audio/");
        if (videoTrackIndex == -1 || audioTrackIndex == -1) {
            return false;
        }
        mVideoExtractor.selectTrack(videoTrackIndex);
        mAudioExtractor.selectTrack(audioTrackIndex);
        MediaFormat videoFormat = mVideoExtractor.getTrackFormat(videoTrackIndex);
        MediaFormat audioFormat = mVideoExtractor.getTrackFormat(audioTrackIndex);
        try {
            mVideoDurationMs = videoFormat.getLong(MediaFormat.KEY_DURATION) / 1000;
            mVideoResolutionRatio = new Size(videoFormat.getInteger(MediaFormat.KEY_WIDTH), videoFormat.getInteger(MediaFormat.KEY_HEIGHT));
            mVideoCodec = MediaCodec.createDecoderByType(videoFormat.getString(MediaFormat.KEY_MIME));
            mVideoCodec.configure(videoFormat, mOutputSurface, null, 0);
            mVideoCodec.setCallback(new VideoDecodeCallback());

            int audioChannels = audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)==1? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
            int audioSampleRate = audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            int audioMaxInputSize = Math.max(audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE),  AudioTrack.getMinBufferSize(audioSampleRate, audioChannels, AUDIO_BIT_PER_SAMPLE));
            mAudioCodec = MediaCodec.createDecoderByType(audioFormat.getString(MediaFormat.KEY_MIME));
            mAudioCodec.configure(audioFormat, null, null, 0);
            mAudioCodec.setCallback(new AudioDecodeCallback());
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, audioSampleRate,audioChannels, AUDIO_BIT_PER_SAMPLE, audioMaxInputSize, AudioTrack.MODE_STREAM);
            isPauseVideo = true;
            isPauseAudio = true;
            return true;
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void start() {
        synchronized (this) {
            if (mVideoCodec == null || mAudioCodec == null) {
                throw new RuntimeException("The VoiceCodec is already release");
            }
            if (isPauseVideo || isPauseAudio) {
                final CountDownLatch latch = new CountDownLatch(1);
                mDecodeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isPauseVideo) {
                            resumeVideo();
                        }
                        if (isPauseAudio) {
                            resumeAudio();
                        }
                        latch.countDown();
                    }
                });
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void pause() {
        synchronized (this) {
            if (mVideoCodec == null || mAudioCodec == null) {
                throw new RuntimeException("The VoiceCodec is already release");
            }
            if (!isPauseVideo || !isPauseAudio) {
                final CountDownLatch latch = new CountDownLatch(1);
                mDecodeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isPauseVideo) {
                            pauseVideo(false);
                        }
                        if (!isPauseAudio) {
                            pauseAudio(false);
                        }
                        latch.countDown();
                    }
                });
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public int getDuration() {
        return (int) mVideoDurationMs;
    }

    @Override
    public int getCurrentPosition() {
        return (int) (mLastPlayFramePresentationTimeUsVideo / 1000);
    }

    @Override
    public void seekTo(final int timeMs) {
        final CountDownLatch latch = new CountDownLatch(1);
        mDecodeHandler.post(new Runnable() {
            @Override
            public void run() {
                mVideoExtractor.seekTo(timeMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                mAudioExtractor.seekTo(timeMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                mLastPlayFramePresentationTimeUsVideo = mVideoExtractor.getSampleTime();
                mLastPlayFramePresentationTimeUsAudio = mAudioExtractor.getSampleTime();
                mStartTimeUsVideo = SystemClock.uptimeMillis() * 1000 - mLastPlayFramePresentationTimeUsVideo;
                mStartTimeUsAudio = SystemClock.uptimeMillis() * 1000 - mLastPlayFramePresentationTimeUsAudio;
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean isPlaying() {
        return !isPauseAudio && !isPauseVideo;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return mAudioTrack.getAudioSessionId();
    }

    public Size getVideoResolutionRatio() {
        return mVideoResolutionRatio;
    }

    private void resumeVideo() {
        mVideoCodec.start();
        isPauseVideo = false;
        if (mLastPlayFramePresentationTimeUsVideo != 0) {
            mVideoExtractor.seekTo(mLastPlayFramePresentationTimeUsVideo, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            mStartTimeUsVideo = SystemClock.uptimeMillis() * 1000 - mVideoExtractor.getSampleTime();
        }
    }

    private void resumeAudio() {
        mAudioTrack.play();
        mAudioCodec.start();
        isPauseAudio = false;
        if (mLastPlayFramePresentationTimeUsAudio != 0) {
            mAudioExtractor.seekTo(mLastPlayFramePresentationTimeUsAudio, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            mStartTimeUsAudio = SystemClock.uptimeMillis() * 1000 - mAudioExtractor.getSampleTime();
        }
    }

    private void pauseVideo(boolean isResetProgress) {
        if (!isPauseVideo) {
            mVideoCodec.flush();
        }
        mDecodeHandler.removeCallbacksAndMessages(mVideoCodec);
        if (isResetProgress) {
            mVideoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            isVideoEndOfStream = false;
            mStartTimeUsVideo = 0;
            mLastPlayFramePresentationTimeUsVideo = 0;
        }
        isPauseVideo = true;
    }

    private void pauseAudio(boolean isResetProgress) {
        if (!isPauseAudio) {
            mAudioCodec.flush();
            mAudioTrack.pause();
            mAudioTrack.flush();
        }
        mDecodeHandler.removeCallbacksAndMessages(mAudioCodec);
        if (isResetProgress) {
            mAudioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            isAudioEndOfStream = false;
            mStartTimeUsAudio = 0;
            mLastPlayFramePresentationTimeUsAudio = 0;
        }
        isPauseAudio = true;
    }

    public void release() {
        synchronized (this) {
            if (mVideoCodec != null || mAudioCodec != null) {
                final CountDownLatch latch = new CountDownLatch(1);
                mDecodeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        isVideoEndOfStream = true;
                        isAudioEndOfStream = true;
                        if (mVideoCodec != null) {
                            mVideoCodec.release();
                            mVideoCodec = null;
                        }
                        if (mAudioCodec != null) {
                            mAudioCodec.release();
                            mAudioCodec = null;
                        }
                        if (mDecodeHandler != null) {
                            mDecodeHandler.removeCallbacksAndMessages(null);
                            mDecodeHandler.getLooper().quit();
                            mDecodeHandler = null;
                        }
                        if (mAudioTrack != null) {
                            mAudioTrack.release();
                            mAudioTrack = null;
                        }
                        if (mVideoExtractor != null) {
                            mVideoExtractor.release();
                            mVideoExtractor = null;
                        }
                        if (mAudioExtractor != null) {
                            mAudioExtractor.release();
                            mAudioExtractor = null;
                        }
                        latch.countDown();
                    }
                });
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
        protected void playFrame(MediaCodec codec, int index, int dataSize, long presentationTimeUs) {
            if (isVideoEndOfStream && !mDecodeHandler.hasMessages(0, codec)) {
                pauseVideo(true);
            } else {
                mLastPlayFramePresentationTimeUsVideo = presentationTimeUs;
                codec.releaseOutputBuffer(index, true);
            }
        }

        @Override
        protected long getNextFramePlayTimeMs(long presentationTimeUs) {
            if (mStartTimeUsVideo == 0) {
                mStartTimeUsVideo = SystemClock.uptimeMillis() * 1000;
            }
            return (presentationTimeUs + mStartTimeUsVideo) / 1000;
        }

        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            if (!isPauseVideo) {
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

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                isVideoEndOfStream = true;
                if (!mDecodeHandler.hasMessages(0, codec)) {
                    pauseVideo(true);
                }
                return;
            }
            if (!isPauseVideo) {
                super.onOutputBufferAvailable(codec, index, info);
            }
        }
    }

    private class AudioDecodeCallback extends DelayCodecCallback {

        @Override
        protected void playFrame(MediaCodec codec, int index, int dataSize, long presentationTimeUs) {
            if (isAudioEndOfStream && !mDecodeHandler.hasMessages(0, codec)) {
                pauseAudio(true);
            } else {
                mLastPlayFramePresentationTimeUsAudio = presentationTimeUs;
                ByteBuffer outputBuffer = codec.getOutputBuffer(index);
                if (outputBuffer != null && dataSize > 0) {
                    mAudioTrack.write(outputBuffer, dataSize, AudioTrack.WRITE_NON_BLOCKING);
                }
                codec.releaseOutputBuffer(index, false);
            }
        }

        @Override
        protected long getNextFramePlayTimeMs(long presentationTimeUs) {
            if (mStartTimeUsAudio == 0) {
                mStartTimeUsAudio = SystemClock.uptimeMillis() * 1000;
            }
            return (presentationTimeUs + mStartTimeUsAudio) / 1000;
        }

        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            if (!isPauseAudio) {
                ByteBuffer inputBuffer = codec.getInputBuffer(index);
                if (inputBuffer != null) {
                    inputBuffer.clear();
                    int readLength = mAudioExtractor.readSampleData(inputBuffer, 0);
                    if (readLength > 0) {
                        codec.queueInputBuffer(index, 0, readLength, mAudioExtractor.getSampleTime(), 0);
                        mAudioExtractor.advance();
                    } else {
                        codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    }
                }
            }
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                isAudioEndOfStream = true;
                if (!mDecodeHandler.hasMessages(0, codec)) {
                    pauseAudio(true);
                }
                return;
            }
            if (!isPauseAudio) {
                super.onOutputBufferAvailable(codec, index, info);
            }
        }
    }


    private abstract class DelayCodecCallback extends MediaCodec.Callback {

        protected abstract void playFrame(MediaCodec codec, int index, int dataSize, long presentationTimeUs);

        protected abstract long getNextFramePlayTimeMs(long presentationTimeUs);

        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

        }

        @Override
        public void onOutputBufferAvailable(@NonNull final MediaCodec codec, final int index, @NonNull final MediaCodec.BufferInfo info) {
            long nextFramePlayTimeMs = getNextFramePlayTimeMs(info.presentationTimeUs);
            final int dataSize = info.size;
            if (SystemClock.uptimeMillis() - nextFramePlayTimeMs >= 0 || dataSize == 0) {
                playFrame(codec, index, dataSize, info.presentationTimeUs);
            } else {
                mDecodeHandler.postAtTime(new Runnable() {
                    @Override
                    public void run() {
                        playFrame(codec, index, dataSize, info.presentationTimeUs);
                    }
                }, codec, nextFramePlayTimeMs);
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
