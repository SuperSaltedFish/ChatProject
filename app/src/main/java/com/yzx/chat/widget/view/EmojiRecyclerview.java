package com.yzx.chat.widget.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.widget.EmojiTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by YZX on 2017年11月29日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class EmojiRecyclerview extends RecyclerView {

    private Context mContext;
    private int[] mEmojiUnicodeArray;
    private GridLayoutManager mGridLayoutManager;
    private EmojiAdapter mAdapter;
    private  EmojiCompat mEmojiCompat;
    private int mEmojiSize;

    public EmojiRecyclerview(Context context) {
        this(context, null);
    }

    public EmojiRecyclerview(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        setDefaultValue();
    }

    private void setDefaultValue() {
        mEmojiSize = 16;
    }

    public EmojiRecyclerview(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mAdapter = new EmojiAdapter();
        mEmojiCompat = EmojiCompat.get();
        setAdapter(mAdapter);
    }

    public void setEmojiData(int[] emojiUnicodeArray, int spanCount) {
        mEmojiUnicodeArray = emojiUnicodeArray;
        if (mGridLayoutManager == null) {
            mGridLayoutManager = new GridLayoutManager(mContext, spanCount);
            setLayoutManager(mGridLayoutManager);
        } else {
            mGridLayoutManager.setSpanCount(spanCount);
        }
    }

    public void setEmojiSize(int size) {
        mEmojiSize = size;
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    private static class EmojiItemView extends TextView {

        public EmojiItemView(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY));
            } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
            }
        }
    }

    private class EmojiAdapter extends RecyclerView.Adapter<EmojiRecyclerview.EmojiHolder> {

        @Override
        public EmojiHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            EmojiItemView itemView = new EmojiItemView(mContext);
            itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            itemView.setGravity(Gravity.CENTER);
            return new EmojiHolder(itemView);
        }

        @Override
        public void onBindViewHolder(EmojiHolder holder, int position) {
            holder.mTextView.setText(mEmojiCompat.process(new String(Character.toChars(mEmojiUnicodeArray[position]))));
            holder.mTextView.setTextSize(mEmojiSize);
        }

        @Override
        public int getItemCount() {
            return mEmojiUnicodeArray == null ? 0 : mEmojiUnicodeArray.length;
        }

    }

    private static class EmojiHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        EmojiHolder(TextView itemView) {
            super(itemView);
            mTextView = itemView;
        }
    }

}
