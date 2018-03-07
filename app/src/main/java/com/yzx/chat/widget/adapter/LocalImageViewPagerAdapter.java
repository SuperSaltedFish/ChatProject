package com.yzx.chat.widget.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;
import com.yzx.chat.util.GlideUtil;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by YZX on 2018年01月27日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class LocalImageViewPagerAdapter extends PagerAdapter {

    private Context mContext;
    private List<String> mPicUrlList;
    private LinkedList<WeakReference<ImageView>> mCacheViewQueue;

    public LocalImageViewPagerAdapter(Context context, List<String> picUrlList) {
        mContext = context;
        mPicUrlList = picUrlList;
        mCacheViewQueue = new LinkedList<>();
    }

    @Override
    public int getCount() {
        return mPicUrlList == null ? 0 : mPicUrlList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ImageView itemView = (ImageView) object;
        container.removeView(itemView);
        GlideUtil.clear(mContext, itemView);
        itemView.setImageBitmap(null);
        mCacheViewQueue.offer(new WeakReference<>(itemView));
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        WeakReference<ImageView> weakReference = mCacheViewQueue.poll();
        ImageView itemView;
        if (weakReference != null && weakReference.get() != null) {
            itemView = weakReference.get();
        } else {
            itemView = new PhotoView(mContext);
        }
        container.addView(itemView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        GlideUtil.loadFromUrl(mContext, itemView, String.format("file://%s", mPicUrlList.get(position)));
        return itemView;
    }
}
