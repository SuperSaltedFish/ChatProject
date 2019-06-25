package com.yzx.chat.widget.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Size;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;

import com.yzx.chat.util.BasicCamera;

import java.nio.ByteBuffer;

import androidx.annotation.Nullable;

/**
 * Created by YZX on 2019年03月02日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class CameraView extends TextureView
        implements TextureView.SurfaceTextureListener, BasicCamera.CaptureCallback {

    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private static final Size DEFAULT_ASPECT_RATIO = new Size(16, 9);

    private Size mAspectRatioSize = DEFAULT_ASPECT_RATIO;
    private SurfaceTexture mSurfaceTexture;
    private GestureDetector mClickGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private Handler mCaptureHandler;
    private CaptureCallback mCaptureCallback;
    private ErrorCallback mErrorCallback;
    private byte[] mCaptureBuffer;

    private BasicCamera mCamera;
    private Size mPreviewSize;
    private RectF mActualPreviewRectF;
    protected int mCameraFacingType;
    protected int mCameraOrientation;
    protected int mMaxZoom;
    protected int mMinZoom;
    protected int mCurrentZoom;
    protected boolean isEnableGesture;
    protected boolean isCaptureNext;
    protected boolean isDelayDestroy;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mCameraFacingType = BasicCamera.CAMERA_FACING_BACK;
        setSurfaceTextureListener(this);
        setClickable(true);
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
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        isDelayDestroy = false;
        mSurfaceTexture = surface;
        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        configureTransform(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        synchronized (this) {
            isDelayDestroy = true;
            if (mCaptureHandler != null) {
                mCaptureHandler.removeCallbacksAndMessages(null);
            }
            closeCamera();
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void destroy() {
        mSurfaceTexture.release();
        mSurfaceTexture = null;
        mCaptureHandler = null;
        mCaptureCallback = null;
        mErrorCallback = null;
        mCaptureBuffer = null;
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnableGesture) {
            return false;
        }
        return mClickGestureDetector.onTouchEvent(event) || mScaleGestureDetector.onTouchEvent(event);
    }


    public void startPreview() {
        if (mCamera == null) {
            if (mSurfaceTexture != null) {
                openCamera();
            }
        } else if (mCamera.isOpen()) {
            mCamera.starPreview();
        }
    }

    public void stopPreview() {
        if (mCamera != null && mCamera.isPreviewing()) {
            mCamera.stopPreview();
        }
    }

    public boolean isPreviewing() {
        if (mCamera != null) {
            return mCamera.isPreviewing();
        }
        return false;
    }

    public void setEnableGesture(boolean enableGesture) {
        isEnableGesture = enableGesture;
    }

    public void setEnableFlash(boolean isEnable) {
        if (mCamera != null && mCamera.isOpen()) {
            mCamera.setEnableFlash(isEnable);
        }
    }

    public void setAspectRatioSize(Size aspectRatioSize) {
        if (aspectRatioSize.equals(mAspectRatioSize)) {
            return;
        }
        mAspectRatioSize = aspectRatioSize;
        requestLayout();
    }

    public void setErrorCallback(ErrorCallback errorCallback) {
        mErrorCallback = errorCallback;
    }

    public RectF getActualPreviewRectF() {
        return mActualPreviewRectF;
    }

    public int getCameraFacingType() {
        return mCameraFacingType;
    }

    public void switchCamera(@BasicCamera.FacingType int facingType) {
        if (mCameraFacingType == facingType) {
            return;
        }
        mCameraFacingType = facingType;
        if (mSurfaceTexture != null) {
            closeCamera();
        }
    }

    protected BasicCamera createCamera() {
        return BasicCamera.createCameraCompat(getContext(), mCameraFacingType);
    }

    private void openCamera() {
        if (mSurfaceTexture == null) {
            return;
        }
        final BasicCamera camera = createCamera();
        if (camera != null) {
            camera.openCamera(new BasicCamera.StateCallback() {
                private int mFacingType = mCameraFacingType;

                @Override
                public void onOpenSuccessful(BasicCamera camera) {
                    if (mFacingType != mCameraFacingType) {
                        camera.closeCamera();
                        return;
                    }
                    mCamera = camera;
                    mCamera.setDisplayOrientationIfSupported(90);//下面的代码在横屏的时候无效，不知道为什么(无论前置还是后置)
//                    mCamera.setDisplayOrientationIfSupported(calculateCameraRotationAngle(getDisplayRotation(), mCamera.getSensorOrientation(), mCameraFacingType));
                    mMaxZoom = mCamera.getMaxZoomValue();
                    mMinZoom = mCamera.getMinZoomValue();
                    mCurrentZoom = mMinZoom;
                    if (mSurfaceTexture != null) {
                        mCameraOrientation = mCamera.getSensorOrientation();
                        boolean swappedDimensions = false;
                        switch (getDisplayRotation()) {
                            case Surface.ROTATION_0:
                            case Surface.ROTATION_180:
                                if (mCameraOrientation == 90 || mCameraOrientation == 270) {
                                    swappedDimensions = true;
                                }
                                break;
                            case Surface.ROTATION_90:
                            case Surface.ROTATION_270:
                                if (mCameraOrientation == 0 || mCameraOrientation == 180) {
                                    swappedDimensions = true;
                                }
                                break;
                        }
                        if (swappedDimensions) {
                            mPreviewSize = mCamera.calculateOptimalDisplaySize(SurfaceTexture.class, getHeight(), getWidth(), MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, mAspectRatioSize, false);
                        } else {
                            mPreviewSize = mCamera.calculateOptimalDisplaySize(SurfaceTexture.class, getWidth(), getHeight(), MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, mAspectRatioSize, false);
                        }
                        if (mPreviewSize != null) {
                            mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                            mCamera.setPreviewDisplay(mSurfaceTexture);
                            mCamera.setPreviewFormat(ImageFormat.YUV_420_888);
                            mCamera.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                            mCamera.setCaptureCallback(CameraView.this);
                            configureTransform(getWidth(), getHeight());
                            startPreview();
                        } else {
                            closeCamera();
                        }
                    }
                }

                @Override
                public void onOpenFailure() {
                    onClose();
                }

                @Override
                public void onDisconnected() {
                    onClose();
                }

                @Override
                public void onClose() {
                    closeCamera();
                    if (isDelayDestroy) {
                        destroy();
                    }
                    if (mSurfaceTexture != null) {
                        openCamera();
                    }
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
    }


    private void closeCamera() {
        synchronized (this) {
            if (mCamera != null) {
                mCamera.closeCamera();
                mCamera = null;
            }
            mActualPreviewRectF = null;
            mPreviewSize = null;
            mMaxZoom = 0;
            mCurrentZoom = 0;
        }
    }

    public void setCaptureCallback(CaptureCallback callback, @Nullable Handler handler) {
        synchronized (this) {
            mCaptureCallback = callback;
            if (mCaptureCallback == null) {
                mCaptureHandler = null;
            } else {
                mCaptureHandler = handler;
            }
            setEnableCapture(true);
        }
    }

    private void setEnableCapture(boolean isEnable) {
        synchronized (this) {
            isCaptureNext = isEnable;
        }
    }

    private final Runnable mCaptureCallbackRunnable = new Runnable() {
        @Override
        public void run() {
            CaptureCallback callback = mCaptureCallback;//一定要先复制，这样支持多线程操作
            Size previewSize = mPreviewSize;
            if (callback != null && previewSize != null) {
                callback.onCapture(mCaptureBuffer, previewSize.getWidth(), previewSize.getHeight(), mCameraOrientation);
            }

            setEnableCapture(true);//处理完一张之后才捕获下一张
        }
    };

    @Override
    public void onCameraCapture(byte[] data, int width, int height) {
        synchronized (this) {
            if (mCamera != null && mCaptureCallback != null && mCaptureHandler != null && isCaptureNext) {
                if (mCaptureBuffer == null || mCaptureBuffer.length != data.length) {
                    mCaptureBuffer = new byte[data.length];
                }
                System.arraycopy(data, 0, mCaptureBuffer, 0, data.length);
                mCaptureHandler.post(mCaptureCallbackRunnable);
                setEnableCapture(false);
            }
        }
    }

    @Override
    public void onCamera2Capture(Image image) {
        synchronized (this) {
            if (mCamera != null && mCaptureCallback != null && mCaptureHandler != null && isCaptureNext) {
                if (mCaptureBuffer == null) {
                    mCaptureBuffer = new byte[mPreviewSize.getWidth() * mPreviewSize.getHeight() * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8];
                }
                yuvImageToNv21(image, mCaptureBuffer);
                mCaptureHandler.post(mCaptureCallbackRunnable);
                setEnableCapture(false);
            }
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (mSurfaceTexture == null || mPreviewSize == null) {
            return;
        }
        int rotation = getDisplayRotation();

        float previewWidth = mPreviewSize.getWidth();
        float previewHeight = mPreviewSize.getHeight();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);

        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        float previewAspectRatio = mPreviewSize.getWidth() * 1.0f / mPreviewSize.getHeight();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            float viewAspectRatio = viewWidth * 1.0f / viewHeight;
            if (viewAspectRatio > previewAspectRatio) {
                mActualPreviewRectF = new RectF(0, 0, viewWidth, viewWidth * (previewHeight / previewWidth));
            } else if (viewAspectRatio < previewAspectRatio) {
                mActualPreviewRectF = new RectF(0, 0, viewHeight * (previewWidth / previewHeight), viewHeight);
            } else {
                mActualPreviewRectF = viewRect;
            }
        } else {
            float viewAspectRatio = viewHeight * 1.0f / viewWidth;
            if (viewAspectRatio > previewAspectRatio) {
                mActualPreviewRectF = new RectF(0, 0, viewHeight * (previewHeight / previewWidth), viewHeight);
            } else if (viewAspectRatio < previewAspectRatio) {
                mActualPreviewRectF = new RectF(0, 0, viewWidth, viewWidth * (previewWidth / previewHeight));
            } else {
                mActualPreviewRectF = viewRect;
            }
        }

//        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
//            RectF rotate90RectF = new RectF(0, 0, mActualPreviewRectF.height(), mActualPreviewRectF.width());
//            rotate90RectF.offset(centerX - rotate90RectF.centerX(), centerY - rotate90RectF.centerY());
//            matrix.setRectToRect(viewRect, rotate90RectF, Matrix.ScaleToFit.FILL);
//            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
//        } else {
//            mActualPreviewRectF.offset(centerX - mActualPreviewRectF.centerX(), centerY - mActualPreviewRectF.centerY());
//            matrix.setRectToRect(viewRect, mActualPreviewRectF, Matrix.ScaleToFit.FILL);
//            if (Surface.ROTATION_180 == rotation) {
//                matrix.postRotate(180, centerX, centerY);
//            }
//        }

        setTransform(matrix);
    }

    protected int getDisplayRotation() {
        Context context = getContext();
        if (context instanceof Activity) {
            return ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        } else {
            return 0;
        }
    }

    public static int calculateCameraRotationAngle(int displayRotation, int cameraRotation, int cameraFacingType) {
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

    public interface CaptureCallback {
        void onCapture(byte[] yuv, int width, int height, int orientation);
    }

    public interface ErrorCallback {
        void onCameraError();
    }
}
