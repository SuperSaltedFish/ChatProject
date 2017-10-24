package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.RelativeLayout;

import com.yzx.chat.util.DensityUtil;

/**
 * Created by YZX on 2017年09月10日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class RoundRelativeLayout extends RelativeLayout {
    private float mRoundRadius;

    public RoundRelativeLayout(Context context) {
        this(context, null);
    }

    public RoundRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setRound();
    }

    private void setRound() {
        mRoundRadius = DensityUtil.dip2px(4);
        this.setClipToOutline(true);
        this.setOutlineProvider(new RoundOutlineProvider(mRoundRadius));
    }

    private static class RoundOutlineProvider extends ViewOutlineProvider {
        private float mRoundRadius;

        RoundOutlineProvider(float roundRadius) {
            mRoundRadius = roundRadius;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mRoundRadius);
        }
    }

    public float getRoundRadius() {
        return mRoundRadius;
    }

    public void setRoundRadius(float roundRadius) {
        mRoundRadius = roundRadius;
        this.setOutlineProvider(new RoundOutlineProvider(mRoundRadius));
    }
}
