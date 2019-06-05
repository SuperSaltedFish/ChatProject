package com.yzx.chat.widget.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Size;
import android.view.Surface;

import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.util.BasicCamera;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidx.annotation.NonNull;

/**
 * Created by YZX on 2019年05月29日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class RecodeView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private static final Size DEFAULT_ASPECT_RATIO = new Size(16, 9);

    private final float[] mPreviewTextureMatrix = new float[16];

    private SurfaceTexture mPreviewSurfaceTexture;
    private Texture2dProgram mTexture2dProgram;
    private int mOESTextureID;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private CameraHelper mCameraHelper;
    private Size mAspectRatioSize = DEFAULT_ASPECT_RATIO;
    private int mCameraFacing;
    private boolean isRequestPreview;

    public RecodeView(Context context) {
        this(context, null);
    }

    public RecodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(3);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
//        setPreserveEGLContextOnPause(true);
        mCameraHelper = new CameraHelper();
        mCameraFacing = BasicCamera.CAMERA_FACING_BACK;
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
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        reset();
        mTexture2dProgram = new Texture2dProgram();
        if (!mTexture2dProgram.tryInit()) {
            mTexture2dProgram = null;
            return;
        }
        initPreviewSurfaceTexture();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mTexture2dProgram == null) {
            return;
        }
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        GLES20.glViewport(0, 0, width, height);
        openCameraIfClose();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        LogUtil.e("onDrawFrame");
        if (mTexture2dProgram == null || !mCameraHelper.isOpen()) {
            return;
        }
        mPreviewSurfaceTexture.updateTexImage();
        //获取外部纹理的矩阵，用来确定纹理的采样位置，没有此矩阵可能导致图像翻转等问题
        mPreviewSurfaceTexture.getTransformMatrix(mPreviewTextureMatrix);
        mTexture2dProgram.draw(mOESTextureID, mPreviewTextureMatrix);
    }

    public void initPreviewSurfaceTexture() {
        mOESTextureID = Texture2dProgram.createOESTextureObject();
        //根据外部纹理ID创建SurfaceTexture
        mPreviewSurfaceTexture = new SurfaceTexture(mOESTextureID);
        mPreviewSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                //每获取到一帧数据时请求OpenGL ES进行渲染
                requestRender();
            }
        });
    }

    private void openCameraIfClose() {
        if (mTexture2dProgram == null || mCameraHelper.isOpen()) {
            return;
        }
        mCameraHelper.post(new Runnable() {
            @Override
            public void run() {
                mCameraHelper.openCamera(getContext(), mCameraFacing, new BasicCamera.StateCallback() {
                    @Override
                    public void onCameraOpen() {
                        mCameraHelper.setPreviewSurfaceTexture(mPreviewSurfaceTexture);
                        mCameraHelper.autoSetOptimalPreviewSize(mSurfaceWidth, mSurfaceHeight, getDisplayRotation(), mAspectRatioSize, true);
                        if (isRequestPreview) {
                            startPreview();
                        }
                    }

                    @Override
                    public void onCameraClose() {

                    }

                    @Override
                    public void onCameraError(int error) {

                    }
                });
            }
        });
    }

    private void reset() {
        if (mCameraHelper != null) {
            mCameraHelper.closeCamera();
        }
        if (mTexture2dProgram != null) {
            final Texture2dProgram program = mTexture2dProgram;
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    program.release();
                }
            });
            mTexture2dProgram = null;
        }
        if (mPreviewSurfaceTexture != null) {
            mPreviewSurfaceTexture.release();
            mPreviewSurfaceTexture = null;
        }
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

    @Override
    public void onResume() {
        super.onResume();
        startPreview();
    }

    @Override
    public void onPause() {
        stopPreview();
        super.onPause();
    }

    public void onDestroy() {
        setPreserveEGLContextOnPause(false);
        super.onPause();
        reset();
    }

    protected int getDisplayRotation() {
        Context context = getContext();
        if (context instanceof Activity) {
            return ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        } else {
            return 0;
        }
    }

    protected static class CameraHelper {

        private static final int MAX_PREVIEW_WIDTH = 1920;
        private static final int MAX_PREVIEW_HEIGHT = 1080;

        private Handler mUIHandler;
        private BasicCamera mCamera;
        private Size mPreviewSize;
        private int mMaxZoom;
        private int mMinZoom;
        private int mCurrentZoom;

        public CameraHelper() {
            mUIHandler = new Handler(Looper.getMainLooper());
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
//                        mCamera.setPreviewFormat(ImageFormat.YUV_420_888);

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
                }
                mPreviewSize = null;
                mMaxZoom = 0;
                mCurrentZoom = 0;
                mUIHandler.removeCallbacksAndMessages(null);
            }
        }

        public Size autoSetOptimalPreviewSize(int desiredWidth, int desiredHeight, int displayRotation, Size aspectRatioSize, boolean isVideoMode) {
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
                return null;
            }
            if (mCamera instanceof BasicCamera.CameraImpl) {
                BasicCamera.CameraImpl camera = (BasicCamera.CameraImpl) mCamera;
                camera.setRecordingHint(true);
            }
            mCamera.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            return mPreviewSize;
        }

        private boolean setPreviewSurfaceTexture(@NonNull SurfaceTexture surfaceTexture) {
            return mCamera.setPreviewDisplay(surfaceTexture);
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
            }

            return mProgramHandle != 0;
        }

        public void release() {
            GLES20.glDeleteProgram(mProgramHandle);
            mProgramHandle = -1;
        }

        public void draw(int textureId, float[] texMatrix) {
            // Select the program.
            GLES20.glUseProgram(mProgramHandle);
            checkGlError("glUseProgram")
            ;
            // Set the texture.
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

            // Copy the model / view / projection matrix over.
            GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, IDENTITY_MATRIX, 0);
            checkGlError("glUniformMatrix4fv");

            // Copy the texture transformation matrix over.
            GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);
            checkGlError("glUniformMatrix4fv");

            // Enable the "aPosition" vertex attribute.
            GLES20.glEnableVertexAttribArray(maPositionLoc);
            checkGlError("glEnableVertexAttribArray");

            // Connect vertexBuffer to "aPosition".
            GLES20.glVertexAttribPointer(maPositionLoc, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, FULL_RECTANGLE_BUF);
            checkGlError("glVertexAttribPointer");

            // Enable the "aTextureCoord" vertex attribute.
            GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
            checkGlError("glEnableVertexAttribArray");

            // Connect texBuffer to "aTextureCoord".
            GLES20.glVertexAttribPointer(maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, TEX_COORD_STRIDE, FULL_RECTANGLE_TEX_BUF);
            checkGlError("glVertexAttribPointer");

            // Draw the rect.
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);
            checkGlError("glDrawArrays");

            // Done -- disable vertex array, texture, and program.
            GLES20.glDisableVertexAttribArray(maPositionLoc);
            GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
            GLES20.glUseProgram(0);
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

}
