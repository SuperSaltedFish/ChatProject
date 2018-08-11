package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;

/**
 * Created by YZX on 2017年11月22日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class RoundLinearLayout extends LinearLayout {

    private float mRoundRadius;
    private Context mContext;

    public RoundLinearLayout(Context context) {
        this(context, null);
    }

    public RoundLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setRound();
    }

    private void setRound() {
        mRoundRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());
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
            int width = view.getWidth();
            int height = view.getHeight();
            if (mRoundRadius == 0 || width == 0 || height == 0) {
                return;
            }
            if (mRoundRadius < 0) {
                outline.setRoundRect(0, 0, width, height, Math.max(width, height) / 2f);
            } else {
                outline.setRoundRect(0, 0, width, height, mRoundRadius);
            }
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
