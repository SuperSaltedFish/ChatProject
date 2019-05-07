package com.yzx.chat.module.common.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Transition;
import android.view.View;
import android.widget.ProgressBar;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.configure.GlideApp;
import com.yzx.chat.widget.listener.SimpleTransitionListener;
import com.yzx.chat.widget.view.CropImageView;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;


/**
 * Created by YZX on 2018年02月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ImageOriginalActivity extends BaseCompatActivity {

    public static final String INTENT_EXTRA_THUMBNAIL_URI = "ThumbnailUri";
    public static final String INTENT_EXTRA_IMAGE_URI = "OriginalImage";
    public static final String TRANSITION_NAME_IMAGE = "TransitionNameImage";

    private CropImageView mCropImageView;
    private ProgressBar mProgressBar;
    private Uri mThumbnailUri;
    private Uri mOriginalImageUri;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_image_original;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mCropImageView = findViewById(R.id.mCropImageView);
        mProgressBar = findViewById(R.id.mProgressBar);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        mCropImageView.setEnablePreviewMode(true);
        ViewCompat.setTransitionName(mCropImageView, TRANSITION_NAME_IMAGE);
        mThumbnailUri = getIntent().getParcelableExtra(INTENT_EXTRA_THUMBNAIL_URI);
        mOriginalImageUri = getIntent().getParcelableExtra(INTENT_EXTRA_IMAGE_URI);
        if (mThumbnailUri != null) {
            loadThumbnail();
            if (mOriginalImageUri != null) {
                if (!addTransitionListener()) {
                    loadOriginalImage();
                }
            }
        } else if (mOriginalImageUri != null) {
            loadOriginalImage();
        } else {
            finish();
        }
    }

    private void loadThumbnail() {
        supportPostponeEnterTransition();
        GlideApp.with(this)
                .load(mThumbnailUri)
                .dontAnimate()
                .format(DecodeFormat.PREFER_RGB_565)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(mCropImageView);
    }

    private void loadOriginalImage() {
        GlideApp.with(this)
                .load(mOriginalImageUri)
                .dontAnimate()
                .placeholder(mCropImageView.getDrawable())//这句代码要加，不要第一次加载会闪烁，原因是glide内部把imageView的bitmap设置成null
                .format(DecodeFormat.PREFER_RGB_565)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        showToast(getString(R.string.Error_Server1));
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        return false;
                    }
                })
                .into(mCropImageView);
    }

    private boolean addTransitionListener() {
        final Transition transition = getWindow().getSharedElementEnterTransition();
        if (transition != null) {
            transition.addListener(new SimpleTransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    transition.removeListener(this);
                    loadOriginalImage();
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                    transition.removeListener(this);
                }
            });
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCropImageView.setImageBitmap(null);
    }
}
