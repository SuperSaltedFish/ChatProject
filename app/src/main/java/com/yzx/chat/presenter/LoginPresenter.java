package com.yzx.chat.presenter;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.configure.AppApplication;
import com.yzx.chat.contract.LoginContract;
import com.yzx.chat.network.api.JsonRequest;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.auth.AuthApi;
import com.yzx.chat.network.api.auth.GetSecretKeyBean;
import com.yzx.chat.network.api.auth.LoginRegisterBean;
import com.yzx.chat.network.api.auth.ObtainSMSCode;
import com.yzx.chat.util.NetworkAsyncTask;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.HttpDataFormatAdapter;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.tool.DBManager;
import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.tool.ApiManager;
import com.yzx.chat.util.Base64Util;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.NetworkUtil;
import com.yzx.chat.util.RSAUtil;


import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import io.rong.imlib.RongIMClient;


/**
 * Created by YZX on 2017年10月20日.
 * 每一个不曾起舞的日子，都是对生命的辜负。
 */

public class LoginPresenter implements LoginContract.Presenter {

    public final static int VERIFY_TYPE_NONE = 0;
    public final static int VERIFY_TYPE_LOGIN = 1;
    public final static int VERIFY_TYPE_REGISTER = 2;

    private LoginContract.View mLoginView;
    private Gson mGson;
    private AuthApi mAuthApi;
    private Call<JsonResponse<GetSecretKeyBean>> mGetSecretKeyCall;
    private Call<JsonResponse<LoginRegisterBean>> mLoginCall;
    private Call<JsonResponse<LoginRegisterBean>> mRegisterCall;
    private Call<JsonResponse<ObtainSMSCode>> mObtainSMSCodeCall;
    private String mServerSecretKey;
    private int mCurrVerifyType;


    private Handler mHandler;

    public LoginPresenter() {
        mAuthApi = (AuthApi) ApiManager.getProxyInstance(AuthApi.class);
        mGson = new GsonBuilder().serializeNulls().create();
        mHandler = new Handler(Looper.myLooper());

        IMClient.getInstance().logout();
        IdentityManager.clearLocalAuthenticationData();
        sHttpExecutor.cleanAllTask();
        NetworkAsyncTask.cleanAllTask();
    }

    @Override
    public void attachView(LoginContract.View view) {
        mLoginView = view;
    }

    @Override
    public void detachView() {
        mLoginView = null;
        reset();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void reset() {
        NetworkUtil.cancelCall(mGetSecretKeyCall);
        NetworkUtil.cancelCall(mLoginCall);
        NetworkUtil.cancelCall(mRegisterCall);
        NetworkUtil.cancelCall(mObtainSMSCodeCall);
    }

    @Override
    public void login(String username, String password, String verifyCode) {
        initLoginCall(username, password, verifyCode);
        sHttpExecutor.submit(mLoginCall);
    }

    @Override
    public void register(String username, String password, String nickname, String verifyCode) {
        initRegisterCall(username, password, nickname, verifyCode);
        sHttpExecutor.submit(mRegisterCall);
    }

    @Override
    public void verifyLogin(String username, String password) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("telephone", username);
        data.put("password", password);
        data.put("deviceID", IdentityManager.getDeviceID());
        mCurrVerifyType = VERIFY_TYPE_LOGIN;
        initSMSCodeCall(username, AuthApi.SMS_CODE_TYPE_LOGIN, data);
        startVerify(mObtainSMSCodeCall);
    }

