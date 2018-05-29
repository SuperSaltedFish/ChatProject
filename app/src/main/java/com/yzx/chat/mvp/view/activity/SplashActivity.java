package com.yzx.chat.mvp.view.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.mvp.contract.SplashContract;
import com.yzx.chat.mvp.presenter.SplashPresenter;



public class SplashActivity extends BaseCompatActivity<SplashContract.Presenter> implements SplashContract.View {

    private static int PERMISSIONS_REQUEST_CODE = 1;

    @Override
    protected int getLayoutID() {
        return 0;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        requestPermissionsInCompatMode(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
    }

    @Override
    protected void onRequestPermissionsResult(int requestCode, boolean isSuccess) {
        if (isSuccess) {
            if (requestCode == PERMISSIONS_REQUEST_CODE) {
                    mPresenter.checkLogin();
            }
        }else {
            finish();
        }
    }

    @Override
    public SplashContract.Presenter getPresenter() {
        return new SplashPresenter();
    }

    @Override
    public void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void startHomeActivity() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void error(String error) {

    }
}
