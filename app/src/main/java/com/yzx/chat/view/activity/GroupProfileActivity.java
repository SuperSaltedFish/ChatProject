package com.yzx.chat.view.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.GroupMemberBean;
import com.yzx.chat.contract.GroupProfileContract;
import com.yzx.chat.presenter.GroupProfilePresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.adapter.GroupMembersAdapter;
import com.yzx.chat.widget.view.ProgressDialog;
import com.yzx.chat.widget.view.SpacesItemDecoration;

import java.util.ArrayList;
import java.util.List;

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
    private View mFooterView;
    private ConstraintLayout mClGroupName;
    private ConstraintLayout mClMyGroupNickname;
    private ConstraintLayout mClQRCode;
    private ProgressDialog mProgressDialog;
    private GroupMembersAdapter mAdapter;

    private List<GroupMemberBean> mGroupMemberList;

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
        mIvEditNotice = findViewById(R.id.GroupProfileActivity_mIvEditNotice);
        mGroupMemberList = new ArrayList<>(64);
        mAdapter = new GroupMembersAdapter(mGroupMemberList, GROUP_MEMBERS_MAX_VISIBILITY_COUNT);
        mProgressDialog = new ProgressDialog(this, getString(R.string.ProgressHint_Modify));
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mCollapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        mCollapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);

        mRvGroupMembers.setHasFixedSize(true);
        mRvGroupMembers.addItemDecoration(new SpacesItemDecoration((int) AndroidUtil.dip2px(10), SpacesItemDecoration.VERTICAL, false, true));
        mRvGroupMembers.setLayoutManager(new GridLayoutManager(this, GROUP_MEMBERS_LINE_MAX_COUNT));
        mRvGroupMembers.setAdapter(mAdapter);
        mAdapter.setFooterView(mFooterView);

        mClGroupName.setOnClickListener(mOnGroupNameClickListener);
        mClMyGroupNickname.setOnClickListener(mOnMyGroupNicknameClickListener);
        mClQRCode.setOnClickListener(mOnQRCodeClickListener);
        mIvEditNotice.setOnClickListener(mOnGroupNoticeClickListener);

        String groupID = getIntent().getStringExtra(INTENT_EXTRA_GROUP_ID);
        if (TextUtils.isEmpty(groupID)) {
            finish();
        } else {
            mPresenter.init(groupID);
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

                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


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
                                        mProgressDialog.show();
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
                                        mProgressDialog.show();
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
                                        mProgressDialog.show();
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
    public void goBack() {
        finish();
    }
}
