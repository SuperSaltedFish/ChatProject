package com.yzx.chat.widget.view;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;


/**
 * Created by YZX on 2017年12月16日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class Alerter {

    private static int sStatusBarHeight;

    private LinearLayout mRootLayout;
    private View mOutsideSpace;
    private View mContentView;
    private Activity mActivity;
    private Animation mSlideInAnimation;
    private Animation mSlideOutAnimation;
    private OnShowAndHideListener mOnShowAndHideListener;

    private boolean isShow;
    private boolean isCanceledOnTouchOutside;


    public Alerter(Activity activity, @LayoutRes int resID) {
        if (activity == null) {
            throw new NullPointerException("activity is null");
        }

        mActivity = activity;
        mRootLayout = new LinearLayout(activity);
        mRootLayout.setOrientation(LinearLayout.VERTICAL);
        mRootLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRootLayout.setPadding(0, getStatusBarHeight(activity), 0, 0);
        mContentView = activity.getLayoutInflater().inflate(resID, mRootLayout, true);
        mOutsideSpace = new View(activity);
        mRootLayout.addView(mOutsideSpace, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        mSlideInAnimation = new TranslateAnimation(0, 0, 0, 0, Animation.RELATIVE_TO_SELF, -1, Animation.RELATIVE_TO_SELF, 0);
        mSlideOutAnimation = new TranslateAnimation(0, 0, 0, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1);
        mSlideInAnimation.setDuration(500);
        mSlideOutAnimation.setDuration(500);
        mSlideInAnimation.setInterpolator(new OvershootInterpolator(1));
        mSlideOutAnimation.setInterpolator(new AnticipateInterpolator(1));

        mOutsideSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCanceledOnTouchOutside) {
                    hide();
                }
            }
        });

    }

    public void show() {
        if (isShow) {
            return;
        }
        ViewGroup decorView = (ViewGroup) mActivity.getWindow().getDecorView();
        decorView.addView(mRootLayout);
        mSlideInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mOnShowAndHideListener != null) {
                    mOnShowAndHideListener.onShow();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mContentView.startAnimation(mSlideInAnimation);
        isShow = true;
    }

    public void hide() {
        if (!isShow) {
            return;
        }
        isShow = false;
        mSlideOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ViewGroup decorView = (ViewGroup) mActivity.getWindow().getDecorView();
                decorView.removeView(mRootLayout);
                if (mOnShowAndHideListener != null) {
                    mOnShowAndHideListener.onHide();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mContentView.startAnimation(mSlideOutAnimation);
    }

    public boolean isShow() {
        return isShow;
    }

    public <T extends View> T findViewById(@IdRes int id) {
        return mContentView.findViewById(id);
    }

    public Alerter setCanceledOnTouchOutside(boolean isEnable) {
        isCanceledOnTouchOutside = isEnable;
        mOutsideSpace.setClickable(isEnable);
        return this;
    }

    public void setOnShowAndHideListener(OnShowAndHideListener listener) {
        mOnShowAndHideListener = listener;
    }

    private static int getStatusBarHeight(Context context) {
        if (sStatusBarHeight == 0) {
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                sStatusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
            }
        }
        return sStatusBarHeight;
    }

    public interface OnShowAndHideListener {
        void onShow();

        void onHide();
    }
}
