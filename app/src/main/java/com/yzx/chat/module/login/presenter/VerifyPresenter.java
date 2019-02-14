package com.yzx.chat.module.login.presenter;

import android.os.Handler;
import android.os.Looper;

import com.yzx.chat.module.login.contract.VerifyContract;
import com.yzx.chat.core.entity.JsonResponse;
import com.yzx.chat.core.net.api.AuthApi;
import com.yzx.chat.core.entity.ObtainSMSCodeEntity;
import com.yzx.chat.core.entity.LoginResponseEntity;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.listener.ResultCallback;
import com.yzx.chat.core.net.framework.Call;
import com.yzx.chat.core.net.ApiHelper;
import com.yzx.chat.util.AsyncUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by YZX on 2018年07月09日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VerifyPresenter implements VerifyContract.Presenter {

    private VerifyContract.View mVerifyView;
    private Call<JsonResponse<LoginResponseEntity>> mLoginCall;
    private Call<JsonResponse<LoginResponseEntity>> mRegisterCall;
    private Call<JsonResponse<ObtainSMSCodeEntity>> mObtainSMSCall;
    private AuthApi mAuthApi;
    private Handler mHandler;

    @Override
    public void attachView(VerifyContract.View view) {
        mVerifyView = view;
        mAuthApi = (AuthApi) ApiHelper.getProxyInstance(AuthApi.class);
        mHandler = new Handler(Looper.myLooper());
    }

    @Override
    public void detachView() {
        AsyncUtil.cancelCall(mLoginCall);
        AsyncUtil.cancelCall(mRegisterCall);
        AsyncUtil.cancelCall(mObtainSMSCall);
        mHandler.removeCallbacksAndMessages(null);
        mVerifyView = null;
    }


    @Override
    public void login(String username, String password, String verifyCode, String serverSecretKey) {
        AsyncUtil.cancelCall(mLoginCall);
        mLoginCall = mAuthApi.login(
                username,
                password,
                ConfigurationManager.getDeviceID(),
                ConfigurationManager.getBase64RSAPublicKey(),
                verifyCode);
        mLoginCall.setHttpConverter(ApiHelper.getRsaHttpConverter(serverSecretKey));
        AppClient.getInstance().login(mLoginCall, mLoginOrRegisterCallBack);
    }

    @Override
    public void register(String username, String password, String nickname, String verifyCode, String serverSecretKey) {
        AsyncUtil.cancelCall(mRegisterCall);
        mRegisterCall = mAuthApi.register(
                username,
                password,
                nickname,
                ConfigurationManager.getDeviceID(),
                ConfigurationManager.getBase64RSAPublicKey(),
                verifyCode);
        mRegisterCall.setHttpConverter(ApiHelper.getRsaHttpConverter(serverSecretKey));
        AppClient.getInstance().login(mRegisterCall, mLoginOrRegisterCallBack);
    }

    @Override
    public void obtainLoginSMS(String username, String password,String serverSecretKey) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("telephone", username);
        data.put("password", password);
        data.put("deviceID", ConfigurationManager.getDeviceID());
        obtainSMS(username, AuthApi.SMS_CODE_TYPE_LOGIN, data,serverSecretKey);
    }

    @Override
    public void obtainRegisterSMS(String username,String serverSecretKey) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("telephone", username);
        obtainSMS(username, AuthApi.SMS_CODE_TYPE_REGISTER, data,serverSecretKey);
    }

    private void obtainSMS(String username, String type, Map<String, Object> data, String serverSecretKey) {
        AsyncUtil.cancelCall(mObtainSMSCall);
        mObtainSMSCall = mAuthApi.obtainSMSCode(
                username,
                type,
                ConfigurationManager.getBase64RSAPublicKey(),
                data);
        mObtainSMSCall.setResponseCallback(new BaseResponseCallback<ObtainSMSCodeEntity>() {

            @Override
            protected void onSuccess(ObtainSMSCodeEntity response) {

            }

            @Override
            protected void onFailure(String message) {
                mVerifyView.showErrorHint(message);
            }
        });
        mObtainSMSCall.setHttpConverter(ApiHelper.getRsaHttpConverter(serverSecretKey));
        sHttpExecutor.submit(mObtainSMSCall);
    }

    private final ResultCallback<Void> mLoginOrRegisterCallBack = new ResultCallback<Void>() {
        @Override
        public void onResult(Void result) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mVerifyView.startSplashActivity();
                }
            });
        }

        @Override
        public void onFailure(final String error) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mVerifyView.showErrorHint(error);
                }
            });
        }
    };
}
