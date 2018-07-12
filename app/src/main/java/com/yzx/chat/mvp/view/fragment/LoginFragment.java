package com.yzx.chat.mvp.view.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
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
import com.yzx.chat.mvp.contract.LoginContract;
import com.yzx.chat.mvp.presenter.LoginPresenter;
import com.yzx.chat.mvp.view.activity.LoginActivity;
import com.yzx.chat.mvp.view.activity.SplashActivity;
import com.yzx.chat.util.AnimationUtil;
import com.yzx.chat.util.RegexUtil;
import com.yzx.chat.util.ViewUtil;

/**
 * Created by YZX on 2018年07月08日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class LoginFragment extends BaseFragment<LoginContract.Presenter> implements LoginContract.View {

    private final static int MIN_PASSWORD_LENGTH = 8;

    private EditText mEtLoginTelephone;
    private EditText mEtLoginPassword;
    private TextView mTvJumpToRegister;
  //  private Button mBtnForgotPassword;
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
        mEtLoginTelephone = parentView.findViewById(R.id.LoginFragment_mEtTelephone);
        mEtLoginPassword = parentView.findViewById(R.id.LoginFragment_mEtPassword);
        mTelephoneUnderline = parentView.findViewById(R.id.LoginFragment_mTelephoneUnderline);
        mPasswordUnderline = parentView.findViewById(R.id.LoginFragment_mPasswordUnderline);
     //   mBtnForgotPassword = parentView.findViewById(R.id.LoginFragment_mBtnForgotPassword);
        mTvJumpToRegister = parentView.findViewById(R.id.LoginFragment_mTvJumpToRegister);
        mPbLoginProgress = parentView.findViewById(R.id.LoginFragment_mPbLoginProgress);
        mBtnLogin = parentView.findViewById(R.id.LoginFragment_mBtnLogin);
        mTvErrorHint = parentView.findViewById(R.id.LoginFragment_mTvErrorHint);
    }

    @Override
    protected void setup() {
        mTvJumpToRegister.setOnClickListener(mOnViewClickListener);
        mBtnLogin.setOnClickListener(mOnViewClickListener);
      //  mBtnForgotPassword.setOnClickListener(mOnViewClickListener);
        mEtLoginTelephone.setOnFocusChangeListener(mOnInputFocusChangeListener);
        mEtLoginPassword.setOnFocusChangeListener(mOnInputFocusChangeListener);
        mEtLoginTelephone.addTextChangedListener(mTextWatcher);
        mEtLoginPassword.addTextChangedListener(mTextWatcher);

        ViewUtil.autoScrollAtInput(mParentView,mBtnLogin);
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
       // mBtnForgotPassword.setEnabled(!isDisable);
        mEtLoginTelephone.clearFocus();
        mEtLoginPassword.clearFocus();
        isDisableInput = isDisable;
    }

    private void tryLogin() {
        showErrorHint(null);
        final String username = mEtLoginTelephone.getText().toString();
        final String password = mEtLoginPassword.getText().toString();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            showErrorHint(getString(R.string.LoginActivity_Error_NoneInput));
            return;
        }
        if (!RegexUtil.isMobile(username)) {
            showErrorHint(getString(R.string.LoginActivity_Error_PhoneNumber));
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            showErrorHint(getString(R.string.LoginActivity_Error_PasswordLength) + MIN_PASSWORD_LENGTH);
            return;
        }

        startProgressAnim(true, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPresenter.tryLogin(username, password);
            }
        });
    }

    private final View.OnClickListener mOnViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isDisableInput) {
                switch (v.getId()) {
                    case R.id.LoginFragment_mBtnLogin:
                        tryLogin();
                        break;
                    case R.id.LoginFragment_mTvJumpToRegister:
                        jumpToRegisterPage();
                        break;
//                    case R.id.LoginFragment_mBtnForgotPassword:
//                        break;
                }
            }
        }
    };

    private final View.OnFocusChangeListener mOnInputFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case R.id.LoginFragment_mEtTelephone:
                    mTelephoneUnderline.setSelected(hasFocus);
                    break;
                case R.id.LoginFragment_mEtPassword:
                    mPasswordUnderline.setSelected(hasFocus);
                    break;
            }
        }
    };

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            showErrorHint(null);
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
        info.serverSecretKey = mPresenter.getServerSecretKey();
        Intent intent = new Intent(LoginActivity.INTENT_ACTION);
        intent.putExtra(LoginActivity.INTENT_EXTRA_PAGE_TYPE, LoginActivity.PAGE_TYPE_VERIFY);
        intent.putExtra(LoginActivity.INTENT_EXTRA_PAGE_PARAM, info);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }


    private void jumpToRegisterPage() {
        Intent intent = new Intent(LoginActivity.INTENT_ACTION);
        intent.putExtra(LoginActivity.INTENT_EXTRA_PAGE_TYPE, LoginActivity.PAGE_TYPE_REGISTER);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void showErrorHint(String error) {
        mTvErrorHint.setText(error);
        if (!TextUtils.isEmpty(error)) {
            AnimationUtil.errorTranslateAnim(mTvErrorHint);
        }
        startProgressAnim(false, null);
    }

    @Override
    public void startSplashActivity() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        AnimationUtil.circularRevealShowByFullActivityAnim(activity, mPbLoginProgress, R.drawable.src_bg_splash, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animation.removeAllListeners();
                startActivity(new Intent(mContext, SplashActivity.class));
            }
        });
    }
}
