package com.yzx.chat.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

/**
 * Created by yzx on 2017年05月14日
 * 当你将信心放在自己身上时，你将永远充满力量
 */
public class AnimationUtil {

    public static void rotateAnim(View view, float degree, int msec) {
        Animation anim = new RotateAnimation(0f, degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setFillAfter(true);
        anim.setDuration(msec);
        view.startAnimation(anim);
    }

    public static void scaleAnim(View view, float fromScaleX, float fromScaleY, float toScaleX, float toScaleY, int msec) {
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("scaleX", fromScaleX, toScaleX);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleY", fromScaleY, toScaleY);
        ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhY);
        scaleAnimator.setDuration(msec);
        scaleAnimator.setAutoCancel(true);
        scaleAnimator.start();
    }


    public static void circularRevealHideAnim(View view, Animator.AnimatorListener... animListener) {
        int width = view.getWidth();
        int height = view.getHeight();
        int radius = Math.max(width, height);
        Animator animator = ViewAnimationUtils.createCircularReveal(view, width / 2, height / 2, radius / 2, 0);
        if (animListener != null) {
            for (Animator.AnimatorListener listener : animListener)
                if (listener != null) {
                    animator.addListener(listener);
                }
        }
        animator.start();
    }

    public static void circularRevealShowAnim(View view, Animator.AnimatorListener... animListener) {
        int width = view.getWidth();
        int height = view.getHeight();
        int radius = Math.max(width, height);
        Animator animator = ViewAnimationUtils.createCircularReveal(view, width / 2, height / 2, 0, radius / 2);
        if (animListener != null) {
            for (Animator.AnimatorListener listener : animListener)
                if (listener != null) {
                    animator.addListener(listener);
                }
        }
        animator.start();
    }

    public static void circularRevealShowByFullActivityAnim(final Activity activity, View triggerView, int colorOrImageRes, final Animator.AnimatorListener animListener) {

        int[] location = new int[2];
        triggerView.getLocationInWindow(location);
        final int cx = location[0] + triggerView.getWidth() / 2;
        final int cy = location[1] + triggerView.getHeight() / 2;
        final ImageView view = new ImageView(activity);
        view.setTag(activity.getApplicationContext());
        view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        view.setImageResource(colorOrImageRes);
        final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        int w = decorView.getWidth();
        int h = decorView.getHeight();
        decorView.addView(view, w, h);

        int maxW = Math.max(cx, w - cx);
        int maxH = Math.max(cy, h - cy);
        final int finalRadius = (int) Math.sqrt(maxW * maxW + maxH * maxH) + 1;
        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        int maxRadius = (int) Math.sqrt(w * w + h * h) + 1;
        double rate = 1d * finalRadius / maxRadius;
        final long finalDuration = (long) (618 * Math.sqrt(rate));
        anim.setDuration((long) (finalDuration * 0.9));
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animListener.onAnimationEnd(animation);
                activity.overridePendingTransition(0, 0);
                //activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        anim.start();
    }

}
