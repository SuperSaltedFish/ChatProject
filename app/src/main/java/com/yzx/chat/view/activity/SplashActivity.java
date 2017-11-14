package com.yzx.chat.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.contract.SplashContract;
import com.yzx.chat.presenter.SplashPresenter;

public class SplashActivity extends BaseCompatActivity<SplashContract.Presenter> implements SplashContract.View {

    private boolean isInit;

    @Override
    protected int getLayoutID() {
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isInit) {
            isInit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPresenter.init();
                }
            }, 1000);
        }
    }

    @Override
    public SplashContract.Presenter getPresenter() {
        return new SplashPresenter();
    }

    @Override
    public void startLoginActivity() {
        startActivity(new Intent(this,LoginActivity.class));
        finish();
    }

    @Override
    public void startHomeActivity() {
        startActivity(new Intent(this,HomeActivity.class));
        finish();
        overridePendingTransition(R.anim.avtivity_slide_in_right,0);
    }

    @Override
    public void error(String error) {

    }
}
