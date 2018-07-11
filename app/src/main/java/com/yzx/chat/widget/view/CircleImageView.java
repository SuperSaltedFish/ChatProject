package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Outline;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

/**
 * Created by YZX on 2018年07月08日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class CircleImageView extends ImageView {
    public CircleImageView(Context context) {
        this(context, null);
    }

    public CircleImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setClipToOutline(true);
        this.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int width = getWidth();
                int height = getHeight();
                int min = Math.min(width, height);

                int left = (width - min) / 2;
                int top = (height - min) / 2;
                int right = left + min;
                int bottom = top + min;
                outline.setRoundRect(left, top, right, bottom, min / 2f);
            }
        });
    }
}
