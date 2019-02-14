package com.yzx.chat.widget.view;


import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.nio.charset.Charset;
import java.security.MessageDigest;

import androidx.annotation.NonNull;

/**
 * Created by YZX on 2017年08月29日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class GlideRoundTransform extends BitmapTransformation {

    private float mRadius;

    public GlideRoundTransform(float px) {
        super();
        mRadius = px;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool bitmapPool, @NonNull Bitmap bitmap, int outWidth, int outHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = bitmapPool.get(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(bitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        RectF rect = new RectF(0, 0, width, height);
        canvas.drawRoundRect(rect, mRadius, mRadius, paint);
        return result;
    }

    public boolean equals(Object o) {
        return o instanceof GlideRoundTransform;
    }

    public int hashCode() {
        return (GlideRoundTransform.class.getName()+mRadius).hashCode();
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update((GlideRoundTransform.class.getName()+mRadius).getBytes(Charset.forName("UTF-8")));
    }
}