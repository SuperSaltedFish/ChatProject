package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaCodec;
import android.util.AttributeSet;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;

import com.yzx.chat.util.Camera2Helper;
import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.util.VideoEncoder;

import java.util.List;

/**
 * Created by YZX on 2018年05月04日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class Camera2RecodeView extends Camera2PreviewView {

    protected static final int MAX_VIDEO_WIDTH = 1280;
    protected static final int MAX_VIDEO_HEIGHT = 720;

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    VideoEncoder mVideoEncoder;
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

    @Override
    public void onPause() {
        super.onPause();
        stopRecorder();
    }

    @Override
    public void switchCamera(int cameraType) {
        super.switchCamera(cameraType);
        stopRecorder();
    }

    @Override
    public void closeCamera() {
        super.closeCamera();
        stopRecorder();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mCamera2Helper != null) {
            Size videoOptimalSize = mCamera2Helper.chooseOptimalSize(MediaCodec.class, MAX_VIDEO_WIDTH, MAX_VIDEO_HEIGHT, MAX_PREVIEW_WIDTH,MAX_PREVIEW_HEIGHT, getAspectRatioSize());
            if (videoOptimalSize == null) {
                return;
            }
            int videoRotation;
            switch (mCamera2Helper.getCameraSensorOrientation()) {
                case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                    videoRotation = DEFAULT_ORIENTATIONS.get(getDisplayRotation());
                    break;
                case SENSOR_ORIENTATION_INVERSE_DEGREES:
                    videoRotation = INVERSE_ORIENTATIONS.get(getDisplayRotation());
                    break;
                default:
                    videoRotation = 0;
            }
            mVideoEncoder = VideoEncoder.createVideoEncoder(videoOptimalSize.getWidth(), videoOptimalSize.getHeight(), videoRotation);
        }
        super.onSurfaceTextureAvailable(surface, width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        super.onSurfaceTextureDestroyed(surface);
        if (mVideoEncoder != null) {
            mVideoEncoder.release();
        }
        return true;
    }

    @Override
    protected List<Surface> getAvailableSurfaces() {
        List<Surface> surfaces = super.getAvailableSurfaces();
        if (surfaces != null && mVideoEncoder != null) {
            mVideoSurface = mVideoEncoder.getInputSurface();
            if (mVideoSurface != null) {
                surfaces.add(mVideoSurface);
            }
        }
        return surfaces;
    }

    @Override
    protected List<Surface> getOutPutSurfaces() {
        List<Surface> surfaces = super.getOutPutSurfaces();
        if (surfaces != null && mVideoSurface != null && isRecording) {
            surfaces.add(mVideoSurface);
        }
        return surfaces;
    }

    @Override
    protected CaptureRequest.Builder getCaptureRequestBuilder(CameraDevice device) {
        try {
            return Camera2Helper.getRecodeTypeCaptureRequestBuilder(device);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean startRecorder(final String savePath) {
        if (isRecording) {
            throw new IllegalStateException("The Camera2RecodeView is already recoding");
        }
        if (mVideoEncoder == null) {
            LogUtil.e("startRecorder fail : The VoiceCodec is not initialized");
            return false;
        }
        if (!mCamera2Helper.isPreviewing()) {
            LogUtil.e("startRecorder fail : The Camera is not open");
            return false;
        }
        boolean isSuccess = mVideoEncoder.start(savePath);
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
        super.onPause();
        mVideoEncoder.stop();
        isRecording = false;
    }

    public void restartPreview() {
        recreateCaptureSession();
    }

    public boolean isRecording() {
        return isRecording;
    }
}
