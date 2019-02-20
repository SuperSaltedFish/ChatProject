package com.yzx.chat.module.me.presenter;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.yzx.chat.R;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.entity.GetTempGroupIDEntity;
import com.yzx.chat.core.entity.GetTempUserIDEntity;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.core.entity.JsonResponse;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.core.net.ApiHelper;
import com.yzx.chat.core.net.api.GroupApi;
import com.yzx.chat.core.net.api.UserApi;
import com.yzx.chat.core.net.framework.Call;
import com.yzx.chat.core.util.MD5Util;
import com.yzx.chat.module.me.contract.MyQRCodeContract;
import com.yzx.chat.tool.DirectoryHelper;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.BitmapUtil;

/**
 * Created by YZX on 2018年02月26日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class MyQRCodePresenter implements MyQRCodeContract.Presenter {

    private MyQRCodeContract.View mMyQRCodeActivityView;
    private Call<JsonResponse<GetTempUserIDEntity>> mGetTempUserIDCall;
    private Call<JsonResponse<GetTempGroupIDEntity>> mGetTempGroupIDCall;
    private UserApi mUserApi;
    private GroupApi mGroupApi;

    private boolean isUpdating;

    @Override
    public void attachView(MyQRCodeContract.View view) {
        mMyQRCodeActivityView = view;
        mUserApi = ApiHelper.getProxyInstance(UserApi.class);
        mGroupApi = ApiHelper.getProxyInstance(GroupApi.class);
    }

    @Override
    public void detachView() {
        AsyncUtil.cancelCall(mGetTempUserIDCall);
        AsyncUtil.cancelCall(mGetTempGroupIDCall);
        mMyQRCodeActivityView = null;
    }

    @Override
    public UserEntity getUserInfo() {
        return AppClient.getInstance().getUserManager().getCurrentUser();
    }

    @Override
    public GroupEntity getGroupInfo(String groupID) {
        if (TextUtils.isEmpty(groupID)) {
            return null;
        }
        return AppClient.getInstance().getGroupManager().getGroup(groupID);
    }

    @Override
    public void updateUserQRCode() {
//        if (isUpdating) {
//            return;
//        }
//        isUpdating = true;
//        mMyQRCodeActivityView.setEnableProgressBar(true);
//        AsyncUtil.cancelCall(mGetTempUserIDCall);
//        mGetTempUserIDCall = mUserApi.getTempUserID();
//        mGetTempUserIDCall.setResponseCallback(new BaseResponseCallback<GetTempUserIDEntity>() {
//            @Override
//            protected void onSuccess(GetTempUserIDEntity response) {
//                mMyQRCodeActivityView.setEnableProgressBar(false);
//                isUpdating = false;
//                String id = response.getTempUserID();
//                if (!TextUtils.isEmpty(id)) {
//                    id = AppClient.getInstance().getConfigurationManager().aesEncryptToBase64(id.getBytes());
//                    if (!TextUtils.isEmpty(id)) {
//                        QRCodeContentEntity qrCodeContent = new QRCodeContentEntity();
//                        qrCodeContent.setId(id);
//                        qrCodeContent.setType(QRCodeContentEntity.TYPE_USER);
//                        mMyQRCodeActivityView.showQRCode(mGson.toJson(qrCodeContent));
//                        return;
//                    }
//                }
//                onFailure(AndroidHelper.getString(R.string.MyQRCodePresenter_GenerateQRCodeFail));
//            }
//
//            @Override
//            protected void onFailure(String message) {
//                mMyQRCodeActivityView.setEnableProgressBar(false);
//                mMyQRCodeActivityView.showHint(message);
//                mMyQRCodeActivityView.showErrorHint(AndroidHelper.getString(R.string.MyQRCodePresenter_GenerateQRCodeFail));
//                isUpdating = false;
//            }
//        });
//        sHttpExecutor.submit(mGetTempUserIDCall);
    }

    @Override
    public void saveQRCodeToLocal(Bitmap bitmap, String id) {
        String savePath = BitmapUtil.saveBitmapToJPEG(bitmap, DirectoryHelper.getUserImagePath(), MD5Util.encrypt16(id));
        if (TextUtils.isEmpty(savePath)) {
            mMyQRCodeActivityView.showHint(AndroidHelper.getString(R.string.MyQRCodeActivity_SaveQRCodeFail));
        } else {
            mMyQRCodeActivityView.showHint(AndroidHelper.getString(R.string.MyQRCodeActivity_SaveQRCodeSuccess) + savePath);
        }
    }

    @Override
    public void updateGroupQRCode(String groupID) {
//        if (isUpdating || TextUtils.isEmpty(groupID)) {
//            return;
//        }
//        isUpdating = true;
//        mMyQRCodeActivityView.setEnableProgressBar(true);
//        AsyncUtil.cancelCall(mGetTempGroupIDCall);
//        mGetTempGroupIDCall = mGroupApi.getTempGroupID(groupID);
//        mGetTempGroupIDCall.setResponseCallback(new BaseResponseCallback<GetTempGroupIDEntity>() {
//            @Override
//            protected void onSuccess(GetTempGroupIDEntity response) {
//                mMyQRCodeActivityView.setEnableProgressBar(false);
//                isUpdating = false;
//                String id = response.getTempGroupID();
//                if (!TextUtils.isEmpty(id)) {
//                    QRCodeContentEntity qrCodeContent = new QRCodeContentEntity();
//                    qrCodeContent.setId(id);
//                    qrCodeContent.setType(QRCodeContentEntity.TYPE_GROUP);
//                    String content = mGson.toJson(qrCodeContent);
//                    content = AppClient.getInstance().getConfigurationManager().aesEncryptToBase64(content.getBytes());
//                    if (!TextUtils.isEmpty(content)) {
//                        mMyQRCodeActivityView.showQRCode(content);
//                        return;
//                    }
//                }
//                onFailure(AndroidHelper.getString(R.string.MyQRCodePresenter_GenerateQRCodeFail));
//            }
//
//            @Override
//            protected void onFailure(String message) {
//                mMyQRCodeActivityView.setEnableProgressBar(false);
//                mMyQRCodeActivityView.showHint(message);
//                mMyQRCodeActivityView.showErrorHint(AndroidHelper.getString(R.string.MyQRCodePresenter_GenerateQRCodeFail));
//                isUpdating = false;
//            }
//        });
//        sHttpExecutor.submit(mGetTempGroupIDCall);
    }
}
