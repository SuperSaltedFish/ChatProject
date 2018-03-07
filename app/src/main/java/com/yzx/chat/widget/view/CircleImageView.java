package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Outline;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

/**
 * Created by YZX on 2017年09月09日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class CircleImageView extends android.support.v7.widget.AppCompatImageView {
    public CircleImageView(Context context) {
        this(context, null);
    }

    public CircleImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setCircle();
    }

    private void setCircle() {
        this.setClipToOutline(true);
        this.setOutlineProvider(new CircleOutlineProvider());
    }

    private static class CircleOutlineProvider extends ViewOutlineProvider {
        @Override
        public void getOutline(View view, Outline outline) {
            int width = view.getWidth();
            int height = view.getHeight();
            int minSize = Math.min(width, height);
            int left = (width - minSize) / 2;
            int top = (height - minSize) / 2;
            outline.setRoundRect(left, top, left + minSize, top + minSize, minSize / 2f);
        }
    }

}
