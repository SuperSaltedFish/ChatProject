package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.yzx.chat.R;

import java.lang.reflect.Field;

/**
 * Created by YZX on 2018年02月10日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class LocationPicker extends NumberPicker {
    private Context mContext;

    public LocationPicker(Context context) {
        super(context, null);
        mContext = context;
        setup();
    }

    public LocationPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setup();
    }

    public LocationPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setup();
    }

    public void setup() {
        this.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        this.setWrapSelectorWheel(false);
        Field field;
        try {
            field = NumberPicker.class.getDeclaredField("mSelectionDivider");
            field.setAccessible(true);
            field.set(this, new ColorDrawable(ContextCompat.getColor(mContext, R.color.colorAccent)));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            field = NumberPicker.class.getDeclaredField("mSelectionDividerHeight");
            field.setAccessible(true);
            field.set(this, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    1, mContext.getResources().getDisplayMetrics()));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        setNumberPicker(child);
    }

    public void setNumberPicker(View view) {
        if (view instanceof TextView) {
            ((TextView) view).setTextSize(18);
        }
    }
}
