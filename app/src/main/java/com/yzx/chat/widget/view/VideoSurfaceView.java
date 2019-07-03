package com.yzx.chat.widget.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.yzx.chat.util.VideoDecoder;

/**
 * Created by YZX on 2019年07月01日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VideoSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private VideoDecoder mVideoDecoder;
    private Size mVideoSize;
    private String mVideoPath;

    private SurfaceHolder mSurfaceHolder;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private OnPlayStateListener mOnPlayStateListener;

    public VideoSurfaceView(Context context) {
        this(context, null);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        setupSurfaceSize();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        tryPlay();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceHolder = null;
        stopPlay();
    }

    public void setVideoPath(String videoPath,OnPlayStateListener listener) {
        mVideoPath = videoPath;
        mOnPlayStateListener = listener;
        setupSurfaceSize();
    }

    private void setupSurfaceSize() {
        stopPlay();
        if (!TextUtils.isEmpty(mVideoPath) && mSurfaceHolder != null) {
            mVideoDecoder = VideoDecoder.createEncoder(mVideoPath, mSurfaceHolder.getSurface());
            if (mVideoDecoder != null) {
                mVideoSize = mVideoDecoder.getVideoResolutionRatio();
                if (mVideoSize.getWidth() != mSurfaceWidth || mVideoSize.getHeight() != mSurfaceHeight) {
                    mSurfaceHolder.setFixedSize(mVideoSize.getWidth(), mVideoSize.getHeight());
                } else {
                    tryPlay();
                }
            } else {
                if (mOnPlayStateListener != null) {
                    mOnPlayStateListener.onPlayFailure();
                }
            }
        }
    }

    private void tryPlay() {
        if (mVideoSize != null && mVideoDecoder != null && !mVideoDecoder.isPlaying() && mSurfaceWidth == mVideoSize.getWidth() && mSurfaceHeight == mVideoSize.getHeight()) {
            mVideoDecoder.start();
            if (mOnPlayStateListener != null) {
                mOnPlayStateListener.onPlaySuccessful();
            }
        }
    }

    public void stopPlay(){
        if (mVideoDecoder != null) {
            mVideoDecoder.release();
        }
    }

    public interface OnPlayStateListener {
        void onPlaySuccessful();

        void onPlayFailure();
    }
}
