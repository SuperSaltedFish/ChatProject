package com.yzx.chat.view.activity;

import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;

import com.github.chrisbanes.photoview.PhotoView;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.util.GlideUtil;

/**
 * Created by YZX on 2018年02月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ImageOriginalActivity extends BaseCompatActivity {

    public static final String INTENT_EXTRA_IMAGE_PATH = "ImagePath";
    public static final String TRANSITION_NAME_IMAGE = "TransitionNameImage";

    private PhotoView mPhotoView;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_image_original;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mPhotoView = findViewById(R.id.ImageOriginal_mPhotoView);
        ViewCompat.setTransitionName(mPhotoView, TRANSITION_NAME_IMAGE);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        String imagePath = getIntent().getStringExtra(INTENT_EXTRA_IMAGE_PATH);
        if (TextUtils.isEmpty(imagePath)) {
            finish();
            return;
        }
        GlideUtil.loadFromUrl(this, mPhotoView, "file://" + imagePath);
    }

    @Override
    public void onBackPressed() {
        ViewCompat.setTransitionName(mPhotoView,null);
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
