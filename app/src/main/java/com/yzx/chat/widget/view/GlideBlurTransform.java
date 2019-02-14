package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.nio.charset.Charset;
import java.security.MessageDigest;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

/**
 * Created by YZX on 2018年05月28日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class GlideBlurTransform extends BitmapTransformation {

    private final static int MAX_BITMAP_WIDTH = 64;
    private final static int MAX_BITMAP_HEIGHT = 64;
    private int mBlurRadius;
    private Context mContext;

    public GlideBlurTransform(Context context, @IntRange(from = 1, to = 25) int blurRadius) {
        mContext = context.getApplicationContext();
        mBlurRadius = blurRadius;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        int width = toTransform.getWidth();
        int height = toTransform.getHeight();
        Bitmap result;
        if (width > MAX_BITMAP_WIDTH || height > MAX_BITMAP_HEIGHT) {
            float scaleX = MAX_BITMAP_WIDTH * 1f / width;
            float scaleY = MAX_BITMAP_HEIGHT * 1f / height;
            float scale = Math.min(scaleX, scaleY);
            width = (int) (width * scale);
            height = (int) (height * scale);
            result = pool.get(width, height, toTransform.getConfig());
            Canvas canvas = new Canvas(result);
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            canvas.drawBitmap(toTransform, matrix, paint);
        } else {
            result = toTransform;
        }
        blurBitmap(result, mBlurRadius, mContext);
        return result;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update((GlideBlurTransform.class.getName() + mBlurRadius).getBytes(Charset.forName("UTF-8")));
    }

    private static void blurBitmap(Bitmap bitmap, float radius, Context context) {
        RenderScript rs = RenderScript.create(context);
        Allocation allocation = Allocation.createFromBitmap(rs, bitmap);
        Allocation blurredAllocation = Allocation.createTyped(rs, allocation.getType());
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));//ScriptIntrinsicBlur只能支持RGB_8888
        blurScript.setRadius(radius);
        blurScript.setInput(allocation);
        blurScript.forEach(blurredAllocation);
        blurredAllocation.copyTo(bitmap);
        allocation.destroy();
        blurredAllocation.destroy();
        blurScript.destroy();
        rs.destroy();
    }
}
