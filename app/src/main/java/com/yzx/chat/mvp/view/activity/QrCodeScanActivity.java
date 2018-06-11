package com.yzx.chat.mvp.view.activity;

import android.app.ActionBar;
import android.app.Service;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.widget.view.Camera2CaptureView;
import com.yzx.chat.widget.view.MaskView;

import java.nio.ByteBuffer;
import java.util.Hashtable;


public class QrCodeScanActivity extends BaseCompatActivity {

    private Camera2CaptureView mCamera2CaptureView;
    private View mScanAnimationView;
    private MaskView mMaskView;
    private FrameLayout mScanLayout;
    private ImageView mIvToggleFlash;
    private ScaleAnimation mScaleAnimation;
    private Vibrator mVibrator;

    private Rect mClipRect;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_qr_code_scan;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mCamera2CaptureView = findViewById(R.id.QrCodeScanActivity_mCamera2CaptureView);
        mScanAnimationView = findViewById(R.id.QrCodeScanActivity_mScanAnimationView);
        mMaskView = findViewById(R.id.QrCodeScanActivity_mMaskView);
        mScanLayout = findViewById(R.id.QrCodeScanActivity_mScanLayout);
        mIvToggleFlash = findViewById(R.id.QrCodeScanActivity_mIvToggleFlash);
        mScaleAnimation = new ScaleAnimation(1f, 1f, 0.0f, 1.0f);
        mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        setBrightness(0.9f);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mCamera2CaptureView.setOnCaptureListener(mOnCaptureListener);

        mMaskView.setMaskColor(Color.argb(64, 0, 0, 0));

        mIvToggleFlash.setOnClickListener(mOnToggleFlashClickListener);

        mScaleAnimation.setDuration(2000);
        mScaleAnimation.setRepeatCount(Animation.INFINITE);
        mScaleAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        mScaleAnimation.setAnimationListener(mAnimationListener);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mScanAnimationView.startAnimation(mScaleAnimation);
        mCamera2CaptureView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScanAnimationView.clearAnimation();
        mCamera2CaptureView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_qr_code_scan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.QCodeeScanMenu_albums:
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private final Camera2CaptureView.OnCaptureListener mOnCaptureListener = new Camera2CaptureView.OnCaptureListener() {
        private QRCodeReader mQRCodeReader;
        private Hashtable<DecodeHintType, Object> mHints;
        private byte[] mDataBuff;
        private Rect mRect = new Rect();
        private int mCurrentOrientation;

        @Override
        public void onCaptureSuccess(@NonNull Image image, final int width, final int height, int imageOrientation) {
            Size preview = mCamera2CaptureView.getPreviewSize();
            if (preview == null || mClipRect == null) {
                return;
            }
            if (mCurrentOrientation != imageOrientation) {
                float scaleX = (float) width / preview.getWidth();
                float scaleY = (float) height / preview.getHeight();
                switch (imageOrientation) {
                    case 90:
                        mRect.left = mClipRect.top;
                        mRect.top = preview.getHeight() - mClipRect.right;
                        mRect.right = mRect.left + mClipRect.height();
                        mRect.bottom = mRect.top + mClipRect.width();
                        break;
                    case 180:
                        mRect.left = preview.getWidth() - mRect.right;
                        mRect.top = preview.getHeight() - mRect.bottom;
                        mRect.right = mRect.left + mClipRect.height();
                        mRect.bottom = mRect.top + mClipRect.width();
                        break;
                    case 270:
                        mRect.left = preview.getWidth() - mClipRect.bottom;
                        mRect.top = mClipRect.left;
                        mRect.right = mRect.left + mClipRect.height();
                        mRect.bottom = mRect.top + mClipRect.width();
                        break;
                }
                mRect.left *= scaleX;
                mRect.right *= scaleX;
                mRect.top *= scaleY;
                mRect.bottom *= scaleY;
                mCurrentOrientation = imageOrientation;
            }

            if (mQRCodeReader == null) {
                mQRCodeReader = new QRCodeReader();
                mHints = new Hashtable<>();
                mHints.put(DecodeHintType.CHARACTER_SET, "utf-8");
                mHints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);
            }
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer byteBuffer = planes[0].getBuffer();
            if (mDataBuff == null || mDataBuff.length < byteBuffer.remaining()) {
                mDataBuff = new byte[byteBuffer.remaining()];
            }
            byteBuffer.get(mDataBuff);
            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(mDataBuff, width, height, mRect.left, mRect.top, mRect.width(), mRect.height(), false);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                Result result = mQRCodeReader.decode(bitmap, mHints);
                String content = result.getText();
                if (!TextUtils.isEmpty(content)) {
                    mVibrator.vibrate(60);
                }
            } catch (NotFoundException | ChecksumException | FormatException ignored) {
            }
        }
    };


    private final View.OnClickListener mOnToggleFlashClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mIvToggleFlash.setSelected(!mIvToggleFlash.isSelected());
            mCamera2CaptureView.setEnableFlash(mIvToggleFlash.isSelected());
        }
    };

    private final Animation.AnimationListener mAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            mMaskView.setSpaceRect(mScanLayout.getLeft(), mScanLayout.getTop(), mScanLayout.getRight(), mScanLayout.getBottom());
            if (mClipRect == null) {
                mClipRect = new Rect();
                mClipRect.left = mScanLayout.getLeft();
                mClipRect.top = mScanLayout.getTop() - mCamera2CaptureView.getTop();
                mClipRect.right = mClipRect.left + mScanLayout.getWidth();
                mClipRect.bottom = mClipRect.top + mScanLayout.getHeight();
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    };

}
