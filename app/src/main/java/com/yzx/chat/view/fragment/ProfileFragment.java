package com.yzx.chat.view.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.contract.ProfileModifyContract;
import com.yzx.chat.presenter.ProfileModifyPresenter;
import com.yzx.chat.view.activity.LoginActivity;
import com.yzx.chat.view.activity.ProfileModifyActivity;
import com.yzx.chat.widget.adapter.AlbumPagerAdapter;
import com.yzx.chat.widget.animation.ZoomPageTransformer;

import java.util.ArrayList;
import java.util.List;

//import com.yzx.chat.adapter.ProfileNavigatorAdapter;
//import net.lucode.hackware.magicindicator.MagicIndicator;

/**
 * Created by YZX on 2017年09月01日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ProfileFragment extends BaseFragment<ProfileModifyContract.Presenter> implements ProfileModifyContract.View {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    private FrameLayout mPagerParentLayout;
    private LinearLayout mLlEditProfile;
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
        mLlEditProfile = parentView.findViewById(R.id.ProfileFragment_mLlEditProfile);

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

        mTsAlbumName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        mLlEditProfile.setOnClickListener(mOnEditProfileClickListener);

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

    private final View.OnClickListener mOnEditProfileClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(mContext, ProfileModifyActivity.class));
//

            ///storage/emulated/0/DCIM/Camera/IMG_20180127_164621.jpg
//            UserApi userApi = (UserApi) ApiManager.getProxyInstance(UserApi.class);
//            Call<JsonResponse<Void>> call  = userApi.uploadAvatar("/storage/emulated/0/DCIM/Camera/IMG_20180127_164621.jpg");
//            call.setCallback(new HttpCallback<JsonResponse<Void>>() {
//                @Override
//                public void onResponse(HttpResponse<JsonResponse<Void>> response) {
//                    LogUtil.e("dwad");
//                }
//
//                @Override
//                public void onError(@NonNull Throwable e) {
//                    LogUtil.e("dwad");
//                }
//
//                @Override
//                public boolean isExecuteNextTask() {
//                    return false;
//                }
//            });
//            NetworkExecutor.getInstance().submit(call);
        }
    };

    @Override
    public ProfileModifyContract.Presenter getPresenter() {
        return new ProfileModifyPresenter();
    }

    @Override
    public void showError(String error) {

    }

    @Override
    public void goBack() {

    }
}
