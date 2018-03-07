package com.yzx.chat.widget.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.yzx.chat.util.LogUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by YZX on 2018年01月22日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class FlowLayout extends ViewGroup {

    private SparseArray<List<View>> mLineViewMap;
    private int mItemSpace;
    private int mLineSpace;


    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLineViewMap = new SparseArray<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);

        mLineViewMap.clear();

        int layoutWidth = 0;
        int layoutHeight = 0;
        int lineCount = 1;
        int currentLineWidth = 0;
        int currentLineHeight = 0;
        int currentLineChildCount = 0;

        for (int i = 0, count = getChildCount(); i < count; i++) {
            View childView = getChildAt(i);
            if(childView.getVisibility()!=View.GONE) {
                measureChild(childView, widthMeasureSpec, heightMeasureSpec);
                MarginLayoutParams childLP = (MarginLayoutParams) childView.getLayoutParams();
                int childWidth = childView.getMeasuredWidth() + childLP.leftMargin + childLP.rightMargin;
                int childHeight = childView.getMeasuredHeight() + childLP.topMargin + childLP.bottomMargin;
                if (currentLineWidth + childWidth + currentLineChildCount * mItemSpace > maxWidth - getPaddingLeft() - getPaddingRight()) {
                    layoutWidth = Math.max(currentLineWidth, layoutWidth);
                    layoutHeight = layoutHeight + currentLineHeight + mLineSpace;
                    lineCount++;
                    currentLineWidth = childWidth;
                    currentLineHeight = childHeight;
                    currentLineChildCount = 0;
                } else {
                    currentLineChildCount++;
                    currentLineWidth += childWidth;
                    currentLineHeight = Math.max(childHeight, currentLineHeight);
                }
                List<View> singleLineViewList = mLineViewMap.get(lineCount);
                if (singleLineViewList == null) {
                    singleLineViewList = new LinkedList<>();
                    mLineViewMap.put(lineCount, singleLineViewList);
                }
                singleLineViewList.add(childView);
            }
            if (i == count - 1) {
                layoutWidth = Math.max(currentLineWidth, layoutWidth);
                layoutHeight += currentLineHeight;
            }
        }

        setMeasuredDimension(
                (widthMode == MeasureSpec.EXACTLY) ? maxWidth : layoutWidth,
                (heightMode == MeasureSpec.EXACTLY) ? maxHeight : layoutHeight+getPaddingTop()+getPaddingBottom()
        );
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int startX = getPaddingLeft();
        int startY = getPaddingTop();
        int i = 1;
        int usedHeight = 0;
        while (mLineViewMap.get(i) != null) {
            List<View> singleViewList = mLineViewMap.get(i);
            int usedWidth = 0;
            int lineHeight = 0;
            MarginLayoutParams childLP;
            for (View child : singleViewList) {
                childLP = (MarginLayoutParams) child.getLayoutParams();
                int left = startX + usedWidth+childLP.leftMargin;
                int top = startY + usedHeight+childLP.topMargin;
                int right = left + child.getMeasuredWidth();
                int bottom = top + child.getMeasuredHeight();
                if (singleViewList.indexOf(child) == 0) {
                    child.layout(left , top , right, bottom);
                    usedWidth +=  (right-left)+childLP.rightMargin+childLP.leftMargin;
                } else {
                    child.layout(mItemSpace + left , top , mItemSpace + right, bottom);
                    usedWidth += (right-left+mItemSpace+childLP.rightMargin+childLP.leftMargin);
                }
                lineHeight = Math.max(lineHeight, bottom-top + childLP.topMargin + childLP.bottomMargin);
            }
            usedHeight += lineHeight + mLineSpace;
            i++;
        }

    }

    public int getItemSpace() {
        return mItemSpace;
    }

    public void setItemSpace(int itemSpace) {
        mItemSpace = itemSpace;
    }

    public int getLineSpace() {
        return mLineSpace;
    }

    public void setLineSpace(int lineSpace) {
        mLineSpace = lineSpace;
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(super.generateDefaultLayoutParams());
    }
}
