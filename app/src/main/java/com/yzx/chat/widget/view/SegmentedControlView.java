package com.yzx.chat.widget.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class SegmentedControlView extends RadioGroup {

    private Context mContext;
    private OnSelectedChangedListener mListener;

    private int mSelectedColor;
    private int mUnselectedColor;
    private int mSelectedTextColor;
    private int mUnselectedTextColor;
    private int mDefaultSelectedPosition;
    private int mStrokeWidth;
    private int mItemPaddingLeft;
    private int mItemPaddingRight;
    private int mItemPaddingTop;
    private int mItemPaddingBottom;
    private float mRadius;
    private float mTextSize;
    private boolean stretch;
    private String[] mItemTexts;

    public SegmentedControlView(Context context) {
        this(context, null);
    }

    public SegmentedControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        this.setOrientation(RadioGroup.HORIZONTAL);
        this.setOnCheckedChangeListener(mOnChildCheckedChanged);
        mSelectedColor = Color.parseColor("#0099CC");
        mUnselectedColor = Color.TRANSPARENT;
        mSelectedTextColor = Color.WHITE;
        mUnselectedTextColor = Color.parseColor("#0099CC");
        mDefaultSelectedPosition = -1;
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, metrics);
        mItemPaddingLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics);
        mItemPaddingRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics);
        mItemPaddingTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics);
        mItemPaddingBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics);
        mStrokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, metrics);
        mRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics);
    }


    public void update() {
        if (getChildCount() != 0) {
            this.removeAllViews();
        }
        int i = 0;
        for (String itemText : mItemTexts) {
            RadioButton rb = new RadioButton(mContext);
            if (i == 0) {
                //Left
                GradientDrawable leftUnselected = new GradientDrawable();
                leftUnselected.setStroke(mStrokeWidth, mSelectedColor);
                leftUnselected.setColor(mUnselectedColor);
                leftUnselected.setCornerRadii(new float[]{mRadius, mRadius, 0, 0, 0, 0, mRadius, mRadius});

                GradientDrawable leftSelected = new GradientDrawable();
                leftSelected.setColor(mSelectedColor);
                leftSelected.setStroke(mStrokeWidth, mSelectedColor);
                leftSelected.setCornerRadii(new float[]{mRadius, mRadius, 0, 0, 0, 0, mRadius, mRadius});

                StateListDrawable leftStateListDrawable = new StateListDrawable();
                leftStateListDrawable.addState(new int[]{-android.R.attr.state_checked}, leftUnselected);
                leftStateListDrawable.addState(new int[]{android.R.attr.state_checked}, leftSelected);
                rb.setBackground(leftStateListDrawable);


            } else if (i == (mItemTexts.length - 1)) {
                //Right
                GradientDrawable rightUnselected = new GradientDrawable();
                rightUnselected.setStroke(mStrokeWidth, mSelectedColor);
                rightUnselected.setColor(mUnselectedColor);
                rightUnselected.setCornerRadii(new float[]{0, 0, mRadius, mRadius, mRadius, mRadius, 0, 0});

                GradientDrawable rightSelected = new GradientDrawable();
                rightSelected.setColor(mSelectedColor);
                rightSelected.setStroke(mStrokeWidth, mSelectedColor);
                rightSelected.setCornerRadii(new float[]{0, 0, mRadius, mRadius, mRadius, mRadius, 0, 0});

                StateListDrawable rightStateListDrawable = new StateListDrawable();
                rightStateListDrawable.addState(new int[]{-android.R.attr.state_checked}, rightUnselected);
                rightStateListDrawable.addState(new int[]{android.R.attr.state_checked}, rightSelected);
                rb.setBackground(rightStateListDrawable);

            } else {
                //Middle
                GradientDrawable middleUnselected = new GradientDrawable();
                middleUnselected.setStroke(mStrokeWidth, mSelectedColor);
                middleUnselected.setDither(true);
                middleUnselected.setColor(mUnselectedColor);

                GradientDrawable middleSelected = new GradientDrawable();
                middleSelected.setColor(mSelectedColor);
                middleSelected.setStroke(mStrokeWidth, mSelectedColor);

                StateListDrawable middleStateListDrawable = new StateListDrawable();
                middleStateListDrawable.addState(new int[]{-android.R.attr.state_checked}, middleUnselected);
                middleStateListDrawable.addState(new int[]{android.R.attr.state_checked}, middleSelected);
                rb.setBackground(middleStateListDrawable);
            }
            ColorStateList textColorStateList = new ColorStateList(
                    new int[][]{{-android.R.attr.state_checked}, {android.R.attr.state_checked}},
                    new int[]{mUnselectedTextColor, mSelectedTextColor});

            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            if (stretch) {
                params.weight = 1.0f;
            }
            if (i > 0) {
                params.setMargins(-mStrokeWidth, 0, 0, 0);
            }

            rb.setTextColor(textColorStateList);
            rb.setButtonDrawable(new StateListDrawable());
            rb.setGravity(Gravity.CENTER);
            rb.setTypeface(null, Typeface.BOLD);
            rb.setText(itemText);
            rb.setTextSize(TypedValue.COMPLEX_UNIT_PX,mTextSize);
            rb.setId(i);
            rb.setPadding(mItemPaddingLeft, mItemPaddingTop, mItemPaddingRight, mItemPaddingBottom);
            this.addViewInLayout(rb, -1, params);
            i++;
        }
        requestLayout();

        if (mDefaultSelectedPosition > -1) {
            this.check(mDefaultSelectedPosition);
        }
    }


    private final OnCheckedChangeListener mOnChildCheckedChanged = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (mListener != null) {
                mListener.onSelected(checkedId, mItemTexts[checkedId]);
            }

        }
    };

    public int getSelectedPosition() {
        return getCheckedRadioButtonId();
    }

    public SegmentedControlView setItems(String[] itemTexts) {
        this.mItemTexts = itemTexts;
        return this;
    }

    public SegmentedControlView setDefaultSelectedPosition(int defaultSelectedPosition) {
        this.mDefaultSelectedPosition = defaultSelectedPosition;
        return this;
    }


    public SegmentedControlView setColors(int primaryColor, int secondaryColor) {
        return setColors(primaryColor, secondaryColor, secondaryColor, primaryColor);
    }

    public SegmentedControlView setColors(int selectedColor, int selectedTextColor, int unselectedColor, int unselectedTextColor) {
        this.mSelectedColor = selectedColor;
        this.mSelectedTextColor = selectedTextColor;
        this.mUnselectedColor = unselectedColor;
        this.mUnselectedTextColor = unselectedTextColor;
        return this;
    }


    public SegmentedControlView setOnSelectionChangedListener(OnSelectedChangedListener listener) {
        this.mListener = listener;
        return this;
    }

    public SegmentedControlView setStretch(boolean stretch) {
        this.stretch = stretch;
        return this;
    }

    public SegmentedControlView setItemPadding(int padding) {
        return setItemPadding(padding, padding, padding, padding);
    }

    public SegmentedControlView setItemPadding(int left, int top, int right, int bottom) {
        this.mItemPaddingLeft = left;
        this.mItemPaddingTop = top;
        this.mItemPaddingRight = right;
        this.mItemPaddingBottom = bottom;
        return this;
    }

    public SegmentedControlView setStrokeWidth(int strokeWidth) {
        this.mStrokeWidth = strokeWidth;
        return this;
    }

    public SegmentedControlView setRadius(float radius) {
        this.mRadius = radius;
        return this;
    }

    public SegmentedControlView setTextSize(float textSize) {
        this.mTextSize = textSize;
        return this;
    }

    public interface OnSelectedChangedListener {
        void onSelected(int position, String text);
    }
}
