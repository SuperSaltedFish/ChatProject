package com.yzx.chat.module.login.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
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
import com.yzx.chat.module.main.view.SplashActivity;
import com.yzx.chat.util.AnimationUtil;
import com.yzx.chat.util.RegexUtil;
import com.yzx.chat.util.ViewUtil;
import com.yzx.chat.widget.listener.OnOnlySingleClickListener;
import com.yzx.chat.widget.listener.SimpleTextWatcher;

import java.util.Objects;

/**
 * Created by YZX on 2018年07月08日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class LoginFragment extends BaseFragment<LoginContract.Presenter> implements LoginContract.View {

    private final static int MIN_PASSWORD_LENGTH = 8;

    private EditText mEtLoginTelephone;
    private EditText mEtLoginPassword;
    private TextView mTvJumpToRegister;
    private TextView mTvErrorHint;
    private Button mBtnLogin;
    private ProgressBar mPbLoginProgress;
    private View mTelephoneUnderline;
    private View mPasswordUnderline;

    private boolean isDisableInput;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_login;
    }

    @Override
    protected void init(View parentView) {
        mEtLoginTelephone = parentView.findViewById(R.id.mEtTelephone);
        mEtLoginPassword = parentView.findViewById(R.id.mEtPassword);
        mTelephoneUnderline = parentView.findViewById(R.id.mTelephoneUnderline);
        mPasswordUnderline = parentView.findViewById(R.id.mPasswordUnderline);
        mTvJumpToRegister = parentView.findViewById(R.id.mTvJumpToRegister);
        mPbLoginProgress = parentView.findViewById(R.id.mPbLoginProgress);
        mBtnLogin = parentView.findViewById(R.id.mBtnLogin);
        mTvErrorHint = parentView.findViewById(R.id.mTvErrorHint);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        mTvJumpToRegister.setOnClickListener(mOnViewClickListener);
        mBtnLogin.setOnClickListener(mOnViewClickListener);
        mEtLoginTelephone.setOnFocusChangeListener(mOnInputFocusChangeListener);
        mEtLoginPassword.setOnFocusChangeListener(mOnInputFocusChangeListener);
        mEtLoginTelephone.addTextChangedListener(mTextWatcher);
        mEtLoginPassword.addTextChangedListener(mTextWatcher);
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewUtil.registerAutoScrollAtInput(getView(), mBtnLogin);
    }

    @Override
    public void onPause() {
        super.onPause();
        ViewUtil.unregisterAutoScrollAtInput(getView());
    }

    private void startProgressAnim(final boolean isCloseAnim, Animator.AnimatorListener listener) {
        if (isCloseAnim) {
            if (mBtnLogin.getVisibility() == View.VISIBLE) {
                setDisableInputState(true);
                AnimationUtil.circularRevealHideAnim(mBtnLogin, listener, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mPbLoginProgress.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animation.removeAllListeners();
                        mBtnLogin.setVisibility(View.INVISIBLE);
                    }
                });
            }
        } else {
            if (mBtnLogin.getVisibility() == View.INVISIBLE) {
                AnimationUtil.circularRevealShowAnim(mBtnLogin, listener, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mBtnLogin.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animation.removeAllListeners();
                        mPbLoginProgress.setVisibility(View.INVISIBLE);
                        setDisableInputState(false);
                    }
                });
            }
        }
    }

    private void setDisableInputState(boolean isDisable) {
        mEtLoginTelephone.setEnabled(!isDisable);
        mEtLoginPassword.setEnabled(!isDisable);
        mTvJumpToRegister.setEnabled(!isDisable);
        mEtLoginTelephone.clearFocus();
        mEtLoginPassword.clearFocus();
        isDisableInput = isDisable;
        LoginActivity.setEnableBackPressed((LoginActivity) Objects.requireNonNull(getActivity()), !isDisable);
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

        startProgressAnim(true, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPresenter.tryLogin(username, password);
            }
        });
    }

    private final View.OnClickListener mOnViewClickListener = new OnOnlySingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            if (!isDisableInput) {
                switch (v.getId()) {
                    case R.id.mBtnLogin:
                        tryLogin();
                        break;
                    case R.id.mTvJumpToRegister:
                        jumpToRegisterPage();
                        break;
                }
            }
        }
    };

    private final View.OnFocusChangeListener mOnInputFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case R.id.mEtTelephone:
                    mTelephoneUnderline.setSelected(hasFocus);
                    break;
                case R.id.mEtPassword:
                    mPasswordUnderline.setSelected(hasFocus);
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
        startProgressAnim(false, null);
        VerifyFragment.VerifyInfo info = new VerifyFragment.VerifyInfo();
        info.telephone = mEtLoginTelephone.getText().toString();
        info.password = mEtLoginPassword.getText().toString();
        LoginActivity.jumpToVerifyPage(Objects.requireNonNull(getActivity()), info);
    }


    private void jumpToRegisterPage() {
        LoginActivity.jumpToRegisterPage(Objects.requireNonNull(getActivity()));
    }

    @Override
    public void showErrorDialog(String error) {
        mTvErrorHint.setText(error);
        if (!TextUtils.isEmpty(error)) {
            AnimationUtil.errorTranslateAnim(mTvErrorHint);
            ((Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE)).vibrate(50);
        }
        startProgressAnim(false, null);
    }

    @Override
    public void startHomeActivity() {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        AnimationUtil.circularRevealShowByFullActivityAnim(activity, mPbLoginProgress, R.drawable.src_bg_splash, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animation.removeAllListeners();
                startActivity(new Intent(mContext, SplashActivity.class));
                activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                activity.finish();
            }
        });
    }
}
