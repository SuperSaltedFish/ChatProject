package com.yzx.chat.view.activity;

import android.content.Intent;
import android.os.Bundle;

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
            mPresenter.initChat();
        }
    }

    @Override
    public SplashContract.Presenter getPresenter() {
        return new SplashPresenter();
    }

    @Override
    public void complete() {
        finish();
        startActivity(new Intent(this,HomeActivity.class));
    }

    @Override
    public void error(String error) {

    }
}
