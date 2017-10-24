package com.yzx.chat.presenter;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.contract.LoginContract;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.auth.AuthApi;
import com.yzx.chat.network.api.auth.GetSecretKeyBean;
import com.yzx.chat.network.api.auth.LoginRegisterBean;
import com.yzx.chat.network.api.auth.ObtainSMSCode;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.HttpDataFormatAdapter;
import com.yzx.chat.tool.AuthenticationManager;
import com.yzx.chat.tool.ApiManager;
import com.yzx.chat.util.Base64Util;
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
    private AuthApi mAuthApi;
    private Call<JsonResponse<GetSecretKeyBean>> mGetSecretKeyCall;
    private Call<JsonResponse<LoginRegisterBean>> mLoginCall;
    private Call<JsonResponse<LoginRegisterBean>> mRegisterCall;
    private Call<JsonResponse<ObtainSMSCode>> mObtainSMSCodeCall;
    private String mSecretKey;
    private String mCurrVerifyType;

    private AuthenticationManager mManager;

    public LoginPresenter() {
        mAuthApi = (AuthApi) ApiManager.getProxyInstance(AuthApi.class);
        mManager = AuthenticationManager.getInstance();
    }

    @Override
    public void attachView(LoginContract.View view) {
        mLoginView = view;
    }

    @Override
    public void detachView() {
        mLoginView = null;
        if (mGetSecretKeyCall != null) {
            mGetSecretKeyCall.cancel();
        }
        if (mLoginCall != null) {
            mLoginCall.cancel();
        }
        if (mRegisterCall != null) {
            mRegisterCall.cancel();
        }
        if (mObtainSMSCodeCall != null) {
            mObtainSMSCodeCall.cancel();
        }
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
    public void loginVerify(String username, String password) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("telephone", username);
        data.put("password", password);
        mCurrVerifyType = AuthApi.SMS_CODE_TYPE_LOGIN;
        initSMSCodeCall(username, AuthApi.SMS_CODE_TYPE_LOGIN, data);
        startVerify(mObtainSMSCodeCall);
    }

    @Override
    public void registerVerify(String username) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("telephone", username);
        mCurrVerifyType = AuthApi.SMS_CODE_TYPE_REGISTER;
        initSMSCodeCall(username, AuthApi.SMS_CODE_TYPE_REGISTER, data);
        startVerify(mObtainSMSCodeCall);
    }

    private final BaseHttpCallback<GetSecretKeyBean> mGetSecretKeyCallback = new BaseHttpCallback<GetSecretKeyBean>() {
        @Override
        protected void onSuccess(GetSecretKeyBean response) {
            mSecretKey = response.getSecretKey();
        }

        @Override
        protected void onFailure(String message) {
            if(AuthApi.SMS_CODE_TYPE_LOGIN.equals(mCurrVerifyType)){
                mLoginView.loginFailure(message);
            }else {
                mLoginView.registerFailure(message);
            }
        }

        @Override
        public boolean isComplete() {
            return mSecretKey != null;
        }
    };

    private final BaseHttpCallback<ObtainSMSCode> mObtainSMSCodeCallback = new BaseHttpCallback<ObtainSMSCode>() {
        @Override
        protected void onSuccess(ObtainSMSCode response) {
            if(AuthApi.SMS_CODE_TYPE_LOGIN.equals(mCurrVerifyType)){
                if(response.isSkipVerify()){
                    mCurrVerifyType=null;
                }
                mLoginView.startLogin(response.isSkipVerify());
            }else {
                mLoginView.startRegister();
            }
        }

        @Override
        protected void onFailure(String message) {
            if(AuthApi.SMS_CODE_TYPE_LOGIN.equals(mCurrVerifyType)){
                mLoginView.loginFailure(message);
            }else {
                mLoginView.registerFailure(message);
            }
        }
    };

    private final BaseHttpCallback<LoginRegisterBean> mLoginCallback = new BaseHttpCallback<LoginRegisterBean>() {
        @Override
        protected void onSuccess(LoginRegisterBean response) {
            mLoginView.verifySuccess();
        }

        @Override
        protected void onFailure(String message) {
            if(AuthApi.SMS_CODE_TYPE_LOGIN.equals(mCurrVerifyType)){
                mLoginView.verifyFailure(message);
            }else {
                mLoginView.loginFailure(message);
            }
        }
    };


    private final BaseHttpCallback<LoginRegisterBean> mRegisterCallback = new BaseHttpCallback<LoginRegisterBean>() {
        @Override
        protected void onSuccess(LoginRegisterBean response) {
            mLoginView.verifySuccess();
        }

        @Override
        protected void onFailure(String message) {
            mLoginView.verifyFailure(message);
        }
    };

    private final HttpDataFormatAdapter mRSADataFormatAdapter = new HttpDataFormatAdapter() {
        @Nullable
        @Override
        public String requestToString(String url, Map<String, Object> params, String requestMethod) {
            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(params);
            byte[] encryptData = RSAUtil.encryptByPublicKey(json.getBytes(), mSecretKey.getBytes());
            return Base64Util.encodeToString(encryptData);
        }

        @Nullable
        @Override
        public Object responseToObject(String url, String httpResponse, Type genericType) {
            byte[] data = Base64Util.decode(httpResponse);
            if(data==null){
                return null;
            }
            String strData = new String(mManager.rsaDecryptByPrivateKey(data));
            try {
                Gson gson = new GsonBuilder().create();
                return gson.fromJson(strData, genericType);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }
    };

    private void initSecretKeyCall() {
        if (mGetSecretKeyCall != null) {
            mGetSecretKeyCall.cancel();
        }
        mGetSecretKeyCall = mAuthApi.getSignature();
        mGetSecretKeyCall.setCallback(mGetSecretKeyCallback);
        mGetSecretKeyCall.setHttpDataFormatAdapter(new HttpDataFormatAdapter() {
            @Nullable
            @Override
            public String requestToString(String url, Map<String, Object> params, String requestMethod) {
                return null;
            }

            @Nullable
            @Override
            public Object responseToObject(String url, String httpResponse, Type genericType) {
                Gson gson = new GsonBuilder().create();
                try {
                    return gson.fromJson(httpResponse, genericType);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    private void initSMSCodeCall(String username, String type, Map<String, Object> data) {
        if (mObtainSMSCodeCall != null) {
            mObtainSMSCodeCall.cancel();
        }
        mObtainSMSCodeCall = mAuthApi.obtainSMSCode(
                username,
                type,
                mManager.getBase64RSAPublicKey(),
                data);
        mObtainSMSCodeCall.setCallback(mObtainSMSCodeCallback);
        mObtainSMSCodeCall.setHttpDataFormatAdapter(mRSADataFormatAdapter);
    }

    private void initLoginCall(String username, String password, String verifyCode) {
        if (mLoginCall != null) {
            mLoginCall.cancel();
        }
        mLoginCall = mAuthApi.login(
                username,
                password,
                mManager.getDeviceID(),
                mManager.getBase64RSAPublicKey(),
                verifyCode);
        mLoginCall.setCallback(mLoginCallback);
        mLoginCall.setHttpDataFormatAdapter(mRSADataFormatAdapter);
    }

    private void initRegisterCall(String username, String password, String nickname, String verifyCode) {
        if (mRegisterCall != null) {
            mRegisterCall.cancel();
        }
        mRegisterCall = mAuthApi.register(
                username,
                password,
                nickname,
                mManager.getDeviceID(),
                mManager.getBase64RSAPublicKey(),
                verifyCode);
        mRegisterCall.setCallback(mRegisterCallback);
        mRegisterCall.setHttpDataFormatAdapter(mRSADataFormatAdapter);
    }

    private void startVerify(Call<?> afterCall) {
        if (mSecretKey == null) {
            initSecretKeyCall();
            sHttpExecutor.submit(mGetSecretKeyCall, afterCall);
        } else {
            sHttpExecutor.submit(afterCall);
        }
    }

}
