package com.yzx.chat.mvp.presenter;

import android.os.Handler;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseResponseCallback;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.GroupMemberBean;
import com.yzx.chat.bean.QRCodeContentBean;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.mvp.contract.QrCodeScanContract;
import com.yzx.chat.network.api.Group.GroupApi;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.chat.GroupManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ResultCallback;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.tool.ApiHelper;
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
    private Call<JsonResponse<UserBean>> GetUserProfileCall;
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
            QRCodeContentBean qrCodeContent = null;
            try {
                qrCodeContent = mGson.fromJson(content, QRCodeContentBean.class);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
            if (qrCodeContent == null || TextUtils.isEmpty(qrCodeContent.getId())) {
                decodeFail(AndroidUtil.getString(R.string.QrCodePresenter_Unrecognized));
            } else {
                switch (qrCodeContent.getType()) {
                    case QRCodeContentBean.TYPE_USER:
                        findUserInfo(qrCodeContent.getId());
                        break;
                    case QRCodeContentBean.TYPE_GROUP:
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
        GetUserProfileCall.setResponseCallback(new BaseResponseCallback<UserBean>() {
            @Override
            protected void onSuccess(UserBean response) {
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
        public void onCreatedGroup(GroupBean group) {

        }

        @Override
        public void onQuitGroup(GroupBean group) {

        }

        @Override
        public void onBulletinChange(GroupBean group) {

        }

        @Override
        public void onNameChange(GroupBean group) {

        }

        @Override
        public void onMemberAdded(GroupBean group, String[] newMembersID) {

        }

        @Override
        public void onMemberJoin(GroupBean group, String memberID) {
            if (memberID.equals(IMClient.getInstance().getUserManager().getUserID()) && isWaitJoiningPush) {
                isWaitJoiningPush = false;
                mHandler.removeCallbacksAndMessages(null);
                mQrCodeView.setEnableProgressDialog(false);
                mQrCodeView.startGroupChatActivity(group.getGroupID());
            }
        }

        @Override
        public void onMemberQuit(GroupBean group, GroupMemberBean quitMember) {

        }

        @Override
        public void onMemberAliasChange(GroupBean group, GroupMemberBean member, String newAlias) {

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
