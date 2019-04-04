package com.yzx.chat.widget.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.yzx.chat.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * Created by YZX on 2019年03月14日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class ProgressButton extends ConstraintLayout {

    private Button mButton;
    private ProgressBar mProgressBar;
    private OnClickListener mOnClickListener;
    private View[] mNeedAutoEnabledViews;

    private TransparentDialog mTransparentDialog;

    private Animator mShowAnimator;
    private Animator mHideAnimator;

    public ProgressButton(@NonNull Context context) {
        this(context, null);
    }

    public ProgressButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTransparentDialog = new TransparentDialog(context);
        initView(context, attrs);
    }

    private void initView(Context context, @Nullable AttributeSet attrs) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_progress_button, this, true);
        mButton = view.findViewById(R.id.mButton);
        mProgressBar = view.findViewById(R.id.mProgressBar);

        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton);
            setText(array.getString(R.styleable.ProgressButton_text));
            array.recycle();
        }

        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(ProgressButton.this);
                }
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mTransparentDialog.isShowing()) {
            mTransparentDialog.dismiss();
        }
        super.onDetachedFromWindow();
    }

    public void bindNeedAutoEnabledViews(View... views) {
        mNeedAutoEnabledViews = views;
    }

    public void startShowAnim(@Nullable Animator.AnimatorListener listener) {
        if (mButton.getVisibility() == View.VISIBLE || (mShowAnimator != null && mShowAnimator.isRunning())) {
            return;
        }
        if (mShowAnimator != null) {
            mShowAnimator.cancel();
            mShowAnimator.removeAllListeners();
        }
        mShowAnimator = circularRevealShowAnim(mButton);
        mShowAnimator.addListener(mShowListener);
        if (listener != null) {
            mShowAnimator.addListener(listener);
        }
        mShowAnimator.start();
    }

    public void startHideAnim(@Nullable Animator.AnimatorListener listener) {
        if (mButton.getVisibility() == View.INVISIBLE || (mHideAnimator != null && mHideAnimator.isRunning())) {
            return;
        }
        if (mHideAnimator != null) {
            mHideAnimator.cancel();
            mHideAnimator.removeAllListeners();
        }
        mHideAnimator = circularRevealHideAnim(mButton);
        mHideAnimator.addListener(mHideListener);
        if (listener != null) {
            mHideAnimator.addListener(listener);
        }
        mHideAnimator.start();
        clearCurrentFocus();
    }

    private void clearCurrentFocus() {
        Context context = getContext();
        if (context instanceof Activity) {
            Window window = ((Activity) context).getWindow();
            if (window != null) {
                View focus = window.getCurrentFocus();
                if (focus != null) {
                    focus.clearFocus();
                    ViewParent parent = focus.getParent();
                    if (parent instanceof View) {
                        ((View) parent).setFocusable(true);
                        ((View) parent).setFocusableInTouchMode(true);
                        ((View) parent).requestFocus();
                    }
                }
            }
        }
    }

    private final Animator.AnimatorListener mShowListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animation) {
            mButton.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            setViewEnable(true);
            mProgressBar.setVisibility(View.INVISIBLE);
            mTransparentDialog.dismiss();
        }
    };

    private final Animator.AnimatorListener mHideListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animation) {
            setViewEnable(false);
            mProgressBar.setVisibility(View.VISIBLE);
            if (!mTransparentDialog.isShowing()) {
                mTransparentDialog.show();
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mButton.setVisibility(View.INVISIBLE);
        }
    };

    private void setViewEnable(boolean isEnable) {
        if (mNeedAutoEnabledViews != null && mNeedAutoEnabledViews.length > 0) {
            for (View view : mNeedAutoEnabledViews) {
                view.setEnabled(isEnable);
                if (!isEnable) {
                    view.clearFocus();
                }
            }
        }
        mButton.setClickable(isEnable);
    }

    public void setText(CharSequence text) {
        mButton.setText(text);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mButton.setEnabled(enabled);
        mProgressBar.setEnabled(enabled);
    }

    @Override
    public void setClickable(boolean clickable) {
        super.setClickable(clickable);
        mButton.setClickable(clickable);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener listener) {
        mOnClickListener = listener;
    }

    public Button getButton() {
        return mButton;
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    private static Animator circularRevealHideAnim(View view) {
        int width = view.getWidth();
        int height = view.getHeight();
        int diameter = Math.max(width, height);
        return ViewAnimationUtils.createCircularReveal(view, width / 2, height / 2, diameter / 2f, 0);

    }

    private static Animator circularRevealShowAnim(View view) {
        int width = view.getWidth();
        int height = view.getHeight();
        int diameter = Math.max(width, height);
        return ViewAnimationUtils.createCircularReveal(view, width / 2, height / 2, 0, diameter / 2f);
    }

    private static final class TransparentDialog extends Dialog {//这个对话框的的作用：当执行动画或ProgressBar可见时弹出全透明的dialog，让返回按钮和UI上的其他东西不可用

        TransparentDialog(@NonNull Context context) {
            super(context);
            setCanceledOnTouchOutside(false);
            setCancelable(false);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            Window window = getWindow();
            if (window != null) {
                window.setBackgroundDrawable(null);
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }
        }

    }
}
