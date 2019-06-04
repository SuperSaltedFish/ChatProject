package com.yzx.chat.module.common.view;

import android.animation.ValueAnimator;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.RotateDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.module.common.contract.QrCodeScanContract;
import com.yzx.chat.module.common.presenter.QrCodeScanPresenter;
import com.yzx.chat.module.contact.view.ContactProfileActivity;
import com.yzx.chat.module.contact.view.StrangerProfileActivity;
import com.yzx.chat.module.conversation.view.ChatActivity;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.util.ViewUtil;
import com.yzx.chat.util.YUVUtil;
import com.yzx.chat.widget.listener.OnOnlySingleClickListener;
import com.yzx.chat.widget.view.CameraView;
import com.yzx.chat.widget.view.MaskView;

import java.util.Arrays;
import java.util.Hashtable;

import androidx.appcompat.widget.Toolbar;
import io.rong.imlib.model.Conversation;


public class QrCodeScanActivity extends BaseCompatActivity<QrCodeScanContract.Presenter> implements QrCodeScanContract.View {

    private static final String TAG = QrCodeScanActivity.class.getName();

    private CameraView mCameraView;
    private MaskView mMaskView;
    private ImageView mIvToggleFlash;
    private Toolbar mDefaultToolbar;
    private TextView mTvScanHint;
    private View mScan;
    private View mScanGradientDown;
    private View mScanGradientUp;
    private View mScanFrame;
    private FrameLayout mFlAnimationLayout;
    private Vibrator mVibrator;
    private Handler mDecodeHandler;
    private ValueAnimator mScanAnimator;

    private volatile boolean isStopCapture;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_qr_code_scan;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mCameraView = findViewById(R.id.mCameraView);
        mMaskView = findViewById(R.id.mMaskView);
        mDefaultToolbar = findViewById(R.id.Default_mToolbar);
        mScan = findViewById(R.id.mScan);
        mScanGradientDown = findViewById(R.id.mScanGradientDown);
        mScanGradientUp = findViewById(R.id.mScanGradientUp);
        mScanFrame = findViewById(R.id.mScanFrame);
        mIvToggleFlash = findViewById(R.id.mIvToggleFlash);
        mTvScanHint = findViewById(R.id.mTvScanHint);
        mFlAnimationLayout = findViewById(R.id.mFlAnimationLayout);
        mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mDecodeHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        setTitle(R.string.QrCodeScanActivity_Title);
        setBrightness(0.9f);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setDisplayHomeAsUpEnabled(true);

        mDefaultToolbar.setBackground(null);

        mCameraView.setCaptureCallback(mCaptureCallback, mDecodeHandler);
        mCameraView.setErrorCallback(mErrorCallback);

        mMaskView.setMaskColor(Color.argb(168, 0, 0, 0));
        mMaskView.setRoundRadius(AndroidHelper.dip2px(8));

        mIvToggleFlash.setOnClickListener(mOnToggleFlashClickListener);

        ViewUtil.setRoundClipToOutline(mFlAnimationLayout, AndroidHelper.dip2px(8));
        ViewUtil.setRoundClipToOutline(mTvScanHint, AndroidHelper.dip2px(8));

        mScan.post(new Runnable() {
            @Override
            public void run() {
                mMaskView.setSpaceRect(mScan.getLeft(), mScan.getTop(), mScan.getRight(), mScan.getBottom());
            }
        });

