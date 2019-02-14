package com.yzx.chat.widget.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.util.AttributeSet;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.yzx.chat.util.Camera2Helper;
import com.yzx.chat.core.util.LogUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.IntDef;

/**
 * Created by YZX on 2018年05月03日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class Camera2PreviewView extends AutoFitTextureView implements TextureView.SurfaceTextureListener, Camera2Helper.OnCameraStateListener {

    public static final int CAMERA_TYPE_FRONT = CameraCharacteristics.LENS_FACING_FRONT;
    public static final int CAMERA_TYPE_BACK = CameraCharacteristics.LENS_FACING_BACK;

    @IntDef({CAMERA_TYPE_FRONT, CAMERA_TYPE_BACK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CameraType {
    }

    protected static final int MAX_PREVIEW_WIDTH = 1920;
    protected static final int MAX_PREVIEW_HEIGHT = 1080;
    protected static final int ZOOM_MAX_TRIGGER_DISTANCE = 800;
    protected static final float ZOOM_MIN_LEVEL = 1f;

    private Context mContext;
    private OnPreviewStateListener mOnPreviewStateListener;
    private Surface mCameraOutSurface;
    private SurfaceTexture mPreviewSurfaceTexture;
    private int mPreviewSurfaceWidth;
    private int mPreviewSurfaceHeight;


    private CameraDevice mCameraDevice;
    protected Camera2Helper mCamera2Helper;
    private Size mPreviewSize;

    private int mCurrentCameraType = -1;
    private float mCameraMaxZoomLevel = ZOOM_MIN_LEVEL;
    private float mCurrentZoom = ZOOM_MIN_LEVEL;

    public Camera2PreviewView(Context context) {
        this(context, null);
    }

    public Camera2PreviewView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Camera2PreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setSurfaceTextureListener(this);
        setOnTouchListener(mOnGestureTouchListener);
        switchCamera(CAMERA_TYPE_BACK);
    }

    public void onResume() {
        recoverPreview();
    }

    public void onPause() {
        stopPreview();
    }

    public boolean isPreviewing() {
        return mCamera2Helper != null && mCamera2Helper.isPreviewing();
    }

    public void stopPreview() {
        if (isPreviewing()) {
            mCamera2Helper.stopPreview();
            callbackPreviewListener(false);
        }
    }

    public void recoverPreview() {
        if (!isPreviewing() && mCamera2Helper != null && mCamera2Helper.isAllowRepeatingRequest()) {
            mCamera2Helper.recoverPreview();
            callbackPreviewListener(true);
        }
    }

    public void switchCamera(@CameraType int cameraType) {
        if (mCurrentCameraType == cameraType) {
            return;
        }
        closeCamera();
        mCurrentCameraType = cameraType;
        switch (mCurrentCameraType) {
            case CAMERA_TYPE_BACK:
                mCamera2Helper = Camera2Helper.createBackCamera2Helper(mContext);
                break;
            case CAMERA_TYPE_FRONT:
                mCamera2Helper = Camera2Helper.createFrontCamera2Helper(mContext);
                break;
        }
        if (mCamera2Helper != null) {
            mCamera2Helper.setOnCameraStateListener(this);
            mCameraMaxZoomLevel = mCamera2Helper.getMaxZoomValue();
            if (mCameraOutSurface != null) {
                setupPreviewSize(mPreviewSurfaceWidth, mPreviewSurfaceHeight);
                configureTransform(mPreviewSurfaceWidth, mPreviewSurfaceHeight);
                mCamera2Helper.openCamera();
            }
        }
    }

    public void closeCamera() {
        if (mCamera2Helper != null) {
            if (mCamera2Helper.isPreviewing()) {
                callbackPreviewListener(false);
            }
            mCamera2Helper.closeCamera();
        }
        mCurrentCameraType = -1;
        mCameraMaxZoomLevel = ZOOM_MIN_LEVEL;
        mCurrentZoom = ZOOM_MIN_LEVEL;
        mPreviewSize = null;
        mCameraDevice = null;
    }

    public void setEnableFlash(boolean isEnable) {
        if (mCamera2Helper != null) {
            mCamera2Helper.setEnableFlash(isEnable);
        }
    }

    public void setOnPreviewStateListener(OnPreviewStateListener onPreviewStateListener) {
        mOnPreviewStateListener = onPreviewStateListener;
    }

    public Size getPreviewSize() {
        return mPreviewSize;
    }

    protected void refreshPreview() {
        if (mCamera2Helper != null && mCameraDevice != null) {
            CaptureRequest.Builder builder = getCaptureRequestBuilder(mCameraDevice);
            if (builder == null) {
                LogUtil.e("create CaptureRequest.Builder fail");
                return;
            }
            for (Surface target : getOutPutSurfaces()) {
                builder.addTarget(target);
            }
            mCamera2Helper.startPreview(builder);
        }
    }

    protected void recreateCaptureSession() {
        if (mCamera2Helper != null) {
            if (mCamera2Helper.isPreviewing()) {
                callbackPreviewListener(false);
            }
            mCamera2Helper.createCaptureSession(getAvailableSurfaces());
        }
    }

    protected List<Surface> getAvailableSurfaces() {
        if (mCameraOutSurface != null) {
            List<Surface> surfaces = new ArrayList<>();
            surfaces.add(mCameraOutSurface);
            return surfaces;
        }
        return null;
    }

    protected List<Surface> getOutPutSurfaces() {
        if (mCameraOutSurface != null) {
            List<Surface> surfaces = new ArrayList<>();
            surfaces.add(mCameraOutSurface);
            return surfaces;
        }
        return null;
    }

    protected CaptureRequest.Builder getCaptureRequestBuilder(CameraDevice device) {
        try {
            return Camera2Helper.getPreviewTypeCaptureRequestBuilder(device);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    protected int getDisplayRotation() {
        if (mContext instanceof Activity) {
            return ((Activity) mContext).getWindowManager().getDefaultDisplay().getRotation();
        } else {
            return 0;
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void setupPreviewSize(int width, int height) {
        boolean swappedDimensions = false;
        int sensorOrientation = mCamera2Helper.getCameraSensorOrientation();
        switch (getDisplayRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    swappedDimensions = true;
                }
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if (sensorOrientation == 0 || sensorOrientation == 180) {
                    swappedDimensions = true;
                }
                break;
        }
        if (swappedDimensions) {
            mPreviewSize = mCamera2Helper.chooseOptimalSize(SurfaceTexture.class, height, width, MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, mAspectRatioSize);
        } else {
            mPreviewSize = mCamera2Helper.chooseOptimalSize(SurfaceTexture.class, width, height, MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, mAspectRatioSize);
        }
        if (mPreviewSize == null) {
            return;
        }
        mPreviewSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (mCameraOutSurface == null || mPreviewSize == null) {
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
            float scale = Math.max((float) viewHeight / mPreviewSize.getHeight(), (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        setTransform(matrix);
    }

    private void callbackPreviewListener(boolean isPreviewStarted) {
        if (mOnPreviewStateListener != null) {
            if (isPreviewStarted) {
                mOnPreviewStateListener.onPreviewStarted(mCurrentCameraType);
            } else {
                mOnPreviewStateListener.onPreviewStopped(mCurrentCameraType);
            }
        }
    }

    private final OnTouchListener mOnGestureTouchListener = new OnTouchListener() {
        private final static int MAX_POINTER_COUNT = 2;
        private Point mSingleDownPoint = new Point();
        private boolean isMultiPointerMode;
        private boolean isDiscarded;
        private int mFingerSpacingWhenDown;
        private float mZoomingLevelWhenDown;
        private float mZoomingLevel;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mCamera2Helper == null || !mCamera2Helper.isPreviewing()) {
                return false;
            }
            int pointerCount = event.getPointerCount();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mSingleDownPoint.set((int) event.getX(), (int) event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    if (!isDiscarded && !isMultiPointerMode) {
                        int x = (int) event.getX();
                        int y = (int) event.getY();
                        if (getSpacing(x, y, mSingleDownPoint.x, mSingleDownPoint.y) < 50) {
                            int totalWidth;
                            int totalHeight;
                            int touchX;
                            int touchY;
                            int rotate = (mCamera2Helper.getCameraSensorOrientation() - getDisplayRotation() * 90 + 360) % 360;
                            switch (rotate) {
                                case 90:
                                    totalWidth = getHeight();
                                    totalHeight = getWidth();
                                    touchX = y;
                                    touchY = totalHeight - x;
                                    break;
                                case 270:
                                    totalWidth = getHeight();
                                    totalHeight = getWidth();
                                    touchY = x;
                                    touchX = totalWidth - y;
                                    break;
                                default:
                                    totalWidth = getWidth();
                                    totalHeight = getHeight();
                                    touchX = x;
                                    touchY = y;
                                    break;
                            }
                            mCamera2Helper.focus(touchX, touchY, totalWidth, totalHeight);
                        }
                    }
                    mCurrentZoom = mZoomingLevel;
                    isDiscarded = false;
                    isMultiPointerMode = false;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    isMultiPointerMode = true;
                    if (!isDiscarded && pointerCount == MAX_POINTER_COUNT) {
                        mFingerSpacingWhenDown = getSpacing(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                        mZoomingLevelWhenDown = mCurrentZoom;
                    } else {
                        isDiscarded = true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!isDiscarded && pointerCount == MAX_POINTER_COUNT) {
                        int fingerSpacing = getSpacing(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                        int diff = fingerSpacing - mFingerSpacingWhenDown;
                        if (Math.abs(diff) > ZOOM_MAX_TRIGGER_DISTANCE / (mCameraMaxZoomLevel * 20)) {
                            mZoomingLevel = mZoomingLevelWhenDown + diff * mCameraMaxZoomLevel / ZOOM_MAX_TRIGGER_DISTANCE;
                            if (mZoomingLevel > mCameraMaxZoomLevel) {
                                mFingerSpacingWhenDown += (mZoomingLevel - mCameraMaxZoomLevel) * ZOOM_MAX_TRIGGER_DISTANCE / mCameraMaxZoomLevel;
                                mZoomingLevel = mCameraMaxZoomLevel;
                            } else if (mZoomingLevel < ZOOM_MIN_LEVEL) {
                                mFingerSpacingWhenDown -= (ZOOM_MIN_LEVEL - mZoomingLevel) * ZOOM_MAX_TRIGGER_DISTANCE / mCameraMaxZoomLevel;
                                mZoomingLevel = ZOOM_MIN_LEVEL;
                            }
                            if (mCurrentZoom != mZoomingLevel) {
                                mCurrentZoom = mZoomingLevel;
                                mCamera2Helper.zoom(mZoomingLevel);
                            }
                        }
                    }

                    break;
                case MotionEvent.ACTION_CANCEL:
                    isDiscarded = false;
                    isMultiPointerMode = false;
                    break;
            }

            return true;
        }
    };


    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mPreviewSurfaceTexture = surface;
        mCameraOutSurface = new Surface(surface);
        mPreviewSurfaceWidth = width;
        mPreviewSurfaceHeight = height;
        if (mCamera2Helper != null) {
            setupPreviewSize(width, height);
            configureTransform(width, height);
            mCamera2Helper.openCamera();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mPreviewSurfaceWidth = width;
        mPreviewSurfaceHeight = height;
        if (mCamera2Helper != null) {
            configureTransform(width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        closeCamera();
        mPreviewSurfaceTexture = null;
        mCameraOutSurface = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onCameraOpened(CameraDevice camera, boolean isOpenSuccessfully) {
        if (isOpenSuccessfully) {
            mCameraDevice = camera;
            recreateCaptureSession();
        } else {
            LogUtil.e("CameraOpened fail");
        }
    }

    @Override
    public void onCaptureSessionCreated(CameraDevice camera, CameraCaptureSession session, boolean isCreatedSuccessfully) {
        if (isCreatedSuccessfully) {
            refreshPreview();
            callbackPreviewListener(true);
        } else {
            LogUtil.e("CaptureSessionCreated fail");
        }
    }

    @Override
    public void onCameraOperatingError(CameraAccessException exception) {
        closeCamera();
        exception.printStackTrace();
    }


    private static int getSpacing(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return (int) Math.sqrt(x * x + y * y);
    }

    public interface OnPreviewStateListener {

        void onPreviewStarted(int cameraType);

        void onPreviewStopped(int cameraType);
    }
}
