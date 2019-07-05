package com.yzx.chat.widget.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.util.GlideUtil;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

/**
 * Created by YZX on 2019年07月05日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class GuidePagerAdapter extends PagerAdapter {

    private final int[] mSplashImageList = {R.drawable.src_splash_1, R.drawable.src_splash_2, R.drawable.src_splash_3};
    private String[] mSplashTitleList;
    private String mSplashContent;

    public GuidePagerAdapter(Context context) {
        mSplashTitleList = context.getResources().getStringArray(R.array.SplashActivity_Titles);
        mSplashContent = context.getString(R.string.SplashActivity_Content);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View itemView = LayoutInflater.from(container.getContext()).inflate(R.layout.item_splash, container, false);
        TextView tvTitle = itemView.findViewById(R.id.mTvTitle);
        TextView tvContent = itemView.findViewById(R.id.mTvContent);
        ImageView ivSplashImage = itemView.findViewById(R.id.mIvSplashImage);
        tvTitle.setText(mSplashTitleList[position]);
        tvContent.setText(mSplashContent);
        ivSplashImage.setImageResource(mSplashImageList[position]);
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        View itemView = (View) object;
        container.removeView(itemView);
        ImageView ivSplashImage = itemView.findViewById(R.id.mIvSplashImage);
        ivSplashImage.setImageBitmap(null);
        GlideUtil.clear(container.getContext(), ivSplashImage);
    }

    @Override
    public int getCount() {
        return 3;
    }

}
