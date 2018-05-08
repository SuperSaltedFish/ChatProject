package com.yzx.chat.widget.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.util.AttributeSet;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.yzx.chat.util.Camera2Helper;
import com.yzx.chat.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年05月03日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class Camera2PreviewView extends TextureView implements TextureView.SurfaceTextureListener, Camera2Helper.OnCameraStateListener {

    protected static final int MAX_PREVIEW_WIDTH = 1920;
    protected static final int MAX_PREVIEW_HEIGHT = 1080;
    protected static final int ZOOM_MAX_TRIGGER_DISTANCE = 800;
    protected static final float ZOOM_MIN_LEVEL = 1f;
    private static final Size DEFAULT_ASPECT_RATIO = new Size(16, 9);

    private Context mContext;
    private Surface mCameraOutSurface;
    protected Camera2Helper mCamera2Helper;
    private CameraDevice mCameraDevice;

    private Size mAspectRatioSize;
    private Size mPreviewSize;
    private float mCameraMaxZoomLevel;
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
        mCamera2Helper = Camera2Helper.createBackCamera2Helper(mContext);
        mAspectRatioSize = DEFAULT_ASPECT_RATIO;
        setSurfaceTextureListener(this);
        setOnTouchListener(mOnGestureTouchListener);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int ratioW;
        int ratioH;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ratioW = mAspectRatioSize.getWidth();
            ratioH = mAspectRatioSize.getHeight();
        } else {
            ratioW = mAspectRatioSize.getHeight();
            ratioH = mAspectRatioSize.getWidth();
        }
        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            height = width * ratioH / ratioW;
        } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            width = height * ratioW / ratioH;
        }
        setMeasuredDimension(width, height);
    }

    public void onResume() {
        if (mCamera2Helper != null && mCamera2Helper.isAllowRepeatingRequest() && !mCamera2Helper.isPreviewing()) {
            mCamera2Helper.recoverPreview();
        }
    }

    public void onPause() {
        if (mCamera2Helper != null && mCamera2Helper.isPreviewing()) {
            mCamera2Helper.stopPreview();
        }
    }

    public void reopenCamera() {
        if (mCamera2Helper != null) {
            closeCamera();
            mCamera2Helper.openCamera();
        }
    }

    public void closeCamera() {
        if (mCamera2Helper != null) {
            mCamera2Helper.closeCamera();
        }
        mCameraDevice = null;
    }

    public void setAspectRatioSize(Size aspectRatioSize) {
        if (aspectRatioSize.equals(mAspectRatioSize)) {
            return;
        }
        mAspectRatioSize = aspectRatioSize;
    }

    public Size getAspectRatioSize() {
        return mAspectRatioSize;
    }

    protected void refreshPreview() {
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

    protected int getDisplayRotation() {
        if (mContext instanceof Activity) {
            return ((Activity) mContext).getWindowManager().getDefaultDisplay().getRotation();
        } else {
            return 0;
        }
    }


    private void setupPreview(SurfaceTexture surface, int width, int height) {
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
        surface.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        mCameraOutSurface = new Surface(surface);
        mCameraMaxZoomLevel = mCamera2Helper.getMaxZoomValue();
        configureTransform(width, height);
        mCamera2Helper.setOnCameraStateListener(this);
        mCamera2Helper.openCamera();
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (mCameraOutSurface == null) {
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


    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mCamera2Helper != null) {
            setupPreview(surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (mCamera2Helper != null) {
            configureTransform(width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        closeCamera();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onCameraOpened(CameraDevice camera, boolean isOpenSuccessfully) {
        if (isOpenSuccessfully) {
            mCameraDevice = camera;
            mCamera2Helper.createCaptureSession(getAvailableSurfaces());
        } else {
            LogUtil.e("CameraOpened fail");
        }
    }

    @Override
    public void onCaptureSessionCreated(CameraDevice camera, CameraCaptureSession session, boolean isCreatedSuccessfully) {
        if (isCreatedSuccessfully) {
            refreshPreview();
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
}
