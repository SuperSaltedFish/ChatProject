package com.yzx.chat.widget.listener;

import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.ViewGroupUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by YZX on 2018年06月14日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public abstract class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {
    public static final int STATE_EXPANDED = 1;
    public static final int STATE_COLLAPSED = 2;

    @IntDef({STATE_EXPANDED, STATE_COLLAPSED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    private Method mGetMinimumHeightForVisibleOverlappingContentMethod;
    private boolean isFindMethodFinish;

    private int mCurrentState;

    @Override
    public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (!isFindMethodFinish && mGetMinimumHeightForVisibleOverlappingContentMethod == null) {
            try {
                mGetMinimumHeightForVisibleOverlappingContentMethod = appBarLayout.getClass().getDeclaredMethod("getMinimumHeightForVisibleOverlappingContent");
                mGetMinimumHeightForVisibleOverlappingContentMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } finally {
                isFindMethodFinish = true;
            }
        }
        int minimumHeight = 0;
        if (mGetMinimumHeightForVisibleOverlappingContentMethod != null) {
            try {
                minimumHeight = (int) mGetMinimumHeightForVisibleOverlappingContentMethod.invoke(appBarLayout);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        if (minimumHeight == 0) {
            minimumHeight = appBarLayout.getTotalScrollRange();
        }
        if (appBarLayout.getHeight()+i <= minimumHeight) {
            if (mCurrentState != STATE_COLLAPSED) {
                onStateChanged(appBarLayout, STATE_COLLAPSED);
            }
            mCurrentState = STATE_COLLAPSED;
        } else {
            if (mCurrentState != STATE_EXPANDED) {
                onStateChanged(appBarLayout, STATE_EXPANDED);
            }
            mCurrentState = STATE_EXPANDED;
        }

    }

    public abstract void onStateChanged(AppBarLayout appBarLayout, @State int state);

}
