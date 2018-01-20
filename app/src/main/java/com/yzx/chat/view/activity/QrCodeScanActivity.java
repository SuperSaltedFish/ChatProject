package com.yzx.chat.view.activity;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toolbar;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.widget.view.Camera2TextureView;
import com.yzx.chat.widget.view.MaskView;

public class QrCodeScanActivity extends BaseCompatActivity {

    private Camera2TextureView mCamera2TextureView;
    private View mScanAnimationView;
    private MaskView mMaskView;
    private FrameLayout mScanLayout;
    private ImageView mIvToggleFlash;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_qr_code_scan;
    }

    @Override
    protected void init() {
        mCamera2TextureView = (Camera2TextureView) findViewById(R.id.QrCodeScanActivity_mCamera2TextureView);
        mScanAnimationView = findViewById(R.id.QrCodeScanActivity_mScanAnimationView);
        mMaskView = (MaskView) findViewById(R.id.QrCodeScanActivity_mMaskView);
        mScanLayout = (FrameLayout) findViewById(R.id.QrCodeScanActivity_mScanLayout);
        mIvToggleFlash = (ImageView) findViewById(R.id.QrCodeScanActivity_mIvToggleFlash);
    }

    @Override
    protected void setup() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        mMaskView.setMaskColor(Color.argb(64, 0, 0, 0));

        mCamera2TextureView.setOnCaptureCallback(mOnCaptureCallback);

        mIvToggleFlash.setOnClickListener(mOnToggleFlashClickListener);

        ScaleAnimation animation = new ScaleAnimation(1f, 1f, 0.0f, 1.0f);
        animation.setDuration(2000);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setAnimationListener(mAnimationListener);
        mScanAnimationView.startAnimation(animation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_qr_code_scan,menu);
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


    private final Camera2TextureView.OnCaptureCallback mOnCaptureCallback = new Camera2TextureView.OnCaptureCallback() {
        @Override
        public void onCaptureFrameAtFocus(byte[] data) {

        }
    };

    private final View.OnClickListener mOnToggleFlashClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mCamera2TextureView.isOpenFlash()){
                mCamera2TextureView.setOpenFlash(false);
                mIvToggleFlash.setImageResource(R.drawable.ic_flash_close);
            }else {
                mCamera2TextureView.setOpenFlash(true);
                mIvToggleFlash.setImageResource(R.drawable.ic_flash_open);
            }
        }
    };

    private final Animation.AnimationListener mAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            mMaskView.setSpaceRect(mScanLayout.getLeft(),mScanLayout.getTop(),mScanLayout.getRight(),mScanLayout.getBottom());
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mCamera2TextureView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera2TextureView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera2TextureView.onDestroy();
        mScanAnimationView.getAnimation().cancel();
        mScanAnimationView.clearAnimation();
    }
}
