package com.yzx.chat.view.fragment;

import android.annotation.SuppressLint;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.yzx.chat.R;
//import com.yzx.chat.adapter.ProfileNavigatorAdapter;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.widget.adapter.AlbumPagerAdapter;
import com.yzx.chat.widget.animation.ZoomPageTransformer;

//import net.lucode.hackware.magicindicator.MagicIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年09月01日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ProfileFragment extends BaseFragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    private FrameLayout mPagerParentLayout;
    private ViewPager mVpAlbum;
    private AlbumPagerAdapter mAlbumAdapter;
    private TextSwitcher mTsAlbumName;
    private List<Object> mObjects;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_profile;
    }

    @Override
    protected void init(View parentView) {
        mPagerParentLayout = parentView.findViewById(R.id.ProfileFragment_mFlPagerParentLayout);
        mVpAlbum = parentView.findViewById(R.id.ProfileFragment_mVpAlbum);
        mTsAlbumName = parentView.findViewById(R.id.ProfileFragment_mTsAlbumName);

        mObjects = new ArrayList<>();
        mObjects.add(R.drawable.temp_share_image);
        mObjects.add(R.drawable.temp_share_image);
        mObjects.add(R.drawable.temp_share_image);
        mObjects.add(R.drawable.temp_share_image);
        mObjects.add(R.drawable.temp_share_image);
        mObjects.add(R.drawable.temp_share_image);
        mAlbumAdapter = new AlbumPagerAdapter(mObjects);
    }

    @Override
    protected void setup() {
        mVpAlbum.setAdapter(mAlbumAdapter);
        //     mVpAlbum.setPageMargin((int) AndroidUtil.dip2px(32));
        mVpAlbum.setPageTransformer(true, new ZoomPageTransformer());
        mVpAlbum.setOffscreenPageLimit(2);
        mVpAlbum.addOnPageChangeListener(mOnAlbumPageChangeListener);

        mTsAlbumName.setFactory(mAlbumViewFactory);

        mPagerParentLayout.setOnTouchListener(mOnPagerParentLayoutTouchListener);
    }

    int i = 0;
    private final ViewPager.OnPageChangeListener mOnAlbumPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            String s = i + " " + i + " 但看完的空间那个" + i + " " + i + " " + i + " " + i + " " + i;
            i++;
            mTsAlbumName.setText(s);
        }
    };

    private final ViewSwitcher.ViewFactory mAlbumViewFactory = new ViewSwitcher.ViewFactory() {
        @Override
        public View makeView() {
            TextView textView = new TextView(mContext);
            textView.setTextSize(16);
            textView.setTextColor(ContextCompat.getColor(mContext, R.color.text_primary_color_black));
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setSingleLine();
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return textView;
        }
    };

    private final View.OnTouchListener mOnPagerParentLayoutTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mVpAlbum.onTouchEvent(event);
            return true;
        }
    };

}
