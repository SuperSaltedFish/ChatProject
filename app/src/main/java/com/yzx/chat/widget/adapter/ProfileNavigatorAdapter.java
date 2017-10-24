package com.yzx.chat.widget.adapter;//package com.yzx.chat.adapter;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.support.v4.content.ContextCompat;
//import android.view.View;
//import android.view.animation.AccelerateInterpolator;
//import android.view.animation.DecelerateInterpolator;
//
//import com.yzx.chat.R;
//import com.yzx.chat.utils.DensityUtils;
//
//import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
//import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
//import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
//import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
//import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;
//
//import java.util.List;
//
///**
// * Created by YZX on 2017年09月01日.
// * 生命太短暂,不要去做一些根本没有人想要的东西
// */
//
//public class ProfileNavigatorAdapter extends CommonNavigatorAdapter {
//
//    private List<String> mTabTitleList;
//    private OnItemClickListener mOnItemClickListener;
//
//    public ProfileNavigatorAdapter(List<String> tabTitleList) {
//        mTabTitleList = tabTitleList;
//    }
//
//    @Override
//    public int getCount() {
//        return mTabTitleList == null ? 0 : mTabTitleList.size();
//    }
//
//    @Override
//    public float getTitleWeight(Context context, int index) {
//        return 1;
//    }
//
//    @Override
//    public IPagerTitleView getTitleView(Context context, int index) {
//        SimplePagerTitleView titleView = new SimplePagerTitleView(context);
//        titleView.setText(mTabTitleList.get(index));
//        titleView.setTextSize(DensityUtils.sp2px(6));
//        titleView.setNormalColor(Color.GRAY);
//        titleView.setSelectedColor(ContextCompat.getColor(context, R.color.theme_main_color));
//        titleView.setOnClickListener(mOnClickListener);
//        titleView.setTag(index);
//        return titleView;
//    }
//
//    @Override
//    public IPagerIndicator getIndicator(Context context) {
//        LinePagerIndicator indicator = new LinePagerIndicator(context);
//        indicator.setMode(LinePagerIndicator.MODE_EXACTLY);
//        indicator.setLineHeight(DensityUtils.dip2px( 6));
//        indicator.setLineWidth(DensityUtils.dip2px(24));
//        indicator.setRoundRadius(DensityUtils.dip2px(3));
//        indicator.setStartInterpolator(new AccelerateInterpolator());
//        indicator.setEndInterpolator(new DecelerateInterpolator(2.0f));
//        indicator.setColors(ContextCompat.getColor(context,R.color.theme_main_color));
//        return indicator;
//    }
//
//    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (mOnItemClickListener != null) {
//                mOnItemClickListener.onItemClick(v, (Integer) v.getTag());
//            }
//        }
//    };
//
//    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
//        mOnItemClickListener = onItemClickListener;
//    }
//
//    public interface OnItemClickListener {
//        void onItemClick(View v, int position);
//    }
//}
