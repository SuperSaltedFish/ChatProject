package com.yzx.chat.mvp.view.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.yzx.chat.mvp.contract.LoginContract;
import com.yzx.chat.mvp.presenter.LoginPresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.AnimationUtil;
import com.yzx.chat.util.RegexUtil;

import java.util.Locale;


public class LoginActivity extends BaseCompatActivity<LoginContract.Presenter> implements LoginContract.View {

    private final static int MIN_PASSWORD_LENGTH = 8;

    public final static int VERIFY_TYPE_LOGIN = 1;
    public final static int VERIFY_TYPE_REGISTER = 2;

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
    private ImageButton mIBtnRegisterBack;
    private ImageButton mIBtnVerifyBack;
    private ImageView mIvBackground;

    private Animation mTranslateAnimation;

    private boolean isDisableInput;
    private boolean isAllowResendVerifyCode;
    private int mVerifyType;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_login;
    }


    protected void init(Bundle savedInstanceState) {
        mVfPageSwitch = findViewById(R.id.LoginActivity_mVfPageSwitch);
        mBtnLogin = findViewById(R.id.FlipperLogin_mBtnLogin);
        mBtnRegister = findViewById(R.id.FlipperRegister_mBtnRegister);
        mBtnVerify = findViewById(R.id.FlipperVerify_mBtnVerify);
        mBtnResend = findViewById(R.id.FlipperVerify_mBtnResend);
        mBtnJumpToRegister = findViewById(R.id.FlipperLogin_mTvJumpToRegister);
        mBtnJumpToLogin = findViewById(R.id.FlipperRegister_mBtnJumpToLogin);
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
        mTvRegisterHint = findViewById(R.id.FlipperRegister_mTvRegisterHint);
        mTvVerifyHint = findViewById(R.id.FlipperVerify_mTvVerifyHint);
        mIBtnRegisterBack = findViewById(R.id.FlipperRegister_mIBtnRegisterBack);
        mIBtnVerifyBack = findViewById(R.id.FlipperVerify_mIBtnVerifyBack);
        mIvBackground = findViewById(R.id.LoginActivity_mIvBackground);
        mTranslateAnimation = AnimationUtils.loadAnimation(this, R.anim.bg_translate_anim);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS);

        mBtnLogin.setOnClickListener(mOnViewClickListener);
        mBtnRegister.setOnClickListener(mOnViewClickListener);
        mBtnVerify.setOnClickListener(mOnViewClickListener);
        mBtnResend.setOnClickListener(mOnViewClickListener);
        mIBtnRegisterBack.setOnClickListener(mOnViewClickListener);
        mIBtnVerifyBack.setOnClickListener(mOnViewClickListener);
        mBtnJumpToRegister.setOnClickListener(mOnViewClickListener);
        mBtnJumpToLogin.setOnClickListener(mOnViewClickListener);
        mEtLoginUsername.addTextChangedListener(mTextWatcher);
        mEtLoginPassword.addTextChangedListener(mTextWatcher);
        mEtRegisterUsername.addTextChangedListener(mTextWatcher);
        mEtRegisterPassword.addTextChangedListener(mTextWatcher);
        mEtRegisterConfirm.addTextChangedListener(mTextWatcher);
        mEtRegisterNickname.addTextChangedListener(mTextWatcher);
        mEtVerifyCode.addTextChangedListener(mTextWatcher);
        mIvBackground.startAnimation(mTranslateAnimation);

        mBtnJumpToRegister.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mBtnJumpToLogin.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

        autoScrollView(mVfPageSwitch, mBtnLogin);
    }


    @Override
    public void onBackPressed() {
        backPager();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTranslateAnimation.cancel();
        mVerifyCountDown.cancel();
    }

    private final View.OnClickListener mOnViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isDisableInput) {
                switch (v.getId()) {
                    case R.id.FlipperLogin_mBtnLogin:
                        loginAfterObtainVerifyCode(true);
                        break;
                    case R.id.FlipperRegister_mBtnRegister:
                        checkRegisterInfoAndObtainVerifyCode(true);
                        break;
                    case R.id.FlipperVerify_mBtnVerify:
                        if (mVerifyType == VERIFY_TYPE_LOGIN) {
                            loginByVerifyCode();
                        } else {
                            register();
                        }
                        break;
                    case R.id.FlipperVerify_mBtnResend:
                        sendVerifyCode();
                        break;
                    case R.id.FlipperLogin_mTvJumpToRegister:
                        jumpToRegisterPage();
                        break;
                    case R.id.FlipperRegister_mBtnJumpToLogin:
                        jumpToLoginPage();
                        break;
                    case R.id.FlipperRegister_mIBtnRegisterBack:
                    case R.id.FlipperVerify_mIBtnVerifyBack:
                        backPager();
                        break;
                }
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

    private final CountDownTimer mVerifyCountDown = new CountDownTimer(60000, 1000) {

        private String mResendName;

        @Override
        public void onTick(long millisUntilFinished) {
            if (mResendName == null) {
                mResendName = getString(R.string.LoginActivity_Layout_Resend);
            }
            int endTime = (int) (millisUntilFinished / 1000);
            mBtnResend.setText(String.format(Locale.getDefault(), mResendName + "(%ds)", endTime));
        }

        @Override
        public void onFinish() {
            setAllowResendVerifyCode(true);
        }
    };

    private void loginAfterObtainVerifyCode(boolean isPlayAnimation) {
        mTvLoginHint.setText(null);
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
        if (isPlayAnimation) {
            startProgressAnimIfNeed(mBtnLogin, mPbLoginProgress, true, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mPresenter.tryObtainLoginVerifyCode(username, password);
                }
            });
        } else {
            mPresenter.tryObtainLoginVerifyCode(username, password);
        }
    }

    private void loginByVerifyCode() {
        mTvVerifyHint.setText(null);
        final String username = mEtLoginUsername.getText().toString();
        final String password = mEtLoginPassword.getText().toString();
        final String verifyCode = mEtVerifyCode.getText().toString();
        if (TextUtils.isEmpty(verifyCode)) {
            showError(mTvVerifyHint, getString(R.string.LoginActivity_Error_NoneVerify));
            return;
        }
        startProgressAnimIfNeed(mBtnVerify, mPbVerifyProgress, true, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPresenter.login(username, password, verifyCode);
            }
        });

    }

    private void register() {
        mTvVerifyHint.setText(null);
        final String username = mEtRegisterUsername.getText().toString();
        final String nickname = mEtRegisterNickname.getText().toString();
        final String password = mEtRegisterPassword.getText().toString();
        final String verifyCode = mEtVerifyCode.getText().toString();
        if (TextUtils.isEmpty(verifyCode)) {
            showError(mTvVerifyHint, getString(R.string.LoginActivity_Error_NoneVerify));
            return;
        }
        startProgressAnimIfNeed(mBtnVerify, mPbVerifyProgress, true, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPresenter.register(username, password, nickname, verifyCode);
            }
        });
    }

    private void checkRegisterInfoAndObtainVerifyCode(boolean isPlayAnimation) {
        mTvRegisterHint.setText(null);
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
        if (isPlayAnimation) {
            startProgressAnimIfNeed(mBtnRegister, mPbRegisterProgress, true, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mPresenter.obtainRegisterVerifyCode(username);
                }
            });
        } else {
            mPresenter.obtainRegisterVerifyCode(username);
        }
    }

    private void sendVerifyCode() {
        switch (mVerifyType) {
            case VERIFY_TYPE_LOGIN:
                loginAfterObtainVerifyCode(false);
                break;
            case VERIFY_TYPE_REGISTER:
                checkRegisterInfoAndObtainVerifyCode(false);
                break;
            default:
                return;
        }
        setAllowResendVerifyCode(false);
        mBtnResend.setText(R.string.LoginActivity_Error_Resending);
        mVerifyCountDown.cancel();
        mVerifyCountDown.start();
    }


    private void startProgressAnimIfNeed(final Button btn, final ProgressBar bar, final boolean isCloseAnim, Animator.AnimatorListener listener) {
        if (isCloseAnim) {
            if (btn.getVisibility() == View.VISIBLE) {
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
            }
        } else {
            if (btn.getVisibility() == View.INVISIBLE) {
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
    }

    private void backPager() {
        switch (mVfPageSwitch.getDisplayedChild()) {
            case 0:
                super.onBackPressed();
                break;
            case 1:
                jumpToLoginPage();
                break;
            case 2:
                mPresenter.reset();
                if (mVerifyType == VERIFY_TYPE_LOGIN) {
                    jumpToLoginPage();
                } else {
                    jumpToRegisterPage();
                }
                break;
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
        mBtnResend.setEnabled((!isDisable) && isAllowResendVerifyCode);
    }

    private void setAllowResendVerifyCode(boolean isAllow) {
        isAllowResendVerifyCode = isAllow;
        mBtnResend.setEnabled((!isDisableInput) && isAllowResendVerifyCode);
        mBtnResend.setText(R.string.LoginActivity_Layout_Resend);

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


    private void autoScrollView(final View root, final View scrollToView) {
        root.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int scrollHeight;
                        Rect rect = new Rect();
                        //获取root在窗体的可视区域
                        root.getWindowVisibleDisplayFrame(rect);
                        //获取root在窗体的不可视区域高度(被遮挡的高度)
                        int rootInvisibleHeight = root.getRootView().getHeight() - rect.bottom;
                        //若不可视区域高度大于150，则键盘显示
                        if (rootInvisibleHeight > 150) {
                            //获取scrollToView在窗体的坐标,location[0]为x坐标，location[1]为y坐标
                            int[] location = new int[2];
                            scrollToView.getLocationInWindow(location);
                            //计算root滚动高度，使scrollToView在可见区域的底部
                            scrollHeight = (location[1] + scrollToView.getHeight() + (int) AndroidUtil.dip2px(8)) - rect.bottom;
                        } else {
                            scrollHeight = -root.getScrollY();
                        }

                        View focusView = getCurrentFocus();
                        if (focusView instanceof EditText) {
                            EditText editText = (EditText) focusView;
                            View label = root.findViewById(editText.getLabelFor());
                            if (label != null && label.getY() - rootInvisibleHeight <= 0) {
                                scrollHeight = -root.getScrollY();

                            }
                        }
                        root.scrollBy(0, scrollHeight);
                    }
                });
    }

    @Override
    public LoginContract.Presenter getPresenter() {
        return new LoginPresenter();
    }

    @Override
    public void jumpToLoginPage() {
        if (mVfPageSwitch.getDisplayedChild() != 0) {
            mVfPageSwitch.setDisplayedChild(0);
            mVerifyCountDown.cancel();
        }
    }

    @Override
    public void jumpToRegisterPage() {
        if (mVfPageSwitch.getDisplayedChild() != 1) {
            mVfPageSwitch.setDisplayedChild(1);
            mVerifyCountDown.cancel();
        }
    }

    @Override
    public void jumpToVerifyPage() {
        int currentPageIndex = mVfPageSwitch.getDisplayedChild();
        switch (currentPageIndex) {
            case 0:
                mBtnVerify.setText(R.string.LoginActivity_Layout_Login);
                mVerifyType = VERIFY_TYPE_LOGIN;
                break;
            case 1:
                mBtnVerify.setText(R.string.LoginActivity_Layout_Register);
                mVerifyType = VERIFY_TYPE_REGISTER;
                break;
            default:
                return;
        }
        mTvVerifyHint.setText(null);
        mEtVerifyCode.setText(null);
        mVfPageSwitch.setDisplayedChild(2);
        mVerifyCountDown.start();
        startProgressAnimIfNeed(mBtnLogin, mPbRegisterProgress, false, null);
        startProgressAnimIfNeed(mBtnRegister, mPbRegisterProgress, false, null);
    }

    @Override
    public void showErrorHint(String error) {
        switch (mVfPageSwitch.getDisplayedChild()) {
            case 0:
                showError(mTvLoginHint, error);
                startProgressAnimIfNeed(mBtnLogin, mPbRegisterProgress, false, null);
                break;
            case 1:
                showError(mTvRegisterHint, error);
                startProgressAnimIfNeed(mBtnRegister, mPbRegisterProgress, false, null);
                break;
            case 2:
                showError(mTvVerifyHint, error);
                startProgressAnimIfNeed(mBtnVerify, mPbRegisterProgress, false, null);
                break;
        }
    }


    @Override
    public void startSplashActivity() {
        int currentPageIndex = mVfPageSwitch.getDisplayedChild();
        ProgressBar bar;
        switch (currentPageIndex) {
            case 0:
                bar = mPbLoginProgress;
                break;
            case 2:
                bar = mPbVerifyProgress;
                break;
            default:
                return;
        }
        AnimationUtil.circularRevealShowByFullActivityAnim(LoginActivity.this, bar, R.drawable.bg_splash, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animation.removeAllListeners();
                LoginActivity.this.startActivity(new Intent(LoginActivity.this, SplashActivity.class));
                LoginActivity.this.finish();
            }
        });
    }
}
