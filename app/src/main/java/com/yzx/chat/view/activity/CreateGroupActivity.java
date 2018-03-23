package com.yzx.chat.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.GroupMemberBean;
import com.yzx.chat.contract.CreateGroupContract;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.presenter.CreateGroupPresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.adapter.CreateGroupAdapter;
import com.yzx.chat.widget.listener.AutoCloseKeyboardScrollListener;
import com.yzx.chat.widget.view.CircleImageView;
import com.yzx.chat.widget.view.FlowLayout;
import com.yzx.chat.widget.view.IndexBarView;
import com.yzx.chat.widget.view.LetterSegmentationItemDecoration;
import com.yzx.chat.widget.view.ProgressDialog;

import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.model.Conversation;

/**
 * Created by YZX on 2018年02月22日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class CreateGroupActivity extends BaseCompatActivity<CreateGroupContract.Presenter> implements CreateGroupContract.View {

    public static final String INTENT_EXTRA_GROUP_ID = "GroupID";
    public static final String INTENT_EXTRA_ALREADY_JOIN_MEMBER = "AlreadyJoinMember";

    private RecyclerView mRecyclerView;
    private View mHeaderView;
    private CreateGroupAdapter mCreateGroupAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private LetterSegmentationItemDecoration mLetterSegmentationItemDecoration;
    private IndexBarView mIndexBarView;
    private TextView mTvIndexBarHint;
    private FlowLayout mFlowLayout;
    private MenuItem mConfirmMenuItem;
    private SearchView mSearchView;
    private ProgressDialog mProgressDialog;
    private List<ContactBean> mAllContactList;
    private List<ContactBean> mFilterContactList;
    private List<ContactBean> mSelectedContactList;
    private List<ContactBean> mAlreadyJoinContactList;
    private String mGroupID;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_create_group;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mRecyclerView = findViewById(R.id.CreateGroupActivity_mRecyclerView);
        mIndexBarView = findViewById(R.id.CreateGroupActivity_mIndexBarView);
        mTvIndexBarHint = findViewById(R.id.CreateGroupActivity_mTvIndexBarHint);
        mHeaderView = getLayoutInflater().inflate(R.layout.item_create_group_header, (ViewGroup) getWindow().getDecorView(), false);
        mFlowLayout = mHeaderView.findViewById(R.id.CreateGroupActivity_mFlowLayout);
        mAllContactList = IMClient.getInstance().contactManager().getAllContacts();
        if (mAllContactList == null) {
            return;
        }
        mFilterContactList = new ArrayList<>(mAllContactList.size());
        mProgressDialog = new ProgressDialog(this, getString(R.string.ProgressHint_Create));
        mSelectedContactList = new ArrayList<>(mAllContactList.size() / 2 + 1);
        mCreateGroupAdapter = new CreateGroupAdapter(mFilterContactList, mSelectedContactList);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (mAllContactList == null) {
            finish();
            return;
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mFilterContactList.addAll(mAllContactList);

        mFlowLayout.setItemSpace((int) AndroidUtil.dip2px(4));
        mFlowLayout.setLineSpace((int) AndroidUtil.dip2px(4));

        mLetterSegmentationItemDecoration = new LetterSegmentationItemDecoration();
        mLetterSegmentationItemDecoration.setLineColor(ContextCompat.getColor(this, R.color.divider_color_black));
        mLetterSegmentationItemDecoration.setLineWidth(1);
        mLetterSegmentationItemDecoration.setTextColor(ContextCompat.getColor(this, R.color.divider_color_black));
        mLetterSegmentationItemDecoration.setTextSize(AndroidUtil.sp2px(16));

        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mCreateGroupAdapter);
        mRecyclerView.setHasFixedSize(true);
        // mContactRecyclerView.setItemAnimator(new NoAnimations());
        mRecyclerView.addItemDecoration(mLetterSegmentationItemDecoration);
        mRecyclerView.addOnScrollListener(new AutoCloseKeyboardScrollListener(this));

        mIndexBarView.setSelectedTextColor(ContextCompat.getColor(this, R.color.text_secondary_color_black));
        mIndexBarView.setOnTouchSelectedListener(mIndexBarSelectedListener);


        mCreateGroupAdapter.setHeaderView(mHeaderView);
        mCreateGroupAdapter.setOnItemSelectedChangeListener(mOnItemSelectedChangeListener);

        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        mGroupID = getIntent().getStringExtra(INTENT_EXTRA_GROUP_ID);
        if (!TextUtils.isEmpty(mGroupID)) {
            mProgressDialog.setHintText(getString(R.string.ProgressHint_Add));
            List<GroupMemberBean> groupMemberList = getIntent().getParcelableArrayListExtra(INTENT_EXTRA_ALREADY_JOIN_MEMBER);
            mAlreadyJoinContactList = new ArrayList<>(groupMemberList.size());
            ContactBean contact;
            for (GroupMemberBean member : groupMemberList) {
                contact = new ContactBean();
                contact.setUserProfile(member.getUserProfile());
                mAlreadyJoinContactList.add(contact);
            }
            mAlreadyJoinContactList.retainAll(mAllContactList);
            if (mAlreadyJoinContactList.size() > 0) {
                mCreateGroupAdapter.setDisableSelectedList(mAlreadyJoinContactList);
            }
        }

    }

    private void setupSearch() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mFilterContactList.clear();
                if (TextUtils.isEmpty(newText)) {
                    mFilterContactList.addAll(mAllContactList);
                } else {
                    newText = newText.toLowerCase();
                    for (ContactBean contact : mAllContactList) {
                        if (contact.getName().contains(newText) || contact.getAbbreviation().contains(newText)) {
                            mFilterContactList.add(contact);
                        }
                    }
                }
                mCreateGroupAdapter.notifyDataSetChanged();
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_group, menu);
        mConfirmMenuItem = menu.findItem(R.id.CreateGroupMenu_Confirm);
        MenuItem searchItem = menu.findItem(R.id.CreateGroupMenu_Search);
        mSearchView = (SearchView) searchItem.getActionView();
        mConfirmMenuItem.setEnabled(false);
        setupSearch();
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.CreateGroupMenu_Confirm) {
            if (TextUtils.isEmpty(mGroupID)) {
                mProgressDialog.show(getString(R.string.ProgressHint_Create));
                mPresenter.createGroup(mSelectedContactList);
            } else {
                mProgressDialog.show(getString(R.string.ProgressHint_Add));
                mPresenter.addMembers(mGroupID, mSelectedContactList);
            }
        } else {
            if (!mSearchView.isIconified()) {
                mSearchView.setQuery(null, false);
                mSearchView.setIconified(true);
            } else {
                return super.onOptionsItemSelected(item);
            }
        }
        return true;
    }

    private final CreateGroupAdapter.OnItemSelectedChangeListener mOnItemSelectedChangeListener = new CreateGroupAdapter.OnItemSelectedChangeListener() {
        @Override
        public void onItemSelectedChange(int position, boolean isSelect) {
            if (isSelect) {
                mSelectedContactList.add(mAllContactList.get(position - 1));
                CircleImageView avatar = new CircleImageView(CreateGroupActivity.this);
                avatar.setId(position);
                avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
                avatar.setImageResource(R.drawable.temp_head_image);
                mFlowLayout.addView(avatar, new ViewGroup.MarginLayoutParams((int) AndroidUtil.dip2px(40), (int) AndroidUtil.dip2px(40)));
            } else {
                mSelectedContactList.remove(mAllContactList.get(position - 1));
                View needRemoveView = mFlowLayout.findViewById(position);
                if (needRemoveView != null) {
                    mFlowLayout.removeView(needRemoveView);
                }
            }
            mConfirmMenuItem.setEnabled(mSelectedContactList.size() > 0);
        }
    };

    private final IndexBarView.OnTouchSelectedListener mIndexBarSelectedListener = new IndexBarView.OnTouchSelectedListener() {
        @Override
        public void onSelected(int position, String text) {
            final int scrollPosition = mCreateGroupAdapter.findPositionByLetter(text);
            if (scrollPosition >= 0) {
                mRecyclerView.scrollToPosition(scrollPosition);
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        int firstPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
                        if (scrollPosition > firstPosition) {
                            View childView = mRecyclerView.getChildAt(scrollPosition - firstPosition);
                            int scrollY = childView.getTop() - mLetterSegmentationItemDecoration.getSpace();
                            mRecyclerView.scrollBy(0, scrollY);
                        }
                    }
                });
            }
            mTvIndexBarHint.setVisibility(View.VISIBLE);
            mTvIndexBarHint.setText(text);
        }

        @Override
        public void onCancelSelected() {
            mTvIndexBarHint.setVisibility(View.INVISIBLE);
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
    public CreateGroupContract.Presenter getPresenter() {
        return new CreateGroupPresenter();
    }


    @Override
    public void showError(String error) {
        showToast(error);
        mProgressDialog.dismiss();
    }

    @Override
    public void launchChatActivity(GroupBean group) {
        mProgressDialog.dismiss();
        Intent intent = new Intent(this, ChatActivity.class);
        Conversation conversation = new Conversation();
        conversation.setConversationType(Conversation.ConversationType.GROUP);
        conversation.setTargetId(group.getGroupID());
        conversation.setConversationTitle(group.getName());
        intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION, conversation);
        startActivity(intent);
        AndroidUtil.finishActivityInstackAbove(HomeActivity.class);

    }


}
