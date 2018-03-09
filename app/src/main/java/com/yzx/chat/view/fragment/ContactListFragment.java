package com.yzx.chat.view.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.contract.ContactContract;
import com.yzx.chat.presenter.ContactPresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.AnimationUtil;
import com.yzx.chat.view.activity.ContactOperationActivity;
import com.yzx.chat.view.activity.ContactProfileActivity;
import com.yzx.chat.view.activity.FindNewContactActivity;
import com.yzx.chat.view.activity.RemarkInfoActivity;
import com.yzx.chat.widget.adapter.ContactAdapter;
import com.yzx.chat.widget.adapter.ContactSearchAdapter;
import com.yzx.chat.widget.listener.AutoEnableOverScrollListener;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.BadgeView;
import com.yzx.chat.widget.view.IndexBarView;
import com.yzx.chat.widget.view.LetterSegmentationItemDecoration;
import com.yzx.chat.widget.view.OverflowMenuShowHelper;
import com.yzx.chat.widget.view.OverflowPopupMenu;
import com.yzx.chat.widget.view.SegmentedControlView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年06月28日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ContactListFragment extends BaseFragment<ContactContract.Presenter> implements ContactContract.View {

    public static final String TAG = ContactListFragment.class.getSimpleName();

    private RecyclerView mContactRecyclerView;
    private ContactAdapter mContactAdapter;
    private ContactSearchAdapter mSearchAdapter;
    private IndexBarView mIndexBarView;
    private View mHeaderView;
    private AutoCompleteTextView mSearchView;
    private TextView mTvIndexBarHint;
    private Toolbar mToolbar;
    private SmartRefreshLayout mSmartRefreshLayout;
    private SegmentedControlView mSegmentedControlView;
    private View mLlContactOperation;
    private View mLlGroup;
    private BadgeView mBadgeView;
    private FloatingActionButton mFBtnAdd;
    private LinearLayoutManager mLinearLayoutManager;
    private AutoEnableOverScrollListener mAutoEnableOverScrollListener;
    private LetterSegmentationItemDecoration mLetterSegmentationItemDecoration;
    private OverflowPopupMenu mContactMenu;
    private List<ContactBean> mContactList;


    @Override
    protected int getLayoutID() {
        return R.layout.fragment_contact_list;
    }

    @Override
    protected void init(View parentView) {
        mToolbar = parentView.findViewById(R.id.Default_mToolbar);
        mContactRecyclerView = parentView.findViewById(R.id.ContactFragmentList_mContactRecyclerView);
        mIndexBarView = parentView.findViewById(R.id.ContactFragmentList_mIndexBarView);
        mTvIndexBarHint = parentView.findViewById(R.id.ContactFragmentList_mTvIndexBarHint);
        mFBtnAdd = parentView.findViewById(R.id.ContactFragmentList_mFBtnAdd);
        mSegmentedControlView = parentView.findViewById(R.id.ContactFragmentList_mSegmentedControlView);
        mSmartRefreshLayout = parentView.findViewById(R.id.ContactFragmentList_mSmartRefreshLayout);
        mHeaderView = LayoutInflater.from(mContext).inflate(R.layout.item_contact_header, (ViewGroup) parentView, false);
        mSearchView = mHeaderView.findViewById(R.id.ContactFragmentList_mSearchView);
        mLlContactOperation = mHeaderView.findViewById(R.id.ContactFragmentList_mLlContactOperation);
        mLlGroup = mHeaderView.findViewById(R.id.ContactFragmentList_mLlGroup);
        mBadgeView = mHeaderView.findViewById(R.id.ContactFragmentList_mBadgeView);

        mContactMenu = new OverflowPopupMenu(mContext);
        mAutoEnableOverScrollListener = new AutoEnableOverScrollListener(mSmartRefreshLayout);
        mContactList = new ArrayList<>(256);
        mContactAdapter = new ContactAdapter(mContactList);
        mSearchAdapter = new ContactSearchAdapter(mContactList);
    }

    @Override
    protected void setup() {
        mToolbar.setTitle(R.string.app_name);
        mToolbar.setTitleTextColor(Color.WHITE);

        mLetterSegmentationItemDecoration = new LetterSegmentationItemDecoration();
        mLetterSegmentationItemDecoration.setLineColor(ContextCompat.getColor(mContext, R.color.divider_color_black));
        mLetterSegmentationItemDecoration.setLineWidth(1);
        mLetterSegmentationItemDecoration.setTextColor(ContextCompat.getColor(mContext, R.color.divider_color_black));
        mLetterSegmentationItemDecoration.setTextSize(AndroidUtil.sp2px(16));

        mLinearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mContactRecyclerView.setLayoutManager(mLinearLayoutManager);
        mContactRecyclerView.setAdapter(mContactAdapter);
        mContactRecyclerView.setHasFixedSize(true);
        mContactRecyclerView.addItemDecoration(mLetterSegmentationItemDecoration);
        mContactRecyclerView.addOnScrollListener(mAutoEnableOverScrollListener);
        mContactRecyclerView.addOnItemTouchListener(mOnRecyclerViewItemClickListener);

        mLlContactOperation.setOnClickListener(mOnContactOperationClick);
        mLlGroup.setOnClickListener(mOnGroupClick);

        mIndexBarView.setSelectedTextColor(ContextCompat.getColor(mContext, R.color.text_secondary_color_black));
        mIndexBarView.setOnTouchSelectedListener(mIndexBarSelectedListener);

        mFBtnAdd.setOnClickListener(mOnAddNewContactClick);

        mSegmentedControlView
                .setColors(Color.WHITE, ContextCompat.getColor(mContext, R.color.colorPrimary))
                .setItems(new String[]{"好友", "群组"})
                .setDefaultSelectedPosition(0)
                .setStretch(true)
                .update();

        setHarderView();
        setOverflowMenu();
    }

    private void setHarderView() {
        mSearchView.setAdapter(mSearchAdapter);
        mSearchView.setDropDownVerticalOffset((int) AndroidUtil.dip2px(8));
        mSearchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mOnRecyclerViewItemClickListener.onItemClick(position + 1, null);
                mSearchView.setText(null);
            }
        });

        mContactAdapter.setHeaderView(mHeaderView);
    }

    private void setOverflowMenu() {
        mContactMenu.setWidth((int) AndroidUtil.dip2px(128));
        mContactMenu.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(mContext, R.color.theme_background_color_white)));
        mContactMenu.setElevation(AndroidUtil.dip2px(2));
        mContactMenu.inflate(R.menu.menu_contact_overflow);
        mContactMenu.setOnMenuItemClickListener(new OverflowPopupMenu.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, int menuID) {
                int index = (int) mContactRecyclerView.getTag();
                if (index < mContactList.size()) {
                    switch (menuID) {
                        case R.id.ContactMenu_UpdateRemarkInfo:
                            Intent intent = new Intent(mContext, RemarkInfoActivity.class);
                            intent.putExtra(RemarkInfoActivity.INTENT_EXTRA_CONTACT, mContactList.get(index));
                            startActivityForResult(intent, 0);
                            break;
                    }
                }
            }
        });
    }

    @Override
    protected void onFirstVisible() {
        mPresenter.loadUnreadCount();
        mPresenter.loadAllContact();
    }

    private final View.OnClickListener mOnContactOperationClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(mContext, ContactOperationActivity.class));
        }
    };

    private final View.OnClickListener mOnGroupClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(mContext, GroupListActivity.class));
        }
    };

    private final View.OnClickListener mOnAddNewContactClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(mContext, FindNewContactActivity.class));
        }
    };

    private final OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener = new OnRecyclerViewItemClickListener() {

        @Override
        public void onItemClick(final int position, RecyclerView.ViewHolder viewHolder) {
            if (position == 0 && mContactAdapter.isHasHeaderView()) {
                return;
            }
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(mContext, ContactProfileActivity.class);
                    intent.putExtra(ContactProfileActivity.INTENT_EXTRA_CONTACT_ID, mContactList.get(position - 1).getUserProfile().getUserID());
                    startActivity(intent);
                }
            });

        }

        @Override
        public void onItemLongClick(int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY) {
            if (position == 0 && mContactAdapter.isHasHeaderView()) {
                return;
            }
            mContactRecyclerView.setTag(position - 1);
            OverflowMenuShowHelper.show(viewHolder.itemView, mContactMenu, mContactRecyclerView.getHeight(), (int) touchX, (int) touchY);
        }
    };


    private final IndexBarView.OnTouchSelectedListener mIndexBarSelectedListener = new IndexBarView.OnTouchSelectedListener() {
        @Override
        public void onSelected(int position, String text) {
            final int scrollPosition = mContactAdapter.findPositionByLetter(text);
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
                AnimationUtil.scaleAnim(mFBtnAdd, 0, 0, 300);
                AnimationUtil.scaleAnim(mTvIndexBarHint, 1f, 1f, 300);
                mFBtnAdd.setTag(true);
            }
            mTvIndexBarHint.setText(text);
            mAutoEnableOverScrollListener.setEnableOverScroll(false);

        }

        @Override
        public void onCancelSelected() {
            mFBtnAdd.setTag(null);
            AnimationUtil.scaleAnim(mTvIndexBarHint, 0, 0, 250);
            AnimationUtil.scaleAnim(mFBtnAdd, 1f, 1f, 250);
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
            mBadgeView.setVisibility(View.INVISIBLE);
        } else {
            mBadgeView.setVisibility(View.VISIBLE);
        }
        mBadgeView.setBadgeNumber(unreadCount);
    }

    @Override
    public void updateContactItem(ContactBean contactBean) {
        int updatePosition = mContactList.indexOf(contactBean);
        if (updatePosition != -1) {
            mContactList.set(updatePosition, contactBean);
            mContactAdapter.notifyItemChangedEx(updatePosition);
        }
    }

    @Override
    public void updateContactListView(DiffUtil.DiffResult diffResult, List<ContactBean> newFriendList) {
        mContactList.clear();
        mContactList.addAll(newFriendList);
        diffResult.dispatchUpdatesTo(new BaseRecyclerViewAdapter.ListUpdateCallback(mContactAdapter));
    }
}
