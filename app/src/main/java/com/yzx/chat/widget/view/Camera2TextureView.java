package com.yzx.chat.widget.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by YZX on 2017年07月12日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


@SuppressWarnings("MissingPermission")
@TargetApi(21)
public class Camera2TextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private static final String TAG = "Camera2TextureView";
    private static final float RATIO_OF_4_TO_3 = 4 / 3f;
    private static final float RATIO_OF_16_TO_9 = 16 / 9f;
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        startOpeningCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        startCaptureSession();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        onDestroy();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private final CameraDevice.StateCallback mCameraStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCamera = camera;
            collectCameraInfo();
            startCaptureSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCamera = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCamera = null;
        }
    };


    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mCaptureSession = session;
            updateFlash();
            try {
                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mPreviewCaptureCallback, mDecodeHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
        }

        @Override
        public void onClosed(@NonNull CameraCaptureSession session) {
            super.onClosed(session);
            mCaptureSession = null;
        }
    };

    private final CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            if (((Integer) (CaptureRequest.CONTROL_AF_STATE_PASSIVE_FOCUSED)).equals(result.get(CaptureResult.CONTROL_AF_STATE)) && !isParsing && !isStop) {
                try {
                    isParsing = true;
                    session.capture(mPictureRequestBuilder.build(), null, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = mImageReader.acquireLatestImage();
            if (image != null) {
                try {
                    if (mOnCaptureCallback != null) {
                        if (mImageBuffer == null) {
                            mImageBuffer = new byte[mImageReader.getWidth() * mImageReader.getHeight()];
                        }
                        Image.Plane[] planes = image.getPlanes();
                        ByteBuffer byteBuffer = planes[0].getBuffer();
                        byteBuffer.get(mImageBuffer);
                        mOnCaptureCallback.onCaptureFrameAtFocus(mImageBuffer, mImageReader.getWidth(), mImageReader.getHeight());
                    }
                } finally {
                    image.close();
                    isParsing = false;
                }
            }
        }
    };

    public interface OnCaptureCallback {
        void onCaptureFrameAtFocus(byte[] data, int width, int height);
    }

    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mRatioW;
    private int mRatioH;
    private float mRatio;
    private boolean isParsing;
    private boolean isOpenFlash;
    private boolean isStop;

    private Context mContext;
    private Handler mDecodeHandler;
    private HandlerThread mDecodeThread;
    private ImageReader mImageReader;
    private Surface mSurface;
    private Size[] mSupportPreviewSizes;
    private Size[] mSupportPictureSizes;
    private Size mPictureSize;
    private byte[] mImageBuffer;
    private OnCaptureCallback mOnCaptureCallback;

    private String mCameraId;
    private CameraDevice mCamera;
    private CameraManager mCameraManager;
    private CompareSizesByArea mSizeComparator;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest.Builder mPictureRequestBuilder;
    private CameraCaptureSession mCaptureSession;


    public Camera2TextureView(Context context) {
        this(context, null);
    }

    public Camera2TextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Camera2TextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setAspectRatio(RATIO_OF_16_TO_9);
        setSurfaceTextureListener(this);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        configureAspectRatio();
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            float ratio = mRatioH / (float) mRatioW;
            int height = (int) (MeasureSpec.getSize(widthMeasureSpec) * ratio);
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            float ratio = mRatioW / (float) mRatioH;
            int width = (int) (MeasureSpec.getSize(heightMeasureSpec) * ratio);
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), heightMeasureSpec);
        } else {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            if (height <= width * mRatioH / mRatioW) {
                width = height * mRatioW / mRatioH;
                super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            } else {
                height = width * mRatioH / mRatioW;
                super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            }
        }
    }


    public void onResume() {
        if (mCamera != null) {
            if (isStop) {
                try {
                    mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mPreviewCaptureCallback, mDecodeHandler);
                    isStop = false;
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onPause() {
        if (mCamera != null) {
            isStop = true;
            if (mDecodeHandler != null) {
                mDecodeHandler.removeCallbacksAndMessages(null);
            }
            try {
                mCaptureSession.stopRepeating();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void onDestroy() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mCamera != null) {
            mCamera.close();
            mCamera = null;
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
        if (mDecodeHandler != null) {
            mDecodeHandler.removeCallbacksAndMessages(null);
            mDecodeHandler = null;
        }
        if (mDecodeThread != null) {
            mDecodeThread.quit();
            mDecodeThread = null;
        }
        mImageBuffer = null;
    }

    private void configureAspectRatio() {
        if (mRatio == RATIO_OF_4_TO_3) {
            mRatioW = 3;
            mRatioH = 4;
        } else {
            mRatioW = 9;
            mRatioH = 16;
        }

    }

    private void startOpeningCamera() {
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mCameraId = String.valueOf(CameraCharacteristics.LENS_FACING_FRONT);
        mSurface = new Surface(getSurfaceTexture());
        try {
            mCameraManager.openCamera(mCameraId, mCameraStateCallback, null);
        } catch (CameraAccessException e) {
            throw new RuntimeException("Failed to open camera: " + mCameraId, e);
        }
    }

    private void collectCameraInfo() {
        CameraCharacteristics cameraCharacteristics;
        try {
            cameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);
        } catch (CameraAccessException e) {
            throw new IllegalStateException("Failed to get configuration map: " + mCameraId);
        }
        StreamConfigurationMap streamConfMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        assert streamConfMap != null;
        mSupportPreviewSizes = streamConfMap.getOutputSizes(SurfaceTexture.class);
        mSupportPictureSizes = streamConfMap.getOutputSizes(ImageFormat.YUV_420_888);
    }

    private void startCaptureSession() {
        if (mCamera == null) {
            return;
        }
        initPreviewSurface();
        initImageReader();
        if (mPreviewRequestBuilder == null) {
            mPreviewRequestBuilder = obtainPreviewCaptureRequest(mCamera);
        }
        if (mPictureRequestBuilder == null) {
            mPictureRequestBuilder = obtainTakePictureCaptureRequest(mCamera);
        }
        if (mPreviewRequestBuilder != null && mPictureRequestBuilder != null) {
            if (mCaptureSession != null) {
                mCaptureSession.close();
            }
            try {
                mCamera.createCaptureSession(Arrays.asList(mSurface, mImageReader.getSurface()), mSessionStateCallback, mDecodeHandler);
            } catch (CameraAccessException e) {
                throw new RuntimeException("Failed to start camera session");
            }
        }
    }

    private void initPreviewSurface() {
        mPreviewWidth = getWidth();
        mPreviewHeight = getHeight();
        Size optimalSize = chooseOptimalSize(mSupportPreviewSizes, mPreviewWidth, mPreviewHeight, mRatio);
        getSurfaceTexture().setDefaultBufferSize(optimalSize.getWidth(), optimalSize.getHeight());
    }

    private void initImageReader() {
        mPictureSize = chooseOptimalSize(mSupportPictureSizes, mPreviewWidth, mPreviewHeight, mRatio);
        if (mImageReader != null) {
            mImageReader.close();
        }
        mImageReader = ImageReader.newInstance(mPictureSize.getWidth(), mPictureSize.getHeight(), ImageFormat.YUV_420_888, 1);
        if (mDecodeThread == null) {
            mDecodeThread = new HandlerThread(TAG);
            mDecodeThread.start();
            mDecodeHandler = new Handler(mDecodeThread.getLooper());
        }
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mDecodeHandler);
    }

    private void updateFlash() {
        if (mCamera != null && mCaptureSession != null) {
            if (isOpenFlash) {
                mPictureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
            } else {
                mPictureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            }
            try {
                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mPreviewCaptureCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }


    private CaptureRequest.Builder obtainPreviewCaptureRequest(CameraDevice camera) {
        CaptureRequest.Builder captureRequest;
        try {
            captureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return null;
        }
        captureRequest.addTarget(mSurface);
        captureRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        return captureRequest;
    }

    private CaptureRequest.Builder obtainTakePictureCaptureRequest(CameraDevice camera) {
        CaptureRequest.Builder captureRequest;
        try {
            captureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return null;
        }
        captureRequest.addTarget(mImageReader.getSurface());
        captureRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
        return captureRequest;
    }

    private Size chooseOptimalSize(Size[] allSize, int width, int height, float ratio) {
        if (width < height) {
            int temp = width;
            width = height;
            height = temp;
        }
        List<Size> suitableSizeList = new ArrayList<>();
        for (Size size : allSize) {
            int sizeWidth = size.getWidth();
            int sizeHeight = size.getHeight();
            if ((sizeWidth <= MAX_PREVIEW_WIDTH || sizeHeight <= MAX_PREVIEW_HEIGHT) && (float) sizeWidth / sizeHeight == ratio) {
                suitableSizeList.add(size);
            }
        }
        if (mSizeComparator == null) {
            mSizeComparator = new CompareSizesByArea();
        }
        if (suitableSizeList.size() != 0) {
            Collections.sort(suitableSizeList, mSizeComparator);
            int index = 0;
            for (int i = 0, size = suitableSizeList.size() - 1; i < size; i++) {
                index = i + 1;
                if ((suitableSizeList.get(index).getWidth() > width && suitableSizeList.get(index).getHeight() > height)) {
                    index = i;
                    break;
                }
            }
            return suitableSizeList.get(index);
        } else {
            return allSize[0];
        }
    }

    public void setAspectRatio(float ratio) {
        mRatio = ratio;
    }

    public void setOpenFlash(boolean isOpenFlash) {
        if (this.isOpenFlash == isOpenFlash) {
            return;
        }
        this.isOpenFlash = isOpenFlash;
        updateFlash();
    }

    public boolean isOpenFlash() {
        return isOpenFlash;
    }

    public void removeCaptureCallback() {
        if (mDecodeHandler != null) {
            mDecodeHandler.post(new Runnable() {
                @Override
                public void run() {
                    mOnCaptureCallback = null;
                }
            });
        } else {
            mOnCaptureCallback = null;
        }
    }

    public void setOnCaptureCallback(OnCaptureCallback captureCallback) {
        mOnCaptureCallback = captureCallback;
    }

    private static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight();
        }
    }

}
