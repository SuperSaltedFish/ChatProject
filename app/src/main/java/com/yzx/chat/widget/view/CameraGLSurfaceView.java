//package com.yzx.chat.widget.view;
//
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.content.Context;
//import android.content.res.Configuration;
//import android.graphics.SurfaceTexture;
//import android.media.MediaFormat;
//import android.opengl.EGL14;
//import android.opengl.EGLContext;
//import android.opengl.EGLDisplay;
//import android.opengl.EGLSurface;
//import android.opengl.GLES11Ext;
//import android.opengl.GLES20;
//import android.opengl.GLSurfaceView;
//import android.opengl.Matrix;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Looper;
//import android.os.Message;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.util.Size;
//import android.view.Display;
//import android.view.GestureDetector;
//import android.view.MotionEvent;
//import android.view.ScaleGestureDetector;
//import android.view.Surface;
//
//import com.yzx.chat.core.util.LogUtil;
//import com.yzx.chat.util.BasicCamera;
//
//import java.io.IOException;
//import java.lang.ref.WeakReference;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.nio.FloatBuffer;
//
//import javax.microedition.khronos.egl.EGLConfig;
//import javax.microedition.khronos.opengles.GL10;
//
///**
// * Created by YZX on 2019年06月24日.
// * 每一个不曾起舞的日子 都是对生命的辜负
// */
//public class CameraGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {
//
//    private static final Size DEFAULT_ASPECT_RATIO = new Size(16, 9);
//
//    private final float[] mTextureMatrix = new float[16];
//    private final float[] mProjectionMatrix = new float[16];
//    private static final float[] IDENTITY_MATRIX;
//
//    static {
//        IDENTITY_MATRIX = new float[16];
//        Matrix.setIdentityM(IDENTITY_MATRIX, 0);
//    }
//
//    private GestureDetector mClickGestureDetector;
//    private ScaleGestureDetector mScaleGestureDetector;
//    protected boolean isEnableGesture;
//
//    private SurfaceTexture mPreviewSurfaceTexture;
//    private Texture2dProgram mPreviewTexture2dProgram;
//    private int mOESTextureID;
//
//    private int mSurfaceWidth;
//    private int mSurfaceHeight;
//    private int mDisplayRotation;
//    private boolean isCamera2Mode;
//    private Display mScreenDisplay;
//
//    private CameraHelper mCameraHelper;
//    private Size mAspectRatioSize;
//    private int mCameraFacing;
//
//    public CameraGLSurfaceView(Context context) {
//        this(context, null);
//    }
//
//    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        setEGLContextClientVersion(2);
//        setRenderer(this);
//        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
//        setGesture();
//
//        mOESTextureID = -1;
//        mCameraHelper = new CameraHelper();
//        mAspectRatioSize = DEFAULT_ASPECT_RATIO;
//
//        mPreviewTexture2dProgram = new Texture2dProgram();
//
//        mScreenDisplay = ((Activity) context).getWindowManager().getDefaultDisplay();
//
//        switchCamera(BasicCamera.CAMERA_FACING_BACK);
//    }
//
//    private void setGesture() {
//        isEnableGesture = true;
//        mClickGestureDetector = new GestureDetector(getContext().getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
//            @Override
//            public boolean onDown(MotionEvent e) {
//                return true;
//            }
//
//            @Override
//            public boolean onSingleTapConfirmed(MotionEvent event) {
//                mCameraHelper.sendEvent(CameraHelper.MSG_SET_FOCUS,
//                        (int) event.getX(),
//                        (int) event.getY(),
//                        mSurfaceWidth,
//                        mSurfaceHeight,
//                        mDisplayRotation);
//                return true;
//            }
//
//        });
//        mScaleGestureDetector = new ScaleGestureDetector(getContext().getApplicationContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
//            @Override
//            public boolean onScale(ScaleGestureDetector detector) {
//                float scaleFactor = detector.getScaleFactor();
//                if (scaleFactor == 1) {
//                    return false;
//                }
//                int maxZoom = mCameraHelper.getMaxZoom();
//                int minZoom = mCameraHelper.getMinZoom();
//                int currentZoom = mCameraHelper.getCurrentZoom();
//                int increase;
//                if (scaleFactor > 1) {
//                    increase = (int) ((scaleFactor - 1f) * (maxZoom - minZoom) + 0.5f);//四舍五入
//                } else {
//                    increase = (int) ((scaleFactor - 1f) * (maxZoom - minZoom) - 0.5f);//四舍五入
//                }
//                increase = (int) (increase * 0.5f);
//                if (increase == 0) {
//                    return false;
//                }
//                int newZoom = currentZoom + increase;
//                newZoom = Math.min(newZoom, maxZoom);
//                newZoom = Math.max(newZoom, minZoom);
//                if (newZoom == currentZoom) {
//                    return false;
//                }
//                mCameraHelper.sendEvent(CameraHelper.MSG_SET_ZOOM, newZoom);
//                return true;
//            }
//        });
//    }
//
//
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        int width = MeasureSpec.getSize(widthMeasureSpec);
//        int height = MeasureSpec.getSize(heightMeasureSpec);
//        int ratioW;
//        int ratioH;
//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            ratioW = mAspectRatioSize.getWidth();
//            ratioH = mAspectRatioSize.getHeight();
//        } else {
//            ratioW = mAspectRatioSize.getHeight();
//            ratioH = mAspectRatioSize.getWidth();
//        }
//        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
//            height = width * ratioH / ratioW;
//        } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
//            width = height * ratioW / ratioH;
//        }
//        setMeasuredDimension(width, height);
//    }
//
//    @Override
//    @SuppressLint("ClickableViewAccessibility")
//    public boolean onTouchEvent(MotionEvent event) {
//        if (!isEnableGesture) {
//            return false;
//        }
//        return mClickGestureDetector.onTouchEvent(event) || mScaleGestureDetector.onTouchEvent(event);
//    }
//
//    @Override
//    public void setPreserveEGLContextOnPause(boolean preserveOnPause) {
//        super.setPreserveEGLContextOnPause(false);
//    }
//
//    @Override
//    public void onResume() {
//        LogUtil.e("onResume");
//        super.onResume();
//        mCameraHelper.sendEvent(CameraHelper.MSG_ENABLE_AUTO_START_PREVIEW);
//    }
//
//    @Override
//    public void onPause() {
//        LogUtil.e("onPause");
//        mCameraHelper.sendEvent(CameraHelper.MSG_STOP_PREVIEW);
//        stopRecode();
//        mEGLVideoEncoder.sendEvent(EGLVideoEncoder.MSG_RELEASE_EGL);
//        queueEvent(new Runnable() {
//            @Override
//            public void run() {
//                releasePreviewGL();
//            }
//        });
//        super.onPause();
//    }
//
//    public void onDestroy() {
//        onPause();
//        mCameraHelper.sendEvent(CameraHelper.MSG_CLOSE_CAMERA);
//        mEGLVideoEncoder.sendEvent(EGLVideoEncoder.MSG_RELEASE);
//        mCameraHelper = null;
//        mEGLVideoEncoder = null;
//    }
//
//    @Override
//    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        if (!mPreviewTexture2dProgram.tryInit()) {
//            mPreviewTexture2dProgram.release();
//            return;
//        }
//        //根据外部纹理ID创建SurfaceTexture
//        mOESTextureID = Texture2dProgram.createOESTextureObject();
//        mPreviewSurfaceTexture = new SurfaceTexture(mOESTextureID);
//        mPreviewSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
//            @Override
//            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//                //每获取到一帧数据时请求OpenGL ES进行渲染
//                requestRender();
//            }
//        });
//    }
//
//    @Override
//    public void onSurfaceChanged(GL10 gl, int width, int height) {
//        LogUtil.e("onSurfaceChanged");
//        if (mPreviewSurfaceTexture == null) {
//            return;
//        }
//        GLES20.glViewport(0, 0, width, height);
//
//        int displayRotation = mScreenDisplay.getRotation();
//
//        mCameraHelper.sendEvent(CameraHelper.MSG_SET_DESIRED_PARAMETER,
//                mPreviewSurfaceTexture,
//                width,
//                height,
//                displayRotation,
//                mAspectRatioSize,
//                mMaxFrameRate);
//
//        configureTransformIfNeed(width, height, mScreenDisplay.getRotation(), mCameraHelper.isCamera2Mode());
//
//        mEGLVideoEncoder.sendEvent(EGLVideoEncoder.MSG_RELEASE_EGL);
//        if (displayRotation == Surface.ROTATION_0 || displayRotation == Surface.ROTATION_180) {
//            float scaleW = (float) mMaxVideoWidth / height;
//            float scaleH = (float) mMaxVideoHeight / width;
//            float scale = Math.min(scaleW, scaleH);
//            int videoWidth = Math.round(scale * width);
//            int videoHeight = Math.round(scale * height);
//            mEGLVideoEncoder.sendEvent(EGLVideoEncoder.MSG_SET_VIDEO_SIZE, new Size(videoWidth, videoHeight), mMaxFrameRate);
//        } else {
//            float scaleW = (float) mMaxVideoWidth / width;
//            float scaleH = (float) mMaxVideoHeight / height;
//            float scale = Math.min(scaleW, scaleH);
//            int videoWidth = Math.round(scale * width);
//            int videoHeight = Math.round(scale * height);
//            mEGLVideoEncoder.sendEvent(EGLVideoEncoder.MSG_SET_VIDEO_SIZE, new Size(videoWidth, videoHeight), mMaxFrameRate);
//        }
//        mEGLVideoEncoder.sendEvent(EGLVideoEncoder.MSG_INIT_EGL, EGL14.eglGetCurrentContext());
//    }
//
//    @Override
//    public void onDrawFrame(GL10 gl) {
//        if (mPreviewSurfaceTexture == null) {
//            return;
//        }
//        configureTransformIfNeed(mSurfaceWidth, mSurfaceHeight, mScreenDisplay.getRotation(), mCameraHelper.isCamera2Mode());
//        mPreviewSurfaceTexture.updateTexImage();
//        //获取外部纹理的矩阵，用来确定纹理的采样位置，没有此矩阵可能导致图像翻转等问题
//        mPreviewSurfaceTexture.getTransformMatrix(mTextureMatrix);//这个代码不需要了，变化在ProjectionMatrix做了
//        mPreviewTexture2dProgram.draw(mOESTextureID, mTextureMatrix, mProjectionMatrix);
//        if (isStartRecording) {
//            mEGLVideoEncoder.sendEvent(EGLVideoEncoder.MSG_DRAW, mOESTextureID, mTextureMatrix, mProjectionMatrix);
//        }
//    }
//
//    private void releasePreviewGL() {
//        mPreviewTexture2dProgram.release();
//        if (mPreviewSurfaceTexture != null) {
//            mPreviewSurfaceTexture.setOnFrameAvailableListener(null);
//            mPreviewSurfaceTexture.release();
//            mPreviewSurfaceTexture = null;
//            mCameraHelper.sendEvent(CameraHelper.MSG_SET_DESIRED_PARAMETER, null, -1, -1, -1, null, 0);
//        }
//        if (mOESTextureID > -1) {
//            Texture2dProgram.deleteOESTextureObject(mOESTextureID);
//        }
//        mOESTextureID = -1;
//    }
//
//    public void switchCamera(@BasicCamera.FacingType int cameraFacingType) {
//        if (mCameraFacing == cameraFacingType) {
//            return;
//        }
//        mCameraFacing = cameraFacingType;
//        mCameraHelper.sendEvent(CameraHelper.MSG_CLOSE_CAMERA);
//        mCameraHelper.sendEvent(CameraHelper.MSG_OPEN_CAMERA, getContext(), mCameraFacing);
//    }
//
//    public void startRecode(String saveFile) {
//        mEGLVideoEncoder.sendEvent(EGLVideoEncoder.MSG_START_RECODE, saveFile);
//        isStartRecording = true;
//    }
//
//    public void stopRecode() {
//        isStartRecording = false;
//        mEGLVideoEncoder.sendEvent(EGLVideoEncoder.MSG_STOP_RECODE);
//    }
//
//    public void setEnableGesture(boolean enableGesture) {
//        isEnableGesture = enableGesture;
//    }
//
//    private void configureTransformIfNeed(int width, int height, int displayRotation, boolean isCamera2) {
//        if (width == mSurfaceWidth && height == mSurfaceHeight && displayRotation == mDisplayRotation && isCamera2Mode == isCamera2) {
//            return;
//        }
//        mSurfaceWidth = width;
//        mSurfaceHeight = height;
//        mDisplayRotation = displayRotation;
//        isCamera2Mode = isCamera2;
//
//        float[] projectionMatrix = new float[16];
//        Matrix.setIdentityM(projectionMatrix, 0);
//        Matrix.orthoM(projectionMatrix, 0, -1, 1, -1, 1, -1, 1);
//
//        float[] transformMatrix = new float[16];
//        Matrix.setIdentityM(transformMatrix, 0);
//
//        float previewAspectRatio = (float) mAspectRatioSize.getWidth() / mAspectRatioSize.getHeight();
//        if (Surface.ROTATION_0 == displayRotation || Surface.ROTATION_180 == displayRotation) {
//            float viewAspectRatio = (float) mSurfaceHeight / mSurfaceWidth;
//            if (viewAspectRatio >= previewAspectRatio) {
//                Matrix.scaleM(transformMatrix, 0, viewAspectRatio / previewAspectRatio, 1, 1f);
//            } else {
//                Matrix.scaleM(transformMatrix, 0, 1, previewAspectRatio / viewAspectRatio, 1f);
//            }
//        } else {
//            float viewAspectRatio = (float) mSurfaceWidth / mSurfaceHeight;
//            if (viewAspectRatio >= previewAspectRatio) {
//                Matrix.scaleM(transformMatrix, 0, 1, viewAspectRatio / previewAspectRatio, 1f);
//            } else {
//                Matrix.scaleM(transformMatrix, 0, previewAspectRatio / viewAspectRatio, 1, 1f);
//            }
//        }
//        if (isCamera2) {
//            switch (displayRotation) {
//                case Surface.ROTATION_90:
//                    Matrix.rotateM(transformMatrix, 0, 90, 0.0f, 0.0f, 1.0f);
//                    break;
//                case Surface.ROTATION_180:
//                    break;
//                case Surface.ROTATION_270:
//                    Matrix.rotateM(transformMatrix, 0, -90, 0.0f, 0.0f, 1.0f);
//                    break;
//            }
//        }
//        Matrix.multiplyMM(mProjectionMatrix, 0, projectionMatrix, 0, transformMatrix, 0);
//    }
//
//    protected static class CameraHelper {
//
//        private static final int MAX_PREVIEW_WIDTH = 1920;
//        private static final int MAX_PREVIEW_HEIGHT = 1080;
//
//        public static final int MSG_OPEN_CAMERA = 1;
//        public static final int MSG_CLOSE_CAMERA = 2;
//        public static final int MSG_ENABLE_AUTO_START_PREVIEW = 3;
//        public static final int MSG_STOP_PREVIEW = 4;
//        public static final int MSG_OPEN_FLASH = 5;
//        public static final int MSG_CLOSE_FLASH = 6;
//        public static final int MSG_SET_DESIRED_PARAMETER = 7;
//        public static final int MSG_SET_FOCUS = 8;
//        public static final int MSG_SET_ZOOM = 9;
//
//
//        private CameraHandler mCameraHandler;
//
//        private BasicCamera mCamera;
//        private Size mPreviewSize;
//        private int mCameraFacingType;
//        private int mMaxZoom;
//        private int mMinZoom;
//        private int mCurrentZoom;
//        private boolean isEnableAutoStartPreview;
//
//        private SurfaceTexture mPreviewSurfaceTexture;
//        private int mDisplayOrientation = -1;
//        private int mDesiredWidth = 0;
//        private int mDesiredHeight = 0;
//        private Size mAspectRatioSize = null;
//        private int mMaxFPS = 0;
//
//        public CameraHelper() {
//            mCameraHandler = new CameraHandler(this);
//        }
//
//        private void openCamera(Context context, @BasicCamera.FacingType final int cameraFacing) {
//            LogUtil.e("openCamera");
//            final BasicCamera camera = BasicCamera.createCameraCompat(context, cameraFacing);
//            if (camera != null) {
//                camera.openCamera(new BasicCamera.StateCallback() {
//                    @Override
//                    public void onOpenSuccessful(BasicCamera camera) {
//                        LogUtil.e("onOpenSuccessful");
//                        mCamera = camera;
//                        mCameraFacingType = cameraFacing;
//                        mMaxZoom = mCamera.getMaxZoomValue();
//                        mMinZoom = mCamera.getMinZoomValue();
//                        mCurrentZoom = mMinZoom;
//                        setupCamera();
//                    }
//
//                    @Override
//                    public void onClose() {
//                        closeCamera();
//                    }
//
//                    @Override
//                    public void onError(int error) {
//                        closeCamera();
//                    }
//                });
//            }
//        }
//
//
//        private void closeCamera() {
//            LogUtil.e("closeCamera");
//            if (mCamera != null) {
//                mCamera.closeCamera();
//                mCamera = null;
//                mCameraFacingType = -1;
//                mMaxZoom = 0;
//                mCurrentZoom = 0;
//                mPreviewSurfaceTexture = null;
//                mDisplayOrientation = -1;
//                mDesiredWidth = 0;
//                mDesiredHeight = 0;
//                mMaxFPS = 0;
//                mAspectRatioSize = null;
//                mPreviewSize = null;
//                mCameraHandler.removeCallbacksAndMessages(null);
//            }
//        }
//
//        private void setupCamera() {
//            LogUtil.e("setupCamera");
//            if (mCamera == null) {
//                return;
//            }
//            if (mPreviewSurfaceTexture == null) {
//                mCamera.setPreviewDisplay((SurfaceTexture) null);
//                return;
//            }
//            if (mDesiredWidth <= 0 || mDesiredHeight <= 0 || mAspectRatioSize == null) {
//
//                return;
//            }
//            int cameraOrientation = mCamera.getSensorOrientation();
//            boolean swappedDimensions = false;
//            switch (mDisplayOrientation) {
//                case Surface.ROTATION_0:
//                case Surface.ROTATION_180:
//                    if (cameraOrientation == 90 || cameraOrientation == 270) {
//                        swappedDimensions = true;
//                    }
//                    break;
//                case Surface.ROTATION_90:
//                case Surface.ROTATION_270:
//                    if (cameraOrientation == 0 || cameraOrientation == 180) {
//                        swappedDimensions = true;
//                    }
//                    break;
//                default:
//                    return;
//            }
//            if (swappedDimensions) {
//                mPreviewSize = mCamera.calculateOptimalDisplaySize(SurfaceTexture.class, mDesiredHeight, mDesiredWidth, MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, mAspectRatioSize, true);
//            } else {
//                mPreviewSize = mCamera.calculateOptimalDisplaySize(SurfaceTexture.class, mDesiredWidth, mDesiredHeight, MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, mAspectRatioSize, true);
//            }
//            if (mPreviewSize == null) {
//                return;
//            }
//            mCamera.setRecordingHint(true);
//            mCamera.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//            if (mMaxFPS > 0) {
//                mCamera.setPreviewFpsIfSupported(mMaxFPS, mMaxFPS);
//            }
//            mCamera.setDisplayOrientationIfSupported(calculateCameraRotationAngle(mDisplayOrientation, mCamera.getSensorOrientation(), mCameraFacingType));
//            mPreviewSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//            mCamera.setPreviewDisplay(mPreviewSurfaceTexture);
//            if (isEnableAutoStartPreview) {
//                startPreviewIfNeed();
//            }
//        }
//
//        private void setDesiredCameraParameter(SurfaceTexture surfaceTexture, int desiredWidth, int desiredHeight, int displayRotation, Size aspectRatioSize, int maxFPS) {
//            mPreviewSurfaceTexture = surfaceTexture;
//            mDesiredWidth = desiredWidth;
//            mDesiredHeight = desiredHeight;
//            mDisplayOrientation = displayRotation;
//            mAspectRatioSize = aspectRatioSize;
//            mMaxFPS = maxFPS;
//            setupCamera();
//        }
//
//
//        private void enableAutoStartPreview() {
//            isEnableAutoStartPreview = true;
//            startPreviewIfNeed();
//        }
//
//        private void startPreviewIfNeed() {
//            if (mCamera != null && mCamera.isOpen() && isEnableAutoStartPreview && mPreviewSurfaceTexture != null && mPreviewSize != null) {
//                mCamera.starPreview();
//            }
//        }
//
//        private void stopPreview() {
//            isEnableAutoStartPreview = false;
//            if (mCamera != null && mCamera.isPreviewing()) {
//                mCamera.stopPreview();
//            }
//        }
//
//        private void setEnableFlash(boolean isEnable) {
//            if (mCamera != null && mCamera.isOpen()) {
//                mCamera.setEnableFlash(isEnable);
//            }
//        }
//
//        private void focus(int x, int y, int width, int height, int displayOrientation) {
//            if (mCamera == null || !mCamera.isOpen()) {
//                return;
//            }
//            int totalWidth;
//            int totalHeight;
//            int touchX;
//            int touchY;
//            int rotate = (mCamera.getSensorOrientation() - displayOrientation * 90 + 360) % 360;
//            switch (rotate) {
//                case 90:
//                    totalWidth = height;
//                    totalHeight = width;
//                    touchX = y;
//                    touchY = totalHeight - x;
//                    break;
//                case 270:
//                    totalWidth = height;
//                    totalHeight = width;
//                    touchY = x;
//                    touchX = totalWidth - y;
//                    break;
//                default:
//                    totalWidth = width;
//                    totalHeight = height;
//                    touchX = x;
//                    touchY = y;
//                    break;
//            }
//            mCamera.setFocusPoint(touchX, touchY, totalWidth, totalHeight);
//        }
//
//        private void zoom(int value) {
//            if (mCamera == null || !mCamera.isOpen()) {
//                return;
//            }
//            value = Math.min(value, mMaxZoom);
//            value = Math.max(value, mMinZoom);
//            if (value == mCurrentZoom) {
//                return;
//            }
//            mCurrentZoom = value;
//            mCamera.setZoom(mCurrentZoom);
//        }
//
//        public boolean isCamera2Mode() {
//            return mCamera instanceof BasicCamera.Camera2Impl;
//        }
//
//        public int getMaxZoom() {
//            return mMaxZoom;
//        }
//
//        public int getMinZoom() {
//            return mMinZoom;
//        }
//
//        public int getCurrentZoom() {
//            return mCurrentZoom;
//        }
//
//        public void sendEvent(int msgType, Object... params) {
//            mCameraHandler.removeMessages(msgType);
//            mCameraHandler.sendMessage(msgType, params);
//        }
//
//
//        public static int calculateCameraRotationAngle(int displayRotation, int cameraRotation, int cameraFacingType) {
//            int degrees = 0;
//            switch (displayRotation) {
//                case Surface.ROTATION_0:
//                    degrees = 0;
//                    break;
//                case Surface.ROTATION_90:
//                    degrees = 90;
//                    break;
//                case Surface.ROTATION_180:
//                    degrees = 180;
//                    break;
//                case Surface.ROTATION_270:
//                    degrees = 270;
//                    break;
//            }
//            int result;
//            if (cameraFacingType == BasicCamera.CAMERA_FACING_FRONT) {
//                result = (cameraRotation + degrees) % 360;
//                result = (360 - result) % 360;
//            } else {
//                result = (cameraRotation - degrees + 360) % 360;
//            }
//            return result;
//        }
//
//
//        private static class CameraHandler extends Handler {
//            private WeakReference<CameraHelper> mCameraWeakReference;
//
//            private CameraHandler(CameraHelper cameraHelper) {
//                super(Looper.getMainLooper());
//                mCameraWeakReference = new WeakReference<>(cameraHelper);
//            }
//
//            @Override
//            public void handleMessage(Message msg) {
//                CameraHelper helper = mCameraWeakReference.get();
//                if (helper == null) {
//                    return;
//                }
//                switch (msg.what) {
//                    case MSG_OPEN_CAMERA:
//                        helper.openCamera((Context) msg.obj, msg.arg1);
//                        break;
//                    case MSG_CLOSE_CAMERA:
//                        helper.closeCamera();
//                        break;
//                    case MSG_ENABLE_AUTO_START_PREVIEW:
//                        helper.enableAutoStartPreview();
//                        break;
//                    case MSG_STOP_PREVIEW:
//                        helper.stopPreview();
//                        break;
//                    case MSG_OPEN_FLASH:
//                        helper.setEnableFlash(true);
//                        break;
//                    case MSG_CLOSE_FLASH:
//                        helper.setEnableFlash(false);
//                        break;
//                    case MSG_SET_DESIRED_PARAMETER:
//                        Object[] params = (Object[]) msg.obj;
//                        helper.setDesiredCameraParameter((SurfaceTexture) params[0], (int) params[1], (int) params[2], (int) params[3], (Size) params[4], (int) params[5]);
//                        break;
//                    case MSG_SET_FOCUS:
//                        params = (Object[]) msg.obj;
//                        helper.focus((int) params[0], (int) params[1], (int) params[2], (int) params[3], (int) params[4]);
//                        break;
//                    case MSG_SET_ZOOM:
//                        helper.zoom((int) msg.obj);
//                        break;
//                }
//            }
//
//            private void sendMessage(int msgType, Object... params) {
//                Message message = obtainMessage(msgType);
//                switch (msgType) {
//                    case MSG_OPEN_CAMERA:
//                        message.obj = params[0];
//                        message.arg1 = (int) params[1];
//                        break;
//                    case MSG_SET_DESIRED_PARAMETER:
//                    case MSG_SET_FOCUS:
//                        message.obj = params;
//                        break;
//                    case MSG_SET_ZOOM:
//                        message.obj = params[0];
//                        break;
//                }
//                sendMessage(message);
//            }
//        }
//
//    }
//
//    private static class Texture2dProgram {
//
//        private static final String VERTEX_SHADER =
//                "uniform mat4 uMVPMatrix;\n" +
//                        "uniform mat4 uTexMatrix;\n" +
//                        "attribute vec4 aPosition;\n" +
//                        "attribute vec4 aTextureCoord;\n" +
//                        "varying vec2 vTextureCoord;\n" +
//                        "void main() {\n" +
//                        "    gl_Position = uMVPMatrix * aPosition;\n" +
//                        "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
//                        "}\n";
//
//        private static final String FRAGMENT_SHADER_EXT =
//                "#extension GL_OES_EGL_image_external : require\n" +
//                        "precision mediump float;\n" +
//                        "varying vec2 vTextureCoord;\n" +
//                        "uniform samplerExternalOES sTexture;\n" +
//                        "void main() {\n" +
//                        "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
//                        "}\n";
//
//        private static final float[] FULL_RECTANGLE_COORDS = {
//                -1.0f, -1.0f,   // 0 bottom left
//                1.0f, -1.0f,   // 1 bottom right
//                -1.0f, 1.0f,   // 2 top left
//                1.0f, 1.0f,   // 3 top right
//        };
//        private static final float[] FULL_RECTANGLE_TEX_COORDS = {
//                0.0f, 0.0f,     // 0 bottom left
//                1.0f, 0.0f,     // 1 bottom right
//                0.0f, 1.0f,     // 2 top left
//                1.0f, 1.0f      // 3 top right
//        };
//        private static final FloatBuffer FULL_RECTANGLE_BUF = createFloatBuffer(FULL_RECTANGLE_COORDS);
//        private static final FloatBuffer FULL_RECTANGLE_TEX_BUF = createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);
//
//        private static final int SIZEOF_FLOAT = 4;
//        private static final int COORDS_PER_VERTEX = 2;
//        private static final int TEX_COORD_STRIDE = 2 * SIZEOF_FLOAT;
//        private static final int VERTEX_STRIDE = COORDS_PER_VERTEX * SIZEOF_FLOAT;
//        private static final int VERTEX_COUNT = FULL_RECTANGLE_COORDS.length / COORDS_PER_VERTEX;
//
//        private int mProgramHandle;
//        private int muMVPMatrixLoc;
//        private int muTexMatrixLoc;
//        private int maPositionLoc;
//        private int maTextureCoordLoc;
//
//
//        public boolean tryInit() {
//            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
//            if (vertexShader == 0) {
//                return false;
//            }
//            int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_EXT);
//            if (pixelShader == 0) {
//                return false;
//            }
//
//            mProgramHandle = GLES20.glCreateProgram();
//            checkGlError("glCreateProgram");
//            if (mProgramHandle == 0) {
//                return false;
//            }
//            GLES20.glAttachShader(mProgramHandle, vertexShader);
//            checkGlError("glAttachShader");
//            GLES20.glAttachShader(mProgramHandle, pixelShader);
//            checkGlError("glAttachShader");
//            GLES20.glLinkProgram(mProgramHandle);
//            int[] linkStatus = new int[1];
//            GLES20.glGetProgramiv(mProgramHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
//            if (linkStatus[0] != GLES20.GL_TRUE) {
//                LogUtil.e("Could not link program: ");
//                LogUtil.e(GLES20.glGetProgramInfoLog(mProgramHandle));
//                GLES20.glDeleteProgram(mProgramHandle);
//                mProgramHandle = 0;
//            } else {
//                maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
//                maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
//                muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
//                muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix");
//                // Select the program.
//                GLES20.glUseProgram(mProgramHandle);
//                checkGlError("glUseProgram");
//
//                // Enable the "aPosition" vertex attribute.
//                GLES20.glEnableVertexAttribArray(maPositionLoc);
//                checkGlError("glEnableVertexAttribArray");
//
//                // Enable the "aTextureCoord" vertex attribute.
//                GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
//                checkGlError("glEnableVertexAttribArray");
//
//                // Connect vertexBuffer to "aPosition".
//                GLES20.glVertexAttribPointer(maPositionLoc, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, FULL_RECTANGLE_BUF);
//                checkGlError("glVertexAttribPointer");
//
//                // Connect texBuffer to "aTextureCoord".
//                GLES20.glVertexAttribPointer(maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, TEX_COORD_STRIDE, FULL_RECTANGLE_TEX_BUF);
//                checkGlError("glVertexAttribPointer");
//            }
//
//            return mProgramHandle != 0;
//        }
//
//        public void release() {
//            // Done -- disable vertex array, texture, and program.
//            GLES20.glDisableVertexAttribArray(maPositionLoc);
//            GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
//            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
//            GLES20.glUseProgram(0);
//            GLES20.glDeleteProgram(mProgramHandle);
//            mProgramHandle = -1;
//        }
//
//        public void draw(int textureID, float[] texMatrix, float[] projectionMatrix) {
//            // Set the texture unit.
//            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureID);
//
//            // Copy the model / view / projection matrix over.
//            GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, projectionMatrix, 0);
//            checkGlError("glUniformMatrix4fv");
//
//            // Copy the texture transformation matrix over.
//            GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);
//            checkGlError("glUniformMatrix4fv");
//
//            // Draw the rect.
//            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);
//            checkGlError("glDrawArrays");
//        }
//
//        public static int createOESTextureObject() {
//            int[] tex = new int[1];
//            //生成一个纹理
//            GLES20.glGenTextures(1, tex, 0);
//            //将此纹理绑定到外部纹理上
//            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
//            //设置纹理过滤参数
//            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
//            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
//            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
//            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
//            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
//            return tex[0];
//        }
//
//        public static void deleteOESTextureObject(int textureID) {
//            //删除纹理
//            GLES20.glDeleteTextures(1, new int[]{textureID}, 0);
//        }
//
//        private static int loadShader(int shaderType, String source) {
//            int shader = GLES20.glCreateShader(shaderType);
//            checkGlError("glCreateShader type=" + shaderType);
//            GLES20.glShaderSource(shader, source);
//            GLES20.glCompileShader(shader);
//            int[] compiled = new int[1];
//            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
//            if (compiled[0] == 0) {
//                LogUtil.e("Could not compile shader " + shaderType + ":");
//                LogUtil.e(" " + GLES20.glGetShaderInfoLog(shader));
//                GLES20.glDeleteShader(shader);
//                shader = 0;
//            }
//            return shader;
//        }
//
//        private static FloatBuffer createFloatBuffer(float[] coords) {
//            // Allocate a direct ByteBuffer, using 4 bytes per float, and copy coords into it.
//            ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
//            bb.order(ByteOrder.nativeOrder());
//            FloatBuffer fb = bb.asFloatBuffer();
//            fb.put(coords);
//            fb.position(0);
//            return fb;
//        }
//
//        private static void checkGlError(String op) {
//            int error = GLES20.glGetError();
//            if (error != GLES20.GL_NO_ERROR) {
//                String msg = op + ": glError 0x" + Integer.toHexString(error);
//                LogUtil.e(msg);
//                throw new RuntimeException(msg);
//            }
//        }
//    }
//}
