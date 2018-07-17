package com.yzx.chat.mvp.view.activity;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.WindowManager;
import android.widget.ImageView;


import com.bumptech.glide.load.HttpException;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.configure.GlideApp;
import com.yzx.chat.network.api.JsonRequest;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.user.GetTempUserID;
import com.yzx.chat.network.api.user.GetUserProfileBean;
import com.yzx.chat.network.api.user.SearchUserBean;
import com.yzx.chat.network.api.user.UploadAvatarBean;
import com.yzx.chat.network.framework.ApiProxy;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.HttpApi;
import com.yzx.chat.network.framework.HttpDataFormatAdapter;
import com.yzx.chat.network.framework.HttpParam;
import com.yzx.chat.network.framework.HttpParamsType;
import com.yzx.chat.network.framework.HttpRequest;
import com.yzx.chat.network.framework.HttpResponse;
import com.yzx.chat.network.framework.NetworkExecutor;
import com.yzx.chat.network.framework.RequestType;
import com.yzx.chat.network.framework.ResponseCallback;
import com.yzx.chat.network.framework.UploadPath;
import com.yzx.chat.tool.ApiHelper;
import com.yzx.chat.util.Base64Util;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.RSAUtil;
import com.yzx.chat.widget.view.GlideHexagonTransform;

import java.lang.reflect.Type;
import java.util.Map;


public class TestActivity extends BaseCompatActivity {


    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        ApiProxy apiProxy = new ApiProxy("http://120.79.212.170/v1/", new HttpDataFormatAdapter() {
            @Nullable
            @Override
            public String paramsToString(HttpRequest httpRequest) {
                Map<String, Object> params = httpRequest.params().get(HttpParamsType.PARAMETER_HTTP);
                LogUtil.e("开始访问：" + httpRequest.url());
                String json = ApiHelper.getDefaultGsonInstance().toJson(params);
                LogUtil.e("request: " + json);
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
                LogUtil.e(httpResponse);
                return null;
            }
        });
        Api api = (Api) apiProxy.getProxyInstance(Api.class);
        Call<JsonBean> call =  api.login("244546875","13631232530","225325");
        call.setResponseCallback(new ResponseCallback<JsonBean>() {
            @Override
            public void onResponse(HttpResponse<JsonBean> response) {
                LogUtil.e("dd");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                LogUtil.e("dd");
            }

            @Override
            public boolean isExecuteNextTask() {
                return false;
            }
        });
        NetworkExecutor.init(this);
        NetworkExecutor.getInstance().submit(call);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {

    }

    public interface Api {

        @HttpApi(RequestType = RequestType.POST, url = "auth/user")
        Call<JsonBean> login(@HttpParam("account") String account,@HttpParam("password") String password,@HttpParam("validateCode") String validateCode);

    }

    public static final class JsonBean {
        public int code;
        public String message;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }
}


