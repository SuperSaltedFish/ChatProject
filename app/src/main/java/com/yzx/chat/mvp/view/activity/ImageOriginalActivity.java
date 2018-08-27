package com.yzx.chat.mvp.view.activity;

import android.app.SharedElementCallback;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.chrisbanes.photoview.PhotoView;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.configure.GlideApp;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.widget.listener.SimpleTransitionListener;

import java.util.List;
import java.util.Map;


/**
 * Created by YZX on 2018年02月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ImageOriginalActivity extends BaseCompatActivity {

    public static final String INTENT_EXTRA_THUMBNAIL_URI = "ThumbnailUri";
    public static final String INTENT_EXTRA_IMAGE_URI = "ImageUri";
    public static final String TRANSITION_NAME_IMAGE = "TransitionNameImage";

    private ImageView mPhotoView;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_image_original;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mPhotoView = findViewById(R.id.ImageOriginal_mPhotoView);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        setSystemUiMode(SYSTEM_UI_MODE_FULLSCREEN);
        ViewCompat.setTransitionName(mPhotoView, TRANSITION_NAME_IMAGE);
        Uri thumbnailUri = getIntent().getParcelableExtra(INTENT_EXTRA_THUMBNAIL_URI);
        final Uri imageUri = getIntent().getParcelableExtra(INTENT_EXTRA_IMAGE_URI);
        if (imageUri == null && thumbnailUri == null) {
            finish();
            return;
        }
        Drawable thumbnail = null;
        if (thumbnailUri != null && "file".equals(thumbnailUri.getScheme())) {
            Bitmap bitmap = BitmapFactory.decodeFile(thumbnailUri.getPath());
            if (bitmap != null) {
                thumbnail = new BitmapDrawable(getResources(), bitmap);
            }
        }
        mPhotoView.setImageDrawable(thumbnail);
        if (imageUri != null && !TextUtils.isEmpty(imageUri.getPath())) {
            final Drawable finalThumbnail = thumbnail;
            getWindow().getSharedElementEnterTransition().addListener(new SimpleTransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    getWindow().getSharedElementEnterTransition().removeListener(this);
                    GlideApp.with(ImageOriginalActivity.this)
                            .load(imageUri)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .dontAnimate()
                            .dontTransform()
                            .skipMemoryCache(true)
                            .placeholder(finalThumbnail)
                            .format(DecodeFormat.PREFER_RGB_565)
                            .into(mPhotoView);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        ViewCompat.setTransitionName(mPhotoView, null);
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPhotoView.setImageBitmap(null);
    }
}
