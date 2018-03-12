package com.yzx.chat.widget.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.util.GlideUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年08月26日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class NineGridImageView extends ViewGroup {

    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private int mSpacing;
    private List<String> mImageUrlList;
    private int mRow;
    private int mColumn;
    private int mViewWidth;
    private int mViewHeight;

    public NineGridImageView(Context context) {
        this(context, null);
    }

    public NineGridImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NineGridImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mSpacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                4, mContext.getResources().getDisplayMetrics());
        mImageUrlList = new ArrayList<>(9);
    }

    public void setImageData(List<String> urlList) {
        mRow = 0;
        mColumn = 0;
        mImageUrlList.clear();
        if (urlList != null) {
            mImageUrlList.addAll(urlList);
        }
        updateView();
    }


    private void updateView() {
        final int childCount = getChildCount();
        final int urlCount = mImageUrlList.size();
        if (urlCount == 0) {
            for (int i = 0; i < childCount; i++) {
                GlideUtil.clear(mContext, (ImageView) getChildAt(i));
            }
            removeAllViews();
            return;
        }
        mRow = (int) Math.ceil(urlCount / 3d);
        if (mRow > 9) {
            mRow = 9;
        }
        if (urlCount > 2) {
            mColumn = 3;
        } else {
            mColumn = urlCount;
        }
        if (urlCount > childCount) {
            for (int i = childCount; i < urlCount; i++) {
                final int position = i;
                RoundImageView imageView = new RoundImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnItemClickListener != null) {
                            mOnItemClickListener.onItemClick((ImageView) v, position, mImageUrlList.get(position));
                        }
                    }
                });
                addView(imageView, -1, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            }
        } else if (urlCount < childCount) {
            for (int i = childCount; i > urlCount; i--) {
                ImageView view = (ImageView) getChildAt(i - 1);
                GlideUtil.clear(mContext, view);
                view.setOnClickListener(null);
                view.setImageBitmap(null);
                removeViewAt(i - 1);
            }
        }
        for (int i = 0; i < urlCount; i++) {
            GlideUtil.loadFromUrl(mContext, (ImageView) getChildAt(i), R.drawable.temp_share_image);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mViewWidth = w;
        mViewHeight = h;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
        int width = maxWidth;
        int height = maxHeight;

        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            if (mRow == 1 && mColumn == 1) {
                height = width * 9 / 16;
            } else {
                height = width * mRow / 3;
            }
        } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            width = height;
        } else {
            int minSize = Math.min(width, height);
            width = minSize;
            height = minSize;
        }

        if (mRow == 1 && mColumn == 1) {
            measureChildren(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        } else {
            int childMeasureSpec = MeasureSpec.makeMeasureSpec(width / 3, MeasureSpec.EXACTLY);
            measureChildren(childMeasureSpec, childMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!changed) {
            return;
        }
        int count = getChildCount();
        int childSize = (mViewWidth - 2 * mSpacing) / 3;
        int row = 0;
        int column = 0;
        for (int i = 0; i < count; i++) {
            if (column >= mColumn) {
                column = 0;
                row++;
            }
            if (row >= mRow) {
                row = 0;
            }
            int childLeft = childSize * column;
            int childTop = childSize * row;
            if (column != 0) {
                childLeft += (mSpacing * column);
            }
            if (row != 0) {
                childTop += (mSpacing * row);
            }
            if (count == 1) {
                getChildAt(i).layout(0, 0, mViewWidth, mViewHeight);
            } else {
                getChildAt(i).layout(childLeft, childTop, childLeft + childSize, childTop + childSize);
            }
            column++;
        }
    }


    public int getSpacing() {
        return mSpacing;
    }

    public void setSpacing(int spacing) {
        mSpacing = spacing;
    }

    public interface OnItemClickListener {
        void onItemClick(ImageView view, int position, Object imageUri);
    }

    public void removeOnItemClickListener() {
        mOnItemClickListener = null;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }
}
