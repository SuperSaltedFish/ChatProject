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

    private SparseArray<List<View>> mIdentitySparseArray;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mIdentitySparseArray = new SparseArray<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);

        int layoutWidth = 0;
        int layoutHeight = 0;
        int lineCount = 1;
        int currentLineWidth = 0;
        int currentLineHeight = 0;

        for (int i = 0, count = getChildCount(); i < count; i++) {
            View childView = getChildAt(i);
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
            MarginLayoutParams childLP = (MarginLayoutParams) childView.getLayoutParams();
            int childWidth = childView.getMeasuredWidth() + childLP.leftMargin + childLP.rightMargin;
            int childHeight = childView.getMeasuredHeight() + childLP.topMargin + childLP.bottomMargin;
            if (currentLineWidth + childWidth > maxWidth - getPaddingLeft() - getPaddingRight()) {
                layoutWidth = Math.max(currentLineWidth, layoutWidth);
                layoutHeight += currentLineHeight;
                lineCount++;
                currentLineWidth = childWidth;
                currentLineHeight = childHeight;
            } else {
                currentLineWidth += childWidth;
                currentLineHeight = Math.max(childHeight, currentLineHeight);
            }
            if (i == count - 1) {
                layoutWidth = Math.max(currentLineWidth, layoutWidth);
                layoutHeight += currentLineHeight;
            }
            List<View> singleLineViewList = mIdentitySparseArray.get(lineCount);
            if (singleLineViewList == null) {
                singleLineViewList = new LinkedList<>();
                mIdentitySparseArray.put(lineCount, singleLineViewList);
            }
            singleLineViewList.add(childView);
        }

        setMeasuredDimension(
                (widthMode == MeasureSpec.EXACTLY) ? maxWidth : layoutWidth,
                (heightMode == MeasureSpec.EXACTLY) ? maxHeight : layoutHeight
        );
        LogUtil.e(layoutWidth+" "+layoutHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int startX = getPaddingLeft();
        int startY = getPaddingTop();

        int i = 1;
        int usedHeight = 0;
        while (mIdentitySparseArray.get(i) != null) {
            List<View> singleViewList = mIdentitySparseArray.get(i);
            int usedWidth = 0;
            int lineHeight = 0;
            MarginLayoutParams childLP;
            for (View child : singleViewList) {
                childLP = (MarginLayoutParams) child.getLayoutParams();
                int left = startX + usedWidth;
                int top = startY + usedHeight;
                int right = left + child.getMeasuredWidth();
                int bottom = top + child.getMeasuredHeight();
                LogUtil.e(left+" "+top+" "+(right-left)+" "+(bottom-top));
                child.layout(left + childLP.leftMargin, top + childLP.topMargin, right, bottom);
                lineHeight = Math.max(lineHeight, child.getMeasuredHeight() + childLP.topMargin + childLP.bottomMargin);
                usedWidth += child.getMeasuredWidth() + childLP.leftMargin + childLP.rightMargin;
            }
            usedHeight += lineHeight;
            i++;
        }

    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }


}
