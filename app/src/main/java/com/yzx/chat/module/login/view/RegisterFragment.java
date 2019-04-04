package com.yzx.chat.module.login.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Service;
import android.os.Bundle;
import android.os.Vibrator;
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
import com.yzx.chat.widget.listener.SimpleTextWatcher;
import com.yzx.chat.widget.view.ProgressButton;

import java.util.Objects;

import androidx.fragment.app.FragmentActivity;

/**
 * Created by YZX on 2018年07月08日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class RegisterFragment extends BaseFragment<RegisterContract.Presenter> implements RegisterContract.View {

    private EditText mEtRegisterTelephone;
    private EditText mEtRegisterPassword;
    private EditText mEtRegisterConfirmPassword;
    private EditText mEtRegisterNickname;
    private ProgressButton mPBtnRegister;
    private ImageView mIvBack;
    private TextView mTvJumpToLogin;
    private TextView mTvLoginHint;
    private TextView mTvErrorHint;
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
        mPBtnRegister = parentView.findViewById(R.id.mPBtnRegister);
        mTvJumpToLogin = parentView.findViewById(R.id.mTvJumpToLogin);
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
        mPBtnRegister.setOnClickListener(mOnViewClickListener);
        mTvJumpToLogin.setOnClickListener(mOnViewClickListener);
        mIvBack.setOnClickListener(mOnViewClickListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewUtil.registerAutoScrollAtInput(getView(), mPBtnRegister);
    }

    @Override
    public void onPause() {
        super.onPause();
        ViewUtil.unregisterAutoScrollAtInput(getView());
    }


    private final View.OnClickListener mOnViewClickListener = new OnOnlySingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            if (!isDisableInput) {
                switch (v.getId()) {
                    case R.id.mPBtnRegister:
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
        mPBtnRegister.startHideAnim(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPresenter.obtainRegisterVerifyCode(username);
            }
        });
    }

    private final TextWatcher mTextWatcher = new SimpleTextWatcher() {
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
        mPBtnRegister.startShowAnim(null);
        VerifyFragment.VerifyInfo info = new VerifyFragment.VerifyInfo();
        info.telephone = mEtRegisterTelephone.getText().toString();
        info.password = mEtRegisterPassword.getText().toString();
        info.nickname = mEtRegisterNickname.getText().toString();
        LoginActivity.jumpToVerifyPage((FragmentActivity) mContext, info);
    }

    private void backPressed() {
        Objects.requireNonNull(getFragmentManager()).popBackStackImmediate();
    }

    @Override
    public void showErrorDialog(String error) {
        mTvErrorHint.setText(error);
        if (!TextUtils.isEmpty(error)) {
            AnimationUtil.errorTranslateAnim(mTvErrorHint);
            ((Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE)).vibrate(50);
        }
        mPBtnRegister.startShowAnim(null);
    }

}