    @Override
    public void verifyRegister(String username) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("telephone", username);
        mCurrVerifyType = VERIFY_TYPE_REGISTER;
        initSMSCodeCall(username, AuthApi.SMS_CODE_TYPE_REGISTER, data);
        startVerify(mObtainSMSCodeCall);
    }

    private void initSecretKeyCall() {
        NetworkUtil.cancelCall(mGetSecretKeyCall);
        mGetSecretKeyCall = mAuthApi.getSignature();
        mGetSecretKeyCall.setCallback(new BaseHttpCallback<GetSecretKeyBean>() {
            @Override
            protected void onSuccess(GetSecretKeyBean response) {
                mServerSecretKey = response.getSecretKey();
            }

            @Override
            protected void onFailure(String message) {
                if (mCurrVerifyType == VERIFY_TYPE_LOGIN) {
                    mLoginView.loginFailure(message);
                } else {
                    mLoginView.registerFailure(message);
                }
            }

            @Override
            public boolean isExecuteNextTask() {
                return !TextUtils.isEmpty(mServerSecretKey);
            }
        });
        mGetSecretKeyCall.setHttpDataFormatAdapter(new HttpDataFormatAdapter() {
            @Nullable
            @Override
            public String requestToString(String url, Map<String, Object> params, String requestMethod) {
                return null;
            }

            @Nullable
            @Override
            public Object responseToObject(String url, String httpResponse, Type genericType) {
                LogUtil.e(httpResponse);
                return mGson.fromJson(httpResponse, genericType);
            }
        });
    }

    private void initSMSCodeCall(String username, String type, Map<String, Object> data) {
        NetworkUtil.cancelCall(mObtainSMSCodeCall);
        mObtainSMSCodeCall = mAuthApi.obtainSMSCode(
                username,
                type,
                IdentityManager.getBase64RSAPublicKey(),
                data);
        mObtainSMSCodeCall.setCallback(new BaseHttpCallback<ObtainSMSCode>() {
            @Override
            protected void onSuccess(ObtainSMSCode response) {
                if (mCurrVerifyType == VERIFY_TYPE_LOGIN) {
                    if (response.isSkipVerify()) {
                        mCurrVerifyType = VERIFY_TYPE_NONE;
                    } else {
                        AndroidUtil.showToast(response.getVerifyCode());
                    }
                    mLoginView.inputLoginVerifyCode(response.isSkipVerify());
                } else {
                    mLoginView.inputRegisterVerifyCode();
                }
            }

            @Override
            protected void onFailure(String message) {
                if (mCurrVerifyType == VERIFY_TYPE_LOGIN) {
                    mLoginView.loginFailure(message);
                } else {
                    mLoginView.registerFailure(message);
                }
            }
        });
        mObtainSMSCodeCall.setHttpDataFormatAdapter(mRSADataFormatAdapter);
    }

    private void initLoginCall(String username, String password, String verifyCode) {
        NetworkUtil.cancelCall(mLoginCall);
        mLoginCall = mAuthApi.login(
                username,
                password,
                IdentityManager.getDeviceID(),
                IdentityManager.getBase64RSAPublicKey(),
                verifyCode);
        mLoginCall.setCallback(new BaseHttpCallback<LoginRegisterBean>() {
            @Override
            protected void onSuccess(LoginRegisterBean response) {
                if (saveVerifyInfo(response)) {
                    loginIMServer();
                } else {
                    onFailure(AndroidUtil.getString(R.string.Server_Error));
                }
            }

            @Override
            protected void onFailure(final String message) {
                if (mCurrVerifyType == VERIFY_TYPE_LOGIN) {
                    mLoginView.verifyFailure(message);
                } else {
                    mLoginView.loginFailure(message);
                }
            }
        });
        mLoginCall.setHttpDataFormatAdapter(mRSADataFormatAdapter);
    }

    private void initRegisterCall(String username, String password, String nickname, String verifyCode) {
        NetworkUtil.cancelCall(mRegisterCall);
        mRegisterCall = mAuthApi.register(
                username,
                password,
                nickname,
                IdentityManager.getDeviceID(),
                IdentityManager.getBase64RSAPublicKey(),
                verifyCode);
        mRegisterCall.setCallback(new BaseHttpCallback<LoginRegisterBean>() {
            @Override
            protected void onSuccess(LoginRegisterBean response) {
                if (saveVerifyInfo(response)) {
                    loginIMServer();
                } else {
                    onFailure(AndroidUtil.getString(R.string.Server_Error));
                }
            }

            @Override
            protected void onFailure(final String message) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mLoginView.verifyFailure(message);
                    }
                });
            }
        }, false);
        mRegisterCall.setHttpDataFormatAdapter(mRSADataFormatAdapter);
    }

    private void startVerify(Call<?> afterCall) {
        if (mServerSecretKey == null) {
            initSecretKeyCall();
            sHttpExecutor.submit(mGetSecretKeyCall, afterCall);
        } else {
            sHttpExecutor.submit(afterCall);
        }
    }

    private boolean saveVerifyInfo(LoginRegisterBean bean) {
        return IdentityManager.init( bean.getToken(), bean.getSecretKey(), bean.getUser());
    }

    private void loginIMServer() {
        IMClient.getInstance().login(IdentityManager.getInstance().getToken(), new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                LogUtil.e("onTokenIncorrect");
                loginIMFail();
            }

            @Override
            public void onSuccess(String s) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mLoginView.verifySuccess();
                    }
                });
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtil.e(errorCode.getMessage());
                loginIMFail();
            }
        });
    }

    private void loginIMFail() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mLoginView.reLogin();
                mLoginView.loginFailure(AndroidUtil.getString(R.string.SplashPresenter_LoginFailInIMSDK));
                IdentityManager.clearLocalAuthenticationData();
            }
        });
    }

    private final HttpDataFormatAdapter mRSADataFormatAdapter = new HttpDataFormatAdapter() {
        @Nullable
        @Override
        public String requestToString(String url, Map<String, Object> params, String requestMethod) {
            JsonRequest request = new JsonRequest();
            request.setParams(params);
            request.setStatus(200);
            String json = mGson.toJson(request);
            LogUtil.e("request: " + json);
            byte[] encryptData = RSAUtil.encryptByPublicKey(json.getBytes(), Base64Util.decode(mServerSecretKey.getBytes()));
            json = Base64Util.encodeToString(encryptData);
            return json;
        }

        @Nullable
        @Override
        public Object responseToObject(String url, String httpResponse, Type genericType) {
            byte[] data = Base64Util.decode(httpResponse);
            if (data == null) {
                return null;
            }
            data = IdentityManager.rsaDecrypt(data);
            if (data == null) {
                return null;
            }
            String strData = new String(data);
            LogUtil.e("response: " + strData);
            return mGson.fromJson(strData, genericType);
        }
    };
}
