package com.yzx.chat.view.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.contract.LoginContract;
import com.yzx.chat.presenter.LoginPresenter;
import com.yzx.chat.util.AnimationUtil;
import com.yzx.chat.util.RegexUtil;

import java.util.Locale;

public class LoginActivity extends BaseCompatActivity<LoginContract.Presenter> implements LoginContract.View {

    private final static int MIN_PASSWORD_LENGTH = 8;

    private ViewFlipper mVfPageSwitch;
    private Button mBtnLogin;
    private Button mBtnRegister;
    private Button mBtnVerify;
    private Button mBtnResend;
    private Button mBtnJumpToRegister;
    private Button mBtnJumpToLogin;
    private ProgressBar mPbLoginProgress;
    private ProgressBar mPbRegisterProgress;
    private ProgressBar mPbVerifyProgress;
    private EditText mEtLoginUsername;
    private EditText mEtLoginPassword;
    private EditText mEtVerifyCode;
    private EditText mEtRegisterUsername;
    private EditText mEtRegisterPassword;
    private EditText mEtRegisterConfirm;
    private EditText mEtRegisterNickname;
    private TextView mTvLoginHint;
    private TextView mTvRegisterHint;
    private TextView mTvVerifyHint;
    private ImageView mIvRegisterBack;
    private ImageView mIvVerifyBack;

