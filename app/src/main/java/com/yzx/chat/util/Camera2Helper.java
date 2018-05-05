package com.yzx.chat.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Size;
import android.view.Surface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by YZX on 2018年05月03日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class Camera2Helper {


    @Nullable
    public static Camera2Helper createFrontCamera2Helper(Context context) {
        return createCamera2Helper(context, CameraCharacteristics.LENS_FACING_FRONT);
    }

    @Nullable
    public static Camera2Helper createBackCamera2Helper(Context context) {
        return createCamera2Helper(context, CameraCharacteristics.LENS_FACING_BACK);
    }


    private static Camera2Helper createCamera2Helper(Context context, int lensFacingType) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            LogUtil.e("missing 'android.permission.CAMERA' permission");
            return null;
        }
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager == null) {
            return null;
        }
        try {
            CameraCharacteristics cameraCharacteristics;
            for (String cameraId : cameraManager.getCameraIdList()) {
                cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == lensFacingType) {
                    return new Camera2Helper(cameraManager, cameraCharacteristics, cameraId);
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private CameraManager mCameraManager;
    private CameraCharacteristics mCameraCharacteristics;
    private String mCameraID;
    private StreamConfigurationMap mStreamConfMap;

    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraSession;
    private CaptureRequest.Builder mCaptureRequestBuilder;


    private boolean isPreviewing;
    private boolean isEnableFlash;
    private Integer mCameraSensorOrientation;

    private OnCameraStateListener mOnCameraStateListener;

    private Camera2Helper(CameraManager cameraManager, CameraCharacteristics cameraCharacteristics, String cameraID) {
        mCameraManager = cameraManager;
        mCameraCharacteristics = cameraCharacteristics;
        mCameraID = cameraID;
        mStreamConfMap = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        mCameraSensorOrientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        mOnCameraStateListener = new SimpleOnCameraStateListener();
    }

    @SuppressLint("MissingPermission")
    public void openCamera() {
        if (mCameraDevice != null) {
            throw new IllegalStateException("The camera is already Open");
        }
        try {
            mCameraManager.openCamera(mCameraID, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    mCameraDevice = camera;
                    mOnCameraStateListener.onCameraOpened(Camera2Helper.this, camera, true);
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                    LogUtil.e("openCamera fail, error=" + error);
                    mOnCameraStateListener.onCameraOpened(Camera2Helper.this, camera, false);
                }
            }, null);
        } catch (CameraAccessException e) {
            mOnCameraStateListener.onCameraOperatingError(Camera2Helper.this, e);
        }
    }

    public void closeCamera() {
        if (mCameraSession != null) {
            try {
                mCameraSession.abortCaptures();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            mCameraSession.close();
            mCameraSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        isPreviewing = false;
    }

    public void createCaptureSession(List<Surface> outSurfaces) {
        if (mCameraDevice == null) {
            throw new IllegalStateException("The camera does not open");
        }
        try {
            mCameraDevice.createCaptureSession(outSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCameraSession = session;
                    mOnCameraStateListener.onCaptureSessionCreated(Camera2Helper.this, session, true);
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    session.close();
                    mOnCameraStateListener.onCaptureSessionCreated(Camera2Helper.this, session, false);
                    mCameraSession = null;
                }
            }, null);
        } catch (CameraAccessException e) {
            mOnCameraStateListener.onCameraOperatingError(Camera2Helper.this, e);
        }
    }

    public void startPreview(List<Surface> targetSurfaces) {
        try {
            CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);//使用3A模式
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);//开启自动对焦
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO);//开启自动曝光
            captureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);//开启自动白平衡
            captureRequestBuilder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON);//开启视频稳定
            captureRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);//开启光学防抖
            captureRequestBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_FAST);//在不降低相对于传感器输出的帧率的情况下应用降噪
            startPreview(targetSurfaces, captureRequestBuilder);
        } catch (CameraAccessException e) {
            mOnCameraStateListener.onCameraOperatingError(Camera2Helper.this, e);
        }
    }

    public void startPreview(List<Surface> targetSurfaces, @NonNull CaptureRequest.Builder captureRequestBuilder) {
        if (mCameraDevice == null) {
            throw new IllegalStateException("The camera does not open");
        }
        if (mCameraSession == null) {
            throw new IllegalStateException("The CaptureSession does not create");
        }
        mCaptureRequestBuilder = captureRequestBuilder;
        for (Surface surface : targetSurfaces) {
            mCaptureRequestBuilder.addTarget(surface);
        }
        isPreviewing = true;
        updateRepeatingRequest();
    }

    public void stopPreview() {
        if (!isPreviewing) {
            return;
        }
        isPreviewing = false;
        updateRepeatingRequest();
        mOnCameraStateListener.onPreviewStopped(Camera2Helper.this);
    }

    public void recoverPreview() {
        if (isPreviewing || mCameraDevice == null || mCameraSession == null) {
            return;
        }
        isPreviewing = true;
        updateRepeatingRequest();
    }

    public boolean isPreviewing() {
        return isPreviewing;
    }

    public void enableFlash(boolean isEnable) {
        if (isEnableFlash == isEnable) {
            return;
        }
        isEnableFlash = isEnable;
        if (mCameraDevice != null || mCameraSession != null) {
            updateRepeatingRequest();
        }
    }

    public int getCameraSensorOrientation() {
        if (mCameraSensorOrientation == null) {
            return 0;
        }
        return mCameraSensorOrientation;
    }

    private void updateRepeatingRequest() {
        if (isEnableFlash && isPreviewing) {
            mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
        } else {
            mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
        }
        try {
            mCameraSession.abortCaptures();
            if (isPreviewing) {
                mCameraSession.setRepeatingRequest(mCaptureRequestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                        mOnCameraStateListener.onPreviewStarted(Camera2Helper.this, request, true);
                    }

                    @Override
                    public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                        mOnCameraStateListener.onPreviewStarted(Camera2Helper.this, request, false);
                    }
                }, null);
            } else {
                mCameraSession.stopRepeating();
            }
        } catch (CameraAccessException e) {
            isPreviewing = false;
            mOnCameraStateListener.onCameraOperatingError(Camera2Helper.this, e);
        }
    }

    public Size chooseOptimalSize(Class outTypeClass, int previewViewWidth, int previewViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {
        Size[] choices = mStreamConfMap.getOutputSizes(outTypeClass);
        if (choices == null || choices.length == 0) {
            LogUtil.e("Couldn't find any supported size based on" + outTypeClass.getName());
            return null;
        }
        List<Size> bigEnough = new ArrayList<>();
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= previewViewWidth &&
                        option.getHeight() >= previewViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            LogUtil.e("Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public void setOnCameraStateListener(OnCameraStateListener onCameraStateListener) {
        mOnCameraStateListener = onCameraStateListener;
    }

    public interface OnCameraStateListener {
        void onCameraOpened(Camera2Helper helper, CameraDevice camera, boolean isOpenSuccessfully);

        void onCaptureSessionCreated(Camera2Helper helper, CameraCaptureSession session, boolean isCreatedSuccessfully);

        void onPreviewStarted(Camera2Helper helper, CaptureRequest captureRequest, boolean isStartedSuccessfully);

        void onPreviewStopped(Camera2Helper helper);

        void onCameraOperatingError(Camera2Helper helper, CameraAccessException exception);
    }

    public static class SimpleOnCameraStateListener implements OnCameraStateListener {

        @Override
        public void onCameraOpened(Camera2Helper helper, CameraDevice camera, boolean isOpenSuccessfully) {

        }

        @Override
        public void onCaptureSessionCreated(Camera2Helper helper, CameraCaptureSession session, boolean isCreatedSuccessfully) {

        }

        @Override
        public void onPreviewStarted(Camera2Helper helper, CaptureRequest captureRequest, boolean isStartedSuccessfully) {

        }

        @Override
        public void onPreviewStopped(Camera2Helper helper) {

        }

        @Override
        public void onCameraOperatingError(Camera2Helper helper, CameraAccessException exception) {

        }
    }

    private static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }

    }


}
