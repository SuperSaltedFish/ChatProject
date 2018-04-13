package com.yzx.chat.mvp.view.activity;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.github.chrisbanes.photoview.PhotoView;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.mvp.contract.CropImageContract;
import com.yzx.chat.mvp.presenter.CropImagePresenter;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.widget.view.CropView;
import com.yzx.chat.widget.view.ProgressDialog;

/**
 * Created by YZX on 2018年03月18日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class CropImageActivity extends BaseCompatActivity<CropImageContract.Presenter> implements CropImageContract.View {

    public static final String INTENT_EXTRA_IMAGE_PATH = "ImagePath";

    private CropView mCropView;
    private PhotoView mPhotoView;
    private ProgressDialog mProgressDialog;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_crop_image;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mCropView = findViewById(R.id.CropActivity_mCropView);
        mPhotoView = findViewById(R.id.CropActivity_mPhotoView);
        mProgressDialog = new ProgressDialog(this, getString(R.string.ProgressHint_Upload));
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String imagePath = getIntent().getStringExtra(INTENT_EXTRA_IMAGE_PATH);
        if (TextUtils.isEmpty(imagePath)) {
            finish();
            return;
        }

        mCropView.setMaskColor(ContextCompat.getColor(this, R.color.mask_color_black));
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
            mProgressDialog.show();
            mPhotoView.setDrawingCacheEnabled(true);
            Bitmap bitmap = mPhotoView.getDrawingCache();
            Rect rect = mCropView.getCircleRect();
            int size = rect.width();
            float scale = 200f / size;
            Matrix matrix = new Matrix();
            matrix.preScale(scale, scale);
            bitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height(), matrix, false);
            mPresenter.uploadAvatar(bitmap);
            mPhotoView.setDrawingCacheEnabled(false);
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public CropImageContract.Presenter getPresenter() {
        return new CropImagePresenter();
    }

    @Override
    public void goBack() {
        mProgressDialog.dismiss();
        finish();
    }

    @Override
    public void showError(String error) {
        mProgressDialog.dismiss();
        showToast(error);
    }
}
