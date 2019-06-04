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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            camera = createCamera2(context, cameraFacing);
//        }
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
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) {
            return null;
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
                return null;
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
                            return new Camera2Impl(cameraId, manager, cameraCharacteristics, CAMERA_LOCK);
                        case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                return new Camera2Impl(cameraId, manager, cameraCharacteristics, CAMERA_LOCK);
                            }
                            break;
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.w(TAG, e);
            return null;
        }
        return null;
    }

    public abstract void openCamera(StateCallback callback);

    public abstract void closeCamera();

    public abstract boolean isOpen();

    public abstract void starPreview();

    public abstract void stopPreview();

    public abstract boolean isPreviewing();

    public abstract boolean setPreviewDisplay(SurfaceTexture texture);

    public abstract boolean setPreviewSize(int width, int height);

    public abstract boolean setPreviewFormat(int format);

    public abstract boolean setDisplayOrientationIfSupport(int displayOrientation);

    public abstract int getSensorOrientation();

    public abstract boolean setEnableFlash(boolean isEnable);

    public abstract boolean setFocusPoint(int x, int y, int previewWidth, int previewHeight);

    public abstract boolean setZoom(int zoom);

    public abstract int getMinZoomValue();

    public abstract int getMaxZoomValue();

    @WorkerThread
    public abstract void setCaptureCallback(CaptureCallback callback);

    public abstract Size calculateOptimalDisplaySize(Class outTypeClass, int expectedWidth, int expectedHeight, int maxWidth, int maxHeight, Size aspectRatio, boolean isVideoMode);


    public interface StateCallback {
        void onCameraOpen();

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
        private boolean isPreviewing;
        private byte[] mCaptureBuffer;

        private Handler mWorkHandler;
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
            if (mWorkHandler != null) {
                mWorkHandler.removeCallbacksAndMessages(null);
                mWorkHandler.getLooper().quit();
            }
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
            mWorkHandler = new Handler(handlerThread.getLooper());
            mWorkHandler.post(new Runnable() {
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
                                    callback.onCameraOpen();
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
                    mWorkHandler.post(new Runnable() {
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
            return mCamera != null;
        }

        @Override
        public void starPreview() {
            synchronized (this) {
                checkCameraOpen();
                mWorkHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isPreviewing) {
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
                mWorkHandler.post(new Runnable() {
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
        public boolean setPreviewDisplay(SurfaceTexture texture) {
            synchronized (this) {
                checkCameraOpen();
                try {
                    stopPreviewNow();
                    mCamera.setPreviewTexture(texture);
                    return true;
                } catch (Exception e) {
                    LogUtil.d(e.toString(), e);
                    return false;
                }
            }
        }

        @Override
        public boolean setPreviewSize(int width, int height) {
            synchronized (this) {
                checkCameraOpen();
                Camera.Size oldSize = mParameters.getPreviewSize();
                if (oldSize.width == width && oldSize.height == height) {
                    return true;
                }
                try {
                    stopPreviewNow();
                    mParameters.setPreviewSize(width, height);
                    mCamera.setParameters(mParameters);
                    mCaptureBuffer = null;
                    return true;
                } catch (RuntimeException e) {
                    mParameters.setPreviewSize(oldSize.width, oldSize.height);
                    LogUtil.d(e.toString(), e);
                    return false;
                } finally {
                    mPreviewSize = mParameters.getPreviewSize();
                }
            }
        }

        @Override
        public boolean setPreviewFormat(int format) {
            if (format == ImageFormat.YUV_420_888) {
                format = ImageFormat.NV21;
            }
            synchronized (this) {
                checkCameraOpen();
                int oldFormat = mParameters.getPreviewFormat();
                if (oldFormat == format) {
                    return true;
                }
                try {
                    stopPreviewNow();
                    mParameters.setPreviewFormat(format);
                    mCamera.setParameters(mParameters);
                    mCaptureBuffer = null;
                    return true;
                } catch (RuntimeException e) {
                    mParameters.setPreviewFormat(oldFormat);
                    LogUtil.d(e.toString(), e);
                    return false;
                }
            }
        }

        @Override
        public boolean setDisplayOrientationIfSupport(int displayOrientation) {
            synchronized (this) {
                checkCameraOpen();
                try {
                    stopPreviewNow();
                    mCamera.setDisplayOrientation(displayOrientation);
                    return true;
                } catch (Exception e) {
                    LogUtil.d(e.toString(), e);
                    return false;
                }
            }
        }

        @Override
        public int getSensorOrientation() {
            return mCameraInfo.orientation;
        }

        @Override
        public boolean setEnableFlash(boolean isEnable) {
            synchronized (this) {
                checkCameraOpen();
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
                    return true;
                } catch (RuntimeException e) {
                    mParameters.setFlashMode(oldMode);
                    LogUtil.d(e.toString(), e);
                    return false;
                }
            }
        }

        public boolean setFocusMode(String focusMode) {
            synchronized (this) {
                checkCameraOpen();
                if (mParameters.getSupportedFocusModes().contains(focusMode)) {
                    stopPreviewNow();
                    String oldMode = mParameters.getFocusMode();
                    mParameters.setFocusMode(focusMode);
                    try {
                        mCamera.setParameters(mParameters);
                        return true;
                    } catch (RuntimeException e) {
                        mParameters.setFocusMode(oldMode);
                        LogUtil.d(e.toString(), e);
                        return false;
                    }
                }
                return false;
            }
        }

        @Override
        public boolean setFocusPoint(int x, int y, int previewWidth, int previewHeight) {
            synchronized (this) {
                checkCameraOpen();
                boolean isSupportFocus = mParameters.getMaxNumFocusAreas() > 0;
                boolean isSupportMetering = mParameters.getMaxNumMeteringAreas() > 0;
                if (!isSupportFocus || !isSupportMetering) {
                    return false;
                }
                x = x * 2000 / previewWidth - 1000;
                y = y * 2000 / previewHeight - 1000;
                Rect rect = new Rect();
                rect.left = x - 100;
                rect.right = x + 100;
                rect.top = y - 100;
                rect.bottom = y + 100;
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
                    return true;
                } catch (RuntimeException e) {
                    mParameters.setMeteringAreas(oldMeteringAreas);
                    mParameters.setFocusAreas(oldFocusAreas);
                    mParameters.setFocusMode(oldFocusMode);
                    LogUtil.d(e.toString(), e);
                    return false;
                }
            }
        }


        @Override
        public boolean setZoom(int zoom) {
            zoom = Math.min(zoom, getMaxZoomValue());
            zoom = Math.max(zoom, getMinZoomValue());
            synchronized (this) {
                checkCameraOpen();
                int oldZoom = mParameters.getZoom();
                if (zoom == oldZoom) {
                    return true;
                }
                try {
                    if (mParameters.isZoomSupported()) {
                        mParameters.setZoom(zoom);
                        mCamera.setParameters(mParameters);
                        return true;
                    } else {
                        return false;
                    }
                } catch (RuntimeException e) {
                    mParameters.setZoom(oldZoom);
                    LogUtil.d(e.toString(), e);
                    return false;
                }
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

        public void setRecordingHint(boolean hint) {
            synchronized (this) {
                checkCameraOpen();
                mParameters.setRecordingHint(hint);
            }
        }

        @Override
        public void setCaptureCallback(final CaptureCallback callback) {
            if (callback == null) {
                return;
            }
            synchronized (this) {
                checkCameraOpen();
                mCaptureCallback = callback;
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
        private int mCaptureFormat;
        private Size mCaptureSize;

        private Set<Surface> mOutputSurfaces;
        private Surface mPreviewSurface;
        private ImageReader mCaptureImageReader;

        private Semaphore mCameraOpenCloseLock;
        private Handler mWorkHandler;
        private Handler mUIHandler;

        private CaptureCallback mCaptureCallback;

        private Camera2Impl(String cameraID, CameraManager cameraManager, CameraCharacteristics cameraCharacteristics, Semaphore cameraLock) {
            mCameraID = cameraID;
            mCameraManager = cameraManager;
            mCameraCharacteristics = cameraCharacteristics;
            mStreamConfMap = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mCameraOpenCloseLock = cameraLock;
            mUIHandler = new Handler(Looper.getMainLooper());
            mOutputSurfaces = new ArraySet<>();
            reset();
        }

        private void reset() {
            synchronized (this) {
                if (mWorkHandler != null) {
                    mWorkHandler.removeCallbacksAndMessages(null);
                    mWorkHandler.getLooper().quit();
                    mWorkHandler = null;
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
                mOutputSurfaces.clear();
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
            mWorkHandler = new Handler(handlerThread.getLooper());
            mWorkHandler.post(new Runnable() {
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
                                            callback.onCameraOpen();
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
                        }, mWorkHandler);
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
                mWorkHandler.post(new Runnable() {
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
                mWorkHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        if (mCameraSession == null) {
                            if (mCaptureImageReader == null) {
                                mCaptureImageReader = ImageReader.newInstance(mCaptureSize.getWidth(), mCaptureSize.getHeight(), mCaptureFormat, 1);
                                mCaptureImageReader.setOnImageAvailableListener(Camera2Impl.this, mWorkHandler);
                            }
                            List<Surface> surfaces = new ArrayList<>(mOutputSurfaces.size() + 2);
                            if (mPreviewSurface != null) {
                                surfaces.add(mPreviewSurface);
                            }
                            surfaces.add(mCaptureImageReader.getSurface());
                            surfaces.addAll(mOutputSurfaces);
                            createCaptureSession(surfaces, new CreateCaptureSessionCallback() {
                                @Override
                                public void onCreatedSuccessful(CameraCaptureSession session) {
                                    if (mCameraDevice != null) {
                                        mCaptureRequestBuilder.addTarget(mCaptureImageReader.getSurface());
                                        updateRepeatingRequest(mCaptureRequestBuilder.build(), null);
                                    }
                                }

                                @Override
                                public void onCreatedFailure() {
                                    LogUtil.e("onCreatedFailure");
                                }
                            });

                        } else {
                            updateRepeatingRequest(mCaptureRequestBuilder.build(), null);
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
                mWorkHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        if (mCameraSession != session) {
                            mCameraSession.close();
                            mCameraSession = session;
                        }
                        updateRepeatingRequest(mCaptureRequestBuilder.build(), null);
                    }
                });
            }
        }

        @Override
        public void stopPreview() {
            synchronized (this) {
                checkCameraIsClosed();
                mWorkHandler.post(new WorkRunnable() {
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
                    } else if (isPreviewing) {
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
        public boolean setPreviewDisplay(final SurfaceTexture texture) {
            if (texture == null) {
                throw new RuntimeException("PreviewDisplay is null");
            }
            synchronized (this) {
                checkCameraIsClosed();
                mWorkHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        stopPreviewNow(true);
                        mPreviewSurface = new Surface(texture);
                        mCaptureRequestBuilder.addTarget(mPreviewSurface);
                    }
                });
            }
            return true;
        }

        @Override
        public boolean setPreviewSize(final int width, final int height) {
            if (mCaptureSize.getWidth() == width && mCaptureSize.getHeight() == height) {
                return true;
            }
            synchronized (this) {
                checkCameraIsClosed();
                mWorkHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
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
            return true;
        }

        @Override
        public boolean setPreviewFormat(final int format) {
            if (mCaptureFormat == format) {
                return true;
            }
            synchronized (this) {
                checkCameraIsClosed();
                mWorkHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
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
            return true;
        }

        @Override
        public boolean setDisplayOrientationIfSupport(int displayOrientation) {
            return false;
        }

        @Override
        public int getSensorOrientation() {
            Integer orientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            return orientation == null ? 0 : orientation;
        }

        @Override
        public boolean setEnableFlash(final boolean isEnable) {
            synchronized (this) {
                checkCameraIsClosed();
                mWorkHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        if (!isPreviewing) {
                            return;
                        }
                        CaptureRequest request;
                        if (isEnable) {
                            request = buildCaptureRequest(new Pair<CaptureRequest.Key, Object>(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH));
                        } else {
                            request = buildCaptureRequest(new Pair<CaptureRequest.Key, Object>(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF));
                        }
                        updateRepeatingRequest(request, null);
                    }
                });
            }
            return true;
        }

        @Override
        public boolean setFocusPoint(final int x, final int y, final int previewWidth, final int previewHeight) {
            synchronized (this) {
                checkCameraIsClosed();
                Integer supportedCount = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
                if (supportedCount == null || supportedCount == 0) {
                    return false;
                }
                int[] supportedArray = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
                if (supportedArray == null || supportedArray.length <= 0) {
                    return false;
                }

                //预览坐标转crop坐标
                Rect rect = mCaptureRequestBuilder.get(CaptureRequest.SCALER_CROP_REGION);
                if (rect == null) {
                    rect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                    if (rect == null) {
                        return false;
                    }
                }
                final Rect cropRegion = rect;
                mWorkHandler.post(new WorkRunnable() {
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
            return true;
        }

        @Override
        public boolean setZoom(int zoom) {
            synchronized (this) {
                checkCameraIsClosed();
                zoom = Math.min(zoom, getMaxZoomValue());
                zoom = Math.max(zoom, getMinZoomValue());
                //预览坐标转crop坐标
                final Rect cameraActiveRegion = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                if (cameraActiveRegion == null) {
                    return false;
                }
                final float zoomLevel = zoom / 10f;
                mWorkHandler.post(new WorkRunnable() {
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
                        CaptureRequest request = buildCaptureRequest(new Pair<CaptureRequest.Key, Object>(CaptureRequest.SCALER_CROP_REGION, cropRect));
                        updateRepeatingRequest(request, null);
                    }
                });
            }
            return true;
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
        public void setCaptureCallback(final CaptureCallback callback) {
            if (callback == null) {
                return;
            }
            synchronized (this) {
                checkCameraIsClosed();
                mCaptureCallback = callback;
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

        public void createCaptureSession(final List<Surface> outSurfaces, final CreateCaptureSessionCallback callback) {
            synchronized (this) {
                checkCameraIsClosed();
                mWorkHandler.post(new WorkRunnable() {
                    @Override
                    public void onRun() {
                        try {
                            if (mCameraSession != null) {
                                mCameraSession.close();
                                mCameraSession = null;
                            }
                            isPreviewing = false;
                            mCameraDevice.createCaptureSession(outSurfaces, new CameraCaptureSession.StateCallback() {
                                @Override
                                public void onConfigured(@NonNull final CameraCaptureSession session) {
                                    mCameraSession = session;
                                    if (callback != null) {
                                        callback.onCreatedSuccessful(session);
                                    }
                                }

                                @Override
                                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                                    session.close();
                                    if (callback != null) {
                                        callback.onCreatedFailure();
                                    }
                                }
                            }, mWorkHandler);
                        } catch (Exception e) {
                            LogUtil.e(TAG, e);
                            stopPreviewNow(true);
                            callback.onCreatedFailure();
                        }
                    }
                });
            }
        }

        private void updateSingleRequest(final CaptureRequest captureRequest, final CameraCaptureSession.CaptureCallback callback) {
            synchronized (this) {
                checkCameraIsClosed();
                if (mCameraSession == null) {
                    return;
                }
                try {
                    mCameraSession.capture(captureRequest, callback, mWorkHandler);
                    isPreviewing = true;
                } catch (Exception e) {
                    LogUtil.e(TAG, e);
                    mCameraSession.close();
                    mCameraSession = null;
                    isPreviewing = false;
                }
            }
        }

        private void updateRepeatingRequest(final CaptureRequest captureRequest, final CameraCaptureSession.CaptureCallback callback) {
            synchronized (this) {
                checkCameraIsClosed();
                if (mCameraSession == null) {
                    return;
                }
                try {
                    mCameraSession.stopRepeating();
                    mCameraSession.setRepeatingRequest(captureRequest, callback, mWorkHandler);
                    isPreviewing = true;
                } catch (Exception e) {
                    LogUtil.e(TAG, e);
                    mCameraSession.close();
                    mCameraSession = null;
                    isPreviewing = false;
                }
            }
        }

        public void addOutputTarget(final Surface surface) {
            if (surface == null) {
                return;
            }
            synchronized (this) {
                checkCameraIsClosed();
                mWorkHandler.post(new WorkRunnable() {
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
                mWorkHandler.post(new WorkRunnable() {
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
                mWorkHandler.post(new WorkRunnable() {
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
