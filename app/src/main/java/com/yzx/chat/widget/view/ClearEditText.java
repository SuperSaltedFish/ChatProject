package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Px;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.util.LogUtil;


/**
 * Created by YZX on 2018年01月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ClearEditText extends android.support.v7.widget.AppCompatEditText {

    private Drawable mClearDrawable;
    private boolean isShowClearDrawable;
    private boolean isEditState;
    private boolean isAutoShow;

    public ClearEditText(Context context) {
        this(context, null);
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        isAutoShow = true;
        setFocusable(true);
        setFocusableInTouchMode(true);
        mClearDrawable = context.getDrawable(R.drawable.ic_close).mutate();
        setCloseDrawableSizeFromTextSize();
        setClearIconVisible(true);
        addTextChangedListener(mTextWatcher);

        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                isEditState = hasFocus;
                if (!TextUtils.isEmpty(getText()) && isEditState) {
                    setClearIconVisible(true);
                } else {
                    if(isAutoShow){
                        setClearIconVisible(false);
                    }
                }
            }
        });
    }

    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);
        setCloseDrawableSizeFromTextSize();
    }

    private void setCloseDrawableSizeFromTextSize(){
        int drawableSize = (int) (getTextSize()*0.8);
        mClearDrawable.setBounds(0, 0, drawableSize, drawableSize);
    }


    @Override
    public boolean performClick() {
        return super.performClick();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();
        }
        if (event.getAction() == MotionEvent.ACTION_UP && isShowClearDrawable) {
            int x = (int) event.getX();
            // 判断触摸点是否在水平范围内
            boolean isInnerWidth = (x > (getWidth() - getTotalPaddingRight()))
                    && (x < (getWidth() - getPaddingRight()));
            // 获取删除图标的边界，返回一个Rect对象
            Rect rect = mClearDrawable.getBounds();
            // 获取删除图标的高度
            int height = rect.height();
            int y = (int) event.getY();
            // 计算图标底部到控件底部的距离
            int distance = (getHeight() - height) / 2;
            // 判断触摸点是否在竖直范围内(可能会有点误差)
            // 触摸点的纵坐标在distance到（distance+图标自身的高度）之内，则视为点中删除图标
            boolean isInnerHeight = (y > distance) && (y < (distance + height));
            if (isInnerHeight && isInnerWidth) {
                this.setText(null);
            }
        }
        return super.onTouchEvent(event);
    }

    public void setAutoShow(boolean autoShow) {
        isAutoShow = autoShow;
    }

    public void setCloseDrawableTint(@ColorInt int color){
        mClearDrawable.setTint(color);
    }

    public void setCloseDrawableSize(int width,int height){
        mClearDrawable.setBounds(0,0,width,height);
    }


    public void setClearIconVisible(boolean visible) {
        if (isShowClearDrawable == visible) {
            return;
        }
        Drawable right = visible ? mClearDrawable : null;
        Drawable[] drawables = getCompoundDrawables();
        setCompoundDrawables(drawables[0], drawables[1], right, drawables[3]);
        isShowClearDrawable = visible;
    }


    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!TextUtils.isEmpty(s) && isEditState) {
                setClearIconVisible(true);
            } else {
                setClearIconVisible(false);
            }
        }
    };
}
