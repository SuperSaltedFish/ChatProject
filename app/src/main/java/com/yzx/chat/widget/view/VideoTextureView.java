package com.yzx.chat.widget.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import com.yzx.chat.util.LogUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by YZX on 2018年03月24日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

@SuppressWarnings("MissingPermission")
@TargetApi(21)
public class VideoTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private static final String TAG = "VideoTextureView";

    private static final float RATIO_OF_4_TO_3 = 4 / 3f;
    private static final float RATIO_OF_16_TO_9 = 16 / 9f;
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    private static final int MIN_VIDEO_WIDTH = 480;
    private static final int MIN_VIDEO_HEIGHT = 480;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
        final CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager == null) {
            return;
        }
        final String cameraId = String.valueOf(CameraCharacteristics.LENS_FACING_FRONT);
        try {
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    mCamera = camera;
                    mPreviewSurface = new Surface(getSurfaceTexture());
                    try {
                        CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                        StreamConfigurationMap streamConfMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                        if (streamConfMap == null) {
                            return;
                        }
                        mOptimalSize = chooseOptimalSize(streamConfMap.getOutputSizes(SurfaceTexture.class), MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, MIN_VIDEO_WIDTH, MIN_VIDEO_HEIGHT, mRatio);
                        if (mOptimalSize == null) {
                            return;
                        }
                    } catch (CameraAccessException e) {
                        throw new IllegalStateException("Failed to get configuration map: " + cameraId);
                    }
                    initMediaCodec();
                    initCamera();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    mCamera = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    mCamera = null;
                }
            }, null);
        } catch (CameraAccessException e) {
            throw new RuntimeException("Failed to open camera: " + cameraId, e);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        destroy();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private Context mContext;

    private Surface mPreviewSurface;
    private Size mOptimalSize;
    private CameraDevice mCamera;
    private CaptureRequest.Builder mPreviewAndVideoRequestBuilder;
    private CameraCaptureSession mCaptureSession;

    private Handler mEncodeHandler;
    private HandlerThread mEncodeThread;

    private MediaCodec mMediaCodec;
    private Surface mMediaCodecInputSurface;

    private String mSavePath;
    private BufferedOutputStream mOutputStream;

    private float mRatio;
    private boolean isOpenFlash;
    private boolean isRecorderMode;

    private int mViewWidth;
    private int mViewHeight;

    public VideoTextureView(Context context) {
        this(context, null);
    }

    public VideoTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setAspectRatio(RATIO_OF_16_TO_9);
        setSurfaceTextureListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int ratioW;
        int ratioH;
        if (mRatio == RATIO_OF_4_TO_3) {
            ratioW = 3;
            ratioH = 4;
        } else {
            ratioW = 9;
            ratioH = 16;
        }
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            float ratio = ratioH / (float) ratioW;
            int height = (int) (MeasureSpec.getSize(widthMeasureSpec) * ratio);
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            float ratio = ratioW / (float) ratioH;
            int width = (int) (MeasureSpec.getSize(heightMeasureSpec) * ratio);
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), heightMeasureSpec);
        } else {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            if (height <= width * ratioH / ratioW) {
                width = height * ratioW / ratioH;
                super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            } else {
                height = width * ratioH / ratioW;
                super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            }
        }
    }

    private void initMediaCodec() {
        try {
            mMediaCodec = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mOptimalSize.getWidth(), mOptimalSize.getHeight());
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 1250000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000 / 30);
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.setCallback(mEncoderCallback);
        mMediaCodecInputSurface = mMediaCodec.createInputSurface();
    }


    private void initCamera() {
        if (mMediaCodec == null || mCamera == null) {
            return;
        }

        getSurfaceTexture().setDefaultBufferSize(mOptimalSize.getWidth(), mOptimalSize.getHeight());
        try {
            mCamera.createCaptureSession(Arrays.asList(mPreviewSurface, mMediaCodecInputSurface), mSessionStateCallback, null);
        } catch (CameraAccessException e) {
            throw new RuntimeException("Failed to start camera session");
        }

    }

    private void update() {
        if (mPreviewAndVideoRequestBuilder == null) {
            try {
                mPreviewAndVideoRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                mPreviewAndVideoRequestBuilder.addTarget(mPreviewSurface);
                mPreviewAndVideoRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                return;
            }
        }
        if (isOpenFlash) {
            mPreviewAndVideoRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
        } else {
            mPreviewAndVideoRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
        }
        mPreviewAndVideoRequestBuilder.removeTarget(mMediaCodecInputSurface);
        if (isRecorderMode) {
            mPreviewAndVideoRequestBuilder.addTarget(mMediaCodecInputSurface);
        }
        if (mCaptureSession != null) {
            try {
                mCaptureSession.setRepeatingRequest(mPreviewAndVideoRequestBuilder.build(), null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void destroy() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mCamera != null) {
            mCamera.close();
            mCamera = null;
        }

        if (mMediaCodec != null) {
            mMediaCodec.reset();
            mMediaCodec.release();
            mMediaCodec = null;
        }

        if (mEncodeHandler != null) {
            mEncodeHandler.removeCallbacksAndMessages(null);
            mEncodeHandler = null;
        }
        if (mEncodeThread != null) {
            mEncodeThread.quit();
            mEncodeThread = null;
        }

        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean startRecorder(String savePath) {
        if (isRecorderMode || TextUtils.isEmpty(savePath) || mCaptureSession == null) {
            return false;
        }

        mSavePath = savePath;
        File file = new File(savePath);
        if (file.exists() && !file.delete()) {
            LogUtil.e("delete file fail");
            return false;
        }
        try {
            if (!file.createNewFile()) {
                LogUtil.e("createNewFile fail");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            mOutputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        isRecorderMode = true;
        mMediaCodec.start();
        update();
        return true;
    }

    public void stopRecorder(final boolean isSave) {
        if (!isRecorderMode) {
            return;
        }
        isRecorderMode = false;
        update();
        mMediaCodec.stop();
        mMediaCodec.reset();
        mEncodeHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mOutputStream = null;
                if (!isSave) {
                    File file = new File(mSavePath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        });
    }


    public void setAspectRatio(float ratio) {
        mRatio = ratio;
    }

    public void setOpenFlash(boolean isOpenFlash) {
        if (this.isOpenFlash == isOpenFlash) {
            return;
        }
        this.isOpenFlash = isOpenFlash;
        update();
    }

    public boolean isOpenFlash() {
        return isOpenFlash;
    }


    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mCaptureSession = session;
            update();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            mCaptureSession = null;
        }

        @Override
        public void onClosed(@NonNull CameraCaptureSession session) {
            super.onClosed(session);
            mCaptureSession = null;
        }
    };

    private MediaCodec.Callback mEncoderCallback = new MediaCodec.Callback() {
        private byte[] mDataBuff;

        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

        }

        @Override
        public void onOutputBufferAvailable(final @NonNull MediaCodec codec, final int index,final @NonNull MediaCodec.BufferInfo info) {
            if (mEncodeThread == null) {
                mEncodeThread = new HandlerThread(TAG);
                mEncodeHandler = new Handler(mEncodeThread.getLooper());
            }
            mEncodeHandler.post(new Runnable() {
                @Override
                public void run() {
                    LogUtil.e(index + "");
                    ByteBuffer outPutByteBuffer = codec.getOutputBuffer(index);
                    if (outPutByteBuffer != null && outPutByteBuffer.limit() > 0) {
                        int dataSize = info.size;
                        if (mDataBuff == null || mDataBuff.length < dataSize) {
                            mDataBuff = new byte[dataSize];
                        }
                        outPutByteBuffer.get(mDataBuff);
                        try {
                            mOutputStream.write(mDataBuff, 0, dataSize);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    codec.releaseOutputBuffer(index, false);
                }
            });

        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
            LogUtil.e("MediaCodec error:" + e.toString());
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

        }
    };


    private static Size chooseOptimalSize(Size[] allSize, int maxWidth, int maxHeight, int minWidth, int minHeight, float ratio) {
        if (allSize == null || allSize.length == 0) {
            return null;
        }
        if (minWidth < minHeight) {
            int temp = minWidth;
            minWidth = minHeight;
            minHeight = temp;
        }
        List<Size> suitableSizeList = new ArrayList<>();
        for (Size size : allSize) {
            int sizeWidth = size.getWidth();
            int sizeHeight = size.getHeight();
            if ((sizeWidth <= maxWidth || sizeHeight <= maxHeight) && (float) sizeWidth / sizeHeight == ratio) {
                suitableSizeList.add(size);
            }
        }

        if (suitableSizeList.size() != 0) {
            Collections.sort(suitableSizeList, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight();
                }
            });
            int index = 0;
            for (int i = 0, size = suitableSizeList.size() - 1; i < size; i++) {
                index = i + 1;
                if ((suitableSizeList.get(index).getWidth() > minWidth && suitableSizeList.get(index).getHeight() > minHeight)) {
                    index = i;
                    break;
                }
            }
            return suitableSizeList.get(index);
        } else {
            return allSize[0];
        }
    }

}
