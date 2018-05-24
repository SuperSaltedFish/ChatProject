package com.yzx.chat.widget.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Size;
import android.view.TextureView;

/**
 * Created by YZX on 2018年05月24日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class AutoFitTextureView extends TextureView {

    private static final Size DEFAULT_ASPECT_RATIO = new Size(16, 9);

    protected Size mAspectRatioSize = DEFAULT_ASPECT_RATIO;

    public AutoFitTextureView(Context context) {
        this(context, null);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAspectRatioSize(Size aspectRatioSize) {
        if (aspectRatioSize.equals(mAspectRatioSize)) {
            return;
        }
        mAspectRatioSize = aspectRatioSize;
        requestLayout();
    }

    public Size getAspectRatioSize() {
        return mAspectRatioSize;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int ratioW;
        int ratioH;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ratioW = mAspectRatioSize.getWidth();
            ratioH = mAspectRatioSize.getHeight();
        } else {
            ratioW = mAspectRatioSize.getHeight();
            ratioH = mAspectRatioSize.getWidth();
        }
        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            height = width * ratioH / ratioW;
        } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            width = height * ratioW / ratioH;
        }
        setMeasuredDimension(width, height);
    }
}
