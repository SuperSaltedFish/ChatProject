package com.yzx.chat.widget.adapter;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.widget.view.MaskImageView;

import java.util.List;

/**
 * Created by YZX on 2018年07月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class CenterCropImagePagerAdapter extends PagerAdapter {

    private List<Object> mPicUrlList;
    private int mCurrentMaskType;

    public CenterCropImagePagerAdapter(List<Object> picUrlList) {
        mPicUrlList = picUrlList;
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
        ImageView itemView = (ImageView)object;
        container.removeView(itemView);
        itemView.setImageBitmap(null);
        GlideUtil.clear(container.getContext(), itemView);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        MaskImageView imageView = new MaskImageView(container.getContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setCurrentMode(mCurrentMaskType);
        container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        GlideUtil.loadFromUrl(container.getContext(), imageView, mPicUrlList.get(position));
        return imageView;
    }

    public void setMaskType(@MaskImageView.MaskType int currentMode){
        if(mCurrentMaskType!=currentMode){
            mCurrentMaskType = currentMode;
            notifyDataSetChanged();
        }
    }
}
