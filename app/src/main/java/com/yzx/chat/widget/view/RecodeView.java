package com.yzx.chat.widget.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.media.MediaFormat;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.util.BasicCamera;
import com.yzx.chat.util.VideoEncoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by YZX on 2019年05月29日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class RecodeView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private static final Size DEFAULT_ASPECT_RATIO = new Size(16, 9);

    private final float[] mPreviewTextureMatrix = new float[16];

    private SurfaceTexture mPreviewSurfaceTexture;
    private Texture2dProgram mPreviewTexture2dProgram;
    private int mOESTextureID = -1;

    private CameraHelper mCameraHelper;
    private Size mAspectRatioSize = DEFAULT_ASPECT_RATIO;
    private int mCameraFacing;
    private boolean isRequestPreview;

    private EGLVideoEncoder mRecodeEGL;
    private Texture2dProgram mRecodeTexture2dProgram;
    private VideoEncoder mVideoEncoder;
    private Surface mVideoSurface;
    private volatile boolean isStartRecording;

    public RecodeView(Context context) {
        this(context, null);
    }

    public RecodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(3);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mCameraHelper = new CameraHelper();
        mCameraFacing = BasicCamera.CAMERA_FACING_BACK;
        mRecodeEGL = new EGLVideoEncoder();
        initVideoEncoder();
    }

    private void initVideoEncoder() {
        try {
            mVideoEncoder = new VideoEncoder();
            MediaFormat videoFormat = mVideoEncoder.createDefaultVideoMediaFormat(960 * 1440 / 2560, 960, 15);
            MediaFormat audioFormat = mVideoEncoder.createDefaultAudioMediaFormat();
            mVideoSurface = mVideoEncoder.configureCodec(videoFormat, audioFormat);
        } catch (IOException | RuntimeException e) {
            if (mVideoEncoder != null) {
                mVideoEncoder.release();
                mVideoEncoder = null;
            }
            e.printStackTrace();
            LogUtil.e("initVideoEncoder error");
        }
    }

    private void releaseVideoEncoder() {
        if (mVideoSurface != null) {
            mVideoSurface.release();
            mVideoSurface = null;
        }
        if (mVideoEncoder != null) {
            mVideoEncoder.release();
            mVideoEncoder = null;
        }
    }

    private void initPreviewGL() {
        mPreviewTexture2dProgram = new Texture2dProgram();
        if (!mPreviewTexture2dProgram.tryInit()) {
            releasePreviewGL();
            return;
        }
        //根据外部纹理ID创建SurfaceTexture
        mOESTextureID = Texture2dProgram.createOESTextureObject();
        mPreviewSurfaceTexture = new SurfaceTexture(mOESTextureID);
        mPreviewSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                //每获取到一帧数据时请求OpenGL ES进行渲染
                requestRender();
            }
        });
        openCamera();
    }

    private void releasePreviewGL() {
        if (mCameraHelper != null) {
            mCameraHelper.closeCamera();
        }
        if (mPreviewTexture2dProgram != null) {
            mPreviewTexture2dProgram.release();
            mPreviewTexture2dProgram = null;
        }
        if (mPreviewSurfaceTexture != null) {
            mPreviewSurfaceTexture.setOnFrameAvailableListener(null);
            mPreviewSurfaceTexture.release();
            mPreviewSurfaceTexture = null;
        }
        mOESTextureID = -1;
    }

    private void initRecodeGL() {
        if (mVideoEncoder == null) {
            return;
        }
        if (mRecodeEGL.initEGL(EGL14.eglGetCurrentContext(), mVideoSurface)) {
            mRecodeEGL.queueEvent(new Runnable() {
                @Override
                public void run() {
                    Texture2dProgram program = new Texture2dProgram();
                    if (program.tryInit()) {
                        mRecodeTexture2dProgram = program;
                    } else {
                        mRecodeEGL.releaseEGL();
                    }
                }
            });
        }
    }

    private void releaseRecodeGL() {
        if (mRecodeEGL.isInitialized()) {
            mRecodeEGL.queueEvent(new Runnable() {
                @Override
                public void run() {
                    if (mRecodeTexture2dProgram != null) {
                        mRecodeTexture2dProgram.release();
                        mRecodeTexture2dProgram = null;
                    }
                    mRecodeEGL.releaseEGL();
                }
            });
        }
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
    public void onResume() {
        super.onResume();
        startPreview();
    }

    @Override
    public void onPause() {
        stopPreview();
        stopRecode();
        queueEvent(new Runnable() {
            @Override
            public void run() {
                releaseRecodeGL();
                releasePreviewGL();
            }
        });
        super.onPause();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        initPreviewGL();
        initRecodeGL();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        setCameraPreviewSize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mPreviewTexture2dProgram == null || !mCameraHelper.isOpen()) {
            return;
        }
        mPreviewSurfaceTexture.updateTexImage();
        //获取外部纹理的矩阵，用来确定纹理的采样位置，没有此矩阵可能导致图像翻转等问题
        mPreviewSurfaceTexture.getTransformMatrix(mPreviewTextureMatrix);
        mPreviewTexture2dProgram.draw(mOESTextureID, mPreviewTextureMatrix);
        if (isStartRecording && mRecodeEGL.isInitialized()) {
            mRecodeEGL.queueEvent(mDrawVideoDataRunnable);
        }
    }

    private final Runnable mDrawVideoDataRunnable = new Runnable() {
        @Override
        public void run() {
            mRecodeTexture2dProgram.draw(mOESTextureID, mPreviewTextureMatrix);
            mRecodeEGL.swapBuffers();
        }
    };

    @Override
    public void setPreserveEGLContextOnPause(boolean preserveOnPause) {
        super.setPreserveEGLContextOnPause(false);
    }

    private void openCamera() {
        mCameraHelper.post(new Runnable() {
            @Override
            public void run() {
                if (mCameraHelper.isOpen()) {
                    mCameraHelper.closeCamera();
                }
                mCameraHelper.openCamera(getContext(), mCameraFacing, new BasicCamera.StateCallback() {
                    @Override
                    public void onCameraOpen() {
                        mCameraHelper.setPreviewSurfaceTexture(mPreviewSurfaceTexture);
                        if (isRequestPreview && mCameraHelper.getPreviewSize() != null) {
                            startPreview();
                        }
                    }

                    @Override
                    public void onCameraClose() {
                        LogUtil.e("onCameraClose");
                    }

                    @Override
                    public void onCameraError(int error) {
                        LogUtil.e("onCameraError");
                    }
                });
            }
        });
    }

    private void setCameraPreviewSize(final int width, final int height) {
        mCameraHelper.post(new Runnable() {
            @Override
            public void run() {
                mCameraHelper.autoChooseOptimalPreviewSize(width, height, getDisplayRotation(), mAspectRatioSize, true);
                if (isRequestPreview) {
                    startPreview();
                }
            }
        });
    }

    public void startPreview() {
        isRequestPreview = true;
        if (mCameraHelper.isOpen() && !mCameraHelper.isPreviewing()) {
            mCameraHelper.startPreview();
        }
    }

    public void stopPreview() {
        isRequestPreview = false;
        if (mCameraHelper.isPreviewing()) {
            mCameraHelper.startPreview();
        }
    }

    public void startRecode(String saveFile) {
        isStartRecording = true;
        if (mVideoEncoder != null && !mVideoEncoder.isRunning()) {
            mVideoEncoder.start(saveFile, 0);
        }
    }

    public void stopRecode() {
        isStartRecording = false;
        if (mVideoEncoder != null && mVideoEncoder.isRunning()) {
            mVideoEncoder.stop();
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

    protected static class CameraHelper extends Handler {

        private static final int MAX_PREVIEW_WIDTH = 1920;
        private static final int MAX_PREVIEW_HEIGHT = 1080;

        public static final int MSG_OPEN_CAMERA = 1;
        public static final int MSG_CLOSE_CAMERA = 2;
        public static final int MSG_SET_PREVIEW_SIZE = 3;
        public static final int MSG_START_PREVIEW = 4;
        public static final int MSG_STOP_PREVIEW = 5;
        public static final int MSG_OPEN_FLASH = 6;
        public static final int MSG_CLOSE_FLASH = 7;

        private Handler mUIHandler;
        private BasicCamera mCamera;
        private Size mPreviewSize;
        private int mMaxZoom;
        private int mMinZoom;
        private int mCurrentZoom;

        private int mDesiredWidth;
        private int mDesiredHeight;
        private int mDisplayRotation;
        private Size mAspectRatioSize;
        private boolean isVideoMode;
        private boolean hasDesiredData;

        public CameraHelper() {
            super(Looper.getMainLooper());
            mUIHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_OPEN_CAMERA:
                    openCamera(msg.obj,msg.arg1);
                    break;
                case MSG_CLOSE_CAMERA:
                    break;
                case MSG_SET_PREVIEW_SIZE:
                    break;
                case MSG_START_PREVIEW:
                    break;
                case MSG_STOP_PREVIEW:
                    break;
                case MSG_OPEN_FLASH:
                    break;
                case MSG_CLOSE_FLASH:
                    break;
            }
        }

        public void openCamera(Context context, @BasicCamera.FacingType int cameraFacing, final BasicCamera.StateCallback callback) {
            final BasicCamera camera = BasicCamera.createCameraCompat(context, cameraFacing);
            if (camera != null) {
                camera.openCamera(new BasicCamera.StateCallback() {
                    @Override
                    public void onCameraOpen() {
                        mCamera = camera;
                        mCamera.setDisplayOrientationIfSupport(90);//下面的代码在横屏的时候无效，不知道为什么(无论前置还是后置)
//                    mCamera.setDisplayOrientationIfSupport(calculateCameraRotationAngle(getDisplayRotation(), mCamera.getSensorOrientation(), mCameraFacingType));
                        mMaxZoom = mCamera.getMaxZoomValue();
                        mMinZoom = mCamera.getMinZoomValue();
                        mCurrentZoom = mMinZoom;
                        if (hasDesiredData) {
                            autoChooseOptimalPreviewSize(mDesiredWidth, mDesiredHeight, mDisplayRotation, mAspectRatioSize, isVideoMode);
                        }

                        if (callback != null) {
                            callback.onCameraOpen();
                        }
                    }

                    @Override
                    public void onCameraClose() {
                        closeCamera();
                        if (callback != null) {
                            callback.onCameraClose();
                        }
                    }

                    @Override
                    public void onCameraError(int error) {
                        closeCamera();
                        if (callback != null) {
                            callback.onCameraError(error);
                        }
                    }
                });
            }
        }


        public void closeCamera() {
            synchronized (this) {
                if (mCamera != null) {
                    mCamera.closeCamera();
                    mCamera = null;
                    mPreviewSize = null;
                    mMaxZoom = 0;
                    mCurrentZoom = 0;
                    hasDesiredData = false;
                    mUIHandler.removeCallbacksAndMessages(null);
                }
            }
        }

        public void autoChooseOptimalPreviewSize(int desiredWidth, int desiredHeight, int displayRotation, Size aspectRatioSize, boolean isVideoMode) {
            mDesiredWidth = desiredWidth;
            mDesiredHeight = desiredHeight;
            mDisplayRotation = displayRotation;
            mAspectRatioSize = aspectRatioSize;
            this.isVideoMode = isVideoMode;
            hasDesiredData = true;
            if (mCamera == null) {
                return;
            }
            int cameraOrientation = mCamera.getSensorOrientation();
            boolean swappedDimensions = false;
            switch (displayRotation) {
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
                mPreviewSize = mCamera.calculateOptimalDisplaySize(SurfaceTexture.class, desiredHeight, desiredWidth, MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, aspectRatioSize, isVideoMode);
            } else {
                mPreviewSize = mCamera.calculateOptimalDisplaySize(SurfaceTexture.class, desiredWidth, desiredHeight, MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, aspectRatioSize, isVideoMode);
            }
            if (mPreviewSize == null) {
                return;
            }
            if (mCamera instanceof BasicCamera.CameraImpl) {
                BasicCamera.CameraImpl camera = (BasicCamera.CameraImpl) mCamera;
                camera.setRecordingHint(true);
            }
            mCamera.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        }

        public Size getPreviewSize() {
            return mPreviewSize;
        }

        public void setPreviewSurfaceTexture(@NonNull SurfaceTexture surfaceTexture) {
            mCamera.setPreviewDisplay(surfaceTexture);
        }

        public void startPreview() {
            if (mCamera.isOpen()) {
                mCamera.starPreview();
            }
        }

        public void stopPreview() {
            if (mCamera != null && mCamera.isPreviewing()) {
                mCamera.stopPreview();
            }
        }

        public boolean isOpen() {
            if (mCamera != null) {
                return mCamera.isOpen();
            }
            return false;
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


        public void post(Runnable runnable) {
            mUIHandler.post(runnable);
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

        private static final float FULL_RECTANGLE_COORDS[] = {
                -1.0f, -1.0f,   // 0 bottom left
                1.0f, -1.0f,   // 1 bottom right
                -1.0f, 1.0f,   // 2 top left
                1.0f, 1.0f,   // 3 top right
        };
        private static final float FULL_RECTANGLE_TEX_COORDS[] = {
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
        private static final float[] IDENTITY_MATRIX;

        static {
            IDENTITY_MATRIX = new float[16];
            Matrix.setIdentityM(IDENTITY_MATRIX, 0);
        }

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

        public void draw(int textureID, float[] texMatrix) {
            // Set the texture unit.
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureID);

            // Copy the model / view / projection matrix over.
            GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, IDENTITY_MATRIX, 0);
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

        private static void checkGlError(String op) {
            int error = GLES20.glGetError();
            if (error != GLES20.GL_NO_ERROR) {
                String msg = op + ": glError 0x" + Integer.toHexString(error);
                LogUtil.e(msg);
                throw new RuntimeException(msg);
            }
        }
    }

    private static class EGLVideoEncoder {//负责创建EGL环境，把GL内容写入Surface

        private static final String TAG = EGLVideoEncoder.class.getName();
        // Android-specific extension.
        private static final int EGL_RECORDABLE_ANDROID = 0x3142;

        private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
        private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
        private android.opengl.EGLConfig mEGLConfig = null;

        private Handler mGLHandler;

        private Semaphore mInitializeLock = new Semaphore(1);

        private boolean initEGL(@Nullable EGLContext sharedContext, Object surface) {
            try {
                mInitializeLock.acquireUninterruptibly();
                if (mEGLDisplay != EGL14.EGL_NO_DISPLAY || mEGLContext != EGL14.EGL_NO_CONTEXT) {
                    throw new RuntimeException("The TextureEncoder already initialization");
                }
                if (!(surface instanceof Surface) && !(surface instanceof SurfaceTexture)) {
                    throw new RuntimeException("invalid surface: " + surface);
                }
                final boolean[] isSuccessful = new boolean[1];
                isSuccessful[0] = initEGLDisplay() && initEGLConfig() && initEGLContent(sharedContext) && initEGLContent(surface);
                if (isSuccessful[0]) {
                    HandlerThread handlerThread = new HandlerThread(TAG);
                    handlerThread.start();
                    mGLHandler = new Handler(handlerThread.getLooper());
                    final CountDownLatch latch = new CountDownLatch(1);
                    mGLHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            isSuccessful[0] = makeCurrent();
                            latch.countDown();
                        }
                    });
                    try {
                        latch.await();
                    } catch (InterruptedException ignored) {
                    }
                }
                if (!isSuccessful[0]) {
                    internalReleaseEGL();
                }
                return isSuccessful[0];
            } finally {
                mInitializeLock.release();
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

        private boolean initEGLConfig() {
            // The actual surface is generally RGBA or RGBX, so situationally omitting alpha
            // doesn't really help.  It can also lead to a huge performance hit on glReadPixels()
            // when reading into a GL_RGBA buffer.
            int[] attributesList = {
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL_RECORDABLE_ANDROID, 1,      // placeholder for recordable [@-3]
                    EGL14.EGL_NONE
            };
            android.opengl.EGLConfig[] configs = new android.opengl.EGLConfig[1];
            int[] numConfigs = new int[1];
            if (!EGL14.eglChooseConfig(mEGLDisplay, attributesList, 0, configs, 0, configs.length, numConfigs, 0)) {
                Log.e(TAG, "Unable to find a suitable EGLConfig");
                return false;
            }
            mEGLConfig = configs[0];
            return true;
        }

        private boolean initEGLContent(EGLContext sharedContext) {
            int[] attributesList = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
            };
            mEGLContext = EGL14.eglCreateContext(mEGLDisplay, mEGLConfig, sharedContext, attributesList, 0);
            return !hasEglError("eglCreateContext");
        }


        private boolean initEGLContent(Object surface) {
            // Create a window surface, and attach it to the Surface we received.
            int[] surfaceAttributes = {
                    EGL14.EGL_NONE
            };
            mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface,
                    surfaceAttributes, 0);
            return !hasEglError("eglCreateWindowSurface") && mEGLSurface != null;
        }

        private boolean makeCurrent() {
            return EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
        }

        public void releaseEGL() {
            try {
                mInitializeLock.acquireUninterruptibly();
                if (mGLHandler != null) {
                    if (mGLHandler.getLooper() == Looper.myLooper()) {
                        internalReleaseEGL();
                    } else {
                        final CountDownLatch latch = new CountDownLatch(1);
                        mGLHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                internalReleaseEGL();
                                latch.countDown();
                            }
                        });
                        try {
                            latch.await();
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            } finally {
                mInitializeLock.release();
            }
        }

        private void internalReleaseEGL() {
            if (mGLHandler != null) {
                mGLHandler.removeCallbacksAndMessages(null);
                mGLHandler.getLooper().quit();
                mGLHandler = null;
            }
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                // Android is unusual in that it uses a reference-counted EGLDisplay.  So for
                // every eglInitialize() we need an eglTerminate().
                EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
                EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
                if (mEGLSurface != null) {
                    EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
                }
                EGL14.eglReleaseThread();
                EGL14.eglTerminate(mEGLDisplay);
            }

            mEGLDisplay = EGL14.EGL_NO_DISPLAY;
            mEGLContext = EGL14.EGL_NO_CONTEXT;
            mEGLSurface = EGL14.EGL_NO_SURFACE;
            mEGLConfig = null;
        }

        public boolean isInitialized() {
            try {
                mInitializeLock.acquireUninterruptibly();
                return mEGLDisplay != EGL14.EGL_NO_DISPLAY;
            } finally {
                mInitializeLock.release();
            }
        }

        public boolean swapBuffers() {
            if (isInitialized()) {
                return EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
            }
            return false;
        }

        public void queueEvent(Runnable r) {
            try {
                mInitializeLock.acquireUninterruptibly();
                if (mGLHandler != null) {
                    mGLHandler.post(r);
                }
            } finally {
                mInitializeLock.release();
            }
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                    // We're limited here -- finalizers don't run on the thread that holds
                    // the EGL state, so if a surface or context is still current on another
                    // thread we can't fully release it here.  Exceptions thrown from here
                    // are quietly discarded.  Complain in the log file.
                    releaseEGL();
                }
            } finally {
                super.finalize();
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
    }

}
