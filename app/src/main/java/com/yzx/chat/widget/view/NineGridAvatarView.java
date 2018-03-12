package com.yzx.chat.widget.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.util.GlideUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by YZX on 2018年03月12日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class NineGridAvatarView extends ViewGroup {

    private Context mContext;
    private List<String> mAvatarUrlList;

    private int mViewWidth;
    private int mViewHeight;

    public NineGridAvatarView(Context context) {
        this(context, null);
    }

    public NineGridAvatarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NineGridAvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mAvatarUrlList = new ArrayList<>(9);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
        int width = maxWidth;
        int height = maxHeight;
        if (mAvatarUrlList.size() == 0) {
            width = 0;
            height = 0;
        } else {
            if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
                width = Math.min(maxWidth, maxHeight);
            } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
                height = Math.min(maxWidth, maxHeight);
            }
        }
        int childSize = Math.min(width, height);
        switch (mAvatarUrlList.size()) {
            case 1:
                break;
            case 2:
            case 3:
            case 4:
                childSize = childSize / 2;
                break;
            default:
                childSize = childSize / 3;
        }
        int childMeasureSpec = MeasureSpec.makeMeasureSpec(childSize, MeasureSpec.EXACTLY);
        measureChildren(childMeasureSpec, childMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        if (childCount == 0) {
            return;
        }

        View child = getChildAt(0);
        int left;
        int top;
        int right;
        int bottom;
        int childWidth = child.getMeasuredWidth();
        int childHeight = child.getMeasuredHeight();

        switch (childCount) {
            case 1:
                child = getChildAt(0);
                left = (mViewWidth - childWidth) / 2;
                top = (mViewHeight - childHeight) / 2;
                right = left + childWidth;
                bottom = top + childHeight;
                child.layout(left, top, right, bottom);
                break;
            case 2:
                child = getChildAt(0);
                left = 0;
                top = 0;
                right = left + childWidth;
                bottom = top + childHeight;
                child.layout(left, top, right, bottom);

                child = getChildAt(1);
                right = mViewWidth;
                bottom = mViewHeight;
                left =right-childWidth;
                top =bottom-childHeight;
                child.layout(left, top, right, bottom);
                break;
            case 3:
                child = getChildAt(0);
                left = (mViewWidth - childWidth) / 2;
                top = (mViewHeight-2*childHeight)/2;
                right = left + childWidth;
                bottom = top + childHeight;
                child.layout(left, top, right, bottom);

                child = getChildAt(1);
                left = (mViewWidth - childWidth*2) / 2;
                top =(mViewHeight-2*childHeight)/2+childHeight;
                right = left+childWidth;
                bottom = top+childHeight;
                child.layout(left, top, right, bottom);

                child = getChildAt(2);
                left =left+childWidth;
                right = left+childWidth;
                child.layout(left, top, right, bottom);
                break;
            case 4:
                child = getChildAt(0);
                left = (mViewWidth - 2*childWidth) / 2;
                top = (mViewHeight-2*childHeight)/2;
                right = left + childWidth;
                bottom = top + childHeight;
                child.layout(left, top, right, bottom);

                child = getChildAt(1);
                left =left+childWidth;
                right = left+childWidth;
                child.layout(left, top, right, bottom);

                child = getChildAt(2);
                left = (mViewWidth - childWidth*2) / 2;
                top =(mViewHeight-2*childHeight)/2+childHeight;
                right = left+childWidth;
                bottom = top+childHeight;
                child.layout(left, top, right, bottom);

                child = getChildAt(3);
                left =left+childWidth;
                right = left+childWidth;
                child.layout(left, top, right, bottom);
                break;
            default:
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mViewWidth = w;
        mViewHeight = h;
    }

    public void setImageData(List<String> urlList) {
        mAvatarUrlList.clear();
        if (urlList != null) {
            mAvatarUrlList.addAll(urlList);
        }
        updateView();
    }

    private void updateView() {
        final int childCount = getChildCount();
        final int urlCount = mAvatarUrlList.size();
        if (urlCount == 0) {
            for (int i = 0; i < childCount; i++) {
                GlideUtil.clear(mContext, (ImageView) getChildAt(i));
            }
            removeAllViews();
            return;
        }
        if (urlCount > childCount) {
            for (int i = childCount; i < urlCount; i++) {
                RoundImageView imageView = new RoundImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                addView(imageView, -1, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            }
        } else if (urlCount < childCount) {
            for (int i = childCount; i > urlCount; i--) {
                ImageView view = (ImageView) getChildAt(i - 1);
                GlideUtil.clear(mContext, view);
                view.setImageBitmap(null);
                removeViewAt(i - 1);
            }
        }
        for (int i = 0; i < urlCount; i++) {
            GlideUtil.loadFromUrl(mContext, (ImageView) getChildAt(i), R.drawable.temp_share_image);
        }
    }

}
