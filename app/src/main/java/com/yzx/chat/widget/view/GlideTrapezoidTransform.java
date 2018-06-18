package com.yzx.chat.widget.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.nio.charset.Charset;
import java.security.MessageDigest;

/**
 * Created by YZX on 2018年06月13日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class GlideTrapezoidTransform extends BitmapTransformation {

    private float mUpperBottomPercent;
    private int mFillColor;

    public GlideTrapezoidTransform(float upperBottomPercent, @ColorInt int fillColor) {
        mUpperBottomPercent = upperBottomPercent;
        mFillColor = fillColor;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap result;
        Canvas canvas;
        int width = toTransform.getWidth();
        int height = toTransform.getHeight();
        if (width == outWidth && height == outHeight) {
            result = toTransform;
            canvas = new Canvas(result);
        } else {
            result = pool.get(outWidth, outHeight, toTransform.getConfig());
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
            canvas.drawBitmap(toTransform, srcRect, new Rect(0, 0, outWidth, outHeight), paint);
        }
        Shader shader = new RadialGradient(outWidth/2,outHeight/2,Math.min(outWidth,outHeight),Color.TRANSPARENT,Color.argb(168,0,0,0), Shader.TileMode.REPEAT);
        paint.setShader(shader);
        canvas.drawRect(0,0,outWidth,outHeight,paint);
        paint.setShader(null);
        paint.setColor(mFillColor);
        Path path = new Path();
        path.moveTo(0, outHeight);
        path.lineTo(0, mUpperBottomPercent * outHeight);
        path.lineTo(outWidth, outHeight);
        path.close();
        canvas.drawPath(path, paint);
        return result;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update((GlideSemicircleTransform.class.getName() + mUpperBottomPercent + mFillColor).getBytes(Charset.forName("UTF-8")));
    }
}
