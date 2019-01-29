package com.yzx.chat.module.me.presenter;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.yzx.chat.R;
import com.yzx.chat.module.me.contract.CropImageContract;
import com.yzx.chat.tool.DirectoryHelper;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.BitmapUtil;
import com.yzx.chat.util.BackstageAsyncTask;

import java.util.UUID;

/**
 * Created by YZX on 2018年03月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class CropImagePresenter implements CropImageContract.Presenter {

    private CropImageContract.View mCropImageView;
    private SaveAvatarToLocalTask mSaveAvatarToLocalTask;

    @Override
    public void attachView(CropImageContract.View view) {
        mCropImageView = view;
    }

    @Override
    public void detachView() {
        AsyncUtil.cancelTask(mSaveAvatarToLocalTask);
        mCropImageView = null;
    }

    @Override
    public void bitmapToFile(Bitmap bitmap) {
        AsyncUtil.cancelTask(mSaveAvatarToLocalTask);
        mSaveAvatarToLocalTask = new SaveAvatarToLocalTask(this);
        mSaveAvatarToLocalTask.execute(bitmap);
        mCropImageView.setEnableProgressDialog(true);
    }

    private void saveComplete(String imagePath) {
        mCropImageView.setEnableProgressDialog(false);
        mCropImageView.returnSaveResult(imagePath);
    }

    private void saveFail() {
        mCropImageView.setEnableProgressDialog(false);
        mCropImageView.showError(AndroidUtil.getString(R.string.CropImageActivity_SaveAvatarFail));
    }

    private static class SaveAvatarToLocalTask extends BackstageAsyncTask<CropImagePresenter, Bitmap, String> {

        SaveAvatarToLocalTask(CropImagePresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected String doInBackground(Bitmap... params) {
            Bitmap bitmap = params[0];
            String savePath = BitmapUtil.saveBitmapToPNG(bitmap, DirectoryHelper.getUserImagePath(), UUID.randomUUID().toString());
            bitmap.recycle();
            return savePath;
        }

        @Override
        protected void onPostExecute(String result, CropImagePresenter lifeDependentObject) {
            super.onPostExecute(result, lifeDependentObject);
            if (!TextUtils.isEmpty(result)) {
                lifeDependentObject.saveComplete(result);
            } else {
                lifeDependentObject.saveFail();
            }
        }
    }

}
