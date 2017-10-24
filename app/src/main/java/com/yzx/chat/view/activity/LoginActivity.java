package com.yzx.chat.view.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.contract.LoginContract;
import com.yzx.chat.presenter.LoginPresenter;
import com.yzx.chat.util.AnimationUtil;
import com.yzx.chat.util.RegexUtil;
import com.yzx.chat.util.ToastUtil;

public class LoginActivity extends BaseCompatActivity<LoginContract.Presenter> implements LoginContract.View {

    private final static int MIN_PASSWORD_LENGTH = 8;
    private final static int VERIFY_TYPE_LOGIN = 1;
    private final static int VERIFY_TYPE_REGISTER = 2;

    private ViewFlipper mVfPageSwitch;
    private Button mBtnLogin;
    private Button mBtnRegister;
    private Button mBtVerify;
    //    private ImageView mIvBackRegister;
//    private ImageView mIvBackRegisterVerify;
//    private TextView mTvRequestRegister;
    //    private TextView mTvBackHomeRegister;
//    private TextView mTvBackHomeRegisterVerify;
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
    private boolean isDisableInput;
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
        mVfPageSwitch = findViewById(R.id.LoginActivity_mVfPageSwitch);
//        mIvBackRegister = (ImageView) findViewById(R.id.FlipperRegister_mIvBackRegister);
//        mIvBackRegisterVerify = (ImageView) findViewById(R.id.FlipperRegister_mIvBackRegisterVerify);
//        mTvRequestRegister = (TextView) findViewById(R.id.FlipperLogin_mTvRequestRegister);
//        mTvBackHomeRegister = (TextView) findViewById(R.id.FlipperRegister_mIvBackHomeRegister);
//        mTvBackHomeRegisterVerify = (TextView) findViewById(R.id.FlipperRegister_mIvBackHomeRegisterVerify);
        mBtnLogin = findViewById(R.id.FlipperLogin_mTvLogin);
        mBtnRegister = findViewById(R.id.FlipperRegister_mBtnRegister);
        mBtVerify = findViewById(R.id.FlipperVerify_mBtVerify);
        mPbLoginProgress = findViewById(R.id.FlipperLogin_mPbLoginProgress);
        mPbRegisterProgress = findViewById(R.id.FlipperRegister_mPbRegisterProgress);
        mPbVerifyProgress = findViewById(R.id.FlipperVerify_mPbVerifyProgress);
        mEtLoginUsername = findViewById(R.id.FlipperLogin_mEtUsername);
        mEtLoginPassword = findViewById(R.id.FlipperLogin_mEtPassword);
        mEtVerifyCode = findViewById(R.id.FlipperVerify_mEtVerifyCode);
        mEtRegisterUsername = findViewById(R.id.FlipperRegister_mEtUsername);
        mEtRegisterNickname = findViewById(R.id.FlipperRegister_mEtNickname);
        mEtRegisterPassword = findViewById(R.id.FlipperRegister_mEtPassword);
        mEtRegisterConfirm = findViewById(R.id.FlipperRegister_mEtConfirm);
        mTvLoginHint = findViewById(R.id.FlipperLogin_mTvLoginHint);
    }

    private void setView() {
//        mTvRequestRegister.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mBtnLogin.setOnClickListener(mOnBtnLoginClick);
        mBtnRegister.setOnClickListener(mOnBtnRegisterClick);
        mBtVerify.setOnClickListener(mOnBtnVerifyClick);
//        mTvRequestRegister.setOnClickListener(mOnRequestRegisterClick);
//        mBtnNext.setOnClickListener(mOnRequestRegisterClick);
//        mIvBackRegister.setOnClickListener(mOnPagePreviousClick);
//        mIvBackRegisterVerify.setOnClickListener(mOnPagePreviousClick);
//        mTvBackHomeRegister.setOnClickListener(mOnBackHomeClick);
//        mTvBackHomeRegisterVerify.setOnClickListener(mOnBackHomeClick);
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
                if (mVerifyType == VERIFY_TYPE_LOGIN) {
                    login(false);
                } else {
                    register();
                }
            }
        }
    };

//    private final View.OnClickListener mOnRequestRegisterClick = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (mState != CURRENT_STATE_INPUTTING) {
//                return;
//            }
//            mVfPageSwitch.setDisplayedChild(1);
//        }
//    };

    //    private final View.OnClickListener mOnPagePreviousClick = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            mVfPageSwitch.showPrevious();
//        }
//    };
//
//    private final View.OnClickListener mOnBackHomeClick = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            mVfPageSwitch.setDisplayedChild(0);
//        }
//    };
//


    private void switchToLoginPager() {
        mVfPageSwitch.setDisplayedChild(0);
    }

    private void switchToRegisterPager() {
        mVfPageSwitch.setDisplayedChild(1);
    }

    private void switchToVerifyPager() {
        mVfPageSwitch.setDisplayedChild(2);
    }

    private void setDisableInputState(boolean isDisable) {
        isDisableInput = isDisable;
    }


    private void loginVerify() {
        final String username = mEtLoginUsername.getText().toString();
        final String password = mEtLoginPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            showError(getString(R.string.LoginActivity_Error_NoneInput));
            return;
        }
        if (!RegexUtil.isMobile(username)) {
            showError(getString(R.string.LoginActivity_Error_PhoneNumber));
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            showError(getString(R.string.LoginActivity_Error_PasswordLength) + MIN_PASSWORD_LENGTH);
            return;
        }
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
            showError(getString(R.string.LoginActivity_Error_NoneVerify));
            return;
        }
        startProgressAnim(mBtVerify, mPbVerifyProgress, true, new AnimatorListenerAdapter() {
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
            showError(getString(R.string.LoginActivity_Error_NoneInput));
            return;
        }
        if (!RegexUtil.isLegalNickname(nickname)) {
            showError(getString(R.string.LoginActivity_Error_IllegalNickname));
            return;
        }
        if (!RegexUtil.isLegalPassword(password)) {
            showError(getString(R.string.LoginActivity_Error_IllegalPassword));
            return;
        }
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
            showError(getString(R.string.LoginActivity_Error_NoneVerify));
            return;
        }
        startProgressAnim(mBtVerify, mPbVerifyProgress, true, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPresenter.register(username, password, nickname, verifyCode);
            }
        });
    }

    private void showError(String errorMessage) {
        TranslateAnimation animation = new TranslateAnimation(-8, 8, 0, 0);
        animation.setDuration(20);
        animation.setRepeatCount(4);
        animation.setRepeatMode(Animation.REVERSE);
        mTvLoginHint.setText(errorMessage);
        mTvLoginHint.startAnimation(animation);
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

    @Override
    public LoginContract.Presenter getPresenter() {
        return new LoginPresenter();
    }

    @Override
    public void startLogin(boolean isSkipVerify) {
        if (isSkipVerify) {
            login(true);
        } else {
            switchToVerifyPager();
            mVerifyType = VERIFY_TYPE_LOGIN;
        }
    }

    @Override
    public void startRegister() {
        switchToVerifyPager();
        mVerifyType = VERIFY_TYPE_REGISTER;
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
        startProgressAnim(mBtnLogin, mPbLoginProgress, false, null);
        showError(reason);
    }

    @Override
    public void registerFailure(String reason) {
        startProgressAnim(mBtnRegister, mPbRegisterProgress, false, null);
        showError(reason);
    }

    @Override
    public void verifyFailure(String reason) {
        startProgressAnim(mBtVerify, mPbVerifyProgress, false, null);
        showError(reason);
    }
}
