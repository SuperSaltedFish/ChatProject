package com.yzx.chat.view.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.yzx.chat.R;
import com.yzx.chat.contract.ContactContract;
import com.yzx.chat.presenter.ContactPresenter;
import com.yzx.chat.tool.AndroidTool;
import com.yzx.chat.view.activity.FriendProfileActivity;
import com.yzx.chat.widget.adapter.ContactAdapter;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.bean.FriendBean;
import com.yzx.chat.widget.listener.AutoEnableOverScrollListener;
import com.yzx.chat.widget.listener.OnRecyclerViewClickListener;
import com.yzx.chat.test.FriendsTestData;
import com.yzx.chat.util.AnimationUtil;
import com.yzx.chat.widget.view.BadgeImageView;
import com.yzx.chat.widget.view.IndexBarView;
import com.yzx.chat.widget.view.LetterSegmentationItemDecoration;
import com.yzx.chat.widget.view.SegmentedControlView;

import java.util.List;

/**
 * Created by YZX on 2017年06月28日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ContactFragment extends BaseFragment<ContactContract.Presenter> implements ContactContract.View {

    public static final String TAG = ContactFragment.class.getSimpleName();

    private RecyclerView mContactRecyclerView;
    private ContactAdapter mAdapter;
    private IndexBarView mIndexBarView;
    private TextView mTvIndexBarHint;
    private Toolbar mToolbar;
    private SmartRefreshLayout mSmartRefreshLayout;
    private SegmentedControlView mSegmentedControlView;
    private BadgeImageView mContactRequestBadge;
    private FloatingActionButton mFBtnAdd;
    private LinearLayoutManager mLinearLayoutManager;
    private AutoEnableOverScrollListener mAutoEnableOverScrollListener;
    private LetterSegmentationItemDecoration mLetterSegmentationItemDecoration;
    private List<FriendBean> mFriendList = FriendsTestData.getTestData();


    @Override
    protected int getLayoutID() {
        return R.layout.fragment_contact;
    }

    @Override
    protected void init(View parentView) {
        mToolbar = (Toolbar) parentView.findViewById(R.id.ContactFragment_mToolbar);
        mContactRecyclerView = (RecyclerView) parentView.findViewById(R.id.ContactFragment_mContactRecyclerView);
        mIndexBarView = (IndexBarView) parentView.findViewById(R.id.ContactFragment_mIndexBarView);
        mTvIndexBarHint = (TextView) parentView.findViewById(R.id.ContactFragment_mTvIndexBarHint);
        mFBtnAdd = (FloatingActionButton) parentView.findViewById(R.id.ContactFragment_mFBtnAdd);
        mContactRequestBadge = (BadgeImageView) parentView.findViewById(R.id.ContactFragment_mContactRequestBadge);
        mSegmentedControlView = (SegmentedControlView) parentView.findViewById(R.id.ContactFragment_mSegmentedControlView);
        mSmartRefreshLayout = (SmartRefreshLayout) parentView.findViewById(R.id.ContactFragment_mSmartRefreshLayout);
        mAutoEnableOverScrollListener = new AutoEnableOverScrollListener(mSmartRefreshLayout);
        mAdapter = new ContactAdapter(mFriendList);
    }

    @Override
    protected void setView() {
        mToolbar.setTitle("微信");
        mToolbar.setTitleTextColor(Color.WHITE);

        mLetterSegmentationItemDecoration = new LetterSegmentationItemDecoration();
        mLetterSegmentationItemDecoration.setLineColor(ContextCompat.getColor(mContext, R.color.separation_line_color_black_alpha));
        mLetterSegmentationItemDecoration.setLineWidth(1);
        mLetterSegmentationItemDecoration.setTextColor(ContextCompat.getColor(mContext, R.color.separation_line_color_black_alpha));
        mLetterSegmentationItemDecoration.setTextSize(AndroidTool.sp2px(16));

        mLinearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mContactRecyclerView.setLayoutManager(mLinearLayoutManager);
        mContactRecyclerView.setAdapter(mAdapter);
        mContactRecyclerView.setHasFixedSize(true);
        mContactRecyclerView.addItemDecoration(mLetterSegmentationItemDecoration);
        mContactRecyclerView.addOnScrollListener(mAutoEnableOverScrollListener);
        mContactRecyclerView.addOnItemTouchListener(mOnRecyclerViewClickListener);

        mContactRequestBadge.setBadgeTextPadding((int) AndroidTool.dip2px(2));
        mContactRequestBadge.setBadgePadding(0, (int) AndroidTool.dip2px(6), (int) AndroidTool.dip2px(4), 0);

        mIndexBarView.setSelectedTextColor(ContextCompat.getColor(mContext, R.color.text_secondary_color_black_alpha));
        mIndexBarView.setOnTouchSelectedListener(mIndexBarSelectedListener);

        mSegmentedControlView
                .setColors(Color.WHITE, ContextCompat.getColor(mContext, R.color.theme_main_color))
                .setItems(new String[]{"好友", "群组"})
                .setDefaultSelectedPosition(0)
                .setStretch(true)
                .update();
    }

    @Override
    protected void onFirstVisible() {
        mAdapter.notifyDataSetChanged();
        mPresenter.loadAllContact();
    }


    private final OnRecyclerViewClickListener mOnRecyclerViewClickListener = new OnRecyclerViewClickListener() {

        @Override
        public void onItemClick(int position, View itemView) {
            if (position == 0) {

            } else {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mContext.startActivity(new Intent(mContext, FriendProfileActivity.class));
                    }
                });
            }
        }

        @Override
        public void onItemLongClick(int position, View itemView) {

        }

    };


    private final IndexBarView.OnTouchSelectedListener mIndexBarSelectedListener = new IndexBarView.OnTouchSelectedListener() {
        @Override
        public void onSelected(int position, String text) {
            final int scrollPosition = mAdapter.findPositionByLetter(text);
            if (scrollPosition >= 0) {
                mContactRecyclerView.scrollToPosition(scrollPosition);
                mContactRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        int firstPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
                        if (scrollPosition > firstPosition) {
                            View childView = mContactRecyclerView.getChildAt(scrollPosition - firstPosition);
                            int scrollY = childView.getTop() - mLetterSegmentationItemDecoration.getSpace();
                            mContactRecyclerView.scrollBy(0, scrollY);
                        }
                    }
                });
            }
            if (mFBtnAdd.getTag() == null) {
                AnimationUtil.scaleAnim(mFBtnAdd, mFBtnAdd.getScaleX(), mFBtnAdd.getScaleY(), 0, 0, 300);
                AnimationUtil.scaleAnim(mTvIndexBarHint, mTvIndexBarHint.getScaleX(), mTvIndexBarHint.getScaleY(), 1f, 1f, 300);
                mFBtnAdd.setTag(true);
            }
            mTvIndexBarHint.setText(text);
            mAutoEnableOverScrollListener.setEnableOverScroll(false);

        }

        @Override
        public void onCancelSelected() {
            mFBtnAdd.setTag(null);
            AnimationUtil.scaleAnim(mTvIndexBarHint, mTvIndexBarHint.getScaleX(), mTvIndexBarHint.getScaleY(), 0, 0, 250);
            AnimationUtil.scaleAnim(mFBtnAdd, mFBtnAdd.getScaleX(), mFBtnAdd.getScaleY(), 1f, 1f, 250);
            mAutoEnableOverScrollListener.setEnableOverScroll(true);
        }

        @Override
        public void onMove(int offsetPixelsY) {
            int startOffset = mTvIndexBarHint.getHeight() / 2;
            if (startOffset > offsetPixelsY) {
                mTvIndexBarHint.setTranslationY(0);
            } else if (offsetPixelsY > mIndexBarView.getHeight() - startOffset) {
                mTvIndexBarHint.setTranslationY(mIndexBarView.getHeight() - startOffset * 2);
            } else {
                mTvIndexBarHint.setTranslationY(offsetPixelsY - startOffset);
            }
        }
    };

    @Override
    public ContactContract.Presenter getPresenter() {
        return new ContactPresenter();
    }

    @Override
    public void updateUnreadBadge(int unreadCount) {
        if (unreadCount == 0) {
            mContactRequestBadge.setBadgeMode(BadgeImageView.MODE_HIDE);
        } else {
            mContactRequestBadge.setBadgeText(unreadCount);
            mContactRequestBadge.setBadgeMode(BadgeImageView.MODE_SHOW);
        }
    }
}
