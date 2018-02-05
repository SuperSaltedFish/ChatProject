package com.yzx.chat.widget.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.widget.view.NineGridImageView;
import com.yzx.chat.widget.view.RoundImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by YZX on 2017年09月04日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class AlbumPagerAdapter extends PagerAdapter {

    private Context mContext;
    private List<Object> mCoverUrl;
    private LinkedList<WeakReference<RoundImageView>> mCacheViewQueue;

    public AlbumPagerAdapter(List<Object> coverUrl) {
        mCoverUrl = coverUrl;
        mCacheViewQueue = new LinkedList<>();
    }

    public AlbumPagerAdapter() {
    }

    @Override
    public int getCount() {
        return mCoverUrl == null ? 0 : mCoverUrl.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (mContext == null) {
            mContext = container.getContext();
        }
        RoundImageView imageView;
        WeakReference<RoundImageView> weakReference = mCacheViewQueue.poll();
        if (weakReference == null || weakReference.get() == null) {
            imageView = (RoundImageView) LayoutInflater.from(mContext).inflate(R.layout.item_album, container, false);
            imageView.setRoundRadius(AndroidUtil.dip2px(6));
        } else {
            imageView = weakReference.get();
        }
        GlideUtil.loadFromUrl(mContext, imageView, mCoverUrl.get(position));
        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        RoundImageView imageView = (RoundImageView) object;
        GlideUtil.clear(mContext, imageView);
        imageView.setImageDrawable(null);
        container.removeView(imageView);
        mCacheViewQueue.offer(new WeakReference<>(imageView));
    }
}
