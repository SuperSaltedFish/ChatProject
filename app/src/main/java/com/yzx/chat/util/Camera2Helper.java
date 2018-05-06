package com.yzx.chat.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


/**
 * Created by YZX on 2018年05月03日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
@TargetApi(21)
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

    public static CaptureRequest.Builder getPreviewTypeCaptureRequestBuilder(CameraDevice device) throws CameraAccessException {
        CaptureRequest.Builder builder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);//使用3A模式
        builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);//开启光学防抖
        builder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_FAST);//在不降低相对于传感器输出的帧率的情况下应用降噪
        return builder;
    }

    public static CaptureRequest.Builder getRecodeTypeCaptureRequestBuilder(CameraDevice device) throws CameraAccessException {
        CaptureRequest.Builder builder = device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);//使用3A模式
        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);//开启自动对焦
        builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO);//开启自动曝光
        builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);//开启自动白平衡
        builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);//开启光学防抖
        builder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_FAST);//在不降低相对于传感器输出的帧率的情况下应用降噪
        return builder;
    }

    private CameraManager mCameraManager;
    private CameraCharacteristics mCameraCharacteristics;
    private String mCameraID;
    private StreamConfigurationMap mStreamConfMap;
    private Integer mCameraSensorOrientation;
    private Semaphore mCameraOpenCloseLock;

    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraSession;
    private CaptureRequest.Builder mCurrentCaptureRequestBuilder;
    private CaptureRequest mCurrentCaptureRequest;

    private boolean isPreviewing;
    private boolean isAllowRepeatingRequest;
    private boolean isEnableFlash;

    private OnCameraStateListener mOnCameraStateListener;

    private Camera2Helper(CameraManager cameraManager, CameraCharacteristics cameraCharacteristics, String cameraID) {
        mCameraManager = cameraManager;
        mCameraCharacteristics = cameraCharacteristics;
        mCameraID = cameraID;
        mStreamConfMap = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        mCameraSensorOrientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        mOnCameraStateListener = new SimpleOnCameraStateListener();
        mCameraOpenCloseLock = new Semaphore(1);
    }

    @SuppressLint("MissingPermission")
    public void openCamera() {
        try {
            if (!mCameraOpenCloseLock.tryAcquire(5000, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            if (mCameraDevice != null) {
                throw new IllegalStateException("The camera is already Open");
            }
            mCameraManager.openCamera(mCameraID, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    mCameraDevice = camera;
                    mCameraOpenCloseLock.release();
                    mOnCameraStateListener.onCameraOpened(camera, true);
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    closeCamera();
                    mCameraOpenCloseLock.release();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    closeCamera();
                    mCameraOpenCloseLock.release();
                    LogUtil.e("openCamera fail, error=" + error);
                    mOnCameraStateListener.onCameraOpened(camera, false);
                }
            }, null);
        } catch (CameraAccessException e) {
            mOnCameraStateListener.onCameraOperatingError(e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    public void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closeCaptureSession();
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            mCurrentCaptureRequestBuilder = null;
            isAllowRepeatingRequest = false;
            isPreviewing = false;
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    public void createCaptureSession(List<Surface> outSurfaces) {
        if (mCameraDevice == null) {
            throw new IllegalStateException("The camera does not open");
        }
        closeCaptureSession();
        try {
            mCameraDevice.createCaptureSession(outSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCameraSession = session;
                    isAllowRepeatingRequest = true;
                    mOnCameraStateListener.onCaptureSessionCreated(mCameraDevice, session, true);
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    session.close();
                    mOnCameraStateListener.onCaptureSessionCreated(mCameraDevice, session, false);
                    mCameraSession = null;
                }

                @Override
                public void onClosed(@NonNull CameraCaptureSession session) {
                    isAllowRepeatingRequest = false;
                }
            }, null);
        } catch (CameraAccessException e) {
            mOnCameraStateListener.onCameraOperatingError(e);
        }
    }

    public void closeCaptureSession() {
        if (mCameraSession != null) {
            mCameraSession.close();
            mCameraSession = null;
        }
        mCurrentCaptureRequest = null;
        isAllowRepeatingRequest = false;
    }

    public void startPreview(@NonNull CaptureRequest.Builder captureRequestBuilder) {
        checkCameraState();
        mCurrentCaptureRequestBuilder = captureRequestBuilder;
        startRepeatingRequest(mCurrentCaptureRequestBuilder, null);
    }

    public void stopPreview() {
        checkCameraState();
        if (isPreviewing) {
            stopRepeatingRequest();
        }
    }

    public void recoverPreview() {
        checkCameraState();
        if (!isPreviewing) {
            startRepeatingRequest(mCurrentCaptureRequestBuilder, null);
        }
    }

    public void setEnableFlash(boolean isEnable) {
        if (isEnable == isEnableFlash) {
            return;
        }
        isEnableFlash = isEnable;
        if (isPreviewing) {
            startRepeatingRequest(mCurrentCaptureRequestBuilder, null);
        }
    }

    public boolean isPreviewing() {
        return isPreviewing;
    }

    public boolean isAllowRepeatingRequest() {
        return isAllowRepeatingRequest;
    }

    public int getCameraSensorOrientation() {
        if (mCameraSensorOrientation == null) {
            return 0;
        }
        return mCameraSensorOrientation;
    }

    public CameraCharacteristics getCameraCharacteristics() {
        return mCameraCharacteristics;
    }

    private void startRepeatingRequest(final CaptureRequest.Builder builder, MeteringRectangle[] focusRectangleArr) {
        builder.set(CaptureRequest.FLASH_MODE, isEnableFlash ? CaptureRequest.FLASH_MODE_TORCH : CaptureRequest.FLASH_MODE_OFF);
        try {
            if (focusRectangleArr == null || focusRectangleArr.length == 0) {
                builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                mCurrentCaptureRequest = builder.build();
                mCameraSession.setRepeatingRequest(mCurrentCaptureRequest, null, null);
            } else {
                builder.set(CaptureRequest.CONTROL_AF_REGIONS, focusRectangleArr);
                builder.set(CaptureRequest.CONTROL_AE_REGIONS, focusRectangleArr);
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
                builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
                builder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
                mCurrentCaptureRequest = builder.build();
                mCameraSession.setRepeatingRequest(mCurrentCaptureRequest, new CameraCaptureSession.CaptureCallback() {
                    private boolean isFocusComplete;

                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                        if (isFocusComplete) {
                            return;
                        }
                        Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                        if (null == afState) {
                            return;
                        }
                        if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                            builder.set(CaptureRequest.CONTROL_AF_REGIONS, null);
                            builder.set(CaptureRequest.CONTROL_AE_REGIONS, null);
                            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
                            startRepeatingRequest(builder, null);
                            isFocusComplete = true;
                        }
                    }
                }, null);
            }
            isPreviewing = true;
        } catch (CameraAccessException e) {
            mOnCameraStateListener.onCameraOperatingError(e);
        }
    }

    private void stopRepeatingRequest() {
        try {
            mCameraSession.stopRepeating();
            isPreviewing = false;
        } catch (CameraAccessException e) {
            isPreviewing = false;
            mOnCameraStateListener.onCameraOperatingError(e);
        }
    }

    private void checkCameraState() {
        if (mCameraDevice == null) {
            throw new IllegalStateException("The camera does not open");
        }
        if (mCameraSession == null) {
            throw new IllegalStateException("The CaptureSession does not create");
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

    public void focus(MeteringRectangle[] focusRectangleArr) {
        checkCameraState();
        if (isPreviewing) {
            startRepeatingRequest(mCurrentCaptureRequestBuilder, focusRectangleArr);
        }
    }

    public void focusOnTouch(int touchX, int touchY, int totalWidth, int totalHeight, int previewWidth, int previewHeight) {
        if (mCurrentCaptureRequest == null || !isPreviewing) {
            return;
        }
        double scale;
        double horizontalOffset = 0;
        double verticalOffset = 0;
        if (previewHeight * totalWidth > previewWidth * totalHeight) {
            scale = totalWidth * 1.0 / previewWidth;
            verticalOffset = (previewHeight - totalHeight / scale) / 2;
        } else {
            scale = totalHeight * 1.0 / previewHeight;
            horizontalOffset = (previewWidth - totalWidth / scale) / 2;
        }
        // 计算取到的图像相对于裁剪区域的缩放系数，以及位移
        double focusX = touchX / scale + horizontalOffset;
        double focusY = touchY / scale + verticalOffset;

        Rect cropRegion = mCurrentCaptureRequest.get(CaptureRequest.SCALER_CROP_REGION);
        if (cropRegion == null) {
            cropRegion = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            if (cropRegion == null) {
                return;
            }
        }
        LogUtil.e(cropRegion.toString());
        int cropWidth = cropRegion.width();
        int cropHeight = cropRegion.height();
        if (previewHeight * cropWidth > previewWidth * cropHeight) {
            scale = cropHeight * 1.0 / previewHeight;
            verticalOffset = 0;
            horizontalOffset = (cropWidth - scale * previewWidth) / 2;
        } else {
            scale = cropWidth * 1.0 / previewWidth;
            horizontalOffset = 0;
            verticalOffset = (cropHeight - scale * previewHeight) / 2;
        }
        // 将点击区域相对于图像的坐标，转化为相对于成像区域的坐标
        focusX = focusX * scale + horizontalOffset + cropRegion.left;
        focusY = focusY * scale + verticalOffset + cropRegion.top;

        double tapAreaRatio = 0.1;
        Rect rect = new Rect();
        rect.left = clamp((int) (focusX - tapAreaRatio / 2 * cropRegion.width()), 0, cropRegion.width());
        rect.right = clamp((int) (focusX + tapAreaRatio / 2 * cropRegion.width()), 0, cropRegion.width());
        rect.top = clamp((int) (focusY - tapAreaRatio / 2 * cropRegion.height()), 0, cropRegion.height());
        rect.bottom = clamp((int) (focusY + tapAreaRatio / 2 * cropRegion.height()), 0, cropRegion.height());

        focus(new MeteringRectangle[]{new MeteringRectangle(rect, 1000)});
    }

    public void setOnCameraStateListener(OnCameraStateListener onCameraStateListener) {
        mOnCameraStateListener = onCameraStateListener;
    }

    public boolean isSupportOIS() {
        int[] supportCount = mCameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);
        return supportCount != null && supportCount.length > 1;
    }

    public boolean isSupportVideoStabilization() {
        int[] supportCount = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES);
        return supportCount != null && supportCount.length > 1;
    }

    public interface OnCameraStateListener {
        void onCameraOpened(CameraDevice camera, boolean isOpenSuccessfully);

        void onCaptureSessionCreated(CameraDevice camera, CameraCaptureSession session, boolean isCreatedSuccessfully);

        void onCameraOperatingError(CameraAccessException exception);
    }

    public static class SimpleOnCameraStateListener implements OnCameraStateListener {

        @Override
        public void onCameraOpened(CameraDevice camera, boolean isOpenSuccessfully) {

        }

        @Override
        public void onCaptureSessionCreated(CameraDevice camera, CameraCaptureSession session, boolean isCreatedSuccessfully) {

        }

        @Override
        public void onCameraOperatingError(CameraAccessException exception) {

        }
    }

    private static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    private static int clamp(int x, int min, int max) {
        if (x > max) return max;
        if (x < min) return min;
        return x;
    }
}
