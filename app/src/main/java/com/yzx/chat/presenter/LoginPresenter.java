package com.yzx.chat.presenter;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.contract.LoginContract;
import com.yzx.chat.network.api.JsonRequest;
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
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.RSAUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

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

    private AuthenticationManager mManager;

    public LoginPresenter() {
        mAuthApi = (AuthApi) ApiManager.getProxyInstance(AuthApi.class);
        mManager = AuthenticationManager.getInstance();
        mGson = new GsonBuilder().serializeNulls().create();
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
    public void reset() {
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
    public void verifyLogin(String username, String password) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("telephone", username);
        data.put("password", password);
        data.put("deviceID", mManager.getDeviceID());
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
                LogUtil.e(httpResponse);
                return mGson.fromJson(httpResponse, genericType);
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
        if (mServerSecretKey == null) {
            initSecretKeyCall();
            sHttpExecutor.submit(mGetSecretKeyCall, afterCall);
        } else {
            sHttpExecutor.submit(afterCall);
        }
    }



    private final BaseHttpCallback<GetSecretKeyBean> mGetSecretKeyCallback = new BaseHttpCallback<GetSecretKeyBean>() {
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
            return mServerSecretKey != null;
        }
    };

    private final BaseHttpCallback<ObtainSMSCode> mObtainSMSCodeCallback = new BaseHttpCallback<ObtainSMSCode>() {
        @Override
        protected void onSuccess(ObtainSMSCode response) {
            if (mCurrVerifyType == VERIFY_TYPE_LOGIN) {
                if (response.isSkipVerify()) {
                    mCurrVerifyType = VERIFY_TYPE_NONE;
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
    };

    private final BaseHttpCallback<LoginRegisterBean> mLoginCallback = new BaseHttpCallback<LoginRegisterBean>() {
        @Override
        protected void onSuccess(LoginRegisterBean response) {
            mLoginView.verifySuccess();
        }

        @Override
        protected void onFailure(String message) {
            if (mCurrVerifyType == VERIFY_TYPE_LOGIN) {
                mLoginView.verifyFailure(message);
            } else {
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
            JsonRequest request = new JsonRequest();
            request.setParams(params);
            request.setStatus(200);
            String json = mGson.toJson(request);
            byte[] encryptData = RSAUtil.encryptByPublicKey(json.getBytes(), Base64Util.decode(mServerSecretKey.getBytes()));
            json = Base64Util.encodeToString(encryptData);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("param", json);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsonObject.toString();
        }

        @Nullable
        @Override
        public Object responseToObject(String url, String httpResponse, Type genericType) {
            byte[] data = Base64Util.decode(httpResponse);
            if (data == null) {
                return null;
            }
            data = mManager.rsaDecryptByPrivateKey(data);
            if (data == null) {
                return null;
            }
            String strData = new String(data);
            LogUtil.e(strData);
            return mGson.fromJson(strData, genericType);

        }
    };
}
