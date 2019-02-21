package com.yzx.chat.module.login.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Service;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.module.login.contract.VerifyContract;
import com.yzx.chat.module.login.presenter.VerifyPresenter;
import com.yzx.chat.util.AnimationUtil;
import com.yzx.chat.widget.listener.OnOnlySingleClickListener;
import com.yzx.chat.widget.view.VerifyEditView;

import java.util.Locale;
import java.util.Objects;

import androidx.fragment.app.FragmentActivity;


/**
 * Created by YZX on 2018年07月09日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VerifyFragment extends BaseFragment<VerifyContract.Presenter> implements VerifyContract.View {

    private static final String ARGUMENT = "VerifyInfo";

    public static VerifyFragment newInstance(VerifyInfo info) {

        Bundle args = new Bundle();
        args.putParcelable(ARGUMENT, info);
        VerifyFragment fragment = new VerifyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private TextView mTvErrorHint;
    private TextView mTvTelephone;
    private Button mBtnVerify;
    private ImageView mIvBack;
    private VerifyEditView mVerifyEditView;
    private ProgressBar mPbLoginProgress;
    private Button mBtnResend;
    private boolean isDisableInput;
    private boolean isAllowResendVerifyCode;

    private String mInputVerifyCode;
    private VerifyInfo mVerifyInfo;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_verify;
    }

    @Override
    protected void init(View parentView) {
        mBtnVerify = parentView.findViewById(R.id.mBtnVerify);
        mVerifyEditView = parentView.findViewById(R.id.mVerifyEditView);
        mPbLoginProgress = parentView.findViewById(R.id.mPbLoginProgress);
        mBtnResend = parentView.findViewById(R.id.mBtnResend);
        mTvErrorHint = parentView.findViewById(R.id.mTvErrorHint);
        mIvBack = parentView.findViewById(R.id.mIvBack);
        mTvTelephone = parentView.findViewById(R.id.mTvTelephone);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mVerifyInfo = args.getParcelable(ARGUMENT);
        }
        if (mVerifyInfo == null && getActivity() != null) {
            getActivity().finish();
            return;
        }

        mBtnVerify.setOnClickListener(mOnViewClickListener);
        mBtnResend.setOnClickListener(mOnViewClickListener);
        mIvBack.setOnClickListener(mOnViewClickListener);
        mVerifyEditView.setOnInputListener(mOnInputListener);

        mTvTelephone.setText(mVerifyInfo.telephone);

        startVerifyCountDown();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mVerifyCountDown.cancel();
    }

    private void backPressed() {
        Objects.requireNonNull(getFragmentManager()).popBackStackImmediate();
    }

    private void startProgressAnim(final boolean isCloseAnim, Animator.AnimatorListener listener) {
        if (isCloseAnim) {
            if (mBtnVerify.getVisibility() == View.VISIBLE) {
                setDisableInputState(true);
                AnimationUtil.circularRevealHideAnim(mBtnVerify, listener, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mPbLoginProgress.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animation.removeAllListeners();
                        mBtnVerify.setVisibility(View.INVISIBLE);
                    }
                });
            }
        } else {
            if (mBtnVerify.getVisibility() == View.INVISIBLE) {
                AnimationUtil.circularRevealShowAnim(mBtnVerify, listener, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mBtnVerify.setVisibility(View.VISIBLE);
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
        mBtnVerify.setEnabled(!isDisable);
        mVerifyEditView.setEnabled(!isDisable);
        mIvBack.setEnabled(!isDisable);
        mBtnResend.setEnabled((!isDisable) && isAllowResendVerifyCode);
        isDisableInput = isDisable;
        LoginActivity.setDisableBackPressed((LoginActivity) mContext, isDisable);
    }

    private void setAllowResendVerifyCode(boolean isAllow) {
        isAllowResendVerifyCode = isAllow;
        mBtnResend.setEnabled((!isDisableInput) && isAllowResendVerifyCode);
        mBtnResend.setText(R.string.LoginActivity_Layout_Resend);

    }

    private void startVerifyCountDown() {
        setAllowResendVerifyCode(false);
        mBtnResend.setText(R.string.LoginActivity_Error_Resending);
        mVerifyCountDown.cancel();
        mVerifyCountDown.start();
    }

    private void resendVerifyCode() {
        startVerifyCountDown();
        if (TextUtils.isEmpty(mVerifyInfo.nickname)) {
            mPresenter.obtainLoginSMS(mVerifyInfo.telephone, mVerifyInfo.password);
        } else {
            mPresenter.obtainRegisterSMS(mVerifyInfo.telephone);
        }
    }

    private void tryLoginOrRegister() {
        if (TextUtils.isEmpty(mInputVerifyCode)) {
            showErrorDialog(getString(R.string.LoginActivity_Error_NoneVerify));
            return;
        }
        startProgressAnim(true, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (TextUtils.isEmpty(mVerifyInfo.nickname)) {
                    mPresenter.login(mVerifyInfo.telephone, mVerifyInfo.password, mInputVerifyCode);
                } else {
                    mPresenter.register(mVerifyInfo.telephone, mVerifyInfo.password, mVerifyInfo.nickname, mInputVerifyCode);
                }
            }
        });

    }

    private final CountDownTimer mVerifyCountDown = new CountDownTimer(60000, 1000) {

        private String mResendName;

        @Override
        public void onTick(long millisUntilFinished) {
            if (mResendName == null) {
                mResendName = getString(R.string.LoginActivity_Layout_Resend);
            }
            int endTime = (int) (millisUntilFinished / 1000);
            mBtnResend.setText(String.format(Locale.getDefault(), mResendName + " %d%s", endTime, getString(R.string.Unit_Second)));
        }

        @Override
        public void onFinish() {
            setAllowResendVerifyCode(true);
        }
    };

    private final View.OnClickListener mOnViewClickListener = new OnOnlySingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            if (!isDisableInput) {
                switch (v.getId()) {
                    case R.id.mIvBack:
                        backPressed();
                        break;
                    case R.id.mBtnVerify:
                        tryLoginOrRegister();
                        break;
                    case R.id.mBtnResend:
                        resendVerifyCode();
                        break;
                }
            }
        }
    };


    private final VerifyEditView.OnInputListener mOnInputListener = new VerifyEditView.OnInputListener() {
        @Override
        public void onInputComplete(String content) {
            mInputVerifyCode = content;
            hideSoftKeyboard();
        }

        @Override
        public void onInputChange(String content) {
            mInputVerifyCode = null;
            showErrorDialog(null);
        }
    };


    @Override
    public VerifyContract.Presenter getPresenter() {
        return new VerifyPresenter();
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
    public void showCountDown() {
        startVerifyCountDown();
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
                animation.removeListener(this);
                LoginActivity.startHomeActivity((Activity) mContext);
            }
        });
    }

    @Override
    public void jumpToLoginPage() {
        LoginActivity.jumpToLoginPage((FragmentActivity) mContext);
    }

    public static final class VerifyInfo implements Parcelable {
        String telephone;
        String password;
        String nickname;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.telephone);
            dest.writeString(this.password);
            dest.writeString(this.nickname);
        }

        public VerifyInfo() {
        }

        protected VerifyInfo(Parcel in) {
            this.telephone = in.readString();
            this.password = in.readString();
            this.nickname = in.readString();
        }

        public static final Parcelable.Creator<VerifyInfo> CREATOR = new Parcelable.Creator<VerifyInfo>() {
            @Override
            public VerifyInfo createFromParcel(Parcel source) {
                return new VerifyInfo(source);
            }

            @Override
            public VerifyInfo[] newArray(int size) {
                return new VerifyInfo[size];
            }
        };
    }
}
