package com.yzx.chat.mvp.view.activity;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.module.contact.view.ContactProfileActivity;
import com.yzx.chat.module.conversation.view.ChatActivity;
import com.yzx.chat.mvp.contract.QrCodeScanContract;
import com.yzx.chat.mvp.presenter.QrCodeScanPresenter;
import com.yzx.chat.widget.view.Camera2CaptureView;
import com.yzx.chat.widget.view.MaskView;
import com.yzx.chat.widget.view.ProgressDialog;

import java.nio.ByteBuffer;
import java.util.Hashtable;


public class QrCodeScanActivity extends BaseCompatActivity<QrCodeScanContract.Presenter> implements QrCodeScanContract.View {

    private Camera2CaptureView mCamera2CaptureView;
    private View mScanAnimationView;
    private MaskView mMaskView;
    private FrameLayout mScanLayout;
    private ImageView mIvToggleFlash;
    private ScaleAnimation mScaleAnimation;
    private Vibrator mVibrator;
    private ProgressDialog mProgressDialog;

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
        mProgressDialog = new ProgressDialog(this, getString(R.string.ProgressHint_Decode));
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        setBrightness(0.9f);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mCamera2CaptureView.setCaptureCallback(mCaptureCallback);

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
                mCamera2CaptureView.stopCapture();
                startActivityForResult(new Intent(this, ImageSingleSelectorActivity.class), 1);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String imagePath = data.getStringExtra(ImageSingleSelectorActivity.INTENT_EXTRA_IMAGE_PATH);
            if (!TextUtils.isEmpty(imagePath)) {
                mPresenter.decodeQRCodeContentFromFile(imagePath);
            }
        }
    }

    private final Camera2CaptureView.CaptureCallback mCaptureCallback = new Camera2CaptureView.CaptureCallback() {
        private QRCodeReader mQRCodeReader;
        private Hashtable<DecodeHintType, Object> mHints;
        private byte[] mDataBuff;
        private Rect mRect = new Rect();
        private int mCurrentOrientation;

        @Override
        public boolean captureSuccess(@NonNull Image image, final int width, final int height, int imageOrientation) {
            int previewWidth = mCamera2CaptureView.getHeight();
            int previewHeight = mCamera2CaptureView.getWidth();
            if (previewWidth == 0 || previewHeight == 0 || mClipRect == null) {
                return false;
            }
            if (mCurrentOrientation != imageOrientation) {
                float scaleX = (float) width / previewWidth;
                float scaleY = (float) height / previewHeight;
                switch (imageOrientation) {
                    case 90:
                        mRect.left = mClipRect.top;
                        mRect.top = previewHeight - mClipRect.right;
                        mRect.right = mRect.left + mClipRect.height();
                        mRect.bottom = mRect.top + mClipRect.width();
                        break;
                    case 180:
                        mRect.left = previewWidth - mRect.right;
                        mRect.top = previewHeight - mRect.bottom;
                        mRect.right = mRect.left + mClipRect.height();
                        mRect.bottom = mRect.top + mClipRect.width();
                        break;
                    case 270:
                        mRect.left = previewWidth - mClipRect.bottom;
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
                final String content = result.getText();
                if (!TextUtils.isEmpty(content)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mVibrator.vibrate(50);
                            mPresenter.decodeQRCodeContent(content, false);
                        }
                    });
                    return true;
                }
            } catch (NotFoundException | ChecksumException | FormatException ignored) {
            }
            return false;
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
            mScaleAnimation.setAnimationListener(null);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    };

    @Override
    public QrCodeScanContract.Presenter getPresenter() {
        return new QrCodeScanPresenter();
    }

    @Override
    public void startStrangerProfileActivity(UserBean user) {
        Intent intent = new Intent(this, StrangerProfileActivity.class);
        intent.putExtra(StrangerProfileActivity.INTENT_EXTRA_USER, user);
        startActivity(intent);
        finish();
    }

    @Override
    public void startContactProfileActivity(String contactID) {
        Intent intent = new Intent(this, ContactProfileActivity.class);
        intent.putExtra(ContactProfileActivity.INTENT_EXTRA_CONTACT_ID, contactID);
        startActivity(intent);
        finish();
    }

    @Override
    public void startGroupChatActivity(String groupID) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION_TYPE_CODE, ChatActivity.CONVERSATION_GROUP);
        intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION_ID, groupID);
        startActivity(intent);
        finish();
    }

    @Override
    public void setEnableProgressDialog(boolean isEnable) {
        if (isEnable) {
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void showErrorDialog(String error) {
        new MaterialDialog.Builder(this)
                .content(error)
                .inputRange(0, 16)
                .positiveText(R.string.Confirm)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mCamera2CaptureView.startCapture();
                    }
                })
                .show();
    }
}
