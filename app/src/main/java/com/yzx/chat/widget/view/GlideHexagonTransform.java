package com.yzx.chat.widget.view;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.nio.charset.Charset;
import java.security.MessageDigest;

/**
 * Created by YZX on 2018年07月02日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class GlideHexagonTransform extends BitmapTransformation {
    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        int srcWidth = toTransform.getWidth();
        int srcHeight = toTransform.getHeight();
        float sideLength = srcHeight / 2f;
        if (sideLength * Math.sqrt(3) > srcWidth) {
            sideLength = (float) (srcWidth / Math.sqrt(3));
        }

        int size = (int) (sideLength * 2);
        Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Path path = new Path();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);

        float centerX = result.getWidth() / 2f;
        float centerY = result.getHeight() / 2f;

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

        Rect centerCropRect = new Rect();
        centerCropRect.left = (srcWidth - size) / 2;
        centerCropRect.top = (srcHeight - size) / 2;
        centerCropRect.right = centerCropRect.left + size;
        centerCropRect.bottom = centerCropRect.top + size;

        canvas.drawPath(path, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(toTransform, centerCropRect, new Rect(0, 0, size, size), paint);

        return result;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(BitmapTransformation.class.getName().getBytes(Charset.forName("UTF-8")));
    }
}
