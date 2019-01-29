package com.yzx.chat.module.common.presenter;

import android.os.Handler;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseResponseCallback;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.core.entity.GroupMemberEntity;
import com.yzx.chat.core.entity.QRCodeContentEntity;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.module.common.contract.QrCodeScanContract;
import com.yzx.chat.core.net.api.Group.GroupApi;
import com.yzx.chat.core.net.api.JsonResponse;
import com.yzx.chat.core.net.api.user.UserApi;
import com.yzx.chat.core.manager.GroupManager;
import com.yzx.chat.core.IMClient;
import com.yzx.chat.core.listener.ResultCallback;
import com.yzx.chat.core.net.framework.Call;
import com.yzx.chat.core.net.api.ApiHelper;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.BackstageAsyncTask;
import com.yzx.chat.util.QRUtils;

/**
 * Created by YZX on 2018年06月12日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class QrCodeScanPresenter implements QrCodeScanContract.Presenter {

    private QrCodeScanContract.View mQrCodeView;
    private BackstageAsyncTask<QrCodeScanPresenter, String, String> mDecodeQRCodeFileTask;
    private Call<JsonResponse<UserEntity>> GetUserProfileCall;
    private GroupManager mGroupManager;
    private Gson mGson;
    private UserApi mUserApi;
    private Handler mHandler;
    private boolean isWaitJoiningPush;

    @Override
    public void attachView(QrCodeScanContract.View view) {
        mQrCodeView = view;
        mHandler = new Handler();
        mGroupManager = IMClient.getInstance().getGroupManager();
        mGroupManager.addGroupChangeListener(mOnGroupOperationListener);
        mUserApi = (UserApi) ApiHelper.getProxyInstance(UserApi.class);
        mGson = ApiHelper.getDefaultGsonInstance();
    }

    @Override
    public void detachView() {
        mGroupManager.removeGroupChangeListener(mOnGroupOperationListener);
        AsyncUtil.cancelCall(GetUserProfileCall);
        AsyncUtil.cancelTask(mDecodeQRCodeFileTask);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mQrCodeView = null;
    }

    @Override
    public void decodeQRCodeContent(String content, boolean isAlreadyDeciphered) {
        if (!isAlreadyDeciphered) {
            byte[] data = IMClient.getInstance().getCryptoManager().aesDecryptFromBase64String(content);
            if (data == null || data.length == 0) {
                content = null;
            } else {
                content = new String(data);
            }
        }
        if (TextUtils.isEmpty(content)) {
            decodeFail(AndroidUtil.getString(R.string.QrCodePresenter_Unrecognized));
        } else {
            QRCodeContentEntity qrCodeContent = null;
            try {
                qrCodeContent = mGson.fromJson(content, QRCodeContentEntity.class);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
            if (qrCodeContent == null || TextUtils.isEmpty(qrCodeContent.getId())) {
                decodeFail(AndroidUtil.getString(R.string.QrCodePresenter_Unrecognized));
            } else {
                switch (qrCodeContent.getType()) {
                    case QRCodeContentEntity.TYPE_USER:
                        findUserInfo(qrCodeContent.getId());
                        break;
                    case QRCodeContentEntity.TYPE_GROUP:
                        joinGroup(qrCodeContent.getId());
                        break;
                    default:
                        decodeFail(AndroidUtil.getString(R.string.QrCodePresenter_Unrecognized));
                }
            }
        }
    }

    @Override
    public void decodeQRCodeContentFromFile(String filePath) {
        mQrCodeView.setEnableProgressDialog(true);
        AsyncUtil.cancelTask(mDecodeQRCodeFileTask);
        mDecodeQRCodeFileTask = new DecodeQRCodeFileTask(this);
        mDecodeQRCodeFileTask.execute(filePath);
    }

    private void joinGroup(String groupID) {
        mHandler.removeCallbacksAndMessages(null);
        isWaitJoiningPush = true;
        IMClient.getInstance().getGroupManager().joinGroup(groupID, GroupApi.JOIN_TYPE_QR_CODE, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isWaitJoiningPush) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isWaitJoiningPush) {
                                decodeFail(AndroidUtil.getString(R.string.Error_Server));
                            }
                        }
                    }, 15000);
                }
            }

            @Override
            public void onFailure(String error) {
                isWaitJoiningPush = false;
                decodeFail(error);
            }
        });
    }

    private void findUserInfo(String tempUserID) {
        AsyncUtil.cancelCall(GetUserProfileCall);
        GetUserProfileCall = mUserApi.getUserProfileByTempUserID(tempUserID);
        GetUserProfileCall.setResponseCallback(new BaseResponseCallback<UserEntity>() {
            @Override
            protected void onSuccess(UserEntity response) {
                mQrCodeView.setEnableProgressDialog(false);
                if (IMClient.getInstance().getContactManager().getContact(response.getUserID()) != null) {
                    mQrCodeView.startContactProfileActivity(response.getUserID());
                } else {
                    mQrCodeView.startStrangerProfileActivity(response);
                }
            }

            @Override
            protected void onFailure(String message) {
                decodeFail(message);
            }
        });
        sHttpExecutor.submit(GetUserProfileCall);
    }

    private void decodeFail(String error) {
        mQrCodeView.setEnableProgressDialog(false);
        mQrCodeView.showErrorDialog(error);
    }

    private final GroupManager.OnGroupOperationListener mOnGroupOperationListener = new GroupManager.OnGroupOperationListener() {

        @Override
        public void onCreatedGroup(GroupEntity group) {

        }

        @Override
        public void onQuitGroup(GroupEntity group) {

        }

        @Override
        public void onBulletinChange(GroupEntity group) {

        }

        @Override
        public void onNameChange(GroupEntity group) {

        }

        @Override
        public void onMemberAdded(GroupEntity group, String[] newMembersID) {

        }

        @Override
        public void onMemberJoin(GroupEntity group, String memberID) {
            if (memberID.equals(IMClient.getInstance().getUserManager().getUserID()) && isWaitJoiningPush) {
                isWaitJoiningPush = false;
                mHandler.removeCallbacksAndMessages(null);
                mQrCodeView.setEnableProgressDialog(false);
                mQrCodeView.startGroupChatActivity(group.getGroupID());
            }
        }

        @Override
        public void onMemberQuit(GroupEntity group, GroupMemberEntity quitMember) {

        }

        @Override
        public void onMemberAliasChange(GroupEntity group, GroupMemberEntity member, String newAlias) {

        }
    };

    private static class DecodeQRCodeFileTask extends BackstageAsyncTask<QrCodeScanPresenter, String, String> {

        DecodeQRCodeFileTask(QrCodeScanPresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected String doInBackground(String... strings) {
            String filePath = strings[0];
            if (TextUtils.isEmpty(filePath)) {
                return null;
            }
            String content = QRUtils.decodeFromLocalFile(filePath);
            if (TextUtils.isEmpty(content)) {
                return "";
            }
            byte[] data = IMClient.getInstance().getCryptoManager().aesDecryptFromBase64String(content);
            if (data == null || data.length == 0) {
                return null;
            }
            return new String(data);
        }

        @Override
        protected void onPostExecute(String s, QrCodeScanPresenter lifeDependentObject) {
            super.onPostExecute(s, lifeDependentObject);
            if (s == null) {
                lifeDependentObject.decodeFail(AndroidUtil.getString(R.string.QrCodePresenter_Unrecognized));
            } else if ("".equals(s)) {
                lifeDependentObject.decodeFail(AndroidUtil.getString(R.string.QrCodePresenter_UnableFind));
            } else {
                lifeDependentObject.decodeQRCodeContent(s, true);
            }

        }
    }
}
