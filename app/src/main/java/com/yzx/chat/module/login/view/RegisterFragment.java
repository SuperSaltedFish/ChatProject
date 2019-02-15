package com.yzx.chat.module.login.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.module.login.contract.RegisterContract;
import com.yzx.chat.module.login.presenter.RegisterPresenter;
import com.yzx.chat.util.AnimationUtil;
import com.yzx.chat.util.RegexUtil;
import com.yzx.chat.util.ViewUtil;
import com.yzx.chat.widget.listener.OnOnlySingleClickListener;

import java.util.Objects;

/**
 * Created by YZX on 2018年07月08日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class RegisterFragment extends BaseFragment<RegisterContract.Presenter> implements RegisterContract.View {

    private EditText mEtRegisterTelephone;
    private EditText mEtRegisterPassword;
    private EditText mEtRegisterConfirmPassword;
    private EditText mEtRegisterNickname;
    private ProgressBar mPbRegisterProgress;
    private Button mBtnRegister;
    private ImageView mIvBack;
    private TextView mTvJumpToLogin;
    private TextView mTvLoginHint;
    private TextView mTvErrorHint;
    private View mTelephoneUnderline;
    private View mNicknameUnderline;
    private View mPasswordUnderline;
    private View mConfirmPasswordUnderline;
    private boolean isDisableInput;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_register;
    }

    @Override
    protected void init(View parentView) {
        mEtRegisterTelephone = parentView.findViewById(R.id.mEtTelephone);
        mEtRegisterNickname = parentView.findViewById(R.id.mEtNickname);
        mEtRegisterPassword = parentView.findViewById(R.id.mEtPassword);
        mEtRegisterConfirmPassword = parentView.findViewById(R.id.mEtConfirmPassword);
        mPbRegisterProgress = parentView.findViewById(R.id.mPbRegisterProgress);
        mBtnRegister = parentView.findViewById(R.id.mBtnRegister);
        mTvJumpToLogin = parentView.findViewById(R.id.mTvJumpToLogin);
        mTelephoneUnderline = parentView.findViewById(R.id.mTelephoneUnderline);
        mNicknameUnderline = parentView.findViewById(R.id.mNicknameUnderline);
        mPasswordUnderline = parentView.findViewById(R.id.mPasswordUnderline);
        mConfirmPasswordUnderline = parentView.findViewById(R.id.mConfirmPasswordUnderline);
        mTvLoginHint = parentView.findViewById(R.id.mTvLoginHint);
        mTvErrorHint = parentView.findViewById(R.id.mTvErrorHint);
        mIvBack = parentView.findViewById(R.id.mIvBack);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        mEtRegisterTelephone.addTextChangedListener(mTextWatcher);
        mEtRegisterNickname.addTextChangedListener(mTextWatcher);
        mEtRegisterPassword.addTextChangedListener(mTextWatcher);
        mEtRegisterConfirmPassword.addTextChangedListener(mTextWatcher);
        mEtRegisterTelephone.setOnFocusChangeListener(mOnInputFocusChangeListener);
        mEtRegisterNickname.setOnFocusChangeListener(mOnInputFocusChangeListener);
        mEtRegisterPassword.setOnFocusChangeListener(mOnInputFocusChangeListener);
        mEtRegisterConfirmPassword.setOnFocusChangeListener(mOnInputFocusChangeListener);
        mBtnRegister.setOnClickListener(mOnViewClickListener);
        mTvJumpToLogin.setOnClickListener(mOnViewClickListener);
        mIvBack.setOnClickListener(mOnViewClickListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewUtil.registerAutoScrollAtInput(getView(), mBtnRegister);
    }

    @Override
    public void onPause() {
        super.onPause();
        ViewUtil.unregisterAutoScrollAtInput(getView());
    }

    private void startProgressAnim(final boolean isCloseAnim, Animator.AnimatorListener listener) {
        if (isCloseAnim) {
            if (mBtnRegister.getVisibility() == View.VISIBLE) {
                setDisableInputState(true);
                AnimationUtil.circularRevealHideAnim(mBtnRegister, listener, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mPbRegisterProgress.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animation.removeAllListeners();
                        mBtnRegister.setVisibility(View.INVISIBLE);
                    }
                });
            }
        } else {
            if (mBtnRegister.getVisibility() == View.INVISIBLE) {
                AnimationUtil.circularRevealShowAnim(mBtnRegister, listener, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mBtnRegister.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animation.removeAllListeners();
                        mPbRegisterProgress.setVisibility(View.INVISIBLE);
                        setDisableInputState(false);
                    }
                });
            }
        }
    }

    private void setDisableInputState(boolean isDisable) {
        mEtRegisterTelephone.setEnabled(!isDisable);
        mEtRegisterPassword.setEnabled(!isDisable);
        mEtRegisterConfirmPassword.setEnabled(!isDisable);
        mEtRegisterNickname.setEnabled(!isDisable);
        mBtnRegister.setEnabled(!isDisable);
        mTvJumpToLogin.setEnabled(!isDisable);
        mTvLoginHint.setEnabled(!isDisable);
        mIvBack.setEnabled(!isDisable);
        mEtRegisterTelephone.clearFocus();
        mEtRegisterNickname.clearFocus();
        mEtRegisterPassword.clearFocus();
        mEtRegisterConfirmPassword.clearFocus();
        isDisableInput = isDisable;
    }


    private final View.OnClickListener mOnViewClickListener = new OnOnlySingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            if (!isDisableInput) {
                switch (v.getId()) {
                    case R.id.mBtnRegister:
                        tryRegister();
                        break;
                    case R.id.mTvJumpToLogin:
                    case R.id.mIvBack:
                        backPressed();
                        break;
                }
            }
        }
    };

    private void tryRegister() {
        showErrorDialog(null);
        final String username = mEtRegisterTelephone.getText().toString();
        final String nickname = mEtRegisterNickname.getText().toString();
        final String password = mEtRegisterPassword.getText().toString();
        final String confirm = mEtRegisterConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
            showErrorDialog(getString(R.string.LoginActivity_Error_NoneInput));
            return;
        }
        if (!RegexUtil.isMobile(username)) {
            showErrorDialog(getString(R.string.LoginActivity_Error_PhoneNumber));
            return;
        }
        if (!RegexUtil.isLegalNickname(nickname)) {
            showErrorDialog(getString(R.string.LoginActivity_Error_IllegalNickname));
            return;
        }
        if (!RegexUtil.isLegalPassword(password)) {
            showErrorDialog(getString(R.string.LoginActivity_Error_IllegalPassword));
            return;
        }
        startProgressAnim(true, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPresenter.obtainRegisterVerifyCode(username);
            }
        });
    }

    private final View.OnFocusChangeListener mOnInputFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case R.id.mEtTelephone:
                    mTelephoneUnderline.setSelected(hasFocus);
                    break;
                case R.id.mEtNickname:
                    mNicknameUnderline.setSelected(hasFocus);
                    break;
                case R.id.mEtPassword:
                    mPasswordUnderline.setSelected(hasFocus);
                    break;
                case R.id.mEtConfirmPassword:
                    mConfirmPasswordUnderline.setSelected(hasFocus);
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
            showErrorDialog(null);
        }
    };

    @Override
    public RegisterContract.Presenter getPresenter() {
        return new RegisterPresenter();
    }

    @Override
    public void jumpToVerifyPage() {
        startProgressAnim(false, null);
        VerifyFragment.VerifyInfo info = new VerifyFragment.VerifyInfo();
        info.telephone = mEtRegisterTelephone.getText().toString();
        info.password = mEtRegisterPassword.getText().toString();
        info.nickname = mEtRegisterNickname.getText().toString();
        LoginActivity.jumpToVerifyPage(Objects.requireNonNull(getActivity()), info);
    }

    private void backPressed() {
        Objects.requireNonNull(getFragmentManager()).popBackStackImmediate();
    }

    @Override
    public void showErrorDialog(String error) {
        mTvErrorHint.setText(error);
        if (!TextUtils.isEmpty(error)) {
            AnimationUtil.errorTranslateAnim(mTvErrorHint);
        }
        startProgressAnim(false, null);
    }

}
