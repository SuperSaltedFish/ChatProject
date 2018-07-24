package com.yzx.chat.mvp.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.mvp.view.fragment.LoginFragment;
import com.yzx.chat.mvp.view.fragment.RegisterFragment;
import com.yzx.chat.mvp.view.fragment.VerifyFragment;
import com.yzx.chat.util.AndroidUtil;

public class LoginActivity extends BaseCompatActivity {

    public final static String INTENT_EXTRA_PAGE_TYPE = "PageType";
    public final static String INTENT_EXTRA_PAGE_PARAM = "Param";
    public final static String INTENT_ACTION = LoginActivity.class + ".Switch";
    public final static int PAGE_TYPE_BACK = 0;
    public final static int PAGE_TYPE_REGISTER = 2;
    public final static int PAGE_TYPE_VERIFY = 3;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_login;
    }


    protected void init(Bundle savedInstanceState) {

    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_LIGHT_BAR_STATUS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mSwitchPageReceiver, new IntentFilter(INTENT_ACTION));
        jumpToLoginPage();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mSwitchPageReceiver);
        super.onDestroy();
    }


    private final BroadcastReceiver mSwitchPageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(INTENT_EXTRA_PAGE_TYPE, 0)) {
                case PAGE_TYPE_BACK:
                    LoginActivity.super.onBackPressed();
                    break;
                case PAGE_TYPE_REGISTER:
                    jumpToRegisterPage();
                    break;
                case PAGE_TYPE_VERIFY:
                    jumpToVerifyPage((VerifyFragment.VerifyInfo) intent.getParcelableExtra(INTENT_EXTRA_PAGE_PARAM));
                    break;
            }
        }
    };

    public void jumpToLoginPage() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.LoginActivity_mFlContent, new LoginFragment())
                .commitAllowingStateLoss();
    }

    public void jumpToRegisterPage() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.anim_fragment_in_right, R.anim.anim_fragment_out_left, R.anim.anim_fragment_in_left, R.anim.anim_fragment_out_right)
                .addToBackStack(null)
                .replace(R.id.LoginActivity_mFlContent, new RegisterFragment())
                .commitAllowingStateLoss();
    }

    public void jumpToVerifyPage(VerifyFragment.VerifyInfo info) {
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.anim_fragment_in_right, R.anim.anim_fragment_out_left, R.anim.anim_fragment_in_left, R.anim.anim_fragment_out_right)
                .replace(R.id.LoginActivity_mFlContent, VerifyFragment.newInstance(info))
                .commitAllowingStateLoss();
    }


}
