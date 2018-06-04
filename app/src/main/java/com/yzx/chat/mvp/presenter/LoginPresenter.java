package com.yzx.chat.mvp.presenter;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.yzx.chat.base.BaseResponseCallback;
import com.yzx.chat.mvp.contract.LoginContract;
import com.yzx.chat.network.api.JsonRequest;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.auth.AuthApi;
import com.yzx.chat.network.api.auth.GetSecretKeyBean;
import com.yzx.chat.network.api.auth.ObtainSMSCode;
import com.yzx.chat.network.api.auth.UserInfoBean;
import com.yzx.chat.network.chat.CryptoManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ResultCallback;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.HttpDataFormatAdapter;
import com.yzx.chat.network.framework.HttpParamsType;
import com.yzx.chat.network.framework.HttpRequest;
import com.yzx.chat.tool.ApiHelper;
import com.yzx.chat.tool.NotificationHelper;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.Base64Util;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.NetworkAsyncTask;
import com.yzx.chat.util.RSAUtil;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by YZX on 2017年10月20日.
 * 每一个不曾起舞的日子，都是对生命的辜负。
 */


public class LoginPresenter implements LoginContract.Presenter {

    private LoginContract.View mLoginView;
    private Gson mGson;
    private AuthApi mAuthApi;
    private Call<JsonResponse<GetSecretKeyBean>> mGetSecretKeyCall;
    private Call<JsonResponse<UserInfoBean>> mLoginCall;
    private Call<JsonResponse<UserInfoBean>> mRegisterCall;
    private Call<JsonResponse<ObtainSMSCode>> mObtainSMSCodeCall;
    private String mServerSecretKey;


    private Handler mHandler;

    public LoginPresenter() {
        mAuthApi = (AuthApi) ApiHelper.getProxyInstance(AuthApi.class);
        mGson = ApiHelper.getDefaultGsonInstance();
        mHandler = new Handler(Looper.myLooper());

        NotificationHelper.getInstance().cancelAllNotification();
        IMClient.getInstance().logout();
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
        AsyncUtil.cancelCall(mGetSecretKeyCall);
        AsyncUtil.cancelCall(mLoginCall);
        AsyncUtil.cancelCall(mRegisterCall);
        AsyncUtil.cancelCall(mObtainSMSCodeCall);
    }

    @Override
    public void login(String username, String password, String verifyCode) {
        initLoginCall(username, password, verifyCode);
        IMClient.getInstance().login(mLoginCall, mLoginOrRegisterCallBack);
    }

    @Override
    public void register(String username, String password, String nickname, String verifyCode) {
        initRegisterCall(username, password, nickname, verifyCode);
        IMClient.getInstance().login(mRegisterCall, mLoginOrRegisterCallBack);
    }

    @Override
    public void tryObtainLoginVerifyCode(String username, String password) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("telephone", username);
        data.put("password", password);
        data.put("deviceID", CryptoManager.getDeviceID());
        initSMSCodeCall(username, AuthApi.SMS_CODE_TYPE_LOGIN, data);
        startVerify(mObtainSMSCodeCall);
    }

    @Override
    public void obtainRegisterVerifyCode(String username) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("telephone", username);
        initSMSCodeCall(username, AuthApi.SMS_CODE_TYPE_REGISTER, data);
        startVerify(mObtainSMSCodeCall);
    }

    private void startVerify(Call<?> afterCall) {
        if (mServerSecretKey == null) {
            initSecretKeyCall();
            sHttpExecutor.submit(mGetSecretKeyCall, afterCall);
        } else {
            sHttpExecutor.submit(afterCall);
        }
    }

    private void initSecretKeyCall() {
        AsyncUtil.cancelCall(mGetSecretKeyCall);
        mGetSecretKeyCall = mAuthApi.getSignature();
        mGetSecretKeyCall.setResponseCallback(new BaseResponseCallback<GetSecretKeyBean>() {
            @Override
            protected void onSuccess(GetSecretKeyBean response) {
                mServerSecretKey = response.getSecretKey();
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
        mGetSecretKeyCall.setHttpDataFormatAdapter(new HttpDataFormatAdapter() {
            @Nullable
            @Override
            public String paramsToString(HttpRequest httpRequest) {
                LogUtil.e("开始访问：" + httpRequest.url());
                return null;
            }

            @Nullable
            @Override
            public Map<HttpParamsType, Map<String, Object>> multiParamsFormat(HttpRequest httpRequest) {
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
                if (response.isSkipVerify()) {
                    login(username, (String) data.get("password"), "");
                } else {
                    AndroidUtil.showToast(response.getVerifyCode());
                    mLoginView.jumpToVerifyPage();
                }
            }

            @Override
            protected void onFailure(String message) {
                mLoginView.showErrorHint(message);
            }
        });
        mObtainSMSCodeCall.setHttpDataFormatAdapter(mRSADataFormatAdapter);
    }

    private void initLoginCall(String username, String password, String verifyCode) {
        AsyncUtil.cancelCall(mLoginCall);
        mLoginCall = mAuthApi.login(
                username,
                password,
                CryptoManager.getDeviceID(),
                CryptoManager.getBase64RSAPublicKey(),
                verifyCode);
        mLoginCall.setHttpDataFormatAdapter(mRSADataFormatAdapter);
    }

    private void initRegisterCall(String username, String password, String nickname, String verifyCode) {
        AsyncUtil.cancelCall(mRegisterCall);
        mRegisterCall = mAuthApi.register(
                username,
                password,
                nickname,
                CryptoManager.getDeviceID(),
                CryptoManager.getBase64RSAPublicKey(),
                verifyCode);
        mRegisterCall.setHttpDataFormatAdapter(mRSADataFormatAdapter);
    }

    private final ResultCallback<Void> mLoginOrRegisterCallBack = new ResultCallback<Void>() {
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

    private final HttpDataFormatAdapter mRSADataFormatAdapter = new HttpDataFormatAdapter() {

        @Nullable
        @Override
        public String paramsToString(HttpRequest httpRequest) {
            Map<String, Object> params = httpRequest.params().get(HttpParamsType.PARAMETER_HTTP);
            LogUtil.e("开始访问：" + httpRequest.url());
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
        public Map<HttpParamsType, Map<String, Object>> multiParamsFormat(HttpRequest httpRequest) {
            return null;
        }

        @Nullable
        @Override
        public Object responseToObject(String url, String httpResponse, Type genericType) {
            byte[] data = Base64Util.decode(httpResponse);
            if (data == null || data.length == 0) {
                LogUtil.e("response: " + null);
                return null;
            }
            data = CryptoManager.rsaDecrypt(data);
            if (data == null) {
                LogUtil.e("response: " + null);
                return null;
            }
            String strData = new String(data);
            LogUtil.e("response: " + strData);
            return mGson.fromJson(strData, genericType);
        }
    };
}
