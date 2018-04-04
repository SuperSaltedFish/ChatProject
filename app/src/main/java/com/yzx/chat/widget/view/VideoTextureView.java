package com.yzx.chat.widget.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.VoiceCodec;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by YZX on 2018年03月24日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

@SuppressWarnings("MissingPermission")
@TargetApi(21)
public class VideoTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private static final String TAG = "VideoTextureView";

    private static final Size DEFAULT_VIDEO_SIZE_4_3 = new Size(800, 600);
    private static final Size DEFAULT_VIDEO_SIZE_16_9 = new Size(960, 540);
    private static final Size ASPECT_RATIO_SIZE_16_9 = new Size(16, 9);
    private static final Size ASPECT_RATIO_SIZE_4_3 = new Size(4, 3);

    private static final int ASPECT_RATIO_TYPE_16_9 = 0;
    private static final int ASPECT_RATIO_TYPE_4_3 = 1;

    @IntDef({ASPECT_RATIO_TYPE_16_9, ASPECT_RATIO_TYPE_4_3})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AspectRatio {
    }

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


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mPreviewSurface = new Surface(surface);
        openCamera(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        configureTransform(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        destroy();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private Context mContext;

    private VoiceCodec mVoiceCodec;
    private Integer mSensorOrientation;

    private Size mPreviewSize;
    private Size mVideoSize;
    private Size mAspectRatio;

    private Surface mPreviewSurface;
    private Surface mMediaSurface;

    private CameraDevice mCamera;
    private CaptureRequest.Builder mPreviewAndVideoRequestBuilder;
    private CameraCaptureSession mPreviewSession;


    private boolean isEnableFlash;
    private boolean isRecorderMode;

    public VideoTextureView(Context context) {
        this(context, null);
    }

    public VideoTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setAspectRatio(ASPECT_RATIO_TYPE_16_9);
        setSurfaceTextureListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int ratioW;
        int ratioH;
        int rotation = getDisplayRotation();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            ratioW = mAspectRatio.getWidth();
            ratioH = mAspectRatio.getHeight();
        } else {
            ratioW = mAspectRatio.getHeight();
            ratioH = mAspectRatio.getWidth();
        }


        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(width * ratioH / ratioW, MeasureSpec.EXACTLY));
        } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(height * ratioW / ratioH, MeasureSpec.EXACTLY), heightMeasureSpec);
        } else {
            if (height <= width * ratioH / ratioW) {
                width = height * ratioW / ratioH;
                super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            } else {
                height = width * ratioH / ratioW;
                super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            }
        }
    }


    private void openCamera(final int previewWidth, final int previewHeight) {
        final CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager == null) {
            return;
        }
        String cameraId = String.valueOf(CameraCharacteristics.LENS_FACING_FRONT);
        CameraCharacteristics cameraCharacteristics;
        try {
            cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return;
        }
        StreamConfigurationMap confMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (confMap == null) {
            return;
        }
        mSensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

        Size videoSize = mAspectRatio.equals(ASPECT_RATIO_SIZE_4_3) ? DEFAULT_VIDEO_SIZE_4_3 : DEFAULT_VIDEO_SIZE_16_9;
        mVideoSize = chooseMinVideoSize(confMap.getOutputSizes(MediaRecorder.class), mAspectRatio, videoSize.getWidth(), videoSize.getHeight());
        if (mVideoSize == null) {
            return;
        }
        int rotation = getDisplayRotation();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            mPreviewSize = chooseOptimalSize(confMap.getOutputSizes(SurfaceTexture.class), previewWidth, previewHeight, mAspectRatio);
        } else {
            mPreviewSize = chooseOptimalSize(confMap.getOutputSizes(SurfaceTexture.class), previewHeight, previewWidth, mAspectRatio);
        }

        if (mPreviewSize == null) {
            return;
        }

        try {
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    mCamera = camera;
                    mVoiceCodec = new VoiceCodec(mVideoSize.getWidth(), mVideoSize.getHeight());
                    configureTransform(previewWidth, previewHeight);
                    startPreview();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    mCamera = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    mCamera = null;
                }
            }, null);
        } catch (CameraAccessException e) {
            throw new RuntimeException("Failed to open camera: " + cameraId, e);
        }
    }

    private void startPreview() {
        if (mCamera == null) {
            return;
        }
        closePreviewSession();
        getSurfaceTexture().setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        try {
            mCamera.createCaptureSession(Collections.singletonList(mPreviewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mPreviewSession = session;
                    update();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    mPreviewSession = null;
                }

                @Override
                public void onClosed(@NonNull CameraCaptureSession session) {
                    super.onClosed(session);
                    mPreviewSession = null;
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            mCamera.close();
            mCamera = null;
        }
    }

    private void update() {
        if (mPreviewAndVideoRequestBuilder == null) {
            try {
                mPreviewAndVideoRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                mPreviewAndVideoRequestBuilder.addTarget(mPreviewSurface);
                mPreviewAndVideoRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
                mPreviewAndVideoRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO);
                mPreviewAndVideoRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                return;
            }
        }
        if (isEnableFlash) {
            mPreviewAndVideoRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
        } else {
            mPreviewAndVideoRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
        }
        mPreviewAndVideoRequestBuilder.removeTarget(mMediaSurface);
        if (isRecorderMode) {
            mPreviewAndVideoRequestBuilder.addTarget(mMediaSurface);
        }
        if (mPreviewSession != null) {
            try {
                mPreviewSession.setRepeatingRequest(mPreviewAndVideoRequestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
                    }
                }, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (mPreviewSize == null) {
            return;
        }
        int rotation = getDisplayRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
            this.setTransform(matrix);
        }
    }

    private int getDisplayRotation() {
        if (mContext instanceof Activity) {
            return ((Activity) mContext).getWindowManager().getDefaultDisplay().getRotation();
        } else {
            return 0;
        }
    }

    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    public void destroy() {
        closePreviewSession();
        if (mCamera != null) {
            mCamera.close();
            mCamera = null;
        }
    }


    public boolean startRecorder(final String savePath) {
        if (mCamera == null || isRecorderMode || TextUtils.isEmpty(savePath) || mPreviewSession == null) {
            return false;
        }
        File file = new File(savePath);
        if (file.exists() && !file.delete()) {
            LogUtil.e("delete file fail");
            return false;
        }
        isRecorderMode = true;
        int orientationHint = 0;
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                orientationHint = DEFAULT_ORIENTATIONS.get(getDisplayRotation());
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                orientationHint = INVERSE_ORIENTATIONS.get(getDisplayRotation());
                break;
        }
        mMediaSurface = mVoiceCodec.prepare(savePath);
        if (mMediaSurface == null) {
            LogUtil.e("VideoRecorder prepare fail");
            return false;
        }
        closePreviewSession();
        getSurfaceTexture().setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        try {
            mCamera.createCaptureSession(Arrays.asList(mPreviewSurface, mMediaSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mPreviewSession = session;
                    update();
                    mVoiceCodec.start();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    mPreviewSession = null;
                    isRecorderMode = false;
                }

                @Override
                public void onClosed(@NonNull CameraCaptureSession session) {
                    super.onClosed(session);
                    mPreviewSession = null;
                    isRecorderMode = false;
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            mCamera.close();
            mCamera = null;
        }
        return true;
    }

    public void stopRecorder() {
        if (!isRecorderMode) {
            return;
        }
        mVoiceCodec.stop();
        startPreview();
        isRecorderMode = false;
    }


    public void setAspectRatio(@AspectRatio int ratio) {
        switch (ratio) {
            case ASPECT_RATIO_TYPE_16_9:
                mAspectRatio = ASPECT_RATIO_SIZE_16_9;
                break;
            case ASPECT_RATIO_TYPE_4_3:
                mAspectRatio = ASPECT_RATIO_SIZE_4_3;
                break;
        }
    }

    public void setEnableFlash(boolean isEnableFlash) {
        if (this.isEnableFlash == isEnableFlash) {
            return;
        }
        this.isEnableFlash = isEnableFlash;
        update();
    }

    public boolean isEnableFlash() {
        return isEnableFlash;
    }

    public boolean isRecorderMode() {
        return isRecorderMode;
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        if (choices == null || choices.length == 0) {
            LogUtil.e("Couldn't find any preview size");
            return null;
        }
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            LogUtil.e("Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    private static Size chooseMinVideoSize(Size[] choices, Size aspectRatio, int expectedWidth, int expectedHeight) {
        if (choices == null || choices.length == 0) {
            LogUtil.e("Couldn't find any video size");
            return null;
        }
        List<Size> bigEnough = new ArrayList<>();
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * aspectRatio.getWidth() / aspectRatio.getHeight()
                    && size.getWidth() >= expectedWidth && size.getHeight() >= expectedHeight) {
                bigEnough.add(size);
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            LogUtil.e("Couldn't find any suitable video size");
            return choices[0];
        }
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

}
