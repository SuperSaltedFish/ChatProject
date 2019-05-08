package com.yzx.chat.module.group.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.core.entity.GroupMemberEntity;
import com.yzx.chat.module.conversation.view.ChatActivity;
import com.yzx.chat.module.group.contract.CreateGroupContract;
import com.yzx.chat.module.group.presenter.CreateGroupPresenter;
import com.yzx.chat.module.main.view.HomeActivity;
import com.yzx.chat.tool.ActivityHelper;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.widget.adapter.CreateGroupAdapter;
import com.yzx.chat.widget.listener.AutoCloseKeyboardItemTouchListener;
import com.yzx.chat.widget.view.FlowLayout;
import com.yzx.chat.widget.view.IndexBarView;
import com.yzx.chat.widget.view.LetterSegmentationItemDecoration;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
    private List<ContactEntity> mAllContactList;
    private List<ContactEntity> mFilterContactList;
    private List<ContactEntity> mSelectedContactList;
    private List<ContactEntity> mAlreadyJoinContactList;
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
        mAllContactList = AppClient.getInstance().getContactManager().getAllContacts();
        if (mAllContactList == null) {
            return;
        }
        mFilterContactList = new ArrayList<>(mAllContactList.size());
        mSelectedContactList = new ArrayList<>(mAllContactList.size() / 2 + 1);
        mCreateGroupAdapter = new CreateGroupAdapter(mFilterContactList, mSelectedContactList);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (mAllContactList == null) {
            finish();
            return;
        }
        setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.CreateGroupActivity_Title);

        mFilterContactList.addAll(mAllContactList);

        mFlowLayout.setItemSpace((int) AndroidHelper.dip2px(4));
        mFlowLayout.setLineSpace((int) AndroidHelper.dip2px(4));

        mLetterSegmentationItemDecoration = new LetterSegmentationItemDecoration();
        mLetterSegmentationItemDecoration.setLineColor(ContextCompat.getColor(this, R.color.dividerColor));
        mLetterSegmentationItemDecoration.setLineWidth(1);
        mLetterSegmentationItemDecoration.setTextColor(ContextCompat.getColor(this, R.color.dividerColor));
        mLetterSegmentationItemDecoration.setTextSize(AndroidHelper.sp2px(16));

        mLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mCreateGroupAdapter);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.addItemDecoration(mLetterSegmentationItemDecoration);
        mRecyclerView.addOnItemTouchListener(new AutoCloseKeyboardItemTouchListener());

        mIndexBarView.setSelectedTextColor(ContextCompat.getColor(this, R.color.textColorSecondaryBlack));
        mIndexBarView.setOnTouchSelectedListener(mIndexBarSelectedListener);


        mCreateGroupAdapter.setHeaderView(mHeaderView);
        mCreateGroupAdapter.setOnItemSelectedChangeListener(mOnItemSelectedChangeListener);


        mGroupID = getIntent().getStringExtra(INTENT_EXTRA_GROUP_ID);
        if (!TextUtils.isEmpty(mGroupID)) {
            List<GroupMemberEntity> groupMemberList = getIntent().getParcelableArrayListExtra(INTENT_EXTRA_ALREADY_JOIN_MEMBER);
            mAlreadyJoinContactList = new ArrayList<>(groupMemberList.size());
            ContactEntity contact;
            for (GroupMemberEntity member : groupMemberList) {
                contact = new ContactEntity();
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
                    for (ContactEntity contact : mAllContactList) {
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
                mPresenter.createGroup(mSelectedContactList);
            } else {
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
                ImageView avatar = new ImageView(CreateGroupActivity.this);
                avatar.setId(position);
                avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
                GlideUtil.loadAvatarFromUrl(CreateGroupActivity.this, avatar, mFilterContactList.get(position - 1).getUserProfile().getAvatar());
                mFlowLayout.addView(avatar, new ViewGroup.MarginLayoutParams((int) AndroidHelper.dip2px(40), (int) AndroidHelper.dip2px(40)));
            } else {
                mSelectedContactList.remove(mAllContactList.get(position - 1));
                ImageView needRemoveView = mFlowLayout.findViewById(position);
                if (needRemoveView != null) {
                    mFlowLayout.removeView(needRemoveView);
                    GlideUtil.clear(CreateGroupActivity.this, needRemoveView);
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
    public void launchChatActivity(GroupEntity group) {
        ActivityHelper.finishActivitiesInStackAbove(HomeActivity.class);
        mProgressDialog.dismiss();
        ChatActivity.startActivity(this, group.getGroupID(), Conversation.ConversationType.GROUP);
    }


}
