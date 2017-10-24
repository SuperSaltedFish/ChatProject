package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;


/**
 * Created by YZX on 2017年05月31日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class HeadPortraitImageView extends android.support.v7.widget.AppCompatImageView {

    private static final int COLOR_DRAWABLE_DIMENSION = 2;


    private Paint mStatePaint;
    private Paint mBitmapPaint;
    private Paint mStateBorderPaint;

    private int mViewWidth;
    private int mViewHeight;
    private float mViewRadius;
    private float mStateImageRadius;
    private float mStateImageBorderWidth;
    private float mOffsetX;
    private float mOffsetY;
    private float mStateImageCenterX;
    private float mStateImageCenterY;

    private Bitmap mBitmap;
    private BitmapShader mShader;
    private Matrix mMatrix;

    private boolean isStateEnabled;


    public HeadPortraitImageView(Context context) {
        this(context, null);
    }

    public HeadPortraitImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeadPortraitImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        mMatrix = new Matrix();

        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStateBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mStateBorderPaint.setColor(Color.WHITE);

        mStatePaint.setColor(Color.parseColor("#00cdff"));
        mStatePaint.setStyle(Paint.Style.FILL);


    }

    private void initSize() {
        int minSize = Math.min(mViewWidth, mViewHeight);
        if (mBitmap != null && minSize != 0) {
            if (isStateEnabled) {
                mStateImageRadius = minSize / 13;
                mStateImageBorderWidth = mStateImageRadius / 2;
                mViewRadius = minSize / 2f - mStateImageRadius - mStateImageBorderWidth;
                mOffsetX = mViewWidth / 2f - mViewRadius;
                mOffsetY = (mViewHeight - mViewRadius * 2 - mStateImageRadius - mStateImageBorderWidth) / 2f;
                mStateImageCenterX = mViewRadius;
                mStateImageCenterY = mViewRadius * 2 - mStateImageRadius / 3;
                mMatrix.setScale(mViewRadius * 2 / mBitmap.getWidth(), mViewRadius * 2 / mBitmap.getHeight());
            } else {
                mViewRadius = minSize / 2f;
                mMatrix.setScale(mViewRadius * 2 / mBitmap.getWidth(), mViewRadius * 2 / mBitmap.getHeight());
            }
            mShader.setLocalMatrix(mMatrix);
            mBitmapPaint.setShader(mShader);
        }
    }

    private void setBitmap(Bitmap bm) {
        if (bm == mBitmap) {
            return;
        }
        if (mBitmap != null) {
            mBitmap = bm;
            initSize();
        } else {
            mBitmap = bm;
        }
        mShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mViewWidth = w;
        mViewHeight = h;
        initSize();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            if (isStateEnabled) {
                canvas.translate(mOffsetX, mOffsetY);
                canvas.drawCircle(mViewRadius, mViewRadius, mViewRadius, mBitmapPaint);
                canvas.drawCircle(mStateImageCenterX, mStateImageCenterY, mStateImageRadius + mStateImageBorderWidth, mStateBorderPaint);
                canvas.drawCircle(mStateImageCenterX, mStateImageCenterY, mStateImageRadius, mStatePaint);
            } else {
                canvas.drawCircle(mViewRadius, mViewRadius, mViewRadius, mBitmapPaint);
            }
        }
    }


    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        setBitmap(bm);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        setBitmap(getBitmapFromDrawable(drawable));
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
        setBitmap(getBitmapFromDrawable(getDrawable()));
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        setBitmap(getBitmapFromDrawable(getDrawable()));
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLOR_DRAWABLE_DIMENSION, COLOR_DRAWABLE_DIMENSION, Bitmap.Config.ARGB_8888);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isStateEnabled() {
        return isStateEnabled;
    }

    public void setStateEnabled(boolean stateEnabled) {
        isStateEnabled = stateEnabled;
    }
}




