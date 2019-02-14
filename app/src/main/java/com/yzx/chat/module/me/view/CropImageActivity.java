package com.yzx.chat.module.me.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.configure.GlideApp;
import com.yzx.chat.module.me.contract.CropImageContract;
import com.yzx.chat.module.me.presenter.CropImagePresenter;
import com.yzx.chat.widget.dialog.ProgressDialog;
import com.yzx.chat.widget.view.CropImageView;

/**
 * Created by YZX on 2018年03月18日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class CropImageActivity extends BaseCompatActivity<CropImageContract.Presenter> implements CropImageContract.View {

    public static final int RESULT_CODE = CropImageActivity.class.hashCode();
    public static final String INTENT_EXTRA_IMAGE_PATH = "ImagePath";

    private CropImageView mCropImageView;
    private ProgressDialog mProgressDialog;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_crop_image;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mCropImageView = findViewById(R.id.CropActivity_mCropImageView);
        mProgressDialog = new ProgressDialog(this, getString(R.string.ProgressHint_Crop));
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

        GlideApp.with(this)
                .load(String.format("file://%s", imagePath))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .dontAnimate()
                .disallowHardwareConfig()
                .format(DecodeFormat.PREFER_RGB_565)
                .into(mCropImageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_crop_image, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.CropImageMenu_Confirm) {
            Bitmap bitmap = mCropImageView.crop();
            if (bitmap == null) {
                showError(getString(R.string.CropImageActivity_CropFail));
                return true;
            }
            mProgressDialog.show();
            mPresenter.bitmapToFile(mCropImageView.crop());
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
    public void returnSaveResult(String imagePath) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_IMAGE_PATH, imagePath);
        setResult(RESULT_CODE, intent);
        finish();
    }

    @Override
    public void showError(String error) {
        showToast(error);
    }
}





























