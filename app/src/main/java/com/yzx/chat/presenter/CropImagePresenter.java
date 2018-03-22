package com.yzx.chat.presenter;

import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;

import com.yzx.chat.R;
import com.yzx.chat.contract.CropImageContract;
import com.yzx.chat.network.api.user.UploadAvatarBean;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ResultCallback;
import com.yzx.chat.tool.DirectoryManager;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.BitmapUtil;
import com.yzx.chat.util.NetworkAsyncTask;

import java.util.UUID;

/**
 * Created by YZX on 2018年03月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class CropImagePresenter implements CropImageContract.Presenter {

    private CropImageContract.View mCropImageView;
    private Handler mHandler;
    private SaveAvatarToLocalTask mSaveAvatarToLocalTask;

    @Override
    public void attachView(CropImageContract.View view) {
        mCropImageView = view;
        mHandler = new Handler();
    }

    @Override
    public void detachView() {
        AsyncUtil.cancelTask(mSaveAvatarToLocalTask);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mCropImageView = null;
    }

    @Override
    public void uploadAvatar(Bitmap bitmap) {
        AsyncUtil.cancelTask(mSaveAvatarToLocalTask);
        mSaveAvatarToLocalTask = new SaveAvatarToLocalTask(this);
        mSaveAvatarToLocalTask.execute(bitmap);

    }

    private void saveComplete(String imagePath) {
        IMClient.getInstance().userManager().uploadAvatar(imagePath, new ResultCallback<UploadAvatarBean>() {
            @Override
            public void onSuccess(UploadAvatarBean result) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCropImageView.goBack();
                    }
                });
            }

            @Override
            public void onFailure(final String error) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCropImageView.showError(error);
                    }
                });

            }
        });
    }

    private void saveFail() {
        mCropImageView.showError(AndroidUtil.getString(R.string.CropImageActivity_SaveAvatarFail));
    }

    private static class SaveAvatarToLocalTask extends NetworkAsyncTask<CropImagePresenter, Bitmap, String> {

        SaveAvatarToLocalTask(CropImagePresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected String doInBackground(Bitmap... params) {
            Bitmap bitmap = params[0];
            String savePath = BitmapUtil.saveBitmapToPNG(bitmap, DirectoryManager.getTempPath(), UUID.randomUUID().toString());
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
