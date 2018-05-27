package com.yzx.chat.mvp.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.GroupMemberBean;
import com.yzx.chat.mvp.contract.GroupProfileContract;
import com.yzx.chat.mvp.presenter.GroupProfilePresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.adapter.GroupMembersAdapter;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.ProgressDialog;
import com.yzx.chat.widget.view.SpacesItemDecoration;

import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.model.Conversation;

/**
 * Created by YZX on 2018年02月18日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class GroupProfileActivity extends BaseCompatActivity<GroupProfileContract.Presenter> implements GroupProfileContract.View {

    public static final String INTENT_EXTRA_GROUP_ID = "GroupID";
    private static final int GROUP_MEMBERS_LINE_MAX_COUNT = 5;
    private static final int GROUP_MEMBERS_MAX_VISIBILITY_COUNT = 9;

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private RecyclerView mRvGroupMembers;
    private TextView mTvContentGroupName;
    private TextView mTvContentNicknameInGroup;
    private TextView mTvContentGroupNotice;
    private ImageView mIvEditNotice;
    private FloatingActionButton mBtnStartChat;
    private View mFooterView;
    private ConstraintLayout mClGroupName;
    private ConstraintLayout mClMyGroupNickname;
    private ConstraintLayout mClQRCode;
    private ConstraintLayout mClClearMessage;
    private Switch mSwitchTop;
    private Switch mSwitchRemind;
    private ProgressDialog mProgressDialog;
    private GroupMembersAdapter mAdapter;

    private String mGroupID;
    private ArrayList<GroupMemberBean> mGroupMemberList;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_group_profile;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mCollapsingToolbarLayout = findViewById(R.id.GroupProfileActivity_mCollapsingToolbarLayout);
        mRvGroupMembers = findViewById(R.id.GroupProfileActivity_mRvGroupMembers);
        mTvContentGroupName = findViewById(R.id.GroupProfileActivity_mTvContentGroupName);
        mTvContentNicknameInGroup = findViewById(R.id.GroupProfileActivity_mTvContentNicknameInGroup);
        mTvContentGroupNotice = findViewById(R.id.GroupProfileActivity_mTvContentGroupNotice);
        mFooterView = getLayoutInflater().inflate(R.layout.item_group_member_footer, (ViewGroup) getWindow().getDecorView(), false);
        mClGroupName = findViewById(R.id.GroupProfileActivity_mClGroupName);
        mClMyGroupNickname = findViewById(R.id.GroupProfileActivity_mClMyGroupNickname);
        mClQRCode = findViewById(R.id.GroupProfileActivity_mClQRCode);
        mClClearMessage = findViewById(R.id.ChatSetup_mClClearMessage);
        mIvEditNotice = findViewById(R.id.GroupProfileActivity_mIvEditNotice);
        mBtnStartChat = findViewById(R.id.GroupProfileActivity_mBtnStartChat);
        mSwitchTop = findViewById(R.id.ChatSetup_mSwitchTop);
        mSwitchRemind = findViewById(R.id.ChatSetup_mSwitchRemind);
        mGroupMemberList = new ArrayList<>(64);
        mAdapter = new GroupMembersAdapter(mGroupMemberList, GROUP_MEMBERS_MAX_VISIBILITY_COUNT);
        mProgressDialog = new ProgressDialog(this, getString(R.string.ProgressHint_Modify));
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mCollapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        mCollapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);

        mRvGroupMembers.setHasFixedSize(true);
        mRvGroupMembers.addItemDecoration(new SpacesItemDecoration((int) AndroidUtil.dip2px(10), SpacesItemDecoration.VERTICAL, false, true));
        mRvGroupMembers.setLayoutManager(new GridLayoutManager(this, GROUP_MEMBERS_LINE_MAX_COUNT));
        mRvGroupMembers.addOnItemTouchListener(mOnGroupMemberItemClickListener);
        mRvGroupMembers.setAdapter(mAdapter);
        mAdapter.setFooterView(mFooterView);

        mBtnStartChat.setOnClickListener(mOnStartChatClickListener);
        mClGroupName.setOnClickListener(mOnGroupNameClickListener);
        mClMyGroupNickname.setOnClickListener(mOnMyGroupNicknameClickListener);
        mClQRCode.setOnClickListener(mOnQRCodeClickListener);
        mIvEditNotice.setOnClickListener(mOnGroupNoticeClickListener);
        mFooterView.setOnClickListener(mOnAddNewMemberClickListener);
        mSwitchTop.setOnCheckedChangeListener(mOnTopSwitchChangeListener);
        mSwitchRemind.setOnCheckedChangeListener(mOnRemindSwitchChangeListener);
        mClClearMessage.setOnClickListener(mOnClearMessageClickListener);

        mGroupID = getIntent().getStringExtra(INTENT_EXTRA_GROUP_ID);
        if (TextUtils.isEmpty(mGroupID)) {
            finish();
        } else {
            mPresenter.init(mGroupID);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.GroupMenu_QuitGroup:
                new MaterialDialog.Builder(GroupProfileActivity.this)
                        .content(R.string.GroupProfileActivity_QuitDialogHint)
                        .positiveText(R.string.Confirm)
                        .negativeText(R.string.Cancel)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                mProgressDialog.show(getString(R.string.ProgressHint_Quit));
                                mPresenter.quitGroup();
                            }
                        })
                        .show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private final View.OnClickListener mOnStartChatClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(GroupProfileActivity.this, ChatActivity.class);
            Conversation conversation = new Conversation();
            conversation.setConversationType(Conversation.ConversationType.GROUP);
            GroupBean group = mPresenter.getGroup();
            conversation.setTargetId(group.getGroupID());
            conversation.setConversationTitle(group.getName());
            intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION, conversation);
            startActivity(intent);
            finish();
        }
    };


    private final View.OnClickListener mOnGroupNameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new MaterialDialog.Builder(GroupProfileActivity.this)
                    .title(R.string.GroupProfileActivity_GroupNameDialogTitle)
                    .content(R.string.GroupProfileActivity_GroupNameDialogHint)
                    .inputType(
                            InputType.TYPE_CLASS_TEXT
                                    | InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                                    | InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                    .inputRange(1, 16)
                    .positiveText(R.string.Confirm)
                    .negativeText(R.string.Cancel)
                    .input(
                            null,
                            mTvContentGroupName.getText(),
                            false,
                            new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    if (!input.toString().equals(mTvContentGroupName.getText())) {
                                        mProgressDialog.show(getString(R.string.ProgressHint_Modify));
                                        mPresenter.updateGroupName(input.toString());
                                    }
                                }
                            })
                    .show();
        }
    };

    private final View.OnClickListener mOnMyGroupNicknameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new MaterialDialog.Builder(GroupProfileActivity.this)
                    .title(R.string.GroupProfileActivity_MyGroupNicknameDialogTitle)
                    .content(R.string.GroupProfileActivity_MyGroupNicknameDialogHint)
                    .inputType(
                            InputType.TYPE_CLASS_TEXT
                                    | InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                                    | InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                    .inputRange(0, 16)
                    .positiveText(R.string.Confirm)
                    .negativeText(R.string.Cancel)
                    .input(
                            null,
                            mTvContentNicknameInGroup.getText(),
                            false,
                            new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    if (!input.toString().equals(mTvContentNicknameInGroup.getText())) {
                                        mProgressDialog.show(getString(R.string.ProgressHint_Modify));
                                        mPresenter.updateMyGroupAlias(input.toString());
                                    }
                                }
                            })
                    .show();
        }
    };

    private final View.OnClickListener mOnGroupNoticeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CharSequence currentNotice = mTvContentGroupNotice.getText();
            if (getString(R.string.None).equals(currentNotice)) {
                currentNotice = null;
            }
            final CharSequence finalCurrentNotice = currentNotice;
            new MaterialDialog.Builder(GroupProfileActivity.this)
                    .title(R.string.GroupProfileActivity_NoticeDialogTitle)
                    .content(R.string.GroupProfileActivity_NoticeDialogHint)
                    .inputType(
                            InputType.TYPE_CLASS_TEXT
                                    | InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                                    | InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                    .inputRange(0, 16)
                    .positiveText(R.string.Confirm)
                    .negativeText(R.string.Cancel)
                    .input(
                            null,
                            finalCurrentNotice,
                            true,
                            new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    if (input == null) {
                                        input = "";
                                    }
                                    if (!input.toString().equals(finalCurrentNotice)) {
                                        mProgressDialog.show(getString(R.string.ProgressHint_Modify));
                                        mPresenter.updateGroupNotice(input.toString());
                                    }
                                }
                            })
                    .show();
        }
    };

    private final View.OnClickListener mOnQRCodeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(GroupProfileActivity.this,MyQRCodeActivity.class);
            intent.putExtra(MyQRCodeActivity.INTENT_EXTRA_QR_TYPE,MyQRCodeActivity.QR_CODE_TYPE_GROUP);
            intent.putExtra(MyQRCodeActivity.INTENT_EXTRA_GROUP_ID,mGroupID);
            startActivity(intent);
        }
    };

    private final View.OnClickListener mOnAddNewMemberClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(GroupProfileActivity.this, CreateGroupActivity.class);
            intent.putExtra(CreateGroupActivity.INTENT_EXTRA_GROUP_ID, mPresenter.getGroup().getGroupID());
            intent.putParcelableArrayListExtra(CreateGroupActivity.INTENT_EXTRA_ALREADY_JOIN_MEMBER, mGroupMemberList);
            startActivity(intent);
        }
    };

    private final OnRecyclerViewItemClickListener mOnGroupMemberItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {
            if (position < mGroupMemberList.size()) {
                Intent intent;
                String userID = mGroupMemberList.get(position).getUserProfile().getUserID();
                if (mPresenter.isMySelf(userID)) {
                    intent = new Intent(GroupProfileActivity.this, ProfileModifyActivity.class);
                } else {
                    intent = new Intent(GroupProfileActivity.this, ContactProfileActivity.class);
                    intent.putExtra(ContactProfileActivity.INTENT_EXTRA_CONTACT_ID, userID);
                }
                startActivity(intent);
            }
        }
    };

    private final CompoundButton.OnCheckedChangeListener mOnTopSwitchChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mPresenter.setConversationToTop(isChecked);
        }
    };

    private final CompoundButton.OnCheckedChangeListener mOnRemindSwitchChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mPresenter.enableConversationNotification(!isChecked);
        }
    };

    private final View.OnClickListener mOnClearMessageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new MaterialDialog.Builder(GroupProfileActivity.this)
                    .content("是否删除所有聊天记录？")
                    .positiveText("确定")
                    .negativeText("取消")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which == DialogAction.POSITIVE) {
                                mPresenter.clearChatMessages();
                            }
                        }
                    })
                    .show();
        }
    };

    @Override
    public GroupProfileContract.Presenter getPresenter() {
        return new GroupProfilePresenter();
    }

    @Override
    public void showGroupInfo(GroupBean group, GroupMemberBean mySelf) {
        if (TextUtils.isEmpty(group.getOwner())) {
            finish();
            return;
        }
        mCollapsingToolbarLayout.setTitle(group.getName());
        mTvContentGroupName.setText(group.getName());
        if (TextUtils.isEmpty(group.getNotice())) {
            mTvContentGroupNotice.setText(R.string.None);
        } else {
            mTvContentGroupNotice.setText(group.getNotice());
        }
        mGroupMemberList.clear();
        List<GroupMemberBean> members = group.getMembers();
        if (members != null) {
            mGroupMemberList.addAll(members);

        }
        mAdapter.notifyDataSetChanged();

        mTvContentNicknameInGroup.setText(mySelf.getNicknameInGroup());
        if (mySelf.getUserProfile().getUserID().equals(group.getOwner())) {
            mIvEditNotice.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showNewGroupName(String newGroupName) {
        mTvContentGroupName.setText(newGroupName);
        mProgressDialog.dismiss();
    }

    @Override
    public void showNewGroupNotice(String newGroupNotice) {
        if (TextUtils.isEmpty(newGroupNotice)) {
            mTvContentGroupNotice.setText(R.string.None);
        } else {
            mTvContentGroupNotice.setText(newGroupNotice);
        }
        mProgressDialog.dismiss();
    }

    @Override
    public void showNewMyAlias(String newAlias) {
        mTvContentNicknameInGroup.setText(newAlias);
        mProgressDialog.dismiss();
    }


    @Override
    public void showError(String error) {
        mProgressDialog.dismiss();
        showToast(error);
    }

    @Override
    public void switchTopState(boolean isOpen) {
        mSwitchTop.setOnCheckedChangeListener(null);
        mSwitchTop.setChecked(isOpen);
        mSwitchTop.setOnCheckedChangeListener(mOnTopSwitchChangeListener);
    }

    @Override
    public void switchRemindState(boolean isOpen) {
        mSwitchRemind.setOnCheckedChangeListener(null);
        mSwitchRemind.setChecked(isOpen);
        mSwitchRemind.setOnCheckedChangeListener(mOnRemindSwitchChangeListener);
    }

    @Override
    public void goBack() {
        mProgressDialog.dismiss();
        finish();
    }
}
