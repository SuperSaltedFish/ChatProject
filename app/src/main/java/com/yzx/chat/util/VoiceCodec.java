package com.yzx.chat.util;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;

import java.io.BufferedOutputStream;
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

    private MediaCodec mMediaCodec;
    private Surface mEncoderSurface;
    private BufferedOutputStream mOutputStream;
    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private int mVideoWidth;
    private int mVideoHeight;

    public VoiceCodec(int videoWidth, int videoHeight) {
        mVideoWidth = videoWidth;
        mVideoHeight = videoHeight;
    }


    public Surface prepare(String file) {
        destroy();
        try {
            mOutputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        final CountDownLatch latch = new CountDownLatch(1);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mVideoWidth, mVideoHeight);
                format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
                format.setInteger(MediaFormat.KEY_BIT_RATE, mVideoWidth * mVideoHeight * 2);
                format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
                format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
                format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000 / 30);
                mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                mEncoderSurface = mMediaCodec.createInputSurface();
                mMediaCodec.setCallback(mCodecCallback);
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mEncoderSurface;
    }

    @Nullable
    public Surface start() {
        if (mMediaCodec == null) {
            return null;
        }
        mMediaCodec.start();
        return mEncoderSurface;
    }

    public void stop() {
        if (mMediaCodec != null) {
            mMediaCodec.signalEndOfInputStream();
            destroy();
        }
    }

    public void destroy() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
            mEncoderSurface = null;
        }
        if (mHandlerThread != null) {
            mHandlerThread.quit();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mOutputStream = null;
        }
    }

    private final MediaCodec.Callback mCodecCallback = new MediaCodec.Callback() {
        private byte[] mBuff;

        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            ByteBuffer outPutByteBuffer = codec.getOutputBuffer(index);
            if (outPutByteBuffer != null) {
                int dataSize = info.size;
                LogUtil.e(dataSize+"");
                if (mBuff == null || dataSize > mBuff.length) {
                    mBuff = new byte[dataSize];
                }
                outPutByteBuffer.get(mBuff, 0, dataSize);
                try {
                    mOutputStream.write(mBuff, 0, dataSize);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            codec.releaseOutputBuffer(index, false);
        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

        }
    };

}