    private boolean isDisableInput;
    private boolean isCountDownFinish;
    private int mVerifyType;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setView();
    }

    private void init() {
        mVfPageSwitch = (ViewFlipper) findViewById(R.id.LoginActivity_mVfPageSwitch);
        mBtnLogin = (Button) findViewById(R.id.FlipperLogin_mBtnLogin);
        mBtnRegister = (Button) findViewById(R.id.FlipperRegister_mBtnRegister);
        mBtnVerify = (Button) findViewById(R.id.FlipperVerify_mBtnVerify);
        mBtnResend = (Button) findViewById(R.id.FlipperVerify_mBtnResend);
        mBtnJumpToRegister = (Button) findViewById(R.id.FlipperLogin_mTvJumpToRegister);
        mBtnJumpToLogin = (Button) findViewById(R.id.FlipperRegister_mBtnJumpToLogin);
        mPbLoginProgress = (ProgressBar) findViewById(R.id.FlipperLogin_mPbLoginProgress);
        mPbRegisterProgress = (ProgressBar) findViewById(R.id.FlipperRegister_mPbRegisterProgress);
        mPbVerifyProgress = (ProgressBar) findViewById(R.id.FlipperVerify_mPbVerifyProgress);
        mEtLoginUsername = (EditText) findViewById(R.id.FlipperLogin_mEtUsername);
        mEtLoginPassword = (EditText) findViewById(R.id.FlipperLogin_mEtPassword);
        mEtVerifyCode = (EditText) findViewById(R.id.FlipperVerify_mEtVerifyCode);
        mEtRegisterUsername = (EditText) findViewById(R.id.FlipperRegister_mEtUsername);
        mEtRegisterNickname = (EditText) findViewById(R.id.FlipperRegister_mEtNickname);
        mEtRegisterPassword = (EditText) findViewById(R.id.FlipperRegister_mEtPassword);
        mEtRegisterConfirm = (EditText) findViewById(R.id.FlipperRegister_mEtConfirm);
        mTvLoginHint = (TextView) findViewById(R.id.FlipperLogin_mTvLoginHint);
        mTvRegisterHint = (TextView) findViewById(R.id.FlipperRegister_mTvRegisterHint);
        mTvVerifyHint = (TextView) findViewById(R.id.FlipperVerify_mTvVerifyHint);
        mIvRegisterBack = (ImageView) findViewById(R.id.FlipperRegister_mIvRegisterBack);
        mIvVerifyBack = (ImageView) findViewById(R.id.FlipperVerify_mIvVerifyBack);
    }

    private void setView() {
        mBtnLogin.setOnClickListener(mOnBtnLoginClick);
        mBtnRegister.setOnClickListener(mOnBtnRegisterClick);
        mBtnVerify.setOnClickListener(mOnBtnVerifyClick);
        mBtnResend.setOnClickListener(mOnBtnResendClick);
        mIvRegisterBack.setOnClickListener(mOnIBtnBackClick);
        mIvVerifyBack.setOnClickListener(mOnIBtnBackClick);
        mBtnJumpToRegister.setOnClickListener(mOnBtnJumpToRegisterClick);
        mBtnJumpToRegister.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mBtnJumpToLogin.setOnClickListener(mOnBtnJumpToLoginClick);
        mBtnJumpToLogin.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mEtLoginUsername.addTextChangedListener(mTextWatcher);
        mEtLoginPassword.addTextChangedListener(mTextWatcher);
        mEtRegisterUsername.addTextChangedListener(mTextWatcher);
        mEtRegisterPassword.addTextChangedListener(mTextWatcher);
        mEtRegisterConfirm.addTextChangedListener(mTextWatcher);
        mEtRegisterNickname.addTextChangedListener(mTextWatcher);
        mEtVerifyCode.addTextChangedListener(mTextWatcher);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVerifyCountDown.cancel();
    }

    private final View.OnClickListener mOnBtnLoginClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isDisableInput) {
                loginVerify();
            }
        }
    };

    private final View.OnClickListener mOnBtnRegisterClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isDisableInput) {
                registerVerify();
            }
        }
    };

    private final View.OnClickListener mOnBtnVerifyClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isDisableInput) {
                if (mVerifyType == LoginPresenter.VERIFY_TYPE_LOGIN) {
                    login(false);
                } else {
                    register();
                }
            }
        }
    };

    private final View.OnClickListener mOnBtnResendClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isDisableInput) {
                resendVerifyCode();
            }
        }
    };

    private final View.OnClickListener mOnBtnJumpToRegisterClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isDisableInput) {
                jumpToRegisterPager();
            }
        }
    };

    private final View.OnClickListener mOnBtnJumpToLoginClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isDisableInput) {
                jumpToLoginPager();
            }
        }
    };

    private final View.OnClickListener mOnIBtnBackClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isDisableInput) {
                backPager();
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
            mTvLoginHint.setVisibility(View.INVISIBLE);
            mTvRegisterHint.setVisibility(View.INVISIBLE);
            mTvVerifyHint.setVisibility(View.INVISIBLE);
        }
    };

    private final CountDownTimer mVerifyCountDown = new CountDownTimer(6000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
            int endTime = (int) (millisUntilFinished / 1000);
            mBtnResend.setText(String.format(Locale.getDefault(), "Resend(%ds)", endTime));
        }

        @Override
        public void onFinish() {
            isCountDownFinish = true;
            if (!isDisableInput) {
                mBtnResend.setEnabled(true);
            }
            mBtnResend.setText("Resend");
        }
    };


    private void loginVerify() {
        final String username = mEtLoginUsername.getText().toString();
        final String password = mEtLoginPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            showError(mTvLoginHint, getString(R.string.LoginActivity_Error_NoneInput));
            return;
        }
        if (!RegexUtil.isMobile(username)) {
            showError(mTvLoginHint, getString(R.string.LoginActivity_Error_PhoneNumber));
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            showError(mTvLoginHint, getString(R.string.LoginActivity_Error_PasswordLength) + MIN_PASSWORD_LENGTH);
            return;
        }
        mVerifyType = LoginPresenter.VERIFY_TYPE_NONE;
        startProgressAnim(mBtnLogin, mPbLoginProgress, true, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPresenter.loginVerify(username, password);
            }
        });
    }

    private void login(boolean isSkipVerify) {
        final String username = mEtLoginUsername.getText().toString();
        final String password = mEtLoginPassword.getText().toString();
        final String verifyCode = mEtVerifyCode.getText().toString();
        if (!isSkipVerify && TextUtils.isEmpty(verifyCode)) {
            showError(mTvVerifyHint, getString(R.string.LoginActivity_Error_NoneVerify));
            return;
        }
        startProgressAnim(mBtnVerify, mPbVerifyProgress, true, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPresenter.login(username, password, verifyCode);
            }
        });
    }

    private void registerVerify() {
        final String username = mEtRegisterUsername.getText().toString();
        final String nickname = mEtRegisterNickname.getText().toString();
        final String password = mEtRegisterPassword.getText().toString();
        final String confirm = mEtRegisterConfirm.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
            showError(mTvRegisterHint, getString(R.string.LoginActivity_Error_NoneInput));
            return;
        }
        if (!RegexUtil.isMobile(username)) {
            showError(mTvRegisterHint, getString(R.string.LoginActivity_Error_PhoneNumber));
            return;
        }
        if (!RegexUtil.isLegalNickname(nickname)) {
            showError(mTvRegisterHint, getString(R.string.LoginActivity_Error_IllegalNickname));
            return;
        }
        if (!RegexUtil.isLegalPassword(password)) {
            showError(mTvRegisterHint, getString(R.string.LoginActivity_Error_IllegalPassword));
            return;
        }
        mVerifyType = LoginPresenter.VERIFY_TYPE_NONE;
        startProgressAnim(mBtnRegister, mPbRegisterProgress, true, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPresenter.registerVerify(username);
            }
        });
    }

    private void register() {
        final String username = mEtRegisterUsername.getText().toString();
        final String nickname = mEtRegisterNickname.getText().toString();
        final String password = mEtRegisterPassword.getText().toString();
        final String verifyCode = mEtVerifyCode.getText().toString();

        if (TextUtils.isEmpty(verifyCode)) {
            showError(mTvVerifyHint, getString(R.string.LoginActivity_Error_NoneVerify));
            return;
        }
        startProgressAnim(mBtnVerify, mPbVerifyProgress, true, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPresenter.register(username, password, nickname, verifyCode);
            }
        });
    }

    private void resendVerifyCode() {
        if (mVerifyType == LoginPresenter.VERIFY_TYPE_LOGIN) {
            loginVerify();
        } else {
            registerVerify();
        }
    }

    private void startVerifyCountDownTimer() {
        isCountDownFinish = false;
        mBtnResend.setEnabled(false);
        mVerifyCountDown.cancel();
        mVerifyCountDown.start();
    }

    private void startProgressAnim(final Button btn, final ProgressBar bar, final boolean isCloseAnim, Animator.AnimatorListener listener) {
        if (isCloseAnim) {
            setDisableInputState(true);
            AnimationUtil.circularRevealHideAnim(btn, listener, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    bar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animation.removeAllListeners();
                    btn.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            AnimationUtil.circularRevealShowAnim(btn, listener, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    btn.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animation.removeAllListeners();
                    bar.setVisibility(View.INVISIBLE);
                    setDisableInputState(false);
                }
            });
        }
    }

    private void jumpToLoginPager() {
        mVerifyType = LoginPresenter.VERIFY_TYPE_NONE;
        if (mVfPageSwitch.getDisplayedChild() != 0) {
            mVfPageSwitch.setDisplayedChild(0);
        }
    }

    private void jumpToRegisterPager() {
        mVerifyType = LoginPresenter.VERIFY_TYPE_NONE;
        if (mVfPageSwitch.getDisplayedChild() != 1) {
            mVfPageSwitch.setDisplayedChild(1);
        }
    }

    private void jumpToVerifyPager() {
        if (mVfPageSwitch.getDisplayedChild() != 2) {
            mVfPageSwitch.setDisplayedChild(2);
        }
        startVerifyCountDownTimer();
    }

    private void backPager() {
        if(mVfPageSwitch.getDisplayedChild()==1){
            jumpToLoginPager();
        }else if(mVerifyType==LoginPresenter.VERIFY_TYPE_LOGIN){
            jumpToLoginPager();
        }else {
            jumpToRegisterPager();
        }

    }

    private void setDisableInputState(boolean isDisable) {
        isDisableInput = isDisable;
        mEtLoginUsername.setEnabled(!isDisable);
        mEtLoginPassword.setEnabled(!isDisable);
        mEtRegisterUsername.setEnabled(!isDisable);
        mEtRegisterPassword.setEnabled(!isDisable);
        mEtRegisterConfirm.setEnabled(!isDisable);
        mEtRegisterNickname.setEnabled(!isDisable);
        mEtVerifyCode.setEnabled(!isDisable);
        mBtnJumpToRegister.setEnabled(!isDisable);
        mBtnJumpToLogin.setEnabled(!isDisable);
        mBtnResend.setEnabled((!isDisable) && isCountDownFinish);
    }

    private void showError(TextView hintView, String errorMessage) {
        TranslateAnimation animation = new TranslateAnimation(-8, 8, 0, 0);
        animation.setDuration(20);
        animation.setRepeatCount(4);
        animation.setRepeatMode(Animation.REVERSE);
        hintView.setVisibility(View.VISIBLE);
        hintView.setText(errorMessage);
        hintView.startAnimation(animation);
    }

    @Override
    public LoginContract.Presenter getPresenter() {
        return new LoginPresenter();
    }

    @Override
    public void inputLoginVerifyCode(boolean isSkipVerify) {
        if (isSkipVerify) {
            login(true);
        } else {
            jumpToVerifyPager();
            startProgressAnim(mBtnRegister, mPbLoginProgress, false, null);
            mVerifyType = LoginPresenter.VERIFY_TYPE_LOGIN;
        }
    }

    @Override
    public void inputRegisterVerifyCode() {
        jumpToVerifyPager();
        startProgressAnim(mBtnRegister, mPbRegisterProgress, false, null);
        mVerifyType = LoginPresenter.VERIFY_TYPE_REGISTER;
    }

    @Override
    public void verifySuccess() {
        AnimationUtil.circularRevealShowByFullActivityAnim(LoginActivity.this, mPbVerifyProgress, R.color.theme_main_color, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animation.removeAllListeners();
                LoginActivity.this.startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                LoginActivity.this.finish();
            }
        });
    }

    @Override
    public void loginFailure(String reason) {
        if (mVerifyType == LoginPresenter.VERIFY_TYPE_LOGIN) {
            verifyFailure(reason);
        } else {
            startProgressAnim(mBtnLogin, mPbLoginProgress, false, null);
            showError(mTvLoginHint, reason);
        }
    }

    @Override
    public void registerFailure(String reason) {
        if (mVerifyType == LoginPresenter.VERIFY_TYPE_REGISTER) {
            verifyFailure(reason);
        } else {
            startProgressAnim(mBtnRegister, mPbRegisterProgress, false, null);
            showError(mTvRegisterHint, reason);
        }
    }

    @Override
    public void verifyFailure(String reason) {
        startProgressAnim(mBtnVerify, mPbVerifyProgress, false, null);
        showError(mTvVerifyHint, reason);
    }
}
