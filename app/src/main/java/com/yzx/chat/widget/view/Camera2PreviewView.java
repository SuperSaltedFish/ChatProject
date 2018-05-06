package com.yzx.chat.widget.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.util.AttributeSet;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;

import com.yzx.chat.util.Camera2Helper;
import com.yzx.chat.util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by YZX on 2018年05月03日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class Camera2PreviewView extends TextureView implements TextureView.SurfaceTextureListener, Camera2Helper.OnCameraStateListener {

    protected static final int MAX_PREVIEW_WIDTH = 1920;
    protected static final int MAX_PREVIEW_HEIGHT = 1080;
    private static final int FOCUS_AREA_SIZE = 200;
    private static final Size DEFAULT_ASPECT_RATIO = new Size(16, 9);

    private Context mContext;
    private Surface mCameraOutSurface;
    protected Camera2Helper mCamera2Helper;
    private CameraDevice mCameraDevice;

    private Size mAspectRatioSize;
    private Size mPreviewSize;
    private Rect mCameraActiveArrayRect;

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
        mAspectRatioSize = DEFAULT_ASPECT_RATIO;
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mCamera2Helper == null || !mCamera2Helper.isPreviewing() || mCameraActiveArrayRect == null) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                int cameraActiveWidth = mCameraActiveArrayRect.width();
//                int cameraActiveHeight = mCameraActiveArrayRect.height();
//                int viewWidth = getWidth();
//                int viewHeight = getHeight();
//                int rotation = getDisplayRotation();
//                int centerX;
//                int centerY;
//                if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
//                    centerX = (int) event.getX() * cameraActiveWidth / viewWidth;
//                    centerY = (int) event.getY() * cameraActiveHeight / viewHeight;
//                } else {
//                    centerX = (int) event.getY() * cameraActiveWidth / viewHeight;
//                    centerY = (int) event.getX() * cameraActiveHeight / viewWidth;
//                }
//                int focusLeft = centerX + FOCUS_AREA_SIZE > cameraActiveWidth ? cameraActiveWidth - FOCUS_AREA_SIZE : centerX + FOCUS_AREA_SIZE;
//                int focusTop = centerY + FOCUS_AREA_SIZE > cameraActiveHeight ? cameraActiveHeight - FOCUS_AREA_SIZE : centerY + FOCUS_AREA_SIZE;
//                int focusRight = focusLeft + FOCUS_AREA_SIZE;
//                int focusBottom = focusTop + FOCUS_AREA_SIZE;
//                Rect focusRect = new Rect(focusLeft, focusTop, focusRight, focusBottom);
                int totalWidth;
                int totalHeight;
                int touchX;
                int touchY;
                int rotate = (mCamera2Helper.getCameraSensorOrientation() - getDisplayRotation() * 90 + 360) % 360;
                if (rotate == 90) {
                    totalWidth = getHeight();
                    totalHeight = getWidth();
                    touchX = (int) event.getY();
                    touchY = (int) (totalHeight - event.getX());
                } else if (rotate == 270) {
                    totalWidth = getHeight();
                    totalHeight = getWidth();
                    touchY = (int) event.getX();
                    touchX = (int) (totalWidth - event.getY());
                } else {
                    totalWidth = getWidth();
                    totalHeight = getHeight();
                    touchX = (int) event.getX();
                    touchY = (int) event.getY();
                }
                mCamera2Helper.focusOnTouch(touchX, touchY, totalWidth, totalHeight, mPreviewSize.getWidth(), mPreviewSize.getHeight());
                break;
        }
        return true;
    }

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
        mCameraActiveArrayRect = mCamera2Helper.getCameraCharacteristics().get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        surface.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        mCameraOutSurface = new Surface(surface);
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
        mCamera2Helper = Camera2Helper.createBackCamera2Helper(mContext);
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
}
