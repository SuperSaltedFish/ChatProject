package com.yzx.chat.view.activity;

import android.os.Bundle;
import android.text.TextUtils;

import com.github.chrisbanes.photoview.PhotoView;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.widget.view.CropView;

/**
 * Created by YZX on 2018年03月18日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class CropActivity extends BaseCompatActivity {

    public static final String INTENT_EXTRA_IMAGE_PATH = "ImagePath";

    private CropView mCropView;
    private PhotoView mPhotoView;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_crop;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mCropView = findViewById(R.id.CropActivity_mCropView);
        mPhotoView = findViewById(R.id.CropActivity_mPhotoView);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        String imagePath = getIntent().getStringExtra(INTENT_EXTRA_IMAGE_PATH);
        if (TextUtils.isEmpty(imagePath)) {
            finish();
            return;
        }
        GlideUtil.loadFromUrl(this, mPhotoView, String.format("file://%s", imagePath));
    }
}
