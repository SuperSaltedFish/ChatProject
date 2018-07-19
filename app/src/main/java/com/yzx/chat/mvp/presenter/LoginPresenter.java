package com.yzx.chat.mvp.presenter;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.yzx.chat.base.BaseResponseCallback;
import com.yzx.chat.mvp.contract.LoginContract;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.auth.AuthApi;
import com.yzx.chat.network.api.auth.GetSecretKeyBean;
import com.yzx.chat.network.api.auth.ObtainSMSCode;
import com.yzx.chat.network.api.auth.UserInfoBean;
import com.yzx.chat.network.chat.CryptoManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ResultCallback;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.HttpConverter;
import com.yzx.chat.network.framework.HttpParamsType;
import com.yzx.chat.network.framework.HttpRequest;
import com.yzx.chat.tool.ApiHelper;
import com.yzx.chat.tool.NotificationHelper;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.BackstageAsyncTask;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by YZX on 2017年10月20日.
 * 每一个不曾起舞的日子，都是对生命的辜负。
 */


public class LoginPresenter implements LoginContract.Presenter {

    private LoginContract.View mLoginView;
    private AuthApi mAuthApi;
    private Call<JsonResponse<GetSecretKeyBean>> mGetSecretKeyCall;
    private Call<JsonResponse<UserInfoBean>> mLoginCall;
    private Call<JsonResponse<ObtainSMSCode>> mObtainSMSCodeCall;
    private String mServerSecretKey;

    private Handler mHandler;

    public LoginPresenter() {
        mAuthApi = (AuthApi) ApiHelper.getProxyInstance(AuthApi.class);
        mHandler = new Handler(Looper.myLooper());

        NotificationHelper.getInstance().cancelAllNotification();
        IMClient.getInstance().logout();
        sHttpExecutor.cleanAllTask();
        BackstageAsyncTask.cleanAllTask();

    }

    @Override
    public void attachView(LoginContract.View view) {
        mLoginView = view;
    }

    @Override
    public void detachView() {
        AsyncUtil.cancelCall(mGetSecretKeyCall);
        AsyncUtil.cancelCall(mLoginCall);
        AsyncUtil.cancelCall(mObtainSMSCodeCall);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mLoginView = null;
    }


    @Override
    public void tryLogin(String username, String password) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("telephone", username);
        data.put("password", password);
        data.put("deviceID", CryptoManager.getDeviceID());
        initSMSCodeCall(username, AuthApi.SMS_CODE_TYPE_LOGIN, data);
        initLoginCall(username, password);
        if (mServerSecretKey == null) {
            initSecretKeyCall();
            sHttpExecutor.submit(mGetSecretKeyCall, mObtainSMSCodeCall);
        } else {
            sHttpExecutor.submit(mObtainSMSCodeCall);
        }
    }

    @Override
    public String getServerSecretKey() {
        return mServerSecretKey;
    }

    private void initSecretKeyCall() {
        AsyncUtil.cancelCall(mGetSecretKeyCall);
        mGetSecretKeyCall = mAuthApi.getSignature();
        mGetSecretKeyCall.setResponseCallback(new BaseResponseCallback<GetSecretKeyBean>() {
            @Override
            protected void onSuccess(GetSecretKeyBean response) {
                mServerSecretKey = response.getSecretKey();
                mObtainSMSCodeCall.setHttpConverter(ApiHelper.getRsaHttpConverter(mServerSecretKey));
                mLoginCall.setHttpConverter(ApiHelper.getRsaHttpConverter(mServerSecretKey));
            }

            @Override
            protected void onFailure(String message) {
                mLoginView.showErrorHint(message);
            }

            @Override
            public boolean isExecuteNextTask() {
                return !TextUtils.isEmpty(mServerSecretKey);
            }
        });

        mGetSecretKeyCall.setHttpConverter(new HttpConverter() {
            @Nullable
            @Override
            public byte[] convertRequest(Map<String, Object> requestParams) {
                return null;
            }

            @Nullable
            @Override
            public byte[] convertMultipartRequest(String partName, Object body) {
                return null;
            }

            @Nullable
            @Override
            public Object convertResponseBody(byte[] body, Type genericType) {
                if(body==null||body.length==0){
                    return null;
                }
                String strBody = new String(body);
                LogUtil.e("convertResponseBody:"+strBody);
                return ApiHelper.getDefaultGsonInstance().fromJson(new String(body), genericType);
            }
        });
    }

    private void initSMSCodeCall(final String username, final String type, final Map<String, Object> data) {
        AsyncUtil.cancelCall(mObtainSMSCodeCall);
        mObtainSMSCodeCall = mAuthApi.obtainSMSCode(
                username,
                type,
                CryptoManager.getBase64RSAPublicKey(),
                data);
        mObtainSMSCodeCall.setResponseCallback(new BaseResponseCallback<ObtainSMSCode>() {

            @Override
            protected void onSuccess(ObtainSMSCode response) {
                if (!response.isSkipVerify()) {
                    AndroidUtil.showToast(response.getVerifyCode());
                    mLoginView.jumpToVerifyPage();
                } else {
                    IMClient.getInstance().login(mLoginCall, mLoginCallBack);
                }
            }

            @Override
            protected void onFailure(String message) {
                mLoginView.showErrorHint(message);
            }
        });
        if (mServerSecretKey != null) {
            mObtainSMSCodeCall.setHttpConverter(ApiHelper.getRsaHttpConverter(mServerSecretKey));
        }
    }

    private void initLoginCall(String username, String password) {
        AsyncUtil.cancelCall(mLoginCall);
        mLoginCall = mAuthApi.login(
                username,
                password,
                CryptoManager.getDeviceID(),
                CryptoManager.getBase64RSAPublicKey(),
                "");
        if (mServerSecretKey != null) {
            mLoginCall.setHttpConverter(ApiHelper.getRsaHttpConverter(mServerSecretKey));
        }
    }

    private final ResultCallback<Void> mLoginCallBack = new ResultCallback<Void>() {
        @Override
        public void onSuccess(Void result) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mLoginView.startSplashActivity();
                }
            });
        }

        @Override
        public void onFailure(final String error) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mLoginView.showErrorHint(error);
                }
            });
        }
    };
}
