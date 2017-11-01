package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Outline;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;


/**
 * Created by YZX on 2017年09月10日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class RoundImageView extends ImageView {
    private float mRoundRadius;
    private Context mContext;

    public RoundImageView(Context context) {
        this(context, null);
    }

    public RoundImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setRound();
    }

    private void setRound() {
        mRoundRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                4, mContext.getResources().getDisplayMetrics());
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
