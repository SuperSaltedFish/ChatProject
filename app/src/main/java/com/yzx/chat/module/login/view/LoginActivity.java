package com.yzx.chat.module.login.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.module.main.view.HomeActivity;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class LoginActivity extends BaseCompatActivity {


    private static final String INTENT_EXTRA_MESSAGE = "Message";

    public static void startActivityOfNewTaskType(Context context, String message) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(INTENT_EXTRA_MESSAGE, message);
        context.startActivity(intent);
    }

    private boolean isEnableBackPressed;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_login;
    }


    protected void init(Bundle savedInstanceState) {

    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_LIGHT_BAR_STATUS);
        jumpToLoginPage(this);

        String message = getIntent().getStringExtra(INTENT_EXTRA_MESSAGE);
        if (!TextUtils.isEmpty(message)) {
            showErrorDialog(message);
        }
    }

    @Override
    public void onBackPressed() {
        if (!isEnableBackPressed) {
            super.onBackPressed();
        }
    }

    static void setDisableBackPressed(LoginActivity activity, boolean isEnable) {
        activity.isEnableBackPressed = isEnable;
    }

    static void jumpToLoginPage(FragmentActivity activity) {
        FragmentManager manager = activity.getSupportFragmentManager();
        if (manager.findFragmentByTag(LoginFragment.TAG) != null) {
            manager.popBackStack(null, 1);
        } else {
            manager.beginTransaction()
                    .replace(R.id.mFlContent, new LoginFragment(), LoginFragment.TAG)
                    .commitAllowingStateLoss();
        }
    }

    static void jumpToRegisterPage(FragmentActivity activity) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.anim_fragment_in_right, R.anim.anim_fragment_out_left, R.anim.anim_fragment_in_left, R.anim.anim_fragment_out_right)
                .addToBackStack(null)
                .replace(R.id.mFlContent, new RegisterFragment())
                .commitAllowingStateLoss();
    }

    static void jumpToVerifyPage(FragmentActivity activity, VerifyFragment.VerifyInfo info) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.anim_fragment_in_right, R.anim.anim_fragment_out_left, R.anim.anim_fragment_in_left, R.anim.anim_fragment_out_right)
                .replace(R.id.mFlContent, VerifyFragment.newInstance(info))
                .commitAllowingStateLoss();
    }

    static void startHomeActivity(Activity activity) {
        activity.startActivity(new Intent(activity, HomeActivity.class));
        activity.finish();
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


}
