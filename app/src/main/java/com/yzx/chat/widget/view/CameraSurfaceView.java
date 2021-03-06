package com.yzx.chat.widget.view;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Size;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.util.BasicCamera;

import java.nio.ByteBuffer;

import androidx.annotation.Nullable;


/**
 * Created by YZX on 2019年06月24日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class CameraSurfaceView
        extends SurfaceView
        implements SurfaceHolder.Callback, BasicCamera.CaptureCallback {

    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private static final Size DEFAULT_ASPECT_RATIO = new Size(16, 9);

    private GestureDetector mClickGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    protected boolean isEnableGesture;


    private Size mAspectRatioSize = DEFAULT_ASPECT_RATIO;

    private BasicCamera mCamera;
    private Size mPreviewSize;
    protected int mMaxZoom;
    protected int mMinZoom;
    protected int mCurrentZoom;
    private boolean isOpenSuccessful;

    private SurfaceHolder mSurfaceHolder;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private CaptureRunnable mCaptureRunnable;
    private byte[] mCaptureBuffer;

    private ErrorCallback mErrorCallback;

    public CameraSurfaceView(Context context) {
        this(context, null);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
        mCamera = BasicCamera.createCameraCompat(context, BasicCamera.CAMERA_FACING_BACK);
        setGesture();
    }

    private void setGesture() {
        isEnableGesture = true;
        mClickGestureDetector = new GestureDetector(getContext().getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                if (mCamera == null || !mCamera.isPreviewing()) {
                    return false;
                }
                int x = (int) event.getX();
                int y = (int) event.getY();
                int totalWidth;
                int totalHeight;
                int touchX;
                int touchY;
                int rotate = (mCamera.getSensorOrientation() - getDisplayRotation() * 90 + 360) % 360;
                switch (rotate) {
                    case 0:
                        totalWidth = getWidth();
                        totalHeight = getHeight();
                        touchX = x;
                        touchY = y;
                        break;
                    case 90:
                        totalWidth = getHeight();
                        totalHeight = getWidth();
                        touchX = y;
                        touchY = totalHeight - x;
                        break;
                    case 180:
                        totalWidth = getWidth();
                        totalHeight = getHeight();
                        touchX = totalWidth - x;
                        touchY = totalHeight - y;
                        break;
                    case 270:
                        totalWidth = getHeight();
                        totalHeight = getWidth();
                        touchY = x;
                        touchX = totalWidth - y;
                        break;
                    default:
                        return false;
                }
                mCamera.setFocusPoint(touchX, touchY, totalWidth, totalHeight);
                return true;
            }

        });
        mScaleGestureDetector = new ScaleGestureDetector(getContext().getApplicationContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (mCamera == null || !mCamera.isPreviewing()) {
                    return false;
                }
                float scaleFactor = detector.getScaleFactor();
                if (scaleFactor == 1) {
                    return false;
                }
                int increase;
                if (scaleFactor > 1) {
                    increase = (int) ((scaleFactor - 1f) * (mMaxZoom - mMinZoom) + 0.5f);//四舍五入
                } else {
                    increase = (int) ((scaleFactor - 1f) * (mMaxZoom - mMinZoom) - 0.5f);//四舍五入
                }
                increase = (int) (increase * 0.5f);
                if (increase == 0) {
                    return false;
                }
                int newZoom = mCurrentZoom + increase;
                newZoom = Math.min(newZoom, mMaxZoom);
                newZoom = Math.max(newZoom, mMinZoom);
                if (newZoom == mCurrentZoom) {
                    return false;
                }
                mCurrentZoom = newZoom;
                mCamera.setZoom(mCurrentZoom);
                return true;
            }
        });
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

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnableGesture) {
            return false;
        }
        return mClickGestureDetector.onTouchEvent(event) || mScaleGestureDetector.onTouchEvent(event);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        mSurfaceHolder.setFixedSize(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT);//这里可以设置一个默认值
        openCamera();
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, int format, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        setupCameraPreviewSize();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceHolder = null;
        mSurfaceWidth = 0;
        mSurfaceHeight = 0;
        closeCamera();
    }

    private void openCamera() {
        if (mCamera == null) {
            return;
        }
        mCamera.openCamera(new BasicCamera.StateCallback() {
            @Override
            public void onOpenSuccessful(BasicCamera camera) {
                mMaxZoom = mCamera.getMaxZoomValue();
                mMinZoom = mCamera.getMinZoomValue();
                mCurrentZoom = mMinZoom;
                isOpenSuccessful = true;
                mCamera.setDisplayOrientationIfSupported(
                        calculateCameraRotationAngle(
                                getDisplayRotation(),
                                mCamera.getSensorOrientation(),
                                BasicCamera.CAMERA_FACING_BACK));
                mCamera.setCaptureCallback(CameraSurfaceView.this);
                setupCameraPreviewSize();
            }

            @Override
            public void onOpenFailure() {

            }

            @Override
            public void onDisconnected() {
                closeCamera();
            }

            @Override
            public void onClose() {

            }

            @Override
            public void onError(int error) {
                closeCamera();
                if (mErrorCallback != null) {
                    mErrorCallback.onCameraError();
                }
            }
        });
    }

    private void setupCameraPreviewSize() {
        if (isOpenSuccessful && mSurfaceHolder != null && mSurfaceHeight > 0 && mSurfaceWidth > 0) {
            int cameraOrientation = mCamera.getSensorOrientation();
            boolean swappedDimensions = false;
            switch (getDisplayRotation()) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (cameraOrientation == 90 || cameraOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (cameraOrientation == 0 || cameraOrientation == 180) {
                        swappedDimensions = true;
                    }
                    break;
            }
            if (swappedDimensions) {
                mPreviewSize = mCamera.calculateOptimalDisplaySize(SurfaceHolder.class, getHeight(), getWidth(), MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, mAspectRatioSize, false);
            } else {
                mPreviewSize = mCamera.calculateOptimalDisplaySize(SurfaceHolder.class, getWidth(), getHeight(), MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, mAspectRatioSize, false);
            }
            if (mPreviewSize != null) {
                if (mSurfaceWidth == mPreviewSize.getWidth() && mSurfaceHeight == mPreviewSize.getHeight()) {
                    mCamera.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                    mCamera.setPreviewDisplay(mSurfaceHolder);
                    mCamera.starPreview();
                } else {
                    mSurfaceHolder.setFixedSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                    mCamera.setPreviewDisplay((SurfaceHolder) null);
                }
            }
        }
    }

    private void closeCamera() {
        mCamera.closeCamera();
        mPreviewSize = null;
        isOpenSuccessful = false;
        mMaxZoom = 0;
        mMinZoom = 0;
        mCurrentZoom = 0;
    }

    public boolean isPreviewing() {
        if (mCamera != null) {
            return mCamera.isPreviewing();
        }
        return false;
    }

    public void setEnableFlash(boolean isEnable) {
        if (mCamera != null && mCamera.isOpen()) {
            mCamera.setEnableFlash(isEnable);
        }
    }

    protected int getDisplayRotation() {
        Context context = getContext();
        if (context instanceof Activity) {
            return ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        } else {
            return 0;
        }
    }

    public void setErrorCallback(ErrorCallback errorCallback) {
        mErrorCallback = errorCallback;
    }

    public void setCaptureCallback(CaptureCallback callback, @Nullable Handler handler) {
        synchronized (this) {
            if (callback == null) {
                mCaptureRunnable = null;
            } else {
                if (handler == null) {
                    handler = new Handler(Looper.getMainLooper());
                }
                mCaptureRunnable = new CaptureRunnable(callback, handler);
            }
        }
    }

    @Override
    public void onCameraCapture(byte[] data, int width, int height, int rotate) {
        synchronized (this) {
            if (mCaptureRunnable != null && mCaptureRunnable.isAlreadyProcessed) {
                if (mCaptureBuffer == null || mCaptureBuffer.length != data.length) {
                    mCaptureBuffer = new byte[data.length];
                }
                System.arraycopy(data, 0, mCaptureBuffer, 0, data.length);
                mCaptureRunnable.process(mCaptureBuffer, width, height, rotate);
            }
        }
    }

    @Override
    public void onCamera2Capture(Image image, int rotate) {
        synchronized (this) {
            if (mCaptureRunnable != null && mCaptureRunnable.isAlreadyProcessed) {
                if (mCaptureBuffer == null) {
                    mCaptureBuffer = new byte[image.getWidth() * image.getHeight() * ImageFormat.getBitsPerPixel(image.getFormat()) / 8];
                }
                yuvImageToNv21(image, mCaptureBuffer);
                mCaptureRunnable.process(mCaptureBuffer, image.getWidth(), image.getHeight(), rotate);
            }
        }
    }

    private class CaptureRunnable implements Runnable {

        CaptureCallback mCallback;
        Handler mHandler;

        int mWidth;
        int mHeight;
        int mRotate;
        byte[] mData;

        boolean isAlreadyProcessed;

        CaptureRunnable(CaptureCallback callback, Handler handler) {
            mCallback = callback;
            mHandler = handler;
            isAlreadyProcessed = true;
        }

        @Override
        public void run() {
            mCallback.onCapture(mData, mWidth, mHeight, mRotate);
            isAlreadyProcessed = true;
        }

        public void process(byte[] data, int width, int height, int rotate) {
            mData = data;
            mWidth = width;
            mHeight = height;
            mRotate = rotate;
            isAlreadyProcessed = false;
            mHandler.post(this);
        }
    }

    private static void yuvImageToNv21(Image image, byte[] dest) {
        int width = image.getWidth();
        int height = image.getHeight();
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer bufferY = planes[0].getBuffer();
        ByteBuffer bufferU = planes[1].getBuffer();
        ByteBuffer bufferV = planes[2].getBuffer();
        int rowStrideY = planes[0].getRowStride();
        int rowStrideUV = planes[1].getRowStride();
        int rowCountUV = (int) Math.ceil(bufferU.capacity() / (float) rowStrideUV);
        int pixelStrideUV = planes[1].getPixelStride();

        //处理Y分量
        for (int row = 0; row < height; row++) {
            bufferY.position(row * rowStrideY);
            bufferY.get(dest, row * width, width);
        }

        //处理UV分量
        int index = width * height;
        int columnCount = Math.min(width / 2 * pixelStrideUV, rowStrideUV);
        for (int row = 0; row < rowCountUV; row++) {
            if (row != 0) {
                bufferV.position(bufferV.position() + rowStrideUV - columnCount);
                bufferU.position(bufferU.position() + rowStrideUV - columnCount);
            }
            for (int column = 0; column < columnCount; column += pixelStrideUV) {
                if (row != 0 || column != 0) {
                    bufferV.position(bufferV.position() + 1);
                    bufferU.position(bufferU.position() + 1);
                }
                dest[index++] = bufferV.get();
                dest[index++] = bufferU.get();
            }
        }
    }

    private static int calculateCameraRotationAngle(int displayRotation, int cameraRotation, int cameraFacingType) {
        int degrees = 0;
        switch (displayRotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (cameraFacingType == BasicCamera.CAMERA_FACING_FRONT) {
            result = (cameraRotation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (cameraRotation - degrees + 360) % 360;
        }
        return result;
    }

    public interface CaptureCallback {
        void onCapture(byte[] yuv, int width, int height, int rotate);
    }

    public interface ErrorCallback {
        void onCameraError();
    }
}
