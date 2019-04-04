package com.yzx.chat.module.login.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Service;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.module.login.contract.LoginContract;
import com.yzx.chat.module.login.presenter.LoginPresenter;
import com.yzx.chat.util.AnimationUtil;
import com.yzx.chat.util.RegexUtil;
import com.yzx.chat.util.ViewUtil;
import com.yzx.chat.widget.listener.OnOnlySingleClickListener;
import com.yzx.chat.widget.listener.SimpleTextWatcher;
import com.yzx.chat.widget.view.ProgressButton;

import androidx.fragment.app.FragmentActivity;

/**
 * Created by YZX on 2018年07月08日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class LoginFragment extends BaseFragment<LoginContract.Presenter> implements LoginContract.View {

    public static final String TAG = LoginFragment.class.getName();

    private final static int MIN_PASSWORD_LENGTH = 8;

    private EditText mEtLoginTelephone;
    private EditText mEtLoginPassword;
    private TextView mTvJumpToRegister;
    private TextView mTvErrorHint;
    private ProgressButton mPBtnLogin;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_login;
    }

    @Override
    protected void init(View parentView) {
        mEtLoginTelephone = parentView.findViewById(R.id.mEtTelephone);
        mEtLoginPassword = parentView.findViewById(R.id.mEtPassword);
        mTvJumpToRegister = parentView.findViewById(R.id.mTvJumpToRegister);
        mPBtnLogin = parentView.findViewById(R.id.mPBtnLogin);
        mTvErrorHint = parentView.findViewById(R.id.mTvErrorHint);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        mTvJumpToRegister.setOnClickListener(mOnViewClickListener);
        mPBtnLogin.setOnClickListener(mOnViewClickListener);
        mEtLoginTelephone.addTextChangedListener(mTextWatcher);
        mEtLoginPassword.addTextChangedListener(mTextWatcher);
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewUtil.registerAutoScrollAtInput(getView(), mPBtnLogin);
    }

    @Override
    public void onPause() {
        super.onPause();
        ViewUtil.unregisterAutoScrollAtInput(getView());
    }

    private void tryLogin() {
        showErrorDialog(null);
        final String username = mEtLoginTelephone.getText().toString();
        final String password = mEtLoginPassword.getText().toString();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            showErrorDialog(getString(R.string.LoginActivity_Error_NoneInput));
            return;
        }
        if (!RegexUtil.isMobile(username)) {
            showErrorDialog(getString(R.string.LoginActivity_Error_PhoneNumber));
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            showErrorDialog(getString(R.string.LoginActivity_Error_PasswordLength) + MIN_PASSWORD_LENGTH);
            return;
        }

        mPBtnLogin.startHideAnim(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPresenter.tryLogin(username, password);
            }
        });
    }

    private final View.OnClickListener mOnViewClickListener = new OnOnlySingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.mPBtnLogin:
                    tryLogin();
                    break;
                case R.id.mTvJumpToRegister:
                    jumpToRegisterPage();
                    break;
            }
        }
    };

    private final TextWatcher mTextWatcher = new SimpleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            showErrorDialog(null);
        }
    };

    @Override
    public LoginContract.Presenter getPresenter() {
        return new LoginPresenter();
    }

    @Override
    public void jumpToVerifyPage() {
        mPBtnLogin.startShowAnim(null);
        VerifyFragment.VerifyInfo info = new VerifyFragment.VerifyInfo();
        info.telephone = mEtLoginTelephone.getText().toString();
        info.password = mEtLoginPassword.getText().toString();
        LoginActivity.jumpToVerifyPage((FragmentActivity) mContext, info);
    }


    private void jumpToRegisterPage() {
        LoginActivity.jumpToRegisterPage((FragmentActivity) mContext);
    }

    @Override
    public void showErrorDialog(String error) {
        mTvErrorHint.setText(error);
        if (!TextUtils.isEmpty(error)) {
            AnimationUtil.errorTranslateAnim(mTvErrorHint);
            ((Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE)).vibrate(50);
        }
        mPBtnLogin.startShowAnim(null);
    }

    @Override
    public void startHomeActivity() {
        AnimationUtil.circularRevealShowByFullActivityAnim((Activity) mContext, mPBtnLogin, R.drawable.src_bg_splash, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animation.removeListener(this);
                LoginActivity.startHomeActivity((Activity) mContext);
            }
        });
    }
}
