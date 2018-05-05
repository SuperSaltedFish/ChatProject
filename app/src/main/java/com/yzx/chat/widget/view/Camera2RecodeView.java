package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.util.AttributeSet;
import android.util.Size;
import android.view.Surface;

import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.VoiceCodec;

import java.util.List;

/**
 * Created by YZX on 2018年05月04日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class Camera2RecodeView extends Camera2PreviewView {

    protected static final int MAX_VIDEO_WIDTH = 1280;
    protected static final int MAX_VIDEO_HEIGHT = 720;

    VoiceCodec mVoiceCodec;
    private Surface mVideoSurface;
    private boolean isRecording;

    public Camera2RecodeView(Context context) {
        this(context, null);
    }

    public Camera2RecodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Camera2RecodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }


    public boolean startRecorder(final String savePath) {
        if (isRecording) {
            throw new IllegalStateException("The Camera2RecodeView is already recoding");
        }
        if (mVoiceCodec == null) {
            LogUtil.e("startRecorder fail : The VoiceCodec is not initialized");
            return false;
        }
        if (!mCamera2Helper.isPreviewing()) {
            LogUtil.e("startRecorder fail : The Camera is not open");
            return false;
        }
        boolean isSuccess = mVoiceCodec.start(savePath);
        if (isSuccess) {
            isRecording = true;
            refreshPreview();
        }
        return isSuccess;
    }

    public void stopRecorder() {
        if (!isRecording) {
            return;
        }
        mVoiceCodec.stop();
        reopenCamera();
        isRecording = false;
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        super.onSurfaceTextureAvailable(surface, width, height);
        if (mCamera2Helper != null) {
            Size videoOptimalSize = mCamera2Helper.chooseOptimalSize(MediaCodec.class, MAX_VIDEO_WIDTH, MAX_VIDEO_HEIGHT, MAX_VIDEO_WIDTH, MAX_VIDEO_HEIGHT, getAspectRatioSize());
            if (videoOptimalSize == null) {
                return;
            }
            mVoiceCodec = VoiceCodec.createEncoder(videoOptimalSize.getWidth(), videoOptimalSize.getHeight());
        }
    }

    @Override
    protected List<Surface> getAvailableSurfaces() {
        List<Surface> surfaces = super.getAvailableSurfaces();
        if (surfaces != null && mVoiceCodec != null) {
            mVideoSurface = mVoiceCodec.getInputSurface();
            if (mVideoSurface != null) {
                surfaces.add(mVideoSurface);
            }
        }
        return surfaces;
    }

    @Override
    protected List<Surface> getOutPutSurfaces() {
        List<Surface> surfaces = super.getOutPutSurfaces();
        if (surfaces != null && mVideoSurface != null&&isRecording) {
            surfaces.add(mVideoSurface);
        }
        return surfaces;
    }
}
