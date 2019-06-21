package com.yzx.chat.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.ArraySet;
import android.util.Pair;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.yzx.chat.core.util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.app.ActivityCompat;

/**
 * Created by YZX on 2019年02月27日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
@SuppressWarnings("deprecation")
public abstract class BasicCamera {

    private static final String TAG = BasicCamera.class.getName();
    private static final Semaphore CAMERA_LOCK = new Semaphore(1);

    public static final int CAMERA_FACING_FRONT = 1;
    public static final int CAMERA_FACING_BACK = 2;

    @IntDef({CAMERA_FACING_FRONT, CAMERA_FACING_BACK})
    public @interface FacingType {
    }

    @Nullable
    public static BasicCamera createCameraCompat(@NonNull Context context, @FacingType int cameraFacing) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            LogUtil.w("missing 'android.permission.CAMERA' permission");
            return null;
        }
        BasicCamera camera = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            camera = createCamera2(context, cameraFacing);
        }
        if (camera == null) {
            camera = createCamera(cameraFacing);
        }
        return camera;
    }

    @Nullable
    public static CameraImpl createFrontCamera() {
        return createCamera(CAMERA_FACING_FRONT);
    }

    @Nullable
    public static CameraImpl createBackCamera() {
        return createCamera(CAMERA_FACING_BACK);
    }


    public static CameraImpl createCamera(@FacingType int lensFacingType) {
        int facing;
        switch (lensFacingType) {
            case CAMERA_FACING_FRONT:
                facing = Camera.CameraInfo.CAMERA_FACING_FRONT;
                break;
            case CAMERA_FACING_BACK:
                facing = Camera.CameraInfo.CAMERA_FACING_BACK;
                break;
            default:
                return null;
        }
        int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        try {
            for (int cameraIndex = 0; cameraIndex < cameraCount; cameraIndex++) {
                Camera.getCameraInfo(cameraIndex, info);
                if (info.facing == facing) {
                    //无论前置还是后置都用同一把锁，camera好像无法同时打开前置和后置
                    return new CameraImpl(cameraIndex, info, CAMERA_LOCK);
                }
            }
        } catch (RuntimeException e) {
            LogUtil.e(TAG, e);
        }
        return null;
    }

    @Nullable
    public static Camera2Impl createFrontCamera2(@NonNull Context context) {
        return createCamera2(context, CAMERA_FACING_FRONT);
    }

    @Nullable
    public static Camera2Impl createBackCamera2(@NonNull Context context) {
        return createCamera2(context, CAMERA_FACING_BACK);
    }

    public static Camera2Impl createCamera2(@NonNull Context context, @FacingType int lensFacingType) {
        if (!isSupportedCamera2(context, lensFacingType)) {
            return null;
        }
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        int facing;
        switch (lensFacingType) {
            case CAMERA_FACING_FRONT:
                facing = CameraCharacteristics.LENS_FACING_FRONT;
                break;
            case CAMERA_FACING_BACK:
                facing = CameraCharacteristics.LENS_FACING_BACK;
                break;
            default:
                return null;
        }
        try {
            CameraCharacteristics cameraCharacteristics;
            for (String cameraId : manager.getCameraIdList()) {
                cameraCharacteristics = manager.getCameraCharacteristics(cameraId);
                if (((Integer) facing).equals(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING))) {
                    return new Camera2Impl(cameraId, manager, cameraCharacteristics, CAMERA_LOCK);
                }
            }
        } catch (Exception e) {
            LogUtil.w(TAG, e);
            return null;
        }
        return null;
    }

    public static boolean isSupportedCamera2(@NonNull Context context, @FacingType int lensFacingType) {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) {
            return false;
        }
        int facing;
        switch (lensFacingType) {
            case CAMERA_FACING_FRONT:
                facing = CameraCharacteristics.LENS_FACING_FRONT;
                break;
            case CAMERA_FACING_BACK:
                facing = CameraCharacteristics.LENS_FACING_BACK;
                break;
            default:
                return false;
        }
        try {
            CameraCharacteristics cameraCharacteristics;
            for (String cameraId : manager.getCameraIdList()) {
                cameraCharacteristics = manager.getCameraCharacteristics(cameraId);
                Integer lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                Integer level = cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                if (lensFacing != null && lensFacing == facing && level != null) {
                    switch (level) {
                        case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                        case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                            return true;
                        case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                return true;
                            }
                            break;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public abstract void openCamera(StateCallback callback);

    public abstract void closeCamera();

    public abstract boolean isOpen();

    public abstract void starPreview();

    public abstract void stopPreview();

    public abstract boolean isPreviewing();

    public abstract void setPreviewDisplay(SurfaceHolder holder);

    public abstract void setPreviewDisplay(SurfaceTexture texture);

    public abstract void setPreviewSize(int width, int height);

    public abstract void setPreviewFormat(int format);

    public abstract void setDisplayOrientationIfSupport(int displayOrientation);

    public abstract int getSensorOrientation();

    public abstract void setEnableFlash(boolean isEnable);

    public abstract void setFocusPoint(int x, int y, int previewWidth, int previewHeight);

    public abstract void setZoom(int zoom);

    public abstract int getMinZoomValue();

    public abstract int getMaxZoomValue();

    public abstract void setRecordingHint(boolean hint);

    @WorkerThread
    public abstract void setCaptureCallback(CaptureCallback callback);

    public abstract Size calculateOptimalDisplaySize(Class outTypeClass, int expectedWidth, int expectedHeight, int maxWidth, int maxHeight, Size aspectRatio, boolean isVideoMode);


    public interface StateCallback {
        void onCameraOpen(BasicCamera camera);

        void onCameraClose();

        void onCameraError(int error);
    }

    public interface CaptureCallback {
        void onCameraCapture(byte[] data, int width, int height);

        void onCamera2Capture(Image image);
    }

    public static class CameraImpl extends BasicCamera
            implements Camera.PreviewCallback {

        private static final String TAG = CameraImpl.class.getName();

        private int mCameraID;
        private Camera mCamera;
        private Camera.CameraInfo mCameraInfo;
        private Camera.Parameters mParameters;
        private Camera.Size mPreviewSize;
        private SurfaceTexture mPreviewSurfaceTexture;
        private SurfaceHolder mPreviewSurfaceHolder;
        private boolean isPreviewing;
        private byte[] mCaptureBuffer;

        private Handler mCameraHandler;
        private Handler mUIHandler;
        private Semaphore mCameraOpenCloseLock;

        private StateCallback mStateCallback;
        private CaptureCallback mCaptureCallback;

        private CameraImpl(int cameraID, Camera.CameraInfo info, Semaphore cameraLock) {
            mCameraID = cameraID;
            mCameraInfo = info;
            mCameraOpenCloseLock = cameraLock;
            mUIHandler = new Handler(Looper.getMainLooper());
        }

        private void reset() {
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
            if (mCameraHandler != null) {
                mCameraHandler.removeCallbacksAndMessages(null);
                mCameraHandler.getLooper().quit();
            }
            mPreviewSurfaceTexture = null;
            mPreviewSurfaceHolder = null;
            mParameters = null;
            mPreviewSize = null;
            isPreviewing = false;
            mCaptureBuffer = null;
            mCaptureCallback = null;
            mStateCallback = null;
        }

        @Override
        public void openCamera(final StateCallback callback) {
            if (callback == null) {
                throw new IllegalArgumentException("callback was null");
            }
            mCameraOpenCloseLock.acquireUninterruptibly();
            if (mCamera != null) {
                mCameraOpenCloseLock.release();
                throw new RuntimeException("Camera already open");
            }
            mStateCallback = callback;
            HandlerThread handlerThread = new HandlerThread(TAG);
            handlerThread.start();
            mCameraHandler = new Handler(handlerThread.getLooper());
            mCameraHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mCamera = Camera.open(mCameraID);
                        mParameters = mCamera.getParameters();
                        mPreviewSize = mParameters.getPreviewSize();
                        List<String> supportFocusMode = mParameters.getSupportedFocusModes();
                        if (supportFocusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                            setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        } else if (supportFocusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                            setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                        } else {
                            setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        }
                        if (mParameters.isVideoStabilizationSupported()) {
                            mParameters.setVideoStabilization(true);
                        }
                        if (mParameters.isAutoWhiteBalanceLockSupported()) {
                            mParameters.setAutoWhiteBalanceLock(true);
                        }
                        mCamera.setParameters(mParameters);
                        mCamera.setErrorCallback(new Camera.ErrorCallback() {
                            @Override
                            public void onError(final int error, Camera camera) {
                                if (error == Camera.CAMERA_ERROR_EVICTED) {//相机断开连接
                                    closeCamera();
                                } else {
                                    reset();
                                    mUIHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            callback.onCameraError(error);
                                        }
                                    });
                                }
                            }
                        });
                        mUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (CameraImpl.this) {
                                    mCameraOpenCloseLock.release();
                                    callback.onCameraOpen(CameraImpl.this);
                                }
                            }
                        });
                    } catch (Exception e) {
                        LogUtil.e(TAG, e);
                        reset();
                        mCameraOpenCloseLock.release();
                        mUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (CameraImpl.this) {
                                    callback.onCameraError(-1);
                                }
                            }
                        });
                    }
                }
            });
        }

        @Override
        public void closeCamera() {
            if (mCamera != null) {
                mCameraOpenCloseLock.acquireUninterruptibly();
                if (mCamera != null) {
                    mCameraHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (CameraImpl.this) {
                                final StateCallback callback = mStateCallback;
                                reset();
                                mCameraOpenCloseLock.release();
                                if (callback != null) {
                                    mUIHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            callback.onCameraClose();
                                        }
                                    });
                                }
                            }
                        }
                    });
                } else {
                    mCameraOpenCloseLock.release();
                }
            }
        }

        @Override
        public boolean isOpen() {
            try {
                mCameraOpenCloseLock.acquireUninterruptibly();
                return mCamera != null;
            } finally {
                mCameraOpenCloseLock.release();
            }
        }

        @Override
        public void starPreview() {
            synchronized (this) {
                checkCameraOpen();
                mCameraHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if ((mPreviewSurfaceTexture != null || mPreviewSurfaceHolder != null) && !isPreviewing) {
                            try {
                                mCamera.startPreview();
                                mCamera.cancelAutoFocus();
                                mCamera.setPreviewCallbackWithBuffer(null);
                                mCamera.setPreviewCallbackWithBuffer(CameraImpl.this);
                                if (mCaptureBuffer == null) {
                                    mCaptureBuffer = new byte[((mPreviewSize.width * mPreviewSize.height) * ImageFormat.getBitsPerPixel(mParameters.getPreviewFormat())) / 8];
                                }
                                mCamera.addCallbackBuffer(mCaptureBuffer);
                                isPreviewing = true;
                            } catch (RuntimeException e) {
                                LogUtil.e(TAG, e);
                            }
                        }
                    }
                });
            }
        }

        @Override
        public void stopPreview() {
            synchronized (this) {
                checkCameraOpen();
                mCameraHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isPreviewing) {
                            stopPreviewNow();
                        }
                    }
                });
            }
        }

        private void stopPreviewNow() {
            try {
                mCamera.stopPreview();
            } catch (RuntimeException e) {
                LogUtil.e(TAG, e);
            } finally {
                isPreviewing = false;
            }
        }

        @Override
        public boolean isPreviewing() {
            return isPreviewing;
        }

        @Override
        public void setPreviewDisplay(final SurfaceHolder holder) {
            synchronized (this) {
                checkCameraOpen();
                mCameraHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            stopPreviewNow();
                            mCamera.setPreviewDisplay(holder);
                            mPreviewSurfaceHolder = holder;
                            mPreviewSurfaceTexture = null;
                        } catch (Exception e) {
                            LogUtil.d(e.toString(), e);
                        }
                    }
                });
            }
        }

        @Override
        public void setPreviewDisplay(final SurfaceTexture texture) {
            synchronized (this) {
                checkCameraOpen();
                mCameraHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            stopPreviewNow();
                            mCamera.setPreviewTexture(texture);
                            mPreviewSurfaceTexture = texture;
                            mPreviewSurfaceHolder = null;
                        } catch (Exception e) {
                            LogUtil.d(e.toString(), e);
                        }
                    }
                });
            }
        }

        @Override
        public void setPreviewSize(final int width, final int height) {
            synchronized (this) {
                checkCameraOpen();
                mCameraHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Camera.Size oldSize = mParameters.getPreviewSize();
                        if (oldSize.width == width && oldSize.height == height) {
                            return;
                        }
                        try {
                            stopPreviewNow();
                            mParameters.setPreviewSize(width, height);
                            mCamera.setParameters(mParameters);
                            mCaptureBuffer = null;
                        } catch (RuntimeException e) {
                            mParameters.setPreviewSize(oldSize.width, oldSize.height);
                            LogUtil.d(e.toString(), e);
                        } finally {
                            mPreviewSize = mParameters.getPreviewSize();
                        }
                    }
                });
            }
        }

        @Override
        public void setPreviewFormat(int format) {
            final int newFormat = format == ImageFormat.YUV_420_888 ? ImageFormat.NV21 : format;
            synchronized (this) {
                checkCameraOpen();
                mCameraHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int oldFormat = mParameters.getPreviewFormat();
                        if (oldFormat == newFormat) {
                            return;
                        }
                        try {
                            stopPreviewNow();
                            mParameters.setPreviewFormat(newFormat);
                            mCamera.setParameters(mParameters);
                            mCaptureBuffer = null;
                        } catch (RuntimeException e) {
                            mParameters.setPreviewFormat(oldFormat);
                            LogUtil.d(e.toString(), e);
                        }
                    }
                });
            }
        }

        @Override
        public void setDisplayOrientationIfSupport(final int displayOrientation) {
            synchronized (this) {
                checkCameraOpen();
                mCameraHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            stopPreviewNow();
                            mCamera.setDisplayOrientation(displayOrientation);
                        } catch (Exception e) {
                            LogUtil.d(e.toString(), e);
                        }
                    }
                });
            }
        }

        @Override
        public int getSensorOrientation() {
            return mCameraInfo.orientation;
        }

        @Override
        public void setEnableFlash(final boolean isEnable) {
            synchronized (this) {
                checkCameraOpen();
                mCameraHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String oldMode = mParameters.getFlashMode();
                        try {
                            if (isEnable) {
                                if (!Camera.Parameters.FLASH_MODE_TORCH.equals(oldMode)) {
                                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                                    mCamera.setParameters(mParameters);
                                }
                            } else {
                                if (!Camera.Parameters.FLASH_MODE_OFF.equals(oldMode)) {
                                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                                    mCamera.setParameters(mParameters);
                                }
                            }
                        } catch (RuntimeException e) {
                            mParameters.setFlashMode(oldMode);
                            LogUtil.d(e.toString(), e);
                        }
                    }
                });
            }
        }

        public void setFocusMode(final String focusMode) {
            synchronized (this) {
                checkCameraOpen();
                mCameraHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mParameters.getSupportedFocusModes().contains(focusMode)) {
                            stopPreviewNow();
                            String oldMode = mParameters.getFocusMode();
                            mParameters.setFocusMode(focusMode);
                            try {
                                mCamera.setParameters(mParameters);
                            } catch (RuntimeException e) {
                                mParameters.setFocusMode(oldMode);
                                LogUtil.d(e.toString(), e);
                            }
                        }
                    }
                });
            }
        }

        @Override
        public void setFocusPoint(final int x, final int y, final int previewWidth, final int previewHeight) {
            synchronized (this) {
                checkCameraOpen();
                mCameraHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean isSupportFocus = mParameters.getMaxNumFocusAreas() > 0;
                        boolean isSupportMetering = mParameters.getMaxNumMeteringAreas() > 0;
                        if (!isSupportFocus || !isSupportMetering) {
                            return;
                        }
                        int pointX = x * 2000 / previewWidth - 1000;
                        int pointY = y * 2000 / previewHeight - 1000;
                        Rect rect = new Rect();
                        rect.left = pointX - 100;
                        rect.right = pointX + 100;
                        rect.top = pointY - 100;
                        rect.bottom = pointY + 100;
                        if (rect.left < -1000) {
                            rect.left = -1000;
                        }
                        if (rect.right > 1000) {
                            rect.right = 1000;
                        }
                        if (rect.top < -1000) {
                            rect.top = -1000;
                        }
                        if (rect.bottom < -1000) {
                            rect.bottom = -1000;
                        }
                        Camera.Area area = new Camera.Area(rect, 1000);
                        List<Camera.Area> areaList = Collections.singletonList(area);
                        List<Camera.Area> oldMeteringAreas = mParameters.getMeteringAreas();
                        List<Camera.Area> oldFocusAreas = mParameters.getFocusAreas();
                        String oldFocusMode = mParameters.getFocusMode();
                        mParameters.setMeteringAreas(areaList);
                        mParameters.setFocusAreas(areaList);
                        mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        try {
                            mCamera.setParameters(mParameters);
                            if (isPreviewing()) {
                                mCamera.cancelAutoFocus();
                                mCamera.autoFocus(null);
                            }
                        } catch (RuntimeException e) {
                            mParameters.setMeteringAreas(oldMeteringAreas);
                            mParameters.setFocusAreas(oldFocusAreas);
                            mParameters.setFocusMode(oldFocusMode);
                            LogUtil.d(e.toString(), e);
                        }
                    }
                });
            }
        }


        @Override
        public void setZoom(int zoom) {
            zoom = Math.min(zoom, getMaxZoomValue());
            zoom = Math.max(zoom, getMinZoomValue());
            final int newZoom = zoom;
            synchronized (this) {
                checkCameraOpen();
                mCameraHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int oldZoom = mParameters.getZoom();
                        if (newZoom == oldZoom) {
                            return;
                        }
                        try {
                            if (mParameters.isZoomSupported()) {
                                mParameters.setZoom(newZoom);
                                mCamera.setParameters(mParameters);
                            }
                        } catch (RuntimeException e) {
                            mParameters.setZoom(oldZoom);
                            LogUtil.d(e.toString(), e);
                        }
                    }
                });
            }
        }

        @Override
        public int getMinZoomValue() {
            return 0;
        }

        @Override
        public int getMaxZoomValue() {
            synchronized (this) {
                checkCameraOpen();
                return Math.max(getMinZoomValue(), mParameters.getMaxZoom());
            }
        }

        @Override
        public void setRecordingHint(final boolean hint) {
            synchronized (this) {
                checkCameraOpen();
                mCameraHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mParameters.setRecordingHint(hint);
                    }
                });
            }
        }

        @Override
        public void setCaptureCallback(final CaptureCallback callback) {
            if (callback == null) {
                return;
            }
            synchronized (this) {
                checkCameraOpen();
                mCameraHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCaptureCallback = callback;
                    }
                });
            }
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mCaptureCallback != null) {
                mCaptureCallback.onCameraCapture(data, mPreviewSize.width, mPreviewSize.height);
            }
            if (mCaptureBuffer != null) {
                mCamera.addCallbackBuffer(mCaptureBuffer);
            }
        }

        @Override
        public Size calculateOptimalDisplaySize(Class outTypeClass, int expectedWidth, int expectedHeight, int maxWidth, int maxHeight, Size aspectRatio, boolean isVideoMode) {
            if (outTypeClass == SurfaceTexture.class || outTypeClass == SurfaceHolder.class || outTypeClass == Surface.class) {
                synchronized (this) {
                    checkCameraOpen();
                    List<Camera.Size> supportedSizes = isVideoMode ? mParameters.getSupportedVideoSizes() : mParameters.getSupportedPreviewSizes();
                    List<Camera.Size> bigEnough = new ArrayList<>();
                    List<Camera.Size> notBigEnough = new ArrayList<>();
                    for (Camera.Size option : supportedSizes) {
                        if (option.width <= maxWidth && option.height <= maxHeight &&
                                option.height == option.width * aspectRatio.getHeight() / aspectRatio.getWidth()) {
                            if (option.width >= expectedWidth &&
                                    option.height >= expectedHeight) {
                                bigEnough.add(option);
                            } else {
                                notBigEnough.add(option);
                            }
                        }
                    }
                    Comparator<Camera.Size> comparator = new Comparator<Camera.Size>() {
                        @Override
                        public int compare(Camera.Size lhs, Camera.Size rhs) {
                            return Long.signum((long) lhs.width * lhs.height - (long) rhs.width * rhs.height);
                        }
                    };
                    Camera.Size resultSize;
                    if (bigEnough.size() > 0) {
                        resultSize = Collections.min(bigEnough, comparator);
                    } else if (notBigEnough.size() > 0) {
                        resultSize = Collections.max(notBigEnough, comparator);
                    } else {
                        LogUtil.w("Couldn't find any suitable preview size");
                        if (isVideoMode) {
                            resultSize = mParameters.getPreferredPreviewSizeForVideo();
                        } else {
                            resultSize = supportedSizes.get(0);
                        }
                    }
                    return new Size(resultSize.width, resultSize.height);
                }
            }
            return null;
        }

        private void checkCameraOpen() {
            if (mCamera == null) {
                throw new RuntimeException("Camera is close");
            }
        }

    }


    @SuppressWarnings("unchecked")
    public static class Camera2Impl extends BasicCamera
            implements ImageReader.OnImageAvailableListener {

        private static final String TAG = Camera2Impl.class.getName();
        private static final int DEFAULT_CAPTURE_FORMAT = ImageFormat.YUV_420_888;
        private static final int DEFAULT_CAPTURE_WIDTH = 1920;
        private static final int DEFAULT_CAPTURE_HEIGHT = 1080;

        public static void setDefaultCameraMetadata(CaptureRequest.Builder builder, CameraCharacteristics characteristics) {
            builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);//3A自动
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);//开启连续自动对焦
            builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);//开启自动曝光
            builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);//开启自动白平衡

            int[] supportCount;
            supportCount = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);
            if (supportCount != null && supportCount.length > 1) {
                builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);//开启光学防抖
            } else {
                supportCount = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES);
                if (supportCount != null && supportCount.length > 1) {
                    builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);//开启视频防抖
                }
            }
            supportCount = characteristics.get(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES);
            if (supportCount != null && supportCount.length > 1) {
                builder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_FAST);//在不降低相对于传感器输出的帧率的情况下应用降噪
            }
        }

        private String mCameraID;
        private CameraManager mCameraManager;
        private CameraDevice mCameraDevice;
        private CameraCaptureSession mCameraSession;
        private CaptureRequest.Builder mCaptureRequestBuilder;
        private CameraCharacteristics mCameraCharacteristics;
        private StreamConfigurationMap mStreamConfMap;

        private volatile boolean isPreviewing;
        private boolean isEnableRecordingHint;
        private int mCaptureFormat;
        private Size mCaptureSize;

        private Set<Surface> mOtherOutputSurfaces;
        private Surface mPreviewSurface;
        private ImageReader mCaptureImageReader;

        private Semaphore mCameraOpenCloseLock;
        private Handler mCameraHandler;
        private Handler mUIHandler;

        private CaptureCallback mCaptureCallback;

        private Camera2Impl(String cameraID, CameraManager cameraManager, CameraCharacteristics cameraCharacteristics, Semaphore cameraLock) {
            mCameraID = cameraID;
            mCameraManager = cameraManager;
            mCameraCharacteristics = cameraCharacteristics;
            mStreamConfMap = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mCameraOpenCloseLock = cameraLock;
            mUIHandler = new Handler(Looper.getMainLooper());
            mOtherOutputSurfaces = new ArraySet<>();
            reset();
        }

        private void reset() {
            synchronized (this) {
                if (mCameraHandler != null) {
                    mCameraHandler.removeCallbacksAndMessages(null);
                    mCameraHandler.getLooper().quit();
                    mCameraHandler = null;
                }
                if (mCaptureImageReader != null) {
                    mCaptureImageReader.close();
                    mCaptureImageReader = null;
                }
                mCameraSession = null;
                mCameraDevice = null;
                mCaptureRequestBuilder = null;
                mPreviewSurface = null;
                mCaptureCallback = null;
                mOtherOutputSurfaces.clear();
                mCaptureFormat = DEFAULT_CAPTURE_FORMAT;
                mCaptureSize = new Size(DEFAULT_CAPTURE_WIDTH, DEFAULT_CAPTURE_HEIGHT);
                isPreviewing = false;
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void openCamera(final StateCallback callback) {
            if (callback == null) {
                throw new IllegalArgumentException("callback was null");
            }
            mCameraOpenCloseLock.acquireUninterruptibly();
            if (mCameraDevice != null) {
                mCameraOpenCloseLock.release();
                throw new IllegalStateException("The camera is already Open");
            }
            HandlerThread handlerThread = new HandlerThread(TAG);
            handlerThread.start();
            mCameraHandler = new Handler(handlerThread.getLooper());
            mCameraHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mCameraManager.openCamera(mCameraID, new CameraDevice.StateCallback() {
                            private int mErrorCode;

                            @Override
                            public void onOpened(@NonNull CameraDevice camera) {
                                try {
                                    mCaptureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                } catch (Exception e) {
                                    LogUtil.e(TAG, e);
                                    onError(camera, CameraDevice.StateCallback.ERROR_CAMERA_DEVICE);
                                    return;
                                }
                                setDefaultCameraMetadata(mCaptureRequestBuilder, mCameraCharacteristics);
                                mCameraDevice = camera;
                                mCameraOpenCloseLock.release();
                                mUIHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        synchronized (Camera2Impl.this) {
                                            callback.onCameraOpen(Camera2Impl.this);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onClosed(@NonNull CameraDevice camera) {
                                reset();
                                if (mCameraOpenCloseLock.drainPermits() == 0) {
                                    mCameraOpenCloseLock.release();
                                }
                                if (mErrorCode > 0) {
                                    mUIHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            synchronized (Camera2Impl.this) {
                                                callback.onCameraError(mErrorCode);
                                            }
                                        }
                                    });
                                } else {
                                    mUIHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            synchronized (Camera2Impl.this) {
                                                callback.onCameraClose();
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onDisconnected(@NonNull CameraDevice camera) {
                                camera.close();
                            }

                            @Override
                            public void onError(@NonNull CameraDevice camera, final int error) {
                                mErrorCode = error;
                                camera.close();
                            }
                        }, mCameraHandler);
                    } catch (Exception e) {
                        reset();
                        mCameraOpenCloseLock.release();
                        mUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (Camera2Impl.this) {
                                    callback.onCameraError(-1);
                                }
                            }
                        });
                    }
                }
            });
        }

        @Override
        public void closeCamera() {
            mCameraOpenCloseLock.acquireUninterruptibly();
            if (mCameraDevice != null) {
                mCameraHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (Camera2Impl.this) {
                            mCameraDevice.close();  //不需要调用reset，因为close之后会回调reset
                            mCameraDevice = null;
                        }
                    }
                });
            } else {
                mCameraOpenCloseLock.release();
            }
        }

        @Override
        public boolean isOpen() {
            try {
                mCameraOpenCloseLock.acquireUninterruptibly();
                return mCameraDevice != null;
            } finally {
                mCameraOpenCloseLock.release();
            }
        }

        @Override
        public void starPreview() {
            synchronized (this) {
                checkCameraIsClosed();
                mCameraHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        if (mCameraSession == null) {
                            createDefaultCaptureSession();
                        }
                        if (mCameraSession != null) {
                            starPreview(mCameraSession);
                        }
                    }
                });
            }
        }

        public void starPreview(final CameraCaptureSession session) {
            if (session == null) {
                throw new RuntimeException("CameraCaptureSession is null");
            }
            synchronized (this) {
                checkCameraIsClosed();
                mCameraHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        mCameraSession = session;
                        updateRepeatingRequest(mCaptureRequestBuilder.build(), null);
                    }
                });
            }
        }

        @Override
        public void stopPreview() {
            synchronized (this) {
                checkCameraIsClosed();
                mCameraHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        stopPreviewNow(false);
                    }
                });
            }
        }

        private void stopPreviewNow(boolean isCloseSession) {
            synchronized (this) {
                if (mCameraSession != null) {
                    if (isCloseSession) {
                        mCameraSession.close();
                        mCameraSession = null;
                    } else {
                        try {
                            mCameraSession.stopRepeating();
                        } catch (Exception e) {
                            LogUtil.e(TAG, e);
                        } finally {
                            isPreviewing = false;
                        }
                    }
                }
            }
        }

        @Override
        public boolean isPreviewing() {
            return isPreviewing;
        }

        @Override
        public void setPreviewDisplay(final SurfaceHolder holder) {
            synchronized (this) {
                checkCameraIsClosed();
                mCameraHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        stopPreviewNow(true);
                        if (holder == null) {
                            if (mPreviewSurface != null) {
                                mCaptureRequestBuilder.removeTarget(mPreviewSurface);
                                mPreviewSurface = null;
                            }
                        } else {
                            mPreviewSurface = holder.getSurface();
                            mCaptureRequestBuilder.addTarget(mPreviewSurface);
                        }
                    }
                });
            }
        }

        @Override
        public void setPreviewDisplay(final SurfaceTexture texture) {
            synchronized (this) {
                checkCameraIsClosed();
                mCameraHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        stopPreviewNow(true);
                        if (texture == null) {
                            if (mPreviewSurface != null) {
                                mCaptureRequestBuilder.removeTarget(mPreviewSurface);
                                mPreviewSurface = null;
                            }
                        } else {
                            mPreviewSurface = new Surface(texture);
                            mCaptureRequestBuilder.addTarget(mPreviewSurface);
                        }
                    }
                });
            }
        }

        @Override
        public void setPreviewSize(final int width, final int height) {
            synchronized (this) {
                checkCameraIsClosed();
                mCameraHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        if (mCaptureSize.getWidth() == width && mCaptureSize.getHeight() == height) {
                            return;
                        }
                        stopPreviewNow(true);
                        mCaptureSize = new Size(width, height);
                        if (mCaptureImageReader != null) {
                            mCaptureRequestBuilder.removeTarget(mCaptureImageReader.getSurface());
                            mCaptureImageReader.close();
                            mCaptureImageReader = null;
                        }
                    }
                });
            }
        }

        @Override
        public void setPreviewFormat(final int format) {
            synchronized (this) {
                checkCameraIsClosed();
                mCameraHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        if (mCaptureFormat == format) {
                            return;
                        }
                        stopPreviewNow(true);
                        mCaptureFormat = format;
                        if (mCaptureImageReader != null) {
                            mCaptureRequestBuilder.removeTarget(mCaptureImageReader.getSurface());
                            mCaptureImageReader.close();
                            mCaptureImageReader = null;
                        }
                    }
                });
            }
        }

        @Override
        public void setDisplayOrientationIfSupport(int displayOrientation) {
        }

        @Override
        public int getSensorOrientation() {
            Integer orientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            return orientation == null ? 0 : orientation;
        }

        @Override
        public void setEnableFlash(final boolean isEnable) {
            synchronized (this) {
                checkCameraIsClosed();
                mCameraHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        if (isEnable) {
                            mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                        } else {
                            mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                        }
                        if (isPreviewing) {
                            updateRepeatingRequest(mCaptureRequestBuilder.build(), null);
                        }
                    }
                });
            }
        }

        @Override
        public void setFocusPoint(final int x, final int y, final int previewWidth, final int previewHeight) {
            synchronized (this) {
                checkCameraIsClosed();
                Integer supportedCount = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
                if (supportedCount == null || supportedCount == 0) {
                    return;
                }
                int[] supportedArray = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
                if (supportedArray == null || supportedArray.length <= 0) {
                    return;
                }

                //预览坐标转crop坐标
                Rect rect = mCaptureRequestBuilder.get(CaptureRequest.SCALER_CROP_REGION);
                if (rect == null) {
                    rect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                    if (rect == null) {
                        return;
                    }
                }
                final Rect cropRegion = rect;
                mCameraHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        if (!isPreviewing) {
                            return;
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
                        int scaleX = (int) (x * scale + horizontalOffset);
                        int scaleY = (int) (y * scale + verticalOffset);

                        double focusAreaRatio = 0.03;
                        final Rect rect = new Rect();
                        rect.left = clamp((int) (scaleX - focusAreaRatio * cropWidth / 2), 0, (int) cropWidth);
                        rect.right = clamp((int) (scaleX + focusAreaRatio * cropWidth / 2), 0, (int) cropWidth);
                        rect.top = clamp((int) (scaleY - focusAreaRatio * cropHeight / 2), 0, (int) cropHeight);
                        rect.bottom = clamp((int) (scaleY + focusAreaRatio * cropHeight / 2), 0, (int) cropHeight);
                        rect.left += cropRegion.left;
                        rect.right += cropRegion.left;
                        rect.top += cropRegion.top;
                        rect.bottom += cropRegion.top;

                        CaptureRequest cancelFocus = buildCaptureRequest(
                                new Pair<CaptureRequest.Key, Object>(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{new MeteringRectangle(rect, 0)}),
                                new Pair<CaptureRequest.Key, Object>(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF),
                                new Pair<CaptureRequest.Key, Object>(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL),
                                new Pair<CaptureRequest.Key, Object>(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{new MeteringRectangle(rect, 0)}),
                                new Pair<CaptureRequest.Key, Object>(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_CANCEL : CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE));
                        updateSingleRequest(cancelFocus, null);

                        CaptureRequest request = buildCaptureRequest(
                                new Pair<CaptureRequest.Key, Object>(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{new MeteringRectangle(rect, MeteringRectangle.METERING_WEIGHT_MAX)}),
                                new Pair<CaptureRequest.Key, Object>(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO),
                                new Pair<CaptureRequest.Key, Object>(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START),
                                new Pair<CaptureRequest.Key, Object>(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{new MeteringRectangle(rect, MeteringRectangle.METERING_WEIGHT_MAX)}),
                                new Pair<CaptureRequest.Key, Object>(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START));
                        updateRepeatingRequest(request, null);
//                        updateRepeatingRequest(request, new CameraCaptureSession.CaptureCallback() {
//                            @Override
//                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
//                                Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
//                                if (afState == null) {
//                                    return;
//                                }
//                                switch (afState) {
//                                    case CameraMetadata.CONTROL_AF_STATE_ACTIVE_SCAN:
//                                    case CameraMetadata.CONTROL_AF_STATE_PASSIVE_SCAN:
//                                        break;
//                                    case CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED:
//                                    case CameraMetadata.CONTROL_AF_STATE_PASSIVE_FOCUSED:
//                                    case CameraMetadata.CONTROL_AF_STATE_INACTIVE:
//                                        break;
//                                    case CameraMetadata.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED:
//                                    case CameraMetadata.CONTROL_AF_STATE_PASSIVE_UNFOCUSED:
//                                        break;
//                                }
//                            }
//                        });
                    }
                });

            }
        }

        @Override
        public void setZoom(int zoom) {
            synchronized (this) {
                checkCameraIsClosed();
                zoom = Math.min(zoom, getMaxZoomValue());
                zoom = Math.max(zoom, getMinZoomValue());
                //预览坐标转crop坐标
                final Rect cameraActiveRegion = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                if (cameraActiveRegion == null) {
                    return;
                }
                final float zoomLevel = zoom / 10f;
                mCameraHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        if (!isPreviewing) {
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
                        mCaptureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, cropRect);
                        updateRepeatingRequest(mCaptureRequestBuilder.build(), null);
                    }
                });
            }
        }

        @Override
        public int getMinZoomValue() {
            return 10;
        }

        @Override
        public int getMaxZoomValue() {
            Float maxZoom = mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
            return maxZoom == null ? getMinZoomValue() : maxZoom.intValue() * 10;
        }

        @Override
        public void setRecordingHint(final boolean hint) {
            synchronized (this) {
                checkCameraIsClosed();
                mCameraHandler.post(new WorkRunnable() {
                    @Override
                    void onRun() {
                        if (hint != isEnableRecordingHint) {
                            stopPreviewNow(false);
                            try {
                                if (hint) {
                                    mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                                } else {
                                    mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                }
                                setDefaultCameraMetadata(mCaptureRequestBuilder, mCameraCharacteristics);
                                isEnableRecordingHint = hint;
                            } catch (Exception e) {
                                LogUtil.e(TAG, e);
                            }

                        }
                    }
                });
            }
        }

        @Override
        public void setCaptureCallback(final CaptureCallback callback) {
            if (callback == null) {
                return;
            }
            synchronized (this) {
                checkCameraIsClosed();
                mCameraHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCaptureCallback = callback;
                    }
                });
            }
        }

        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireNextImage();
            if (image != null) {
                if (mCaptureCallback != null) {
                    mCaptureCallback.onCamera2Capture(image);
                }
                image.close();
            }
        }

        private void createDefaultCaptureSession() {
            if (mPreviewSurface == null) {
                return;
            }
            if (mCaptureImageReader == null) {
                mCaptureImageReader = ImageReader.newInstance(mCaptureSize.getWidth(), mCaptureSize.getHeight(), mCaptureFormat, 1);
                mCaptureImageReader.setOnImageAvailableListener(Camera2Impl.this, mCameraHandler);
            }
            mCameraSession = createCaptureSessionSync(Arrays.asList(mPreviewSurface, mCaptureImageReader.getSurface()));
        }

        public void createCustomCaptureSession(final List<Surface> outSurfaces, final CreateCaptureSessionCallback callback) {
            synchronized (this) {
                checkCameraIsClosed();
                mCameraHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        mCameraSession = createCaptureSessionSync(outSurfaces);
                        if (callback != null) {
                            if (mCameraSession != null) {
                                mUIHandler.post(new WorkRunnable() {
                                    @Override
                                    void onRun() {
                                        callback.onCreatedSuccessful(mCameraSession);
                                    }
                                });
                            } else {
                                mUIHandler.post(new WorkRunnable() {
                                    @Override
                                    void onRun() {
                                        callback.onCreatedFailure();
                                    }
                                });
                            }
                        }
                    }
                });
            }
        }

        private CameraCaptureSession createCaptureSessionSync(final List<Surface> outSurfaces) {
            final CameraCaptureSession[] result = new CameraCaptureSession[1];
            final CountDownLatch latch = new CountDownLatch(1);
            try {
                mCameraDevice.createCaptureSession(outSurfaces, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull final CameraCaptureSession session) {
                        result[0] = session;
                        latch.countDown();
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        session.close();
                        latch.countDown();
                    }
                }, mUIHandler);
            } catch (Exception e) {
                LogUtil.e(TAG, e);
            }
            try {
                latch.await();
            } catch (InterruptedException ignored) {
            }
            isPreviewing = false;
            return result[0];
        }

        private void updateSingleRequest(final CaptureRequest captureRequest, final CameraCaptureSession.CaptureCallback callback) {
            try {
                mCameraSession.capture(captureRequest, callback, mCameraHandler);
            } catch (Exception e) {
                LogUtil.e(TAG, e);
                isPreviewing = false;
            }
        }

        private void updateRepeatingRequest(final CaptureRequest captureRequest, final CameraCaptureSession.CaptureCallback callback) {
            try {
                mCameraSession.stopRepeating();
                mCameraSession.setRepeatingRequest(captureRequest, callback, mCameraHandler);
            } catch (Exception e) {
                LogUtil.e(TAG, e);
                isPreviewing = false;
            }
        }

        public void addOutputTarget(final Surface surface) {
            if (surface == null) {
                return;
            }
            synchronized (this) {
                checkCameraIsClosed();
                mCameraHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        mCaptureRequestBuilder.addTarget(surface);
                    }
                });
            }
        }

        public void removeOutputTarget(final Surface surface) {
            if (surface == null) {
                return;
            }
            synchronized (this) {
                checkCameraIsClosed();
                mCameraHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        mCaptureRequestBuilder.removeTarget(surface);
                    }
                });
            }
        }

        public void setCaptureRequestField(final Pair<CaptureRequest.Key, Object>... pairs) {
            if (pairs == null || pairs.length == 0) {
                return;
            }
            synchronized (this) {
                checkCameraIsClosed();
                mCameraHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        for (Pair<CaptureRequest.Key, Object> pair : pairs) {
                            mCaptureRequestBuilder.set(pair.first, pair.second);
                        }
                    }
                });
            }
        }

        private CaptureRequest buildCaptureRequest(Pair<CaptureRequest.Key, Object>... pairs) {
            if (pairs == null || pairs.length == 0) {
                return null;
            }
            synchronized (this) {
                List<Pair<CaptureRequest.Key, Object>> temp = new ArrayList<>(pairs.length);
                for (Pair<CaptureRequest.Key, Object> pair : pairs) {
                    temp.add(new Pair<>(pair.first, mCaptureRequestBuilder.get(pair.first)));
                    mCaptureRequestBuilder.set(pair.first, pair.second);
                }
                CaptureRequest request = mCaptureRequestBuilder.build();
                for (Pair<CaptureRequest.Key, Object> pair : temp) {
                    mCaptureRequestBuilder.set(pair.first, pair.second);
                }
                return request;
            }
        }

        @Override
        public Size calculateOptimalDisplaySize(Class outTypeClass, int expectedWidth, int expectedHeight, int maxWidth, int maxHeight, Size aspectRatio, boolean isVideoMode) {
            Size[] choices = mStreamConfMap.getOutputSizes(outTypeClass);
            if (choices == null || choices.length == 0) {
                LogUtil.e("Couldn't find any supported size based on " + outTypeClass.getName());
                return null;
            }
            return calculateOptimalDisplaySize(choices, expectedWidth, expectedHeight, maxWidth, maxHeight, aspectRatio, isVideoMode);
        }

        private Size calculateOptimalDisplaySize(Size[] choices, int expectedWidth, int expectedHeight, int maxWidth, int maxHeight, Size aspectRatio, boolean isVideoMode) {
            List<Size> bigEnough = new ArrayList<>();
            List<Size> notBigEnough = new ArrayList<>();
            int w = aspectRatio.getWidth();
            int h = aspectRatio.getHeight();
            for (Size option : choices) {
                if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                        option.getHeight() == option.getWidth() * h / w) {
                    if (option.getWidth() >= expectedWidth &&
                            option.getHeight() >= expectedHeight) {
                        bigEnough.add(option);
                    } else {
                        notBigEnough.add(option);
                    }
                }
            }
            Comparator<Size> comparator = new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
                }
            };
            if (bigEnough.size() > 0) {
                return Collections.min(bigEnough, comparator);
            } else if (notBigEnough.size() > 0) {
                return Collections.max(notBigEnough, comparator);
            } else {
                LogUtil.w("Couldn't find any suitable preview size");
                return choices[0];
            }
        }

        private void checkCameraIsClosed() {
            try {
                mCameraOpenCloseLock.acquireUninterruptibly();
                if (mCameraDevice == null) {
                    throw new RuntimeException("Camera already close");
                }
            } finally {
                mCameraOpenCloseLock.release();
            }
        }

        private static int clamp(int x, int min, int max) {
            if (x > max) return max;
            if (x < min) return min;
            return x;
        }

        public interface CreateCaptureSessionCallback {
            void onCreatedSuccessful(CameraCaptureSession session);

            void onCreatedFailure();
        }

        private abstract class WorkRunnable implements Runnable {
            abstract void onRun();

            @Override
            public final void run() {
                if (mCameraDevice != null) {
                    onRun();
                }
            }
        }
    }

}
