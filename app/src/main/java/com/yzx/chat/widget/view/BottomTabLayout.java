package com.yzx.chat.widget.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.Px;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;

import java.util.LinkedList;

/**
 * Created by YZX on 2018年06月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class BottomTabLayout extends LinearLayout {

    private final static int MAX_BADGE_NUMBER = 99;

    private Context mContext;
    private ViewPager mViewPager;
    private LinkedList<OnTabItemSelectedListener> mOnTabItemSelectedListeners;
    private int mCurrentSelectedPosition = -1;

    private boolean isClicked;

    public BottomTabLayout(Context context) {
        this(context, null);
    }

    public BottomTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mOnTabItemSelectedListeners = new LinkedList<>();
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setMotionEventSplittingEnabled(false);
    }


    public BottomTabLayout addTab(@DrawableRes int icon, String title, @ColorInt int selectedIconTintColor) {
        return addTab(icon, title, selectedIconTintColor, getThemeColor(mContext, android.R.attr.colorAccent, Color.BLACK), getThemeColor(mContext, android.R.attr.textColorTertiary, Color.BLACK));
    }

    public BottomTabLayout addTab(@DrawableRes int icon, String title, @ColorInt int selectedIconTintColor, int selectedTitleColor, int unselectedTitleColo) {
        Drawable selectedDrawable = ContextCompat.getDrawable(mContext, icon);
        Drawable unselectedDrawable = ContextCompat.getDrawable(mContext, icon);
        selectedDrawable = selectedDrawable.mutate();
        unselectedDrawable = unselectedDrawable.mutate();
        selectedDrawable.setTint(selectedIconTintColor);
        return addTab(selectedDrawable, unselectedDrawable, title, selectedTitleColor, unselectedTitleColo);
    }

    public BottomTabLayout addTab(@DrawableRes int selectedIcon, @DrawableRes int unselectedIcon, String title) {
        Drawable selectedDrawable = ContextCompat.getDrawable(mContext, selectedIcon);
        Drawable unselectedDrawable = ContextCompat.getDrawable(mContext, unselectedIcon);
        selectedDrawable = selectedDrawable.mutate();
        unselectedDrawable = unselectedDrawable.mutate();
        return addTab(selectedDrawable, unselectedDrawable, title);
    }

    public BottomTabLayout addTab(Drawable selectedIcon, Drawable unselectedIcon, String title) {
        return addTab(selectedIcon, unselectedIcon, title, getThemeColor(mContext, android.R.attr.colorAccent, Color.BLACK), getThemeColor(mContext, android.R.attr.textColorTertiary, Color.BLACK));
    }

    public BottomTabLayout addTab(Drawable selectedIcon, Drawable unselectedIcon, String title, int titleSelectedColor, int titleUnselectedColor) {
        TabView tabView = new TabView(mContext);
        tabView.setIcon(selectedIcon, unselectedIcon);
        tabView.setTitleText(title);
        tabView.setTitleColor(titleSelectedColor, titleUnselectedColor);
        tabView.setOnClickListener(mOnTabClickListener);
        tabView.setTag(getChildCount());
        addView(tabView, new LayoutParams(0, LayoutParams.MATCH_PARENT, 1));
        return this;
    }

    public BottomTabLayout setIconSize(@Px int size) {
        TabView tabView;
        for (int i = 0, count = getChildCount(); i < count; i++) {
            tabView = (TabView) getChildAt(i);
            tabView.setIconSize(size);
        }
        return this;
    }

    public BottomTabLayout setTitleTextSize(@Px int size) {
        TabView tabView;
        for (int i = 0, count = getChildCount(); i < count; i++) {
            tabView = (TabView) getChildAt(i);
            tabView.setTitleTextSize(size);
        }
        return this;
    }

    public BottomTabLayout setBadge(int position, int number) {
        if (position >= getChildCount()) {
            return this;
        }
        TabView tabView = (TabView) getChildAt(position);
        String badge;
        if (number < 0) {
            tabView.setBadge("");
        } else if (number == 0) {
            tabView.setBadge(null);
        } else {
            if (number > MAX_BADGE_NUMBER) {
                badge = String.valueOf(MAX_BADGE_NUMBER) + "+";
            } else {
                badge = String.valueOf(number);
            }
            if (number < 10) {
                tabView.setBadgeTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, mContext.getResources().getDisplayMetrics()));
            } else {
                tabView.setBadgeTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, mContext.getResources().getDisplayMetrics()));
            }
            tabView.setBadge(badge);
        }
        return this;
    }

    public BottomTabLayout setBadgeColor(@ColorInt int color) {
        TabView tabView;
        for (int i = 0, count = getChildCount(); i < count; i++) {
            tabView = (TabView) getChildAt(i);
            tabView.setBadgeTextColor(color);
        }
        return this;
    }

    public BottomTabLayout setBadgeBackgroundColor(@ColorInt int color) {
        TabView tabView;
        for (int i = 0, count = getChildCount(); i < count; i++) {
            tabView = (TabView) getChildAt(i);
            tabView.setBadgeBackgroundColor(color);
        }
        return this;
    }

    public BottomTabLayout setSelectPosition(int position, boolean isEnableAnimation, boolean isCallbackListener) {
        if (getChildCount() > position) {
            if (position != mCurrentSelectedPosition) {
                TabView tabView = (TabView) getChildAt(position);
                if (isEnableAnimation) {
                    tabView.startSelectionAnimator();
                } else {
                    tabView.setSelectionAnimatorProgress(1);
                }

                if (mCurrentSelectedPosition >= 0) {
                    tabView = (TabView) getChildAt(position);
                    if (tabView != null) {
                        if (isEnableAnimation) {
                            tabView.startUnselectedAnimator();
                        } else {
                            tabView.setSelectionAnimatorProgress(0);
                        }

                    }
                }
                mCurrentSelectedPosition = position;
                if (isCallbackListener) {
                    dispenseSelectedEvent();
                }
            } else if (isCallbackListener) {
                dispenseRepeatedEvent();
            }
        }
        return this;
    }

    public BottomTabLayout setupWithViewPager(ViewPager viewPager) {
        if (mViewPager != null) {
            mViewPager.removeOnPageChangeListener(mOnPageChangeListener);
            mViewPager = null;
        }
        if (viewPager != null) {
            mViewPager = viewPager;
            mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        }
        return this;
    }

    public BottomTabLayout addOnTabItemSelectedListener(OnTabItemSelectedListener onTabItemSelectedListener) {
        if (!mOnTabItemSelectedListeners.contains(onTabItemSelectedListener)) {
            mOnTabItemSelectedListeners.add(onTabItemSelectedListener);
        }
        return this;
    }

    public void rempveOnTabItemSelectedListener(OnTabItemSelectedListener onTabItemSelectedListener) {
        mOnTabItemSelectedListeners.remove(onTabItemSelectedListener);
    }

    private void dispenseSelectedEvent() {
        for (OnTabItemSelectedListener listener : mOnTabItemSelectedListeners) {
            listener.onSelected(mCurrentSelectedPosition);
        }
    }

    private void dispenseRepeatedEvent() {
        for (OnTabItemSelectedListener listener : mOnTabItemSelectedListeners) {
            listener.onRepeated(mCurrentSelectedPosition);
        }
    }

    private final View.OnClickListener mOnTabClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int childIndex = (int) v.getTag();
            if (mCurrentSelectedPosition == childIndex) {
                dispenseRepeatedEvent();
                return;
            }
            TabView tabView;
            for (int i = 0, count = getChildCount(); i < count; i++) {
                tabView = (TabView) getChildAt(i);
                if (childIndex != i) {
                    tabView.startUnselectedAnimator();
                } else {
                    tabView.startSelectionAnimator();
                }
            }
            if (mViewPager != null && mViewPager.getCurrentItem() != childIndex) {
                PagerAdapter adapter = mViewPager.getAdapter();
                if (adapter != null && adapter.getCount() > childIndex) {
                    isClicked = true;
                    mViewPager.setCurrentItem(childIndex);
                }
            }
            mCurrentSelectedPosition = childIndex;
            dispenseSelectedEvent();
        }
    };

    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (!isClicked && position < getChildCount()) {
                TabView tabView = (TabView) getChildAt(position);
                if (tabView != null) {
                    tabView.setSelectionAnimatorProgress(1 - positionOffset);
                }
            }
            if (!isClicked && position + 1 < getChildCount()) {
                TabView tabView = (TabView) getChildAt(position + 1);
                if (tabView != null) {
                    tabView.setSelectionAnimatorProgress(positionOffset);
                }
            }
            if (isClicked && mViewPager != null && position == position + positionOffset && position == mViewPager.getCurrentItem()) {
                isClicked = false;
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (mCurrentSelectedPosition != position) {
                mCurrentSelectedPosition = position;
                if (mCurrentSelectedPosition < getChildCount()) {
                    dispenseSelectedEvent();
                }
            }
        }
    };


    private static class TabView extends View {

        private static final long MAX_ANIMATOR_DURATION = 128;

        private ValueAnimator mAnimator;
        private float mCurrentSelectionProgress;
        private final float mMaxTranslationY;
        private final float mMaxTitleSizeOverflow;
        private Rect mDisplayRect;

        private Drawable mSelectedIcon;
        private Drawable mUnselectedIcon;
        private Rect mIconDisplayRect;
        private int mIconSize;

        private Rect mTitleBoundsRect;
        private String mTitleText;
        private float mTitleTextSize;
        private int mTitleSelectedColor;
        private int mTitleUnselectedColor;

        private RectF mBadgeDisplayRectF;
        private Rect mBadgeRect;
        private String mBadgeText;
        private float mBadgeSize;
        private int mBadgeTextColor;
        private int mBadgeBackgroundColor;

        private float mPaddingBetweenIconAndTitle;

        private Paint mTitlePaint;
        private Paint mBadgePaint;

        public TabView(Context context) {
            this(context, null);
        }

        public TabView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public TabView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            mMaxTranslationY = metrics.density * 2;
            mMaxTitleSizeOverflow = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 2, metrics);

            setBackground(new RippleDrawable(new ColorStateList(new int[][]{{}}, new int[]{0x56000000}), null, null));
            initAnimator();
            initDefaultValue(context);
        }

        private void initDefaultValue(Context context) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

            mIconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, displayMetrics);

            mPaddingBetweenIconAndTitle = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, displayMetrics);

            mBadgeTextColor = Color.WHITE;
            mBadgeBackgroundColor = ContextCompat.getColor(context, android.R.color.holo_red_light);
            mBadgeSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, displayMetrics);

            mTitleTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, displayMetrics);
            mTitleSelectedColor = getThemeColor(context, android.R.attr.colorAccent, Color.BLACK);
            mTitleUnselectedColor = getThemeColor(context, android.R.attr.textColorTertiary, Color.BLACK);

            mTitlePaint = new Paint();
            mTitlePaint.setAntiAlias(true);
            mTitlePaint.setTextAlign(Paint.Align.CENTER);
            mTitlePaint.setTextSize(mTitleTextSize);

            mBadgePaint = new Paint();
            mBadgePaint.setAntiAlias(true);
            mBadgePaint.setTextAlign(Paint.Align.CENTER);
            mBadgePaint.setTypeface(Typeface.DEFAULT_BOLD);
            mBadgePaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, displayMetrics));
        }

        private void initAnimator() {
            mAnimator = new ValueAnimator();
            mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setSelectionAnimatorProgress((float) animation.getAnimatedValue(), false);
                }
            });
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if (mDisplayRect == null) {
                mDisplayRect = new Rect(0, 0, w, h);
            } else {
                mDisplayRect.set(0, 0, w, h);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int width = getWidth();
            int height = getHeight();

            if (!TextUtils.isEmpty(mTitleText)) {
                if (mTitleBoundsRect == null) {
                    mTitleBoundsRect = new Rect();
                    mTitlePaint.setTextSize(mTitleTextSize);
                    mTitlePaint.getTextBounds(mTitleText, 0, mTitleText.length(), mTitleBoundsRect);
                }
                float drawY = mTitleBoundsRect.height();
                if (mSelectedIcon != null || mUnselectedIcon != null) {
                    drawY = (height + drawY + mIconSize + mPaddingBetweenIconAndTitle) / 2f;
                } else {
                    drawY = (height + drawY) / 2f;
                }
                mTitlePaint.setTextSize(mTitleTextSize + mMaxTitleSizeOverflow * mCurrentSelectionProgress);
                mTitlePaint.setColor(getTransitionColor(mCurrentSelectionProgress, mTitleUnselectedColor, mTitleSelectedColor));
                canvas.drawText(mTitleText, width / 2f, drawY, mTitlePaint);
            }

            if (mSelectedIcon != null || mUnselectedIcon != null) {
                if (mIconDisplayRect == null) {
                    int iconLeft = (width - mIconSize) / 2;
                    int iconTop;
                    if (mTitleBoundsRect != null) {
                        iconTop = (int) ((height - (mIconSize + mPaddingBetweenIconAndTitle + mTitleBoundsRect.height())) / 2);
                    } else {
                        iconTop = (height - mIconSize) / 2;
                    }
                    int iconRight = iconLeft + mIconSize;
                    int iconBottom = iconTop + mIconSize;
                    mIconDisplayRect = new Rect(iconLeft, iconTop, iconRight, iconBottom);
                }
                float translationY = -mCurrentSelectionProgress * mMaxTranslationY;
                if (mUnselectedIcon != null) {
                    mUnselectedIcon.setBounds(mIconDisplayRect.left, (int) (mIconDisplayRect.top + translationY), mIconDisplayRect.right, (int) (mIconDisplayRect.bottom + translationY));
                    mUnselectedIcon.draw(canvas);
                }
                if (mSelectedIcon != null && mCurrentSelectionProgress != 0) {
                    mSelectedIcon.setBounds(mIconDisplayRect.left, (int) (mIconDisplayRect.top + translationY), mIconDisplayRect.right, (int) (mIconDisplayRect.bottom + translationY));
                    mSelectedIcon.setAlpha((int) (255 * mCurrentSelectionProgress));
                    canvas.save();
                    mSelectedIcon.draw(canvas);
                    canvas.restore();
                }
            }

            if (mBadgeText != null) {
                if (mBadgeDisplayRectF == null) {
                    mBadgeDisplayRectF = new RectF();
                    if (mBadgeRect == null) {
                        mBadgeRect = new Rect();
                    }
                    mBadgePaint.getTextBounds(mBadgeText, 0, mBadgeText.length(), mBadgeRect);
                    float left;
                    float top;
                    if (mIconDisplayRect != null) {
                        left = mIconDisplayRect.right - mBadgeSize / 2 + mBadgeSize / 10;
                        if (left + mBadgeSize > width) {
                            left = left + mBadgeSize - width;
                        }
                        top = mIconDisplayRect.top - mBadgeSize / 2 + mBadgeSize / 5;
                        if (top < 0) {
                            top = 8;
                        }
                        mBadgeDisplayRectF.set(left, top, left + mBadgeSize, top + mBadgeSize);
                    } else if (mTitleBoundsRect != null) {
                        left = (width + mTitleBoundsRect.width() - mBadgeSize) / 2 + mBadgeSize / 5;
                        if (left + mBadgeSize > width) {
                            left = left + mBadgeSize - width;
                        }
                        top = (height - mTitleBoundsRect.height() - mBadgeSize) / 2 - mBadgeSize / 10;
                        if (top < 0) {
                            top = 4;
                        }
                    } else {
                        left = (width - mBadgeSize) / 2;
                        top = (height - mBadgeSize) / 2;
                    }
                    mBadgeDisplayRectF.set(left, top, left + mBadgeSize, top + mBadgeSize);
                }
                mBadgePaint.setColor(mBadgeBackgroundColor);
                if (!"".equals(mBadgeText)) {
                    canvas.drawRoundRect(mBadgeDisplayRectF, mBadgeDisplayRectF.width() / 2f, mBadgeDisplayRectF.height() / 2f, mBadgePaint);
                    mBadgePaint.setColor(mBadgeTextColor);
                    canvas.drawText(mBadgeText, mBadgeDisplayRectF.centerX(), (mBadgeDisplayRectF.top + mBadgeRect.height() + mBadgeDisplayRectF.bottom) / 2f, mBadgePaint);
                } else {
                    canvas.drawCircle(mBadgeDisplayRectF.centerX(), mBadgeDisplayRectF.centerY(), mBadgeSize / 4, mBadgePaint);
                }
            }
        }

        private void update() {
            mTitleBoundsRect = null;
            mIconDisplayRect = null;
            mBadgeDisplayRectF = null;
            invalidate();
        }

        public void setSelectionAnimatorProgress(@FloatRange(from = 0, to = 1f) float progress) {
            setSelectionAnimatorProgress(progress, true);
        }

        private void setSelectionAnimatorProgress(float progress, boolean isCancelAnimator) {
            if (isCancelAnimator) {
                cancelAnimator();
            }
            if (mCurrentSelectionProgress != progress) {
                if (Math.abs(mCurrentSelectionProgress - progress) > 0.05 || mCurrentSelectionProgress == 1 || mCurrentSelectionProgress == 0) {
                    mCurrentSelectionProgress = progress;
                    invalidate();
                }
            }
        }

        public void startSelectionAnimator() {
            if (mCurrentSelectionProgress == 1) {
                return;
            }
            cancelAnimator();
            mAnimator.setDuration((long) (MAX_ANIMATOR_DURATION * (1 - mCurrentSelectionProgress)));
            mAnimator.setFloatValues(mCurrentSelectionProgress, 1);
            mAnimator.start();
        }

        public void startUnselectedAnimator() {
            if (mCurrentSelectionProgress == 0) {
                return;
            }
            cancelAnimator();
            mAnimator.setDuration((long) (MAX_ANIMATOR_DURATION * (mCurrentSelectionProgress)));
            mAnimator.setFloatValues(mCurrentSelectionProgress, 0);
            mAnimator.start();
        }

        public void cancelAnimator() {
            if (mAnimator.isStarted()) {
                mAnimator.cancel();
            }
        }

        public void setBadge(String badge) {
            mBadgeText = badge;
            update();
        }

        public void setBadgeTextSize(float badgeTextSize) {
            mBadgePaint.setTextSize(badgeTextSize);
            update();
        }

        public void setBadgeTextColor(int badgeTextColor) {
            mBadgeTextColor = badgeTextColor;
            update();
        }

        public void setBadgeBackgroundColor(int badgeBackgroundColor) {
            mBadgeBackgroundColor = badgeBackgroundColor;
            update();
        }

        public void setTitleText(String titleText) {
            mTitleText = titleText;
            mTitleBoundsRect = null;
            mIconDisplayRect = null;
            update();
        }

        public void setTitleTextSize(float titleTextSize) {
            mTitleTextSize = titleTextSize;
            mTitleBoundsRect = null;
            update();
        }

        public void setTitleColor(int titleSelectedColor, int titleUnselectedColor) {
            mTitleSelectedColor = titleSelectedColor;
            mTitleUnselectedColor = titleUnselectedColor;
            update();
        }

        public void setIcon(Drawable selectedIcon, Drawable unselectedIcon) {
            mSelectedIcon = selectedIcon;
            mUnselectedIcon = unselectedIcon;
            update();
        }

        public void setIconSize(int iconSize) {
            mIconSize = iconSize;
            update();
        }


        private static int getTransitionColor(@FloatRange(from = 0, to = 1) float transition, int startColor, int endColor) {
            int redCurrent;
            int blueCurrent;
            int greenCurrent;
            int alphaCurrent;

            int redStart = Color.red(startColor);
            int blueStart = Color.blue(startColor);
            int greenStart = Color.green(startColor);
            int alphaStart = Color.alpha(startColor);

            int redEnd = Color.red(endColor);
            int blueEnd = Color.blue(endColor);
            int greenEnd = Color.green(endColor);
            int alphaEnd = Color.alpha(endColor);

            int redDifference = redEnd - redStart;
            int blueDifference = blueEnd - blueStart;
            int greenDifference = greenEnd - greenStart;
            int alphaDifference = alphaEnd - alphaStart;

            redCurrent = (int) (redStart + transition * redDifference);
            blueCurrent = (int) (blueStart + transition * blueDifference);
            greenCurrent = (int) (greenStart + transition * greenDifference);
            alphaCurrent = (int) (alphaStart + transition * alphaDifference);

            return Color.argb(alphaCurrent, redCurrent, greenCurrent, blueCurrent);
        }

    }

    private static int getThemeColor(Context context, @AttrRes int attr, int defaultColor) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attr, typedValue, true)) {
            return typedValue.data;
        } else {
            return defaultColor;
        }
    }

    public interface OnTabItemSelectedListener {
        void onSelected(int position);

        void onRepeated(int position);
    }

}
