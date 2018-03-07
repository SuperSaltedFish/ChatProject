package com.yzx.chat.widget.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.util.GlideUtil;

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
    }

    public void setImageData(List<String> urlList) {
        if (urlList == null && mImageUrlList == null) {
            return;
        }
        if (urlList != null && mImageUrlList != null && mImageUrlList.size() == urlList.size() && mImageUrlList.containsAll(urlList)) {
            return;
        }
        mImageUrlList = urlList;
        if (mImageUrlList == null || mImageUrlList.size() == 0) {
            removeAllViews();
            mRow = 0;
            mColumn = 0;
        } else {
            createChildVew();
        }
    }

    //别忘记了，这个地方删除一个imageview后要删除glideAPP clear,现在还没做
    private void createChildVew() {
        final int urlCount = mImageUrlList.size();
        final int childCount = getChildCount();
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
                addViewInLayout(imageView, -1, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            }
        } else if (urlCount < childCount) {
            for (int i = childCount; i > urlCount; i--) {
                ImageView view = (ImageView) getChildAt(i - 1);
                view.setOnClickListener(null);
                view.setImageBitmap(null);
                removeViewInLayout(view);
            }
        }
        for (int i = 0; i < urlCount; i++) {
            GlideUtil.loadFromUrl(mContext, (ImageView) getChildAt(i), R.drawable.temp_share_image);
        }
        requestLayout();
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
        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            if (mRow == 1 && mColumn == 1) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec( (MeasureSpec.getSize(widthMeasureSpec) * 9/16), MeasureSpec.EXACTLY));
            } else {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mRow * MeasureSpec.getSize(widthMeasureSpec) / 3, MeasureSpec.EXACTLY));
            }
        } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        } else {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            int minSize = Math.min(width, height);
            super.onMeasure(MeasureSpec.makeMeasureSpec(minSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(minSize, MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!changed) {
            return;
        }
        int count = getChildCount();
        int   childSize = (mViewWidth - 2 * mSpacing) / 3;
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
            if(count==1){
                getChildAt(i).layout(0, 0, mViewWidth, mViewHeight);
            }else {
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
