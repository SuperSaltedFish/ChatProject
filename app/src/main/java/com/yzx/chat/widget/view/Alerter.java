package com.yzx.chat.widget.view;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import com.yzx.chat.util.LogUtil;

import java.lang.ref.WeakReference;

/**
 * Created by YZX on 2017年12月16日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class Alerter {

    private static int sStatusBarHeight;

    private FrameLayout mRootLayout;
    private View mContentView;
    private WeakReference<Activity> mActivityWeakReference;
    private Animation mSlideInAnimation;
    private Animation mSlideOutAnimation;

    private boolean isShow;


    public Alerter(Activity activity, @LayoutRes int resID) {
        if (activity == null) {
            throw new NullPointerException("activity is null");
        }

        mActivityWeakReference = new WeakReference<>(activity);
        mRootLayout = new FrameLayout(activity);
        mRootLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRootLayout.setPadding(0, getStatusBarHeight(activity), 0, 0);
        mContentView = activity.getLayoutInflater().inflate(resID, mRootLayout, true);

        mSlideInAnimation = new TranslateAnimation(0, 0, 0, 0, Animation.RELATIVE_TO_SELF, -1, Animation.RELATIVE_TO_SELF, 0);
        mSlideOutAnimation = new TranslateAnimation(0, 0, 0, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1);
        mSlideInAnimation.setDuration(500);
        mSlideOutAnimation.setDuration(500);
        mSlideInAnimation.setInterpolator(new OvershootInterpolator(1));
        mSlideOutAnimation.setInterpolator(new AnticipateInterpolator(1));


        mRootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.e("ddwadawd");
            }
        });
    }

    public void show() {
        if (isShow) {
            return;
        }
        Activity activity = mActivityWeakReference.get();
        if (activity != null) {
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            decorView.addView(mRootLayout);
            mContentView.startAnimation(mSlideInAnimation);
            isShow = true;
        }
    }

    public void hide() {
        if (!isShow) {
            return;
        }
        if (mActivityWeakReference.get() == null) {
            return;
        }
        isShow = false;
        mSlideOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Activity activity = mActivityWeakReference.get();
                if (activity != null) {
                    ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                    decorView.removeView(mRootLayout);
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

    public void setCanceledOnTouchOutside(boolean isEnable) {

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
}
