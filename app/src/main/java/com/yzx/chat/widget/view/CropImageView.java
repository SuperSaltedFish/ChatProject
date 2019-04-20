package com.yzx.chat.widget.view;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;


/**
 * Created by YZX on 2018年08月06日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
@SuppressLint("AppCompatCustomView")
public class CropImageView extends ImageView
        implements View.OnTouchListener,
        ScaleGestureDetector.OnScaleGestureListener {

    private static final int MAX_CROP_SIZE = 200;

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
    private float mCropPadding;
    private Paint mPaint;
    private PorterDuffXfermode mPorterDuffXfermode;

    private boolean isUninitialized;
    private boolean isCropping;
    private boolean isPreviewMode;

    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnTouchListener(this);
        setPadding(0, 0, 0, 0);
        mScaleDetector = new ScaleGestureDetector(context.getApplicationContext(), this);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        super.setScaleType(ScaleType.MATRIX);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        initDefaultMode(context);
    }

    private void initDefaultMode(Context context) {
        mStrokeColor = Color.WHITE;
        mMaskColor = Color.argb(96, 0, 0, 0);
        mStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
        mCropPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());
        mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
    }

    private void initPreviewMode() {
        mStrokeColor = Color.TRANSPARENT;
        mMaskColor = Color.TRANSPARENT;
        mStrokeWidth = 0;
        mCropPadding = 0;
        mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
    }

    public void setEnablePreviewMode(boolean isEnable) {
        if (isPreviewMode == isEnable) {
            return;
        }
        isPreviewMode = isEnable;
        if (isEnable) {
            initPreviewMode();
        } else {
            initDefaultMode(getContext());
        }
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        super.setScaleType(ScaleType.MATRIX);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(0, 0, 0, 0);
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        super.setPaddingRelative(0, 0, 0, 0);
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

        if (isCropping) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        int layerId = canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mMaskColor);
        canvas.drawRect(0, 0, width, height, mPaint);

        float cx = width / 2f;
        float cy = height / 2f;
        float radius = Math.min(width - mCropPadding * 2, height - mCropPadding * 2) / 2f - mStrokeWidth;
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

    public void setStrokeColor(int strokeColor) {
        mStrokeColor = strokeColor;
        invalidate();
    }

    public void setStrokeWidth(float strokeWidth) {
        mStrokeWidth = strokeWidth;
        isUninitialized = true;
        invalidate();
    }

    public void setMaskColor(int maskColor) {
        mMaskColor = maskColor;
        invalidate();
    }

    public void setCropPadding(float cropPadding) {
        mCropPadding = cropPadding;
        isUninitialized = true;
        invalidate();
    }

    public Bitmap crop() {
        if (mCropRadius == 0) {
            return null;
        }
        int width = getWidth();
        int height = getHeight();
        int cx = width / 2;
        int cy = height / 2;
        int radius = (int) mCropRadius;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        isCropping = true;
        super.draw(new Canvas(bitmap));
        isCropping = false;
        int size = (int) (mCropRadius * 2);
        if (size > MAX_CROP_SIZE) {
            size = MAX_CROP_SIZE;
        }
        Bitmap crop = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(crop);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, new Rect(cx - radius, cy - radius, cx + radius, cy + radius), new Rect(0, 0, size, size), paint);
        bitmap.recycle();
        return crop;
    }
}
