package com.yzx.chat.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
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

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Size;
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

    private static final String TAG = "Camera2Helper";

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
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);//3A自动
        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);//开启自动对焦
        builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);//开启自动曝光
        builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);//开启自动白平衡
        builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);//开启光学防抖
        builder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_FAST);//在不降低相对于传感器输出的帧率的情况下应用降噪
        return builder;
    }

    public static CaptureRequest.Builder getRecodeTypeCaptureRequestBuilder(CameraDevice device) throws CameraAccessException {
        CaptureRequest.Builder builder = device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);//3A自动
        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);//开启自动对焦
        builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);//开启自动曝光
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

    private HandlerThread mCameraHandlerThread;
    private Handler mCameraHandler;
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
            mCameraHandlerThread = new HandlerThread(TAG);
            mCameraHandlerThread.start();
            mCameraHandler = new Handler(mCameraHandlerThread.getLooper());
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
            }, mCameraHandler);
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
            if (mCameraHandler != null) {
                mCameraHandler.removeCallbacksAndMessages(null);
                mCameraHandler = null;
            }
            if (mCameraHandlerThread != null) {
                mCameraHandlerThread.quit();
                mCameraHandlerThread = null;
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
            }, mCameraHandler);
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
        setupRequestBuilder(true);
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
            setupRequestBuilder(true);
        }
    }

    public void setEnableFlash(boolean isEnable) {
        if (isEnable == isEnableFlash) {
            return;
        }
        isEnableFlash = isEnable;
        if (isPreviewing) {
            setupRequestBuilder(true);
        }
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

    public float getMaxZoomValue() {
        Float maxZoom = mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
        return maxZoom == null ? 1 : maxZoom;
    }

    private void setupRequestBuilder(boolean startAfterSetup) {
        if (mCurrentCaptureRequestBuilder != null) {
            mCurrentCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, isEnableFlash ? CaptureRequest.FLASH_MODE_TORCH : CaptureRequest.FLASH_MODE_OFF);
            if (startAfterSetup) {
                startRepeatingRequest(mCurrentCaptureRequestBuilder.build(), false);
            }
        }
    }

    private void startRepeatingRequest(final CaptureRequest captureRequest, final boolean lockFocus) {
        try {
            mCurrentCaptureRequest = captureRequest;
            if (!lockFocus) {
                mCameraSession.setRepeatingRequest(mCurrentCaptureRequest, null, mCameraHandler);
            } else {
                mCameraSession.setRepeatingRequest(mCurrentCaptureRequest, new CameraCaptureSession.CaptureCallback() {
                    private Integer mCurrentAfState = -1;

                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                        Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                        if (afState == null || afState.equals(mCurrentAfState)) {
                            return;
                        }
                        mCurrentAfState = afState;
                        switch (mCurrentAfState) {
                            case CameraMetadata.CONTROL_AF_STATE_ACTIVE_SCAN:
                            case CameraMetadata.CONTROL_AF_STATE_PASSIVE_SCAN:
                                //   focusFocusing();
                                break;
                            case CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED:
                            case CameraMetadata.CONTROL_AF_STATE_PASSIVE_FOCUSED:
                                //   focusSucceed();
                                mCurrentCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                                startRepeatingRequest(mCurrentCaptureRequestBuilder.build(), false);

                                break;
                            case CameraMetadata.CONTROL_AF_STATE_INACTIVE:
                                //   focusInactive();
                                break;
                            case CameraMetadata.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED:
                            case CameraMetadata.CONTROL_AF_STATE_PASSIVE_UNFOCUSED:
                                //   focusFailed();
                                break;
                        }
                    }
                }, mCameraHandler);
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

    public void focus(int x, int y, int previewWidth, int previewHeight) {
        if (!isPreviewing) {
            return;
        }
        //预览坐标转crop坐标
        Rect cropRegion = mCurrentCaptureRequest.get(CaptureRequest.SCALER_CROP_REGION);
        if (cropRegion == null) {
            cropRegion = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            if (cropRegion == null) {
                return;
            }
        }
        double cropWidth = cropRegion.width();
        double cropHeight = cropRegion.height();
        double scale;
        double horizontalOffset = 0;
        double verticalOffset = 0;
        if ((previewWidth * 1.0 / previewHeight) > (cropWidth / cropHeight)) {
            scale = cropWidth / previewWidth;
            verticalOffset = (cropHeight - scale * previewHeight) / 2;
        } else {
            scale = cropHeight / previewHeight;
            horizontalOffset = (cropWidth - scale * previewWidth) / 2;
        }
        x = (int) (x * scale + horizontalOffset);
        y = (int) (y * scale + verticalOffset);

        double focusAreaRatio = 0.05;
        Rect rect = new Rect();
        rect.left = clamp((int) (x - focusAreaRatio / 2 * cropWidth), 0, (int) cropWidth);
        rect.right = clamp((int) (x + focusAreaRatio / 2 * cropWidth), 0, (int) cropWidth);
        rect.top = clamp((int) (y - focusAreaRatio / 2 * cropHeight), 0, (int) cropHeight);
        rect.bottom = clamp((int) (y + focusAreaRatio / 2 * cropHeight), 0, (int) cropHeight);
        rect.left += cropRegion.left;
        rect.right += cropRegion.left;
        rect.top += cropRegion.top;
        rect.bottom += cropRegion.top;

        LogUtil.e(rect.toString());

        // mCurrentCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
        mCurrentCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{new MeteringRectangle(rect, MeteringRectangle.METERING_WEIGHT_MAX - 1)});
        mCurrentCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{new MeteringRectangle(rect, MeteringRectangle.METERING_WEIGHT_MAX - 1)});
        mCurrentCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        mCurrentCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        mCurrentCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
        startRepeatingRequest(mCurrentCaptureRequestBuilder.build(), true);
    }

    public void zoom(float zoomLevel) {
        if (mCurrentCaptureRequest == null || !isPreviewing) {
            return;
        }
        float maxZoom = getMaxZoomValue();
        if (zoomLevel > maxZoom) {
            zoomLevel = maxZoom;
        }
        if (zoomLevel < 1) {
            zoomLevel = 1;
        }

        //预览坐标转crop坐标
        Rect cameraActiveRegion = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        if (cameraActiveRegion == null) {
            return;
        }
        int maxCropWidth = cameraActiveRegion.width();
        int maxCropHeight = cameraActiveRegion.height();
        int cropWidth = (int) (maxCropWidth / zoomLevel);
        int cropHeight = (int) (maxCropHeight / zoomLevel);
        Rect cropRect = new Rect();
        cropRect.left = (maxCropWidth - cropWidth) / 2;
        cropRect.right = cropRect.left + cropWidth;
        cropRect.top = (maxCropHeight - cropHeight) / 2;
        cropRect.bottom = cropRect.top + cropHeight;
        mCurrentCaptureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, cropRect);
        startRepeatingRequest(mCurrentCaptureRequestBuilder.build(), false);
    }

    public void setOnCameraStateListener(OnCameraStateListener onCameraStateListener) {
        mOnCameraStateListener = onCameraStateListener;
    }

    public boolean isPreviewing() {
        return isPreviewing;
    }

    public boolean isAllowRepeatingRequest() {
        return isAllowRepeatingRequest;
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
