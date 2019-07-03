package com.yzx.chat.widget.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.media.MediaFormat;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.util.BasicCamera;
import com.yzx.chat.util.VideoEncoder;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.CountDownLatch;

import javax.microedition.khronos.opengles.GL10;

import androidx.annotation.Nullable;

/**
 * Created by YZX on 2019年07月02日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class RecordView extends SurfaceView implements SurfaceHolder.Callback {

    private static final Size DEFAULT_ASPECT_RATIO = new Size(16, 9);
    private static final int MAX_SIZE = 1920;

    private final float[] mTextureMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private static final float[] IDENTITY_MATRIX;

    static {
        IDENTITY_MATRIX = new float[16];
        Matrix.setIdentityM(IDENTITY_MATRIX, 0);
    }

    private EglHelper mEglHelper;
    private SurfaceTexture mPreviewSurfaceTexture;
    private int mOESTextureID;


    private CameraHelper mCameraHelper;
    private Size mAspectRatioSize;
    private int mCameraFacing;
    private boolean isCamera2Mode;

    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private int mDisplayRotation;
    private Display mScreenDisplay;

    private GLVideoEncoder mGLVideoEncoder;
    private volatile boolean isStartRecording;
    private int mMaxVideoWidth = 960;
    private int mMaxVideoHeight = 960;
    private int mMaxFrameRate = 30;

    public RecordView(Context context) {
        this(context, null);
    }

    public RecordView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);

        mCameraHelper = new CameraHelper();
        mCameraFacing = BasicCamera.CAMERA_FACING_BACK;
        mAspectRatioSize = DEFAULT_ASPECT_RATIO;

        mScreenDisplay = ((Activity) context).getWindowManager().getDefaultDisplay();
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

        float scale;
        if (MAX_SIZE >= Math.max(width, height)) {
            scale = 1;
        } else {
            scale = (float) MAX_SIZE / Math.max(width, height);
        }
        getHolder().setFixedSize(Math.round(width * scale), Math.round(height * scale));
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mEglHelper = EglHelper.create(holder.getSurface(), null, false);
        if (mEglHelper != null) {
            mCameraHelper.sendEvent(CameraHelper.MSG_OPEN_CAMERA, getContext(), mCameraFacing);
            mCameraHelper.sendEvent(CameraHelper.MSG_ENABLE_AUTO_START_PREVIEW);
            mEglHelper.postGLRunnable(new Runnable() {
                @Override
                public void run() {
                    mGLVideoEncoder = GLVideoEncoder.create(EGL14.eglGetCurrentContext());
                    mOESTextureID = EglHelper.Texture2dProgram.createOESTextureObject();
                    mPreviewSurfaceTexture = new SurfaceTexture(mOESTextureID);
                    mPreviewSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                        @Override
                        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                            //每获取到一帧数据时请求OpenGL ES进行渲染
                            onDrawFrame();
                        }
                    });
                }
            });
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, final int width, final int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        if (mEglHelper != null) {
            mEglHelper.postGLRunnable(new Runnable() {
                @Override
                public void run() {
                    mCameraHelper.sendEvent(CameraHelper.MSG_SET_DESIRED_PARAMETER,
                            mPreviewSurfaceTexture,
                            width,
                            height,
                            mScreenDisplay.getRotation(),
                            mAspectRatioSize,
                            mMaxFrameRate);
                }
            });
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mEglHelper != null) {
            mCameraHelper.sendEvent(CameraHelper.MSG_CLOSE_CAMERA);
            mCameraHelper.sendEvent(CameraHelper.MSG_SET_DESIRED_PARAMETER, null, -1, -1, -1, null, 0);
            mGLVideoEncoder.release();
            mEglHelper.postGLRunnable(new Runnable() {
                @Override
                public void run() {
                    if (mPreviewSurfaceTexture != null) {
                        mPreviewSurfaceTexture.setOnFrameAvailableListener(null);
                        mPreviewSurfaceTexture.release();
                        mPreviewSurfaceTexture = null;
                    }
                    if (mOESTextureID > -1) {
                        EglHelper.Texture2dProgram.deleteOESTextureObject(mOESTextureID);
                    }
                    mOESTextureID = -1;
                }
            });
            mEglHelper.releaseEGL();
            mEglHelper = null;
        }
    }

    private void onDrawFrame() {
        configureTransformIfNeed(mSurfaceWidth, mSurfaceHeight, mScreenDisplay.getRotation(), mCameraHelper.isCamera2Mode());
        mPreviewSurfaceTexture.updateTexImage();
        //获取外部纹理的矩阵，用来确定纹理的采样位置，没有此矩阵可能导致图像翻转等问题
        mPreviewSurfaceTexture.getTransformMatrix(mTextureMatrix);//这个代码不需要了，变化在ProjectionMatrix做了
        mEglHelper.drawOESTexture(mOESTextureID, mTextureMatrix, mProjectionMatrix);
        if (isStartRecording) {
            mGLVideoEncoder.encode(mOESTextureID, mTextureMatrix, mProjectionMatrix);
        }
    }

    private void configureTransformIfNeed(int width, int height, int displayRotation, boolean isCamera2) {
        if (width == mSurfaceWidth && height == mSurfaceHeight && displayRotation == mDisplayRotation && isCamera2Mode == isCamera2) {
            return;
        }
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        mDisplayRotation = displayRotation;
        isCamera2Mode = isCamera2;

        float[] projectionMatrix = new float[16];
        Matrix.setIdentityM(projectionMatrix, 0);
        Matrix.orthoM(projectionMatrix, 0, -1, 1, -1, 1, -1, 1);

        float[] transformMatrix = new float[16];
        Matrix.setIdentityM(transformMatrix, 0);

        float previewAspectRatio = (float) mAspectRatioSize.getWidth() / mAspectRatioSize.getHeight();
        if (Surface.ROTATION_0 == displayRotation || Surface.ROTATION_180 == displayRotation) {
            float viewAspectRatio = (float) mSurfaceHeight / mSurfaceWidth;
            if (viewAspectRatio >= previewAspectRatio) {
                Matrix.scaleM(transformMatrix, 0, viewAspectRatio / previewAspectRatio, 1, 1f);
            } else {
                Matrix.scaleM(transformMatrix, 0, 1, previewAspectRatio / viewAspectRatio, 1f);
            }
        } else {
            float viewAspectRatio = (float) mSurfaceWidth / mSurfaceHeight;
            if (viewAspectRatio >= previewAspectRatio) {
                Matrix.scaleM(transformMatrix, 0, 1, viewAspectRatio / previewAspectRatio, 1f);
            } else {
                Matrix.scaleM(transformMatrix, 0, previewAspectRatio / viewAspectRatio, 1, 1f);
            }
        }
        if (isCamera2) {
            switch (displayRotation) {
                case Surface.ROTATION_90:
                    Matrix.rotateM(transformMatrix, 0, 90, 0.0f, 0.0f, 1.0f);
                    break;
                case Surface.ROTATION_180:
                    break;
                case Surface.ROTATION_270:
                    Matrix.rotateM(transformMatrix, 0, -90, 0.0f, 0.0f, 1.0f);
                    break;
            }
        }
        Matrix.multiplyMM(mProjectionMatrix, 0, projectionMatrix, 0, transformMatrix, 0);
    }

    public boolean startRecode(String saveFile) {
        if (mGLVideoEncoder != null) {
            int videoWidth;
            int videoHeight;
            int displayRotation = mScreenDisplay.getRotation();
            if (displayRotation == Surface.ROTATION_0 || displayRotation == Surface.ROTATION_180) {
                float scaleW = (float) mMaxVideoWidth / mSurfaceHeight;
                float scaleH = (float) mMaxVideoHeight / mSurfaceWidth;
                float scale = Math.min(scaleW, scaleH);
                videoWidth = Math.round(scale * mSurfaceWidth);
                videoHeight = Math.round(scale * mSurfaceHeight);
            } else {
                float scaleW = (float) mMaxVideoWidth / mSurfaceWidth;
                float scaleH = (float) mMaxVideoHeight / mSurfaceHeight;
                float scale = Math.min(scaleW, scaleH);
                videoWidth = Math.round(scale * mSurfaceWidth);
                videoHeight = Math.round(scale * mSurfaceHeight);
            }
            isStartRecording = mGLVideoEncoder.startRecode(videoWidth, videoHeight, mMaxFrameRate, saveFile);
            return isStartRecording;
        } else {
            return false;
        }
    }

    public void stopRecode() {
        if (mGLVideoEncoder != null) {
            mGLVideoEncoder.stopRecode();
            isStartRecording = false;
        }
    }

    public void setEnableFlash(boolean isEnable) {
        if (isEnable) {
            mCameraHelper.sendEvent(CameraHelper.MSG_OPEN_FLASH);
        } else {
            mCameraHelper.sendEvent(CameraHelper.MSG_CLOSE_FLASH);
        }
    }

    public void switchCamera(@BasicCamera.FacingType int cameraFacingType) {
        if (mCameraFacing == cameraFacingType) {
            return;
        }
        mCameraFacing = cameraFacingType;
        if (mEglHelper != null) {
            mCameraHelper.sendEvent(CameraHelper.MSG_CLOSE_CAMERA);
            mCameraHelper.sendEvent(CameraHelper.MSG_OPEN_CAMERA, getContext(), mCameraFacing);
            mCameraHelper.sendEvent(CameraHelper.MSG_ENABLE_AUTO_START_PREVIEW);
        }
    }

    protected static class CameraHelper {

        private static final String TAG = CameraHelper.class.getName();

        private static final int MAX_PREVIEW_WIDTH = 1920;
        private static final int MAX_PREVIEW_HEIGHT = 1080;

        public static final int MSG_OPEN_CAMERA = 1;
        public static final int MSG_CLOSE_CAMERA = 2;
        public static final int MSG_ENABLE_AUTO_START_PREVIEW = 3;
        public static final int MSG_STOP_PREVIEW = 4;
        public static final int MSG_OPEN_FLASH = 5;
        public static final int MSG_CLOSE_FLASH = 6;
        public static final int MSG_SET_DESIRED_PARAMETER = 7;
        public static final int MSG_SET_FOCUS = 8;
        public static final int MSG_SET_ZOOM = 9;
        public static final int MSG_RELEASE = 10;

        private CameraHandler mCameraHandler;

        private BasicCamera mCamera;
        private Size mPreviewSize;
        private int mCameraFacingType;
        private int mMaxZoom;
        private int mMinZoom;
        private int mCurrentZoom;
        private boolean isEnableAutoStartPreview;

        private CountDownLatch mCameraCloseLatch;

        private SurfaceTexture mPreviewSurfaceTexture;
        private int mDisplayOrientation = -1;
        private int mDesiredWidth = 0;
        private int mDesiredHeight = 0;
        private Size mAspectRatioSize = null;
        private int mMaxFPS = 0;

        public CameraHelper() {
            HandlerThread thread = new HandlerThread(TAG);
            thread.start();
            mCameraHandler = new CameraHandler(this, thread.getLooper());
        }

        private void openCamera(Context context, @BasicCamera.FacingType final int cameraFacing) {
            if (mCamera != null) {
                throw new RuntimeException("Camera already open");
            }
            BasicCamera camera = BasicCamera.createCameraCompat(context, cameraFacing);
            if (camera != null) {
                final CountDownLatch latch = new CountDownLatch(1);
                camera.openCamera(new BasicCamera.StateCallback() {
                    @Override
                    public void onOpenSuccessful(BasicCamera camera) {
                        mCamera = camera;
                        mCameraFacingType = cameraFacing;
                        mMaxZoom = mCamera.getMaxZoomValue();
                        mMinZoom = mCamera.getMinZoomValue();
                        mCurrentZoom = mMinZoom;
                        setupCamera();
                        mCameraCloseLatch = new CountDownLatch(1);
                        latch.countDown();
                    }

                    @Override
                    public void onOpenFailure() {
                        latch.countDown();
                    }

                    @Override
                    public void onDisconnected() {
                        closeCamera();
                    }

                    @Override
                    public void onClose() {
                        mCameraCloseLatch.countDown();
                    }

                    @Override
                    public void onError(int error) {
                        closeCamera();
                    }
                });
                try {
                    latch.await();
                } catch (InterruptedException ignored) {
                }
            }
        }


        private void closeCamera() {
            if (mCamera != null) {
                mCamera.closeCamera();
                mCamera = null;
                mMaxZoom = 0;
                mCurrentZoom = 0;
                mPreviewSize = null;
                mCameraFacingType = -1;
                try {
                    mCameraCloseLatch.await();
                } catch (InterruptedException ignored) {
                }
            }
        }

        private void release() {
            closeCamera();
            mCameraHandler.removeCallbacksAndMessages(null);
            mCameraHandler.getLooper().quit();
            mCameraHandler = null;
        }

        private void setupCamera() {
            if (mCamera == null) {
                return;
            }
            if (mPreviewSurfaceTexture == null) {
                mCamera.setPreviewDisplay((SurfaceTexture) null);
                return;
            }
            if (mDesiredWidth <= 0 || mDesiredHeight <= 0 || mAspectRatioSize == null) {

                return;
            }
            int cameraOrientation = mCamera.getSensorOrientation();
            boolean swappedDimensions = false;
            switch (mDisplayOrientation) {
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
                default:
                    return;
            }
            if (swappedDimensions) {
                mPreviewSize = mCamera.calculateOptimalDisplaySize(SurfaceTexture.class, mDesiredHeight, mDesiredWidth, MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, mAspectRatioSize, true);
            } else {
                mPreviewSize = mCamera.calculateOptimalDisplaySize(SurfaceTexture.class, mDesiredWidth, mDesiredHeight, MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, mAspectRatioSize, true);
            }
            if (mPreviewSize == null) {
                return;
            }
            mCamera.setRecordingHint(true);
            mCamera.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            if (mMaxFPS > 0) {
                mCamera.setPreviewFpsIfSupported(mMaxFPS, mMaxFPS);
            }
            mCamera.setDisplayOrientationIfSupported(calculateCameraRotationAngle(mDisplayOrientation, mCamera.getSensorOrientation(), mCameraFacingType));
            mPreviewSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mCamera.setPreviewDisplay(mPreviewSurfaceTexture);
            if (isEnableAutoStartPreview) {
                startPreviewIfNeed();
            }
        }

        private void setDesiredCameraParameter(SurfaceTexture surfaceTexture, int desiredWidth, int desiredHeight, int displayRotation, Size aspectRatioSize, int maxFPS) {
            mPreviewSurfaceTexture = surfaceTexture;
            mDesiredWidth = desiredWidth;
            mDesiredHeight = desiredHeight;
            mDisplayOrientation = displayRotation;
            mAspectRatioSize = aspectRatioSize;
            mMaxFPS = maxFPS;
            setupCamera();
        }

        private void enableAutoStartPreview() {
            isEnableAutoStartPreview = true;
            startPreviewIfNeed();
        }

        private void startPreviewIfNeed() {
            if (mCamera != null && mCamera.isOpen() && isEnableAutoStartPreview && mPreviewSurfaceTexture != null && mPreviewSize != null) {
                mCamera.starPreview();
            }
        }

        private void stopPreview() {
            isEnableAutoStartPreview = false;
            if (mCamera != null && mCamera.isPreviewing()) {
                mCamera.stopPreview();
            }
        }

        private void setEnableFlash(boolean isEnable) {
            if (mCamera != null && mCamera.isOpen()) {
                mCamera.setEnableFlash(isEnable);
            }
        }

        private void focus(int x, int y, int width, int height, int displayOrientation) {
            if (mCamera == null || !mCamera.isPreviewing()) {
                return;
            }
            int totalWidth;
            int totalHeight;
            int touchX;
            int touchY;
            int rotate = (mCamera.getSensorOrientation() - displayOrientation * 90 + 360) % 360;
            switch (rotate) {
                case 0:
                    totalWidth = width;
                    totalHeight = height;
                    touchX = x;
                    touchY = y;
                    break;
                case 90:
                    totalWidth = height;
                    totalHeight = width;
                    touchX = y;
                    touchY = totalHeight - x;
                    break;
                case 180:
                    totalWidth = width;
                    totalHeight = height;
                    touchX = totalWidth - x;
                    touchY = totalHeight - y;
                    break;
                case 270:
                    totalWidth = height;
                    totalHeight = width;
                    touchY = x;
                    touchX = totalWidth - y;
                    break;
                default:
                    return;
            }
            mCamera.setFocusPoint(touchX, touchY, totalWidth, totalHeight);
        }

        private void zoom(int value) {
            if (mCamera == null || !mCamera.isPreviewing()) {
                return;
            }
            value = Math.min(value, mMaxZoom);
            value = Math.max(value, mMinZoom);
            if (value == mCurrentZoom) {
                return;
            }
            mCurrentZoom = value;
            mCamera.setZoom(mCurrentZoom);
        }

        public boolean isCamera2Mode() {
            return mCamera instanceof BasicCamera.Camera2Impl;
        }

        public int getMaxZoom() {
            return mMaxZoom;
        }

        public int getMinZoom() {
            return mMinZoom;
        }

        public int getCurrentZoom() {
            return mCurrentZoom;
        }

        public void sendEvent(int msgType, Object... params) {
            if (mCameraHandler == null) {
                throw new RuntimeException("CameraHelper already released.");
            }
            mCameraHandler.removeMessages(msgType);
            mCameraHandler.sendMessage(msgType, params);
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


        private static class CameraHandler extends Handler {
            private WeakReference<CameraHelper> mCameraWeakReference;

            private CameraHandler(CameraHelper cameraHelper, Looper looper) {
                super(looper);
                mCameraWeakReference = new WeakReference<>(cameraHelper);
            }

            @Override
            public void handleMessage(Message msg) {
                CameraHelper helper = mCameraWeakReference.get();
                if (helper == null) {
                    return;
                }
                switch (msg.what) {
                    case MSG_OPEN_CAMERA:
                        helper.openCamera((Context) msg.obj, msg.arg1);
                        break;
                    case MSG_CLOSE_CAMERA:
                        helper.closeCamera();
                        break;
                    case MSG_ENABLE_AUTO_START_PREVIEW:
                        helper.enableAutoStartPreview();
                        break;
                    case MSG_STOP_PREVIEW:
                        helper.stopPreview();
                        break;
                    case MSG_OPEN_FLASH:
                        helper.setEnableFlash(true);
                        break;
                    case MSG_CLOSE_FLASH:
                        helper.setEnableFlash(false);
                        break;
                    case MSG_SET_DESIRED_PARAMETER:
                        Object[] params = (Object[]) msg.obj;
                        helper.setDesiredCameraParameter((SurfaceTexture) params[0], (int) params[1], (int) params[2], (int) params[3], (Size) params[4], (int) params[5]);
                        break;
                    case MSG_SET_FOCUS:
                        params = (Object[]) msg.obj;
                        helper.focus((int) params[0], (int) params[1], (int) params[2], (int) params[3], (int) params[4]);
                        break;
                    case MSG_SET_ZOOM:
                        helper.zoom((int) msg.obj);
                        break;
                    case MSG_RELEASE:
                        helper.release();
                        break;
                }
            }

            private void sendMessage(int msgType, Object... params) {
                Message message = obtainMessage(msgType);
                switch (msgType) {
                    case MSG_OPEN_CAMERA:
                        message.obj = params[0];
                        message.arg1 = (int) params[1];
                        break;
                    case MSG_SET_DESIRED_PARAMETER:
                    case MSG_SET_FOCUS:
                        message.obj = params;
                        break;
                    case MSG_SET_ZOOM:
                        message.obj = params[0];
                        break;
                }
                sendMessage(message);
            }
        }

    }

    private static class GLVideoEncoder {//把GL内容写入Surface

        public static GLVideoEncoder create(EGLContext shareContext) {
            try {
                return new GLVideoEncoder(shareContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private EglHelper mEglHelper;
        private VideoEncoder mVideoEncoder;
        private Surface mEncoderSurface;
        private EncodeRunnable mEncodeRunnable;

        private GLVideoEncoder(EGLContext shareContext) throws Exception {
            mVideoEncoder = new VideoEncoder();
            mEglHelper = EglHelper.create(null, shareContext, true);
            if (mEglHelper == null) {
                release();
                throw new Exception("Create EglHelper fail: GLVideoEncoder");
            }
            mEncodeRunnable = new EncodeRunnable();
        }

        private boolean initCodec(int width, int height, int frameRate) {
            MediaFormat videoFormat;
            videoFormat = mVideoEncoder.createDefaultVideoMediaFormat(width, height, frameRate);
            MediaFormat audioFormat = mVideoEncoder.createDefaultAudioMediaFormat();
            if (videoFormat != null && audioFormat != null) {
                mEncoderSurface = mVideoEncoder.configureCodec(videoFormat, audioFormat);
                final CountDownLatch latch = new CountDownLatch(1);
                final boolean[] isChangeSuccessful = new boolean[1];
                mEglHelper.postGLRunnable(new Runnable() {
                    @Override
                    public void run() {
                        isChangeSuccessful[0] = mEglHelper.changeSurface(mEncoderSurface);
                        latch.countDown();
                    }
                });
                try {
                    latch.await();
                } catch (InterruptedException ignored) {

                }
                if (!isChangeSuccessful[0]) {
                    mVideoEncoder.reset();
                    return false;
                }
                return true;
            }
            return false;
        }


        private void release() {
            synchronized (this) {
                if (mVideoEncoder != null) {
                    mVideoEncoder.stopAndReset();
                    if (mEncoderSurface != null) {
                        mEncoderSurface.release();
                        mEncoderSurface = null;
                    }
                    mVideoEncoder.release();
                    mVideoEncoder = null;
                }
                if (mEglHelper != null) {
                    mEglHelper.releaseEGL();
                    mEglHelper = null;
                }

            }
        }

        private void encode(int textureID, float[] texMatrix, float[] projectionMatrix) {
            mEglHelper.postGLRunnable(mEncodeRunnable.fill(textureID, texMatrix, projectionMatrix));
        }

        boolean startRecode(int width, int height, int frameRate, String savePath) {
            if (mVideoEncoder == null) {
                return false;
            }
            if (mVideoEncoder.isRunning()) {
                throw new RuntimeException("The VideoEncoder is already Starting");
            }
            if (initCodec(width, height, frameRate)) {
                return mVideoEncoder.start(savePath, 0);
            }
            return false;
        }

        void stopRecode() {
            if (mVideoEncoder != null) {
                mVideoEncoder.stopAndReset();
            }
        }

        private class EncodeRunnable implements Runnable {
            private int mTextureID;
            private float[] mTexMatrix;
            private float[] mProjectionMatrix;

            @Override
            public void run() {
                mEglHelper.drawOESTexture(mTextureID, mTexMatrix, mProjectionMatrix);
            }

            EncodeRunnable fill(int textureID, float[] texMatrix, float[] projectionMatrix) {
                mTextureID = textureID;
                mTexMatrix = texMatrix;
                mProjectionMatrix = projectionMatrix;
                return this;

            }
        }

    }

    private static class EglHelper {

        public static EglHelper create(@Nullable Surface surface, @Nullable EGLContext shareContext, boolean isRecordMode) {
            try {
                return new EglHelper(surface, shareContext, isRecordMode);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private static final String TAG = EglHelper.class.getName();
        private static final int EGL_RECORDABLE_ANDROID = 0x3142;      // Android-specific extension.

        private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
        private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
        private android.opengl.EGLConfig mEGLConfig = null;
        private int mGlVersion;

        private Texture2dProgram mTexture2dProgram = new Texture2dProgram();

        private Handler mGLHandler;

        private EglHelper(final Surface surface, @Nullable final EGLContext shareContext, final boolean isRecordMode) throws Exception {
            HandlerThread glThread = new HandlerThread(TAG);
            glThread.start();
            mGLHandler = new Handler(glThread.getLooper());
            final CountDownLatch latch = new CountDownLatch(1);
            mGLHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!initEGLDisplay()) {
                            throw new Exception("initEGLDisplay fail");
                        }
                        if (!initEGLContext(shareContext, isRecordMode)) {
                            throw new Exception("initEGLContext fail");
                        }
                        if (surface != null) {
                            mEGLSurface = createEGLSurface(surface);
                            if (mEGLSurface == EGL14.EGL_NO_SURFACE || mEGLSurface == null) {
                                throw new Exception("createEGLSurface fail");
                            }
                            if (!makeCurrent()) {
                                throw new Exception("makeCurrent fail");
                            }
                            if (!initRecordTexture2dProgram()) {
                                throw new Exception("initRecordTexture2dProgram fail");
                            }
                        }
                    } catch (Exception e) {
                        releaseEGL();
                        Log.e(TAG, e.getMessage());
                    }
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException ignored) {

            }
            if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
                throw new Exception("Create EglHelper fail");
            }
        }

        private boolean initEGLDisplay() {
            mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
                Log.e(TAG, "unable to get EGL14 display");
                return false;
            }
            int[] version = new int[2];
            if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
                mEGLDisplay = null;
                Log.e(TAG, "unable to initialize EGL14");
                return false;
            }
            return true;
        }

        private boolean initEGLContext(EGLContext sharedContext, boolean isRecordMode) {
            android.opengl.EGLConfig config = getEGLConfig(isRecordMode, 3);
            if (config != null) {
                int[] attributesList = {
                        EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                        EGL14.EGL_NONE
                };
                sharedContext = sharedContext != null ? sharedContext : EGL14.EGL_NO_CONTEXT;
                EGLContext context = EGL14.eglCreateContext(mEGLDisplay, config, sharedContext, attributesList, 0);
                if (EGL14.eglGetError() == EGL14.EGL_SUCCESS) {
                    mEGLConfig = config;
                    mEGLContext = context;
                    mGlVersion = 3;
                }
            }
            if (mEGLContext == EGL14.EGL_NO_CONTEXT) {
                config = getEGLConfig(isRecordMode, 2);
                if (config != null) {
                    int[] attributesList = {
                            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                            EGL14.EGL_NONE
                    };
                    EGLContext context = EGL14.eglCreateContext(mEGLDisplay, config, sharedContext, attributesList, 0);
                    if (EGL14.eglGetError() == EGL14.EGL_SUCCESS) {
                        mEGLConfig = config;
                        mEGLContext = context;
                        mGlVersion = 2;
                    }
                }
            }
            return mEGLContext != EGL14.EGL_NO_CONTEXT;
        }

        private android.opengl.EGLConfig getEGLConfig(boolean isRecordMode, int version) {
            // The actual surface is generally RGBA or RGBX, so situationally omitting alpha
            // doesn't really help.  It can also lead to a huge performance hit on glReadPixels()
            // when reading into a GL_RGBA buffer.
            int[] attributesList = new int[]{
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 0,
                    EGL14.EGL_DEPTH_SIZE, 0,
                    EGL14.EGL_STENCIL_SIZE, 0,
                    EGL14.EGL_RENDERABLE_TYPE, version == 3 ? EGLExt.EGL_OPENGL_ES3_BIT_KHR : EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_NONE, 0,      // placeholder for recordable [@-3]
                    EGL14.EGL_NONE
            };
            if (isRecordMode) {
                attributesList[attributesList.length - 3] = EGL_RECORDABLE_ANDROID;
                attributesList[attributesList.length - 2] = 1;
            }
            android.opengl.EGLConfig[] configs = new android.opengl.EGLConfig[1];
            int[] numConfigs = new int[1];
            if (!EGL14.eglChooseConfig(mEGLDisplay, attributesList, 0, configs, 0, configs.length, numConfigs, 0)) {
                Log.e(TAG, "Unable to find a suitable EGLConfig");
                return null;
            }
            return configs[0];
        }

        private EGLSurface createEGLSurface(Surface surface) {
            int[] surfaceAttributes = {
                    EGL14.EGL_NONE
            };
            return EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface, surfaceAttributes, 0);
        }

        private boolean initRecordTexture2dProgram() {
            return mTexture2dProgram.tryInit();
        }

        private boolean makeCurrent() {
            return EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
        }

        private void releaseEGL() {
            synchronized (this) {
                if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                    final CountDownLatch latch = new CountDownLatch(1);
                    mGLHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Android is unusual in that it uses a reference-counted EGLDisplay.  So for
                            // every eglInitialize() we need an eglTerminate().
                            mTexture2dProgram.release();
                            EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
                            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
                            if (mEGLSurface != null) {
                                EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
                            }
                            EGL14.eglReleaseThread();
                            EGL14.eglTerminate(mEGLDisplay);

                            mEGLDisplay = EGL14.EGL_NO_DISPLAY;
                            mEGLContext = EGL14.EGL_NO_CONTEXT;
                            mEGLSurface = EGL14.EGL_NO_SURFACE;
                            mEGLConfig = null;
                            mGlVersion = 0;
                            mGLHandler.removeCallbacksAndMessages(null);
                            mGLHandler.getLooper().quit();
                            mGLHandler = null;
                            latch.countDown();
                        }
                    });
                    try {
                        latch.await();
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }

        public boolean changeSurface(Surface surface) {
            EGLSurface eglSurface = createEGLSurface(surface);
            if (eglSurface == EGL14.EGL_NO_SURFACE || eglSurface == null) {
                Log.e(TAG, "createEGLSurface fail");
                return false;
            }
            if (mEGLSurface != null) {
                mTexture2dProgram.release();
                EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
            }
            mEGLSurface = eglSurface;
            if (!makeCurrent()) {
                Log.e(TAG, "makeCurrent fail");
                return false;
            }
            if (!initRecordTexture2dProgram()) {
                Log.e(TAG, "initRecordTexture2dProgram fail");
                return false;
            }
            return true;
        }

        public void drawOESTexture(int textureID, float[] texMatrix, float[] projectionMatrix) {
            mTexture2dProgram.draw(textureID, texMatrix, projectionMatrix);
            EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
        }

        public void postGLRunnable(Runnable runnable) {
            synchronized (this) {
                if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
                    throw new RuntimeException("EglHelper already release!");
                }
                mGLHandler.post(runnable);
            }
        }

        private static class Texture2dProgram {

            private static final String VERTEX_SHADER =
                    "uniform mat4 uMVPMatrix;\n" +
                            "uniform mat4 uTexMatrix;\n" +
                            "attribute vec4 aPosition;\n" +
                            "attribute vec4 aTextureCoord;\n" +
                            "varying vec2 vTextureCoord;\n" +
                            "void main() {\n" +
                            "    gl_Position = uMVPMatrix * aPosition;\n" +
                            "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
                            "}\n";

            private static final String FRAGMENT_SHADER_EXT =
                    "#extension GL_OES_EGL_image_external : require\n" +
                            "precision mediump float;\n" +
                            "varying vec2 vTextureCoord;\n" +
                            "uniform samplerExternalOES sTexture;\n" +
                            "void main() {\n" +
                            "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                            "}\n";

            private static final float[] FULL_RECTANGLE_COORDS = {
                    -1.0f, -1.0f,   // 0 bottom left
                    1.0f, -1.0f,   // 1 bottom right
                    -1.0f, 1.0f,   // 2 top left
                    1.0f, 1.0f,   // 3 top right
            };
            private static final float[] FULL_RECTANGLE_TEX_COORDS = {
                    0.0f, 0.0f,     // 0 bottom left
                    1.0f, 0.0f,     // 1 bottom right
                    0.0f, 1.0f,     // 2 top left
                    1.0f, 1.0f      // 3 top right
            };
            private static final FloatBuffer FULL_RECTANGLE_BUF = createFloatBuffer(FULL_RECTANGLE_COORDS);
            private static final FloatBuffer FULL_RECTANGLE_TEX_BUF = createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);

            private static final int SIZEOF_FLOAT = 4;
            private static final int COORDS_PER_VERTEX = 2;
            private static final int TEX_COORD_STRIDE = 2 * SIZEOF_FLOAT;
            private static final int VERTEX_STRIDE = COORDS_PER_VERTEX * SIZEOF_FLOAT;
            private static final int VERTEX_COUNT = FULL_RECTANGLE_COORDS.length / COORDS_PER_VERTEX;

            private int mProgramHandle;
            private int muMVPMatrixLoc;
            private int muTexMatrixLoc;
            private int maPositionLoc;
            private int maTextureCoordLoc;


            public boolean tryInit() {
                int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
                if (vertexShader == 0) {
                    return false;
                }
                int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_EXT);
                if (pixelShader == 0) {
                    return false;
                }

                mProgramHandle = GLES20.glCreateProgram();
                checkGlError("glCreateProgram");
                if (mProgramHandle == 0) {
                    return false;
                }
                GLES20.glAttachShader(mProgramHandle, vertexShader);
                checkGlError("glAttachShader");
                GLES20.glAttachShader(mProgramHandle, pixelShader);
                checkGlError("glAttachShader");
                GLES20.glLinkProgram(mProgramHandle);
                int[] linkStatus = new int[1];
                GLES20.glGetProgramiv(mProgramHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
                if (linkStatus[0] != GLES20.GL_TRUE) {
                    LogUtil.e("Could not link program: ");
                    LogUtil.e(GLES20.glGetProgramInfoLog(mProgramHandle));
                    GLES20.glDeleteProgram(mProgramHandle);
                    mProgramHandle = 0;
                } else {
                    maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
                    maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
                    muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
                    muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix");
                    // Select the program.
                    GLES20.glUseProgram(mProgramHandle);
                    checkGlError("glUseProgram");

                    // Enable the "aPosition" vertex attribute.
                    GLES20.glEnableVertexAttribArray(maPositionLoc);
                    checkGlError("glEnableVertexAttribArray");

                    // Enable the "aTextureCoord" vertex attribute.
                    GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
                    checkGlError("glEnableVertexAttribArray");

                    // Connect vertexBuffer to "aPosition".
                    GLES20.glVertexAttribPointer(maPositionLoc, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, FULL_RECTANGLE_BUF);
                    checkGlError("glVertexAttribPointer");

                    // Connect texBuffer to "aTextureCoord".
                    GLES20.glVertexAttribPointer(maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, TEX_COORD_STRIDE, FULL_RECTANGLE_TEX_BUF);
                    checkGlError("glVertexAttribPointer");
                }

                return mProgramHandle != 0;
            }

            public void release() {
                // Done -- disable vertex array, texture, and program.
                GLES20.glDisableVertexAttribArray(maPositionLoc);
                GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
                GLES20.glUseProgram(0);
                GLES20.glDeleteProgram(mProgramHandle);
                mProgramHandle = -1;
            }

            public void draw(int textureID, float[] texMatrix, float[] projectionMatrix) {
                // Set the texture unit.
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureID);

                // Copy the model / view / projection matrix over.
                GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, projectionMatrix, 0);
                checkGlError("glUniformMatrix4fv");

                // Copy the texture transformation matrix over.
                GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);
                checkGlError("glUniformMatrix4fv");

                // Draw the rect.
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);
                checkGlError("glDrawArrays");
            }

            public static int createOESTextureObject() {
                int[] tex = new int[1];
                //生成一个纹理
                GLES20.glGenTextures(1, tex, 0);
                //将此纹理绑定到外部纹理上
                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
                //设置纹理过滤参数
                GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
                GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
                GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
                return tex[0];
            }

            public static void deleteOESTextureObject(int textureID) {
                //删除纹理
                GLES20.glDeleteTextures(1, new int[]{textureID}, 0);
            }

            private static int loadShader(int shaderType, String source) {
                int shader = GLES20.glCreateShader(shaderType);
                checkGlError("glCreateShader type=" + shaderType);
                GLES20.glShaderSource(shader, source);
                GLES20.glCompileShader(shader);
                int[] compiled = new int[1];
                GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
                if (compiled[0] == 0) {
                    LogUtil.e("Could not compile shader " + shaderType + ":");
                    LogUtil.e(" " + GLES20.glGetShaderInfoLog(shader));
                    GLES20.glDeleteShader(shader);
                    shader = 0;
                }
                return shader;
            }

            private static FloatBuffer createFloatBuffer(float[] coords) {
                // Allocate a direct ByteBuffer, using 4 bytes per float, and copy coords into it.
                ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
                bb.order(ByteOrder.nativeOrder());
                FloatBuffer fb = bb.asFloatBuffer();
                fb.put(coords);
                fb.position(0);
                return fb;
            }

        }

        private static boolean hasEglError(String msg) {
            int error;
            if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
                Log.e(TAG, msg + ": EGL error: 0x" + Integer.toHexString(error));
                return true;
            }
            return false;
        }

        private static void checkGlError(String op) {
            int error = GLES20.glGetError();
            if (error != GLES20.GL_NO_ERROR) {
                String msg = op + ": glError 0x" + Integer.toHexString(error);
                LogUtil.e(msg);
                throw new RuntimeException(msg);
            }
        }
    }
}
