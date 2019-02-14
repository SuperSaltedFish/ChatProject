package com.yzx.chat.module.login.presenter;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.yzx.chat.core.entity.GetSecretKeyEntity;
import com.yzx.chat.core.entity.JsonResponse;
import com.yzx.chat.core.entity.ObtainSMSCodeEntity;
import com.yzx.chat.core.net.ApiHelper;
import com.yzx.chat.core.net.api.AuthApi;
import com.yzx.chat.core.net.framework.Call;
import com.yzx.chat.core.net.framework.HttpConverter;
import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.module.login.contract.RegisterContract;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.util.AsyncUtil;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;

/**
 * Created by YZX on 2018年07月09日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class RegisterPresenter implements RegisterContract.Presenter {

    private RegisterContract.View mRegisterView;
    private AuthApi mAuthApi;
    private Call<JsonResponse<GetSecretKeyEntity>> mGetSecretKeyCall;
    private Call<JsonResponse<ObtainSMSCodeEntity>> mObtainSMSCodeCall;
    private String mServerSecretKey;
    private Handler mHandler;


    @Override
    public void attachView(RegisterContract.View view) {
        mRegisterView = view;
        mAuthApi = ApiHelper.getProxyInstance(AuthApi.class);
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
        mGetSecretKeyCall.setResponseCallback(new BaseResponseCallback<GetSecretKeyEntity>() {
            @Override
            protected void onSuccess(GetSecretKeyEntity response) {
                mServerSecretKey = response.getSecretKey();
                mObtainSMSCodeCall.setHttpConverter(ApiHelper.getRsaHttpConverter(mServerSecretKey));
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
    }

    private void initSMSCodeCall(final String username, final String type, final Map<String, Object> data) {
        AsyncUtil.cancelCall(mObtainSMSCodeCall);
        mObtainSMSCodeCall = mAuthApi.obtainSMSCode(
                username,
                type,
                ConfigurationManager.getBase64RSAPublicKey(),
                data);
        mObtainSMSCodeCall.setResponseCallback(new BaseResponseCallback<ObtainSMSCodeEntity>() {
            @Override
            protected void onSuccess(ObtainSMSCodeEntity response) {
                AndroidHelper.showToast(response.getVerifyCode());
                mRegisterView.jumpToVerifyPage();
            }

            @Override
            protected void onFailure(String message) {
                mRegisterView.showErrorHint(message);
            }
        });
        if (!TextUtils.isEmpty(mServerSecretKey)) {
            mObtainSMSCodeCall.setHttpConverter(ApiHelper.getRsaHttpConverter(mServerSecretKey));
        }
    }
}
