package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

/**
 * Created by YZX on 2018年07月13日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class HexagonAvatarView extends ImageView {
    private Path mHexagonPath;
    private Paint mPaint;

    public HexagonAvatarView(Context context) {
        this(context, null);
    }

    public HexagonAvatarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HexagonAvatarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mHexagonPath = new Path();

        this.setClipToOutline(true);
        this.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                mHexagonPath.reset();
                setupHexagonPath(mHexagonPath, getWidth(), getHeight());
                outline.setConvexPath(mHexagonPath);
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mHexagonPath.reset();
        setupHexagonPath(mHexagonPath, getWidth(), getHeight());
        canvas.drawPath(mHexagonPath, mPaint);
        super.onDraw(canvas);
    }

    private static void setupHexagonPath(Path path, int width, int height) {
        float sideLength = height / 2f;
        if (sideLength * Math.sqrt(3) > width) {
            sideLength = (float) (width / Math.sqrt(3));
        }
        float centerX = width / 2f;
        float centerY = height / 2f;

        float offsetX = (float) (sideLength * Math.sqrt(3) / 2);
        float offsetY = sideLength / 2;

        float x1 = centerX;
        float y1 = centerY - sideLength;
        float x2 = centerX + offsetX;
        float y2 = centerY - offsetY;
        float x3 = centerX + offsetX;
        float y3 = centerY + offsetY;
        float x4 = centerX;
        float y4 = centerY + sideLength;
        float x5 = centerX - offsetX;
        float y5 = centerY + offsetY;
        float x6 = centerX - offsetX;
        float y6 = centerY - offsetY;

        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.lineTo(x4, y4);
        path.lineTo(x5, y5);
        path.lineTo(x6, y6);
        path.close();

    }
}
