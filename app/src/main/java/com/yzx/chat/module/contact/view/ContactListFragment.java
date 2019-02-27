package com.yzx.chat.module.contact.view;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.broadcast.BackPressedReceive;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.module.contact.contract.ContactListContract;
import com.yzx.chat.module.contact.presenter.ContactListPresenter;
import com.yzx.chat.module.group.view.GroupListActivity;
import com.yzx.chat.module.main.view.HomeActivity;
import com.yzx.chat.module.me.view.MyTagListActivity;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.util.AnimationUtil;
import com.yzx.chat.widget.adapter.ContactAdapter;
import com.yzx.chat.widget.adapter.ContactSearchAdapter;
import com.yzx.chat.widget.listener.AutoCloseKeyboardScrollListener;
import com.yzx.chat.widget.listener.ImageAutoLoadScrollListener;
import com.yzx.chat.widget.listener.OnOnlySingleClickListener;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.BadgeView;
import com.yzx.chat.widget.view.IndexBarView;
import com.yzx.chat.widget.view.LetterSegmentationItemDecoration;
import com.yzx.chat.widget.view.OverflowMenuShowHelper;
import com.yzx.chat.widget.view.OverflowPopupMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by YZX on 2017年06月28日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class ContactListFragment extends BaseFragment<ContactListContract.Presenter> implements ContactListContract.View {

    public static final String TAG = ContactListFragment.class.getSimpleName();

    private RecyclerView mRvContact;
    private RecyclerView mRvSearchContact;
    private ContactAdapter mContactAdapter;
    private ContactSearchAdapter mSearchAdapter;
    private IndexBarView mIndexBarView;
    private View mHeaderView;
    private TextView mTvIndexBarHint;
    private Toolbar mToolbar;
    private PopupWindow mSearchPopupWindow;
    private SearchView mSearchView;
    private View mLlContactOperation;
    private View mLlGroup;
    private View mLlTags;
    private TextView mTvTags;
    private BadgeView mBadgeView;

    private FloatingActionButton mFBtnAdd;
    private LinearLayoutManager mLinearLayoutManager;
    private LetterSegmentationItemDecoration mLetterSegmentationItemDecoration;
    private OverflowPopupMenu mContactMenu;
    private Handler mSearchHandler;
    private List<ContactEntity> mContactSearchList;


    @Override
    protected int getLayoutID() {
        return R.layout.fragment_contact_list;
    }

    @Override
    protected void init(View parentView) {
        mToolbar = parentView.findViewById(R.id.Default_mToolbar);
        mRvContact = parentView.findViewById(R.id.ContactFragmentList_mRvContact);
        mIndexBarView = parentView.findViewById(R.id.ContactFragmentList_mIndexBarView);
        mTvIndexBarHint = parentView.findViewById(R.id.ContactFragmentList_mTvIndexBarHint);
        mFBtnAdd = parentView.findViewById(R.id.ContactFragmentList_mFBtnAdd);
        mHeaderView = LayoutInflater.from(mContext).inflate(R.layout.item_contact_header, (ViewGroup) parentView, false);
        mLlContactOperation = mHeaderView.findViewById(R.id.ContactFragmentList_mLlContactOperation);
        mLlGroup = mHeaderView.findViewById(R.id.ContactFragmentList_mLlGroup);
        mLlTags = mHeaderView.findViewById(R.id.ContactFragmentList_mLlTags);
        mBadgeView = mHeaderView.findViewById(R.id.ContactFragmentList_mBadgeView);
        mTvTags = mHeaderView.findViewById(R.id.ContactFragmentList_mTvTags);
        mRvSearchContact = new RecyclerView(mContext);
        mContactMenu = new OverflowPopupMenu(mContext);
        mContactSearchList = new ArrayList<>(32);
        mContactAdapter = new ContactAdapter();
        mSearchAdapter = new ContactSearchAdapter(mContactSearchList);
        mSearchHandler = new Handler();
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        mToolbar.setTitle(R.string.app_name);

        mLetterSegmentationItemDecoration = new LetterSegmentationItemDecoration();
        mLetterSegmentationItemDecoration.setLineColor(ContextCompat.getColor(mContext, R.color.dividerColorBlack));
        mLetterSegmentationItemDecoration.setLineWidth(1);
        mLetterSegmentationItemDecoration.setTextColor(ContextCompat.getColor(mContext, R.color.textSecondaryColorBlackLight));
        mLetterSegmentationItemDecoration.setTextSize(AndroidHelper.sp2px(15));

        mLinearLayoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        mRvContact.setLayoutManager(mLinearLayoutManager);
        mRvContact.setAdapter(mContactAdapter);
        mRvContact.setHasFixedSize(true);
        mRvContact.addItemDecoration(mLetterSegmentationItemDecoration);
        mRvContact.addOnScrollListener(new ImageAutoLoadScrollListener());
        mRvContact.addOnItemTouchListener(mOnRecyclerViewItemClickListener);

        mLlContactOperation.setOnClickListener(mOnViewClickListener);
        mLlGroup.setOnClickListener(mOnViewClickListener);
        mLlTags.setOnClickListener(mOnViewClickListener);
        mFBtnAdd.setOnClickListener(mOnViewClickListener);

        mIndexBarView.setSelectedTextColor(ContextCompat.getColor(mContext, R.color.textSecondaryColorBlack));
        mIndexBarView.setOnTouchSelectedListener(mIndexBarSelectedListener);

        mContactAdapter.setHeaderView(mHeaderView);

        setSearchBar();
        setOverflowMenu();

        BackPressedReceive.registerBackPressedListener(mBackPressedListener);
    }


    private void setSearchBar() {
        mRvSearchContact.setLayoutManager(new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false));
        mRvSearchContact.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mRvSearchContact.setRecycledViewPool(mRvContact.getRecycledViewPool());
        mRvSearchContact.setAdapter(mSearchAdapter);
        mRvSearchContact.addOnScrollListener(new AutoCloseKeyboardScrollListener((Activity) mContext));
        mRvSearchContact.addOnItemTouchListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(final int position, RecyclerView.ViewHolder viewHolder) {
                ContactProfileActivity.startActivity(mContext, mContactSearchList.get(position).getContactID());
                mSearchView.setQuery(null, false);
                mSearchView.setIconified(true);
                mSearchPopupWindow.dismiss();
            }
        });


        final int searchPopupWindowWidth = (int) (AndroidHelper.getScreenWidth() - AndroidHelper.dip2px(32));
        mSearchPopupWindow = new PopupWindow(mContext);
        mSearchPopupWindow.setAnimationStyle(-1);
        mSearchPopupWindow.setWidth(searchPopupWindowWidth);
        mSearchPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mSearchPopupWindow.setContentView(mRvSearchContact);
        mSearchPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mSearchPopupWindow.setElevation(AndroidHelper.dip2px(8));
        mSearchPopupWindow.setOutsideTouchable(true);
        mSearchPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        mSearchPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        mToolbar.inflateMenu(R.menu.menu_contact_list);
        MenuItem searchItem = mToolbar.getMenu().findItem(R.id.ContactList_Search);
        mSearchView = (SearchView) searchItem.getActionView();
        ImageView searchButton = mSearchView.findViewById(androidx.appcompat.R.id.search_button);
        if (searchButton != null) {
            searchButton.setImageResource(R.drawable.ic_search);
            searchButton.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        }
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                mSearchHandler.removeCallbacksAndMessages(null);
                mSearchHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!TextUtils.isEmpty(newText)) {
                            if (mSearchAdapter.setFilterText(newText) > 0) {
                                if (!mSearchPopupWindow.isShowing()) {
                                    mSearchPopupWindow.showAsDropDown(mToolbar, (mToolbar.getWidth() - searchPopupWindowWidth) / 2, 0, Gravity.START);
                                }
                            } else {
                                mSearchPopupWindow.dismiss();
                            }
                        } else {
                            mSearchAdapter.setFilterText(null);
                            mSearchPopupWindow.dismiss();
                        }
                    }
                }, 250);
                return false;
            }
        });
    }

    private void setOverflowMenu() {
        mContactMenu.setWidth((int) AndroidHelper.dip2px(152));
        mContactMenu.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mContactMenu.setTitleTextColor(ContextCompat.getColor(mContext, R.color.textPrimaryColorBlack));
        mContactMenu.setElevation(AndroidHelper.dip2px(4));
        mContactMenu.inflate(R.menu.menu_contact_overflow);
        mContactMenu.setOnMenuItemClickListener(new OverflowPopupMenu.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, int menuID) {
                ContactEntity contact = (ContactEntity) mRvContact.getTag();
                if (contact != null) {
                    switch (menuID) {
                        case R.id.ContactMenu_UpdateRemarkInfo:
                            RemarkInfoActivity.startActivityForResult(ContactListFragment.this, contact);
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
        mPresenter.loadTagCount();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSearchView.clearFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BackPressedReceive.unregisterBackPressedListener(mBackPressedListener);
        mSearchHandler.removeCallbacksAndMessages(null);
    }

    private final View.OnClickListener mOnViewClickListener = new OnOnlySingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.ContactFragmentList_mLlContactOperation:
                    startActivity(new Intent(mContext, NotificationMessageActivity.class));
                    break;
                case R.id.ContactFragmentList_mLlGroup:
                    startActivity(new Intent(mContext, GroupListActivity.class));
                    break;
                case R.id.ContactFragmentList_mLlTags:
                    startActivity(new Intent(mContext, MyTagListActivity.class));
                    break;
                case R.id.ContactFragmentList_mFBtnAdd:
                    startActivity(new Intent(mContext, FindNewContactActivity.class));
                    break;
            }
        }
    };

    private final BackPressedReceive.BackPressedListener mBackPressedListener = new BackPressedReceive.BackPressedListener() {

        @Override
        public boolean onBackPressed(String initiator) {
            if (HomeActivity.class.getSimpleName().equals(initiator)) {
                if (mSearchPopupWindow.isShowing()) {
                    mSearchPopupWindow.dismiss();
                    return true;
                }
            }
            return false;
        }
    };

    private final OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(final int position, RecyclerView.ViewHolder viewHolder) {
            if (position == 0) {
                return;
            }
            ContactProfileActivity.startActivity(mContext, mContactAdapter.getItem(position - 1).getContactID());
        }

        @Override
        public void onItemLongClick(int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY) {
            if (position == 0) {
                return;
            }

            mRvContact.setTag(mContactAdapter.getItem(position - 1));
            OverflowMenuShowHelper.show(viewHolder.itemView, mContactMenu, mRvContact.getHeight(), (int) touchX, (int) touchY);
        }
    };


    private final IndexBarView.OnTouchSelectedListener mIndexBarSelectedListener = new IndexBarView.OnTouchSelectedListener() {
        @Override
        public void onSelected(int position, String text) {
            final int scrollPosition = mContactAdapter.findPositionByLetter(text);
            mLinearLayoutManager.scrollToPositionWithOffset(scrollPosition, 0);
            if (mFBtnAdd.getTag() == null) {
                AnimationUtil.scaleAnim(mFBtnAdd, 0, 0, 300);
                AnimationUtil.scaleAnim(mTvIndexBarHint, 1f, 1f, 300);
                mFBtnAdd.setTag(true);
            }
            mTvIndexBarHint.setText(text);

        }

        @Override
        public void onCancelSelected() {
            mFBtnAdd.setTag(null);
            AnimationUtil.scaleAnim(mTvIndexBarHint, 0, 0, 250);
            AnimationUtil.scaleAnim(mFBtnAdd, 1f, 1f, 250);
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
    public ContactListContract.Presenter getPresenter() {
        return new ContactListPresenter();
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
    public void showContactList(List<ContactEntity> contactList) {
        mSearchAdapter.setContactList(contactList);
        mContactAdapter.submitList(contactList);
    }

    @Override
    public void showTagCount(int count) {
        mTvTags.setText(String.format(Locale.getDefault(), "%s(%d)", getString(R.string.MyTagListActivity_Title), count));
    }
}

