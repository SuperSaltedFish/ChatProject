package com.yzx.chat.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.OnSingleFlingListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.widget.view.CropView;

/**
 * Created by YZX on 2018年03月18日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class CropImageActivity extends BaseCompatActivity {

    public static final String INTENT_EXTRA_IMAGE_PATH = "ImagePath";

    private CropView mCropView;
    private PhotoView mPhotoView;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_crop_image;
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

        mCropView.setMaskColor(ContextCompat.getColor(this,R.color.mask_color_black));
        GlideUtil.loadFromUrl(this, mPhotoView, String.format("file://%s", imagePath));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_crop_image, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.CropImageMenu_Confirm) {
            mPhotoView.setDrawingCacheEnabled(true);
            Bitmap bitmap = mPhotoView.getDrawingCache();
            Rect rect = mCropView.getCircleRect();
            int size = rect.width();
            float scale = size/200f;
            Matrix matrix = new Matrix();
            matrix.preScale(scale, scale);
            bitmap = Bitmap.createBitmap(bitmap,rect.left,rect.top,rect.width(),rect.height(),matrix,false);
            mPhotoView.setDrawingCacheEnabled(false);
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
