package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;


/**
 * Created by YZX on 2018年08月06日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class CropImageView extends ImageView
        implements View.OnTouchListener,
        ScaleGestureDetector.OnScaleGestureListener {

    private static final float MAX_SCALE = 4.0f;
    private static float MIN_SCALE = 1.0f;

    private final float[] mMatrixValue = new float[9];
    private ScaleGestureDetector mScaleDetector;
    private Matrix mMatrix = new Matrix();
    private PointF mPrevPointF = new PointF();

    private int mStrokeColor;
    private float mStrokeWidth;
    private int mMaskColor;
    private float mCropRadius;
    private Paint mPaint;
    private PorterDuffXfermode mPorterDuffXfermode;

    private boolean isUninitialized;

    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnTouchListener(this);
        setScaleType(ScaleType.MATRIX);
        mScaleDetector = new ScaleGestureDetector(context, this);
        mStrokeColor = Color.WHITE;
        mMaskColor = Color.argb(96, 0, 0, 0);
        mStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
        mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        super.setScaleType(ScaleType.MATRIX);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        isUninitialized = true;

    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        isUninitialized = true;
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        isUninitialized = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int layerId = canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mMaskColor);
        canvas.drawRect(0, 0, width, height, mPaint);

        float cx = width / 2f;
        float cy = height / 2f;
        float radius = Math.min(width, height) / 2f - mStrokeWidth;
        mPaint.setColor(Color.WHITE);
        mPaint.setXfermode(mPorterDuffXfermode);
        canvas.drawCircle(cx, cy, radius, mPaint);
        mPaint.setXfermode(null);
        canvas.restoreToCount(layerId);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mStrokeColor);
        mPaint.setStrokeWidth(mStrokeWidth);
        canvas.drawCircle(cx, cy, radius, mPaint);

        mCropRadius = radius + mStrokeWidth;

        if (isUninitialized) {
            isUninitialized = false;
            Drawable drawable = getDrawable();
            if (drawable == null) {
                return;
            }
            int dw = drawable.getIntrinsicWidth();
            int dh = drawable.getIntrinsicHeight();
            MIN_SCALE = Math.max(mCropRadius * 2f / dw, mCropRadius * 2f / dh);
            mMatrix.postTranslate((width - dw) / 2, (height - dh) / 2);
            mMatrix.postScale(MIN_SCALE, MIN_SCALE, getWidth() / 2, getHeight() / 2);
            setImageMatrix(mMatrix);
        }
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
        float scale = getMatrixScale();
        if ((scaleFactor > 1 && scale >= MAX_SCALE) || (scaleFactor < 1 && scale <= MIN_SCALE)) {
            return true;
        }
        if (scaleFactor * scale > MAX_SCALE) {
            scaleFactor = MAX_SCALE / scale;
        } else if (scaleFactor * scale < MIN_SCALE) {
            scaleFactor = MIN_SCALE / scale;
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

    private float getMatrixScale() {
        return getMatrixValue(Matrix.MSCALE_X);
    }

    private float getMatrixValue(int index) {
        mMatrix.getValues(mMatrixValue);
        return mMatrixValue[index];
    }

    private RectF getMatrixRectF() {
        Matrix matrix = mMatrix;
        RectF rect = new RectF();
        Drawable d = getDrawable();
        if (null != d) {
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rect);
        }
        return rect;
    }


}
