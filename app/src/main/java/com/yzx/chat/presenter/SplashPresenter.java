package com.yzx.chat.presenter;

import android.os.Handler;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.contract.SplashContract;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.auth.AuthApi;
import com.yzx.chat.network.api.user.GetUserContactsBean;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.tool.ApiManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.tool.DBManager;
import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.NetworkUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.rong.imlib.RongIMClient;

/**
 * Created by YZX on 2017年11月04日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class SplashPresenter implements SplashContract.Presenter {
    //
    private SplashContract.View mSplashView;
    private Call<JsonResponse<Void>> mTokenVerify;
    private Call<JsonResponse<GetUserContactsBean>> mGetUserFriendsTask;

    private AtomicInteger mTaskCount;

    @Override
    public void attachView(SplashContract.View view) {
        mSplashView = view;
        mTaskCount = new AtomicInteger(2);
    }

    @Override
    public void detachView() {
        NetworkUtil.cancelCall(mTokenVerify);
        NetworkUtil.cancelCall(mGetUserFriendsTask);
        mSplashView = null;
    }

    @Override
    public void init(boolean isAlreadyLoggedIM) {
        if (IdentityManager.getInstance().isLogged()) {
            if (isAlreadyLoggedIM) {
                mTaskCount.decrementAndGet();
            } else {
                initIMServer();
            }
            initHTTPServer();
        } else {
            mSplashView.startLoginActivity();
        }
    }

    private void initComplete() {
        if (mTaskCount.decrementAndGet() == 0) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mSplashView.startHomeActivity();
                }
            });
        }
    }

    private void initIMServer() {
        //"nxv/AObbYd4yTGG14RkxiaE4ovwvabHEXU8xDrUJSvHwGIJoS4kz3vgMQ+4tQkG9HkDogLCSeC4Q1Tv4cVPPjmaWYJKFaTH8"
        IMClient.getInstance().login(IdentityManager.getInstance().getToken(), new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                AndroidUtil.showToast(R.string.SplashPresenter_TokenIncorrect);
                mSplashView.startLoginActivity();
            }

            @Override
            public void onSuccess(String s) {
                initComplete();
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtil.e(errorCode.getMessage());
                switch (errorCode) {
                    case RC_CONN_ID_REJECT:
                    case RC_CONN_USER_OR_PASSWD_ERROR:
                    case RC_CONN_NOT_AUTHRORIZED:
                    case RC_CONN_PACKAGE_NAME_INVALID:
                    case RC_CONN_APP_BLOCKED_OR_DELETED:
                    case RC_CONN_USER_BLOCKED:
                    case RC_DISCONN_KICK:
                        AndroidUtil.showToast(R.string.SplashPresenter_LoginFailInIMSDK);
                        mSplashView.startLoginActivity();
                        break;
                    default:
                        initComplete();
                }
            }
        });
    }

    private void initHTTPServer() {
        UserApi userApi = (UserApi) ApiManager.getProxyInstance(UserApi.class);
        AuthApi authApi = (AuthApi) ApiManager.getProxyInstance(AuthApi.class);

        NetworkUtil.cancelCall(mTokenVerify);
        mTokenVerify = authApi.tokenVerify();
        mTokenVerify.setCallback(new BaseHttpCallback<Void>() {
            boolean isSuccess;

            @Override
            protected void onSuccess(Void response) {
                isSuccess = true;
            }

            @Override
            protected void onFailure(String message) {
                isSuccess = true;
                //mSplashView.startLoginActivity();
            }

            @Override
            public boolean isExecuteNextTask() {
                return isSuccess;
            }
        });

        NetworkUtil.cancelCall(mGetUserFriendsTask);
        mGetUserFriendsTask = userApi.getUserContacts();
        mGetUserFriendsTask.setCallback(new BaseHttpCallback<GetUserContactsBean>() {
            @Override
            protected void onSuccess(GetUserContactsBean response) {
                DBManager.getInstance().getContactDao().cleanTable();
                List<ContactBean> contactBeans = response.getContacts();
                String myID = IdentityManager.getInstance().getUserID();
                if (contactBeans != null) {
                    for (ContactBean bean : contactBeans) {
                        bean.setContactOf(myID);
                    }
                    if (!DBManager.getInstance().getContactDao().insertAll(contactBeans)) {
                        LogUtil.e("insertAll contact error");
                    }
                } else {
                    LogUtil.e("response.getContacts() is null");
                }
                IMClient.getInstance().contactManager().loadAllContact(myID);
                initComplete();
            }

            @Override
            protected void onFailure(String message) {
                LogUtil.e(message);
                if (mTaskCount.decrementAndGet() == 0) {
                    mSplashView.startHomeActivity();
                }
            }
        });

        sHttpExecutor.submit(mTokenVerify, mGetUserFriendsTask);
    }

}
