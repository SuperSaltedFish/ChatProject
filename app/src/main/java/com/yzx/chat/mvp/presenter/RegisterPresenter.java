package com.yzx.chat.mvp.presenter;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.yzx.chat.base.BaseResponseCallback;
import com.yzx.chat.mvp.contract.RegisterContract;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.auth.AuthApi;
import com.yzx.chat.network.api.auth.GetSecretKeyBean;
import com.yzx.chat.network.api.auth.ObtainSMSCode;
import com.yzx.chat.network.chat.CryptoManager;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.HttpDataFormatAdapter;
import com.yzx.chat.network.framework.HttpParamsType;
import com.yzx.chat.network.framework.HttpRequest;
import com.yzx.chat.tool.ApiHelper;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.LogUtil;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by YZX on 2018年07月09日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class RegisterPresenter implements RegisterContract.Presenter {

    private RegisterContract.View mRegisterView;
    private AuthApi mAuthApi;
    private Call<JsonResponse<GetSecretKeyBean>> mGetSecretKeyCall;
    private Call<JsonResponse<ObtainSMSCode>> mObtainSMSCodeCall;
    private String mServerSecretKey;
    private Handler mHandler;


    @Override
    public void attachView(RegisterContract.View view) {
        mRegisterView = view;
        mAuthApi = (AuthApi) ApiHelper.getProxyInstance(AuthApi.class);
        mHandler = new Handler(Looper.myLooper());
    }

    @Override
    public void detachView() {
        AsyncUtil.cancelCall(mGetSecretKeyCall);
        AsyncUtil.cancelCall(mObtainSMSCodeCall);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mRegisterView = null;
    }

    @Override
    public void obtainRegisterVerifyCode(String username) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("telephone", username);
        initSMSCodeCall(username, AuthApi.SMS_CODE_TYPE_REGISTER, data);
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
                mObtainSMSCodeCall.setHttpDataFormatAdapter(ApiHelper.getRsaHttpDataFormatAdapter(mServerSecretKey));
            }

            @Override
            protected void onFailure(String message) {
                mRegisterView.showErrorHint(message);
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
                return ApiHelper.getDefaultGsonInstance().fromJson(httpResponse, genericType);
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
                AndroidUtil.showToast(response.getVerifyCode());
                mRegisterView.jumpToVerifyPage();
            }

            @Override
            protected void onFailure(String message) {
                mRegisterView.showErrorHint(message);
            }
        });
        if (!TextUtils.isEmpty(mServerSecretKey)) {
            mObtainSMSCodeCall.setHttpDataFormatAdapter(ApiHelper.getRsaHttpDataFormatAdapter(mServerSecretKey));
        }
    }
}
