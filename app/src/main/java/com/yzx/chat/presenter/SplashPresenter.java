package com.yzx.chat.presenter;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.contract.SplashContract;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.auth.AuthApi;
import com.yzx.chat.network.api.auth.TokenVerifyBean;
import com.yzx.chat.network.api.user.GetUserContactsBean;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.HttpCallback;
import com.yzx.chat.network.framework.HttpResponse;
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
    private Call<JsonResponse<TokenVerifyBean>> mTokenVerify;
    private Call<JsonResponse<GetUserContactsBean>> mGetUserFriendsTask;

    private AtomicInteger mTaskCount;
    private boolean isInitIMComplete;
    private boolean isInitHTTPComplete;

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
        isInitIMComplete = isAlreadyLoggedIM;
        initIMServer();
        initHTTPServer();
    }

    private synchronized void initComplete() {
        if (mTaskCount.decrementAndGet() == 0) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if(isInitHTTPComplete&&isInitIMComplete){
                        mSplashView.startHomeActivity();
                    }else {
                        mSplashView.startLoginActivity();
                    }
                }
            });
        }
    }

    private void initIMServer() {
        if (isInitIMComplete) {
            initComplete();
            return;
        }
        IMClient.getInstance().login(IdentityManager.getInstance().getToken(), new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                AndroidUtil.showToast(R.string.SplashPresenter_TokenIncorrect);
                initComplete();
            }

            @Override
            public void onSuccess(String s) {
                isInitIMComplete = true;
                initComplete();
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtil.e("login im error :" + errorCode.getMessage());
                switch (errorCode) {
                    case RC_CONN_ID_REJECT:
                    case RC_CONN_USER_OR_PASSWD_ERROR:
                    case RC_CONN_NOT_AUTHRORIZED:
                    case RC_CONN_PACKAGE_NAME_INVALID:
                    case RC_CONN_APP_BLOCKED_OR_DELETED:
                    case RC_CONN_USER_BLOCKED:
                    case RC_DISCONN_KICK:
                        break;
                    default:
                        isInitIMComplete = true;
                        initComplete();
                }
                initComplete();
            }
        });
    }

    private void initHTTPServer() {
        UserApi userApi = (UserApi) ApiManager.getProxyInstance(UserApi.class);
        AuthApi authApi = (AuthApi) ApiManager.getProxyInstance(AuthApi.class);

        NetworkUtil.cancelCall(mTokenVerify);
        mTokenVerify = authApi.tokenVerify();
        mTokenVerify.setCallback(new HttpCallback<JsonResponse<TokenVerifyBean>>() {
            private boolean isSuccess;

            @Override
            public void onResponse(HttpResponse<JsonResponse<TokenVerifyBean>> response) {
                if (response.getResponseCode() == 200) {
                    JsonResponse<TokenVerifyBean> jsonResponse = response.getResponse();
                    if (jsonResponse != null) {
                        TokenVerifyBean tokenVerifyBean = jsonResponse.getData();
                        if (jsonResponse.getStatus() != 200 || tokenVerifyBean == null || tokenVerifyBean.getUser() == null) {
                            mSplashView.startLoginActivity();
                        } else {
                            UserBean userBean = tokenVerifyBean.getUser();
                            if (userBean.isEmpty() || !IdentityManager.getInstance().saveUser(userBean)) {
                                mSplashView.startLoginActivity();
                                return;
                            }
                        }
                    }
                }
                if (IdentityManager.getInstance().initFromLocalDB()) {
                    isSuccess = true;
                } else {
                    mSplashView.startLoginActivity();
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
                if (IdentityManager.getInstance().initFromLocalDB()) {
                    isSuccess = true;
                } else {
                    mSplashView.startLoginActivity();
                }
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
                IMClient.getInstance().contactManager().loadAllContact(IdentityManager.getInstance().getUserID());
                if (mTaskCount.decrementAndGet() == 0) {
                    mSplashView.startHomeActivity();
                }
            }
        });

        sHttpExecutor.submit(mTokenVerify, mGetUserFriendsTask);
    }

}