        initScanAnimation();
    }


    @Override
    protected void onStart() {
        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mCameraView.startPreview();
        if (!mScanAnimator.isStarted()) {
            mScanAnimator.start();
        }
    }

    @Override
    protected void onStop() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mCameraView.stopPreview();
        mScanAnimator.end();
        mScanAnimator.cancel();
        super.onStop();
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
            case R.id.Albums:
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

    private void initScanAnimation() {
        RotateDrawable drawable = new RotateDrawable();
        drawable.setDrawable(mScanGradientUp.getBackground());
        drawable.setToDegrees(180);
        drawable.setLevel(10000);
        mScanGradientUp.setBackground(drawable);

        //扫码动画，分为3部分，往下扫的动画，往上扫动画，还有四边框的呼吸效果，这里用一个ValueAnimator同时实现了3个童话
        //之前考虑分别用3个ObjectAnimator去实现的，但是这有问题，3个动画的时候要用AnimatorSet去做，但是AnimatorSet不支持
        //动画的重复，他只能start一次而且不能设置动画为重复模式
        final int openDuration = 900;
        final int closeDuration = 250;
        final int interval = 300;
        final int totalDuration = (openDuration + closeDuration + interval) * 2;

        mScanAnimator = ValueAnimator.ofInt(0, totalDuration);
        mScanAnimator.setDuration(totalDuration);
        mScanAnimator.setInterpolator(new LinearInterpolator());
        mScanAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mScanAnimator.setRepeatMode(ValueAnimator.RESTART);
        mScanAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private AccelerateInterpolator mAccelerateInterpolator = new AccelerateInterpolator();
            private DecelerateInterpolator mDecelerateInterpolator = new DecelerateInterpolator();
            private AccelerateDecelerateInterpolator mAccelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                float value;
                if (animatedValue <= openDuration) {//下扫缩放动画(Open)
                    value = animatedValue / (float) openDuration;
                    value = mAccelerateInterpolator.getInterpolation(value);
                    mScanGradientDown.setPivotY(0);
                    mScanGradientDown.setScaleY(value);
                } else if (animatedValue <= openDuration + closeDuration) {//下扫缩放动画(Close)
                    value = (animatedValue - openDuration) / (float) closeDuration;
                    value = mAccelerateDecelerateInterpolator.getInterpolation(value);
                    mScanGradientDown.setPivotY(mScanGradientDown.getHeight());
                    mScanGradientDown.setScaleY(1f - value);
                } else if (animatedValue <= openDuration + closeDuration + interval) {
                    //上扫动画和下扫动画的间隔，300左右
                    mScanGradientDown.setScaleY(0f);
                } else if (animatedValue <= openDuration + closeDuration + interval + openDuration) {//上扫缩放动画(Open)
                    value = (animatedValue - openDuration - closeDuration - interval) / (float) openDuration;
                    value = mAccelerateInterpolator.getInterpolation(value);
                    mScanGradientUp.setPivotY(mScanGradientDown.getHeight());
                    mScanGradientUp.setScaleY(value);
                } else if (animatedValue <= openDuration + closeDuration + interval + openDuration + closeDuration) {//上扫缩放动画(Close)
                    value = (animatedValue - openDuration - closeDuration - interval - openDuration) / (float) closeDuration;
                    value = mAccelerateDecelerateInterpolator.getInterpolation(value);
                    mScanGradientUp.setPivotY(0);
                    mScanGradientUp.setScaleY(1f - value);
                } else {
                    mScanGradientUp.setScaleY(0f);
                }

                float scale;
                if (animatedValue <= totalDuration / 2) {//呼吸效果的动画,前一半时间放大，后一半时间缩小
                    value = animatedValue / (totalDuration / 2f);
                    value = mDecelerateInterpolator.getInterpolation(value);
                    scale = 1 + value * 0.03f;
                    mScanFrame.setScaleX(scale);
                    mScanFrame.setScaleY(scale);
                } else {
                    value = (animatedValue - totalDuration / 2f) / (totalDuration / 2f);
                    value = mDecelerateInterpolator.getInterpolation(value);
                    scale = 1 + (1 - value) * 0.03f;
                }
                mScanFrame.setScaleX(scale);
                mScanFrame.setScaleY(scale);
            }
        });
    }

    private void requestCapture() {
        isStopCapture = false;
    }

    private void stopCapture() {
        isStopCapture = true;
    }

    private final View.OnClickListener mOnToggleFlashClickListener = new OnOnlySingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            mIvToggleFlash.setSelected(!mIvToggleFlash.isSelected());
            mCameraView.setEnableFlash(mIvToggleFlash.isSelected());
        }
    };

    private final CameraView.ErrorCallback mErrorCallback = new CameraView.ErrorCallback() {
        @Override
        public void onCameraError() {
            showErrorDialog(getString(R.string.Error_Client), new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            });
        }
    };

    private final CameraView.CaptureCallback mCaptureCallback = new CameraView.CaptureCallback() {
        private MultiFormatReader mMultiFormatReader;
        private Hashtable<DecodeHintType, Object> mHints;
        private Rect mClipRect;
        private byte[] mRotateDataBuffer;

        @Override
        public void onCapture(byte[] yuv, int width, int height, int orientation) {
            RectF displayRectF = mCameraView.getActualPreviewRectF();
            if (isStopCapture || mDecodeHandler == null || displayRectF == null) {
                return;
            }
            if (orientation == 90) {//旋转90后扫码性能会提高很多
                if (mRotateDataBuffer == null || mRotateDataBuffer.length < yuv.length) {
                    mRotateDataBuffer = new byte[yuv.length];
                }
                YUVUtil.rotateYUVDegree90(yuv, mRotateDataBuffer, width, height);
                yuv = mRotateDataBuffer;
                orientation = 0;
                int temp = width;
                width = height;
                height = temp;

            }
            if (mClipRect == null) {
                Rect clipLayoutRect = new Rect();
                clipLayoutRect.left = mScan.getLeft() - mCameraView.getLeft();
                clipLayoutRect.top = mScan.getTop() - mCameraView.getTop();
                clipLayoutRect.right = clipLayoutRect.left + mScan.getWidth();
                clipLayoutRect.bottom = clipLayoutRect.top + mScan.getHeight();
                clipLayoutRect.offset(-(int) displayRectF.left, -(int) displayRectF.top);

                mClipRect = new Rect();
                float scaleX;
                float scaleY;
                switch (orientation) {
                    case 0:
                        mClipRect.set(clipLayoutRect);
                        scaleX = (float) width / displayRectF.width();
                        scaleY = (float) height / displayRectF.height();
                        break;
                    case 90:
                        mClipRect.left = clipLayoutRect.top;
                        mClipRect.top = clipLayoutRect.left;
                        mClipRect.right = mClipRect.left + clipLayoutRect.height();
                        mClipRect.bottom = mClipRect.top + clipLayoutRect.width();
                        scaleX = (float) width / displayRectF.height();
                        scaleY = (float) height / displayRectF.width();
                        break;
                    case 180:
                        mClipRect.left = clipLayoutRect.left;
                        mClipRect.top = (int) (displayRectF.height() - clipLayoutRect.bottom);
                        mClipRect.right = mClipRect.left + clipLayoutRect.height();
                        mClipRect.bottom = mClipRect.top + clipLayoutRect.width();
                        scaleX = (float) width / displayRectF.width();
                        scaleY = (float) height / displayRectF.height();
                        break;
                    case 270:
                        mClipRect.left = (int) (displayRectF.height() - clipLayoutRect.bottom);
                        mClipRect.top = clipLayoutRect.left;
                        mClipRect.right = mClipRect.left + clipLayoutRect.height();
                        mClipRect.bottom = mClipRect.top + clipLayoutRect.width();
                        scaleX = (float) width / displayRectF.height();
                        scaleY = (float) height / displayRectF.width();
                        break;
                    default:
                        return;
                }
                mClipRect.left *= scaleX;
                mClipRect.right *= scaleX;
                mClipRect.top *= scaleY;
                mClipRect.bottom *= scaleY;
            }

            if (mMultiFormatReader == null) {
                mMultiFormatReader = new MultiFormatReader();
                mHints = new Hashtable<>();
                mHints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
                mHints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_128));
                mMultiFormatReader.setHints(mHints);
            }

            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(yuv, width, height, mClipRect.left, mClipRect.top, mClipRect.width(), mClipRect.height(), false);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                Result result = mMultiFormatReader.decodeWithState(bitmap);
                final String content = result.getText();
                if (!TextUtils.isEmpty(content)) {
                    stopCapture();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mDecodeHandler == null) {
                                return;
                            }
                            mVibrator.vibrate(50);
                            mPresenter.decodeQRCodeContent(content, false);
                        }
                    });
                }
            } catch (NotFoundException ignored) {
            } finally {
                mMultiFormatReader.reset();
            }
        }
    };


    @Override
    public QrCodeScanContract.Presenter getPresenter() {
        return new QrCodeScanPresenter();
    }

    @Override
    public void startStrangerProfileActivity(UserEntity user) {
        StrangerProfileActivity.startActivity(this, user);
        finish();
    }

    @Override
    public void startContactProfileActivity(String contactID) {
        ContactProfileActivity.startActivity(this, contactID);
        finish();
    }

    @Override
    public void startGroupChatActivity(String groupID) {
        ChatActivity.startActivity(this, groupID, Conversation.ConversationType.GROUP);
        finish();
    }

    @Override
    public void showErrorDialog(String error) {
        stopCapture();
        super.showErrorDialog(error, new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                requestCapture();
            }
        });
    }
}
