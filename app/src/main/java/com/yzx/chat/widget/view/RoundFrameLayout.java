package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;

/**
 * Created by YZX on 2017年11月20日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class RoundFrameLayout extends FrameLayout {

    private float mRoundRadius;
    private Context mContext;

    public RoundFrameLayout(Context context) {
        this(context, null);
    }

    public RoundFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setRound();
    }

    private void setRound() {
        mRoundRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                20, mContext.getResources().getDisplayMetrics());
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
