package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;


/**
 * Created by YZX on 2018年08月06日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class CropImageView extends AppCompatImageView
        implements View.OnTouchListener,
        ScaleGestureDetector.OnScaleGestureListener,
        View.OnLayoutChangeListener {

    private static final int MAX_CROP_SIZE = 200;

    private final float[] mMatrixValue = new float[9];
    private ScaleGestureDetector mScaleDetector;
    private Matrix mMatrix = new Matrix();
    private PointF mPrevPointF = new PointF();
    private RectF mSpaceRectF;
    private Paint mPaint;
    private PorterDuffXfermode mXfermode;
    private float mCropRadius;
    private float mMinScale;
    private float mMaxScale;

    private int mStrokeColor;
    private float mStrokeWidth;
    private int mMaskColor;
    private float mCropPadding;
    private boolean isPreviewMode;

    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setScaleType(ScaleType.MATRIX);
        super.setPadding(0, 0, 0, 0);

        setOnTouchListener(this);
        addOnLayoutChangeListener(this);

        mScaleDetector = new ScaleGestureDetector(context.getApplicationContext(), this);
        mSpaceRectF = new RectF();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        mStrokeColor = Color.WHITE;
        mMaskColor = Color.argb(96, 0, 0, 0);
        mStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
        mCropPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());
    }

    private void reset() {
        mMatrix.reset();
        Drawable drawable = getDrawable();
        if (drawable != null) {
            int width = getWidth();
            int height = getHeight();
            int dw = drawable.getIntrinsicWidth();
            int dh = drawable.getIntrinsicHeight();
            if (isPreviewMode) {
                mMinScale = Math.min((float) width / dw, (float) height / dh);
                mMaxScale = mMinScale > 1 ? Math.min(mMinScale * 4, 4) : 4;
            } else {
                mCropRadius = (float) Math.ceil(Math.min(width, height) / 2f - mStrokeWidth - mCropPadding);
                float size = mCropRadius * 2f;
                mMinScale = Math.max(size / dw, size / dh);
                mMaxScale = mMinScale > 1 ? Math.min(mMinScale * 4, 4) : 4;
            }
            mMatrix.postTranslate((width - dw) / 2f, (height - dh) / 2f);
            mMatrix.postScale(mMinScale, mMinScale, width / 2f, height / 2f);
        }
        setImageMatrix(mMatrix);
    }


    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        reset();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isPreviewMode) {
            return;
        }

        int width = getWidth();
        int height = getHeight();


        mSpaceRectF.left = mCropPadding;
        mSpaceRectF.right = mSpaceRectF.left + mCropRadius * 2;
        mSpaceRectF.top = height / 2f - mCropRadius;
        mSpaceRectF.bottom = mSpaceRectF.top + mCropRadius * 2;

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mMaskColor);
        canvas.drawRect(0, 0, width, mSpaceRectF.top, mPaint);
        canvas.drawRect(0, mSpaceRectF.bottom, width, height, mPaint);
        canvas.drawRect(0, mSpaceRectF.top, mSpaceRectF.left, mSpaceRectF.bottom, mPaint);
        canvas.drawRect(mSpaceRectF.right, mSpaceRectF.top, width, mSpaceRectF.bottom, mPaint);

        int layerId = canvas.saveLayer(mSpaceRectF, null);
        canvas.drawColor(mMaskColor);
        mPaint.setXfermode(mXfermode);
        canvas.drawCircle(mSpaceRectF.centerX(), mSpaceRectF.centerY(), mCropRadius, mPaint);
        mPaint.setXfermode(null);
        canvas.restoreToCount(layerId);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mStrokeColor);
        mPaint.setStrokeWidth(mStrokeWidth);
        canvas.drawCircle(mSpaceRectF.centerX(), mSpaceRectF.centerY(), mCropRadius, mPaint);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return true;
        }
        float scaleFactor = detector.getScaleFactor();
        if (scaleFactor == 1) {
            return false;
        }
        mMatrix.getValues(mMatrixValue);
        float scale = mMatrixValue[Matrix.MSCALE_X];
        if ((scaleFactor > 1 && scale >= mMaxScale) || (scaleFactor < 1 && scale <= mMinScale)) {
            return true;
        }
        if (scaleFactor * scale > mMaxScale) {
            scaleFactor = mMaxScale / scale;
        } else if (scaleFactor * scale < mMinScale) {
            scaleFactor = mMinScale / scale;
        }
        mMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
        checkTrans(drawable, mMatrix, getWidth() / 2f, getHeight() / 2f, mCropRadius);
        setImageMatrix(mMatrix);
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return false;
        }
        mScaleDetector.onTouchEvent(event);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mPrevPointF.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    float x = event.getX();
                    float y = event.getY();
                    float dx = event.getX() - mPrevPointF.x;
                    float dy = event.getY() - mPrevPointF.y;

                    mMatrix.postTranslate(dx, dy);
                    checkTrans(drawable, mMatrix, getWidth() / 2f, getHeight() / 2f, mCropRadius);
                    setImageMatrix(mMatrix);
                    mPrevPointF.set(x, y);
                }
                break;
        }
        return true;
    }

    private static void checkTrans(Drawable drawable, Matrix matrix, float cx, float cy, float radius) {
        RectF rect = new RectF();
        rect.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        matrix.mapRect(rect);
        float offsetX = 0;
        float offsetY = 0;
        if (rect.left > cx - radius) {
            offsetX = cx - radius - rect.left;
        }
        if (rect.right < cx + radius) {
            offsetX = cx + radius - rect.right;
        }
        if (rect.top > cy - radius) {
            offsetY = cy - radius - rect.top;
        }
        if (rect.bottom < cy + radius) {
            offsetY = cy + radius - rect.bottom;
        }
        matrix.postTranslate(offsetX, offsetY);
    }

    @Override
    public void setScaleType(ScaleType scaleType) {

    }

    @Override
    public ScaleType getScaleType() {
        return ScaleType.CENTER_INSIDE;
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {

    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {

    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        reset();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        reset();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        reset();
    }

    public void setEnablePreviewMode(boolean isEnable) {
        if (isPreviewMode == isEnable) {
            return;
        }
        isPreviewMode = isEnable;
        reset();
    }

    public void setStrokeWidth(float strokeWidth) {
        mStrokeWidth = strokeWidth;
        reset();
    }

    public void setCropPadding(float cropPadding) {
        mCropPadding = cropPadding;
        reset();
    }

    public void setStrokeColor(int strokeColor) {
        mStrokeColor = strokeColor;
        invalidate();

    }

    public void setMaskColor(int maskColor) {
        mMaskColor = maskColor;
        invalidate();
    }

    public Bitmap crop() {
        if (mCropRadius == 0 || isPreviewMode) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
        super.draw(new Canvas(bitmap));

        Bitmap crop = Bitmap.createBitmap(MAX_CROP_SIZE, MAX_CROP_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(crop);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(MAX_CROP_SIZE / 2f, MAX_CROP_SIZE / 2f, MAX_CROP_SIZE / 2f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        Rect src = new Rect((int) mSpaceRectF.left, (int) mSpaceRectF.top, (int) mSpaceRectF.right, (int) mSpaceRectF.bottom);
        canvas.drawBitmap(bitmap, src, new Rect(0, 0, MAX_CROP_SIZE, MAX_CROP_SIZE), paint);
        bitmap.recycle();
        return crop;
    }
}
