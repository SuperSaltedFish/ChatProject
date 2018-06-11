package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.AttributeSet;
import android.util.Size;
import android.view.Surface;

import com.yzx.chat.util.Camera2Helper;

import java.util.List;

/**
 * Created by YZX on 2018年06月10日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class Camera2CaptureView extends Camera2PreviewView {

    private static final String TAG = Camera2CaptureView.class.getSimpleName();

    protected static final int MAX_CAPTURE_WIDTH = 1280;
    protected static final int MAX_CAPTURE_HEIGHT = 720;
    protected static final int DEFAULT_FORMAT = ImageFormat.YUV_420_888;

    private ImageReader mImageReader;
    private OnCaptureListener mOnCaptureListener;
    private Handler mCaptureHandler;
    private Size mExpectedSize;
    private int mImageFormat;
    private int mCameraSensorOrientation;

    private boolean isCapturePrepared = true;

    public Camera2CaptureView(Context context) {
        this(context, null);
    }

    public Camera2CaptureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Camera2CaptureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mExpectedSize = new Size(MAX_CAPTURE_WIDTH, MAX_CAPTURE_HEIGHT);
        mImageFormat = DEFAULT_FORMAT;
    }

    public void setCaptureFormat(int format) {
        setCaptureFormat(MAX_CAPTURE_WIDTH, MAX_CAPTURE_HEIGHT, format);
    }

    public void setCaptureFormat(int width, int height, int format) {
        if (width == mExpectedSize.getWidth() && height == mExpectedSize.getHeight() && format == mImageFormat) {
            return;
        }
        mExpectedSize = new Size(width, height);
        mImageFormat = format;
        stopPreview();
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
        recreateCaptureSession();
    }

    private void createImageReader(int width, int height, int format) {
        if (mImageReader != null) {
            mImageReader.close();
        }
        if (mCaptureHandler == null) {
            HandlerThread handlerThread = new HandlerThread(TAG);
            handlerThread.start();
            mCaptureHandler = new Handler(handlerThread.getLooper());
        } else {
            mCaptureHandler.removeCallbacksAndMessages(null);
        }
        mImageReader = ImageReader.newInstance(width, height, format, 1);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                if (mOnCaptureListener != null) {
                    Image image = reader.acquireNextImage();
                    if (image != null) {
                        isCapturePrepared = false;
                        refreshPreview();
                        mOnCaptureListener.onCaptureSuccess(image, image.getWidth(), image.getHeight(),mCameraSensorOrientation);
                        image.close();
                        isCapturePrepared = true;
                        refreshPreview();
                    }
                }
            }
        }, mCaptureHandler);
    }

    public void setOnCaptureListener(OnCaptureListener onCaptureListener) {
        mOnCaptureListener = onCaptureListener;
        if (isPreviewing()) {
            recreateCaptureSession();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        super.onSurfaceTextureDestroyed(surface);
        if (mCaptureHandler != null) {
            mCaptureHandler.removeCallbacksAndMessages(null);
            mCaptureHandler.getLooper().quit();
        }
        if (mImageReader != null) {
            mImageReader.close();
        }
        return true;
    }

    @Override
    protected List<Surface> getAvailableSurfaces() {
        List<Surface> surfaces = super.getAvailableSurfaces();
        if (mOnCaptureListener != null && mImageReader == null) {
            Size captureSize = mCamera2Helper.chooseOptimalSize(ImageReader.class, mExpectedSize.getWidth(), mExpectedSize.getHeight(), MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, getAspectRatioSize());
            if (captureSize != null) {
                createImageReader(captureSize.getWidth(), captureSize.getHeight(), DEFAULT_FORMAT);
            }
        }
        if (surfaces != null && mImageReader != null) {
            surfaces.add(mImageReader.getSurface());
        }
        return surfaces;
    }

    @Override
    protected List<Surface> getOutPutSurfaces() {
        List<Surface> surfaces = super.getOutPutSurfaces();
        if (surfaces != null && mImageReader != null && isCapturePrepared) {
            mCameraSensorOrientation = mCamera2Helper.getCameraSensorOrientation();
            surfaces.add(mImageReader.getSurface());
        }
        return surfaces;
    }

    @Override
    protected CaptureRequest.Builder getCaptureRequestBuilder(CameraDevice device) {
        try {
            return Camera2Helper.getCaptureTypeCaptureRequestBuilder(device);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface OnCaptureListener {
        void onCaptureSuccess(@NonNull Image image, int width, int height,int imageOrientation);
    }
}
