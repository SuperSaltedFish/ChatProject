package com.yzx.chat.widget.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.nio.charset.Charset;
import java.security.MessageDigest;

/**
 * Created by YZX on 2018年05月30日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class GlideSemicircleTransform extends BitmapTransformation {


    private float mChangeFactor;
    private int mSemicircleFillColor;

    public GlideSemicircleTransform(float changeFactorPx, @ColorInt int semicircleFillColor) {
        mChangeFactor = changeFactorPx;
        mSemicircleFillColor = semicircleFillColor;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool bitmapPool, @NonNull Bitmap bitmap, int outWidth, int outHeight) {

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(mSemicircleFillColor);
        Bitmap result;
        Canvas canvas;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width == outWidth && height == outHeight) {
            result = bitmap;
            canvas = new Canvas(result);
        } else {
            result = bitmapPool.get(outWidth, outHeight, bitmap.getConfig());
            canvas = new Canvas(result);
            float scaleX = width / (float) outWidth;
            float scaleY = height / (float) outHeight;
            float scale = Math.min(scaleX, scaleY);
            int scaledWidth = (int) (scale * outWidth);
            int scaledHeight = (int) (scale * outHeight);
            Rect srcRect = new Rect();
            srcRect.left = (width - scaledWidth) / 2;
            srcRect.top = (height - scaledHeight) / 2;
            srcRect.right = srcRect.left + scaledWidth;
            srcRect.bottom = srcRect.top + scaledHeight;
            canvas.drawBitmap(bitmap, srcRect, new Rect(0, 0, outWidth, outHeight), paint);
        }

        Path path = new Path();
        path.moveTo(0, outHeight);
        path.rLineTo(0, -mChangeFactor);
        path.quadTo(outWidth / 2f, outHeight, outWidth, outHeight - mChangeFactor);
        path.rLineTo(0, mChangeFactor);
        path.close();
        path.offset(0, mChangeFactor / 2 - 1);
        canvas.drawPath(path, paint);

        return result;
    }

    public boolean equals(Object o) {
        return o instanceof GlideRoundTransform;
    }

    public int hashCode() {
        return (GlideRoundTransform.class.getName() + mChangeFactor).hashCode();
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update((GlideSemicircleTransform.class.getName() + mChangeFactor).getBytes(Charset.forName("UTF-8")));
    }
}