package com.yzx.chat.mvp.view.activity;


import android.os.Bundle;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.POST;
import com.yzx.chat.network.framework.Param;

import com.yzx.chat.network.framework.NetworkExecutor;


public class TestActivity extends BaseCompatActivity {


    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

        NetworkExecutor.init(this);

    }

    @Override
    protected void setup(Bundle savedInstanceState) {

    }

    public interface Api {

        @POST( "auth/user")
        Call<JsonBean> login(@Param("account") String account, @Param("password") String password, @Param("validateCode") String validateCode);

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


