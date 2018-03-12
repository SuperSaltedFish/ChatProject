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
import android.view.View;
import android.view.ViewGroup;
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
import com.yzx.chat.widget.view.SpacesItemDecoration;

/**
 * Created by YZX on 2018年02月18日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class GroupProfileActivity extends BaseCompatActivity<GroupProfileContract.Presenter> implements GroupProfileContract.View {

    public static final String INTENT_EXTRA_GROUP_ID = "GroupID";
    private static final int GROUP_MEMBERS_LINE_MAX_COUNT = 5;

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private RecyclerView mRvGroupMembers;
    private TextView mTvContentGroupName;
    private TextView mTvContentMyGroupNickname;
    private TextView mTvContentGroupNotice;
    private View mFooterView;
    private ConstraintLayout mClGroupName;
    private ConstraintLayout mClMyGroupNickname;
    private ConstraintLayout mClQRCode;
    private GroupMembersAdapter mAdapter;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_group_profile;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mCollapsingToolbarLayout = findViewById(R.id.GroupProfileActivity_mCollapsingToolbarLayout);
        mRvGroupMembers = findViewById(R.id.GroupProfileActivity_mRvGroupMembers);
        mTvContentGroupName = findViewById(R.id.ChatSetup_mTvContentGroupName);
        mTvContentMyGroupNickname = findViewById(R.id.ChatSetup_mTvContentMyGroupNickname);
        mTvContentGroupNotice = findViewById(R.id.ChatSetup_mTvContentGroupNotice);
        mFooterView = getLayoutInflater().inflate(R.layout.item_group_member_footer, (ViewGroup) getWindow().getDecorView(), false);
        mClGroupName = findViewById(R.id.ChatSetup_mClGroupName);
        mClMyGroupNickname = findViewById(R.id.ChatSetup_mClMyGroupNickname);
        mClQRCode = findViewById(R.id.ChatSetup_mClQRCode);
        mAdapter = new GroupMembersAdapter(GROUP_MEMBERS_LINE_MAX_COUNT);
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

        String groupID = getIntent().getStringExtra(INTENT_EXTRA_GROUP_ID);
        if (TextUtils.isEmpty(groupID)) {
            finish();
        } else {
            mPresenter.init(groupID);
        }
    }


    private final View.OnClickListener mOnGroupNameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new MaterialDialog.Builder(GroupProfileActivity.this)
                    .title(R.string.GroupProfileActivity_GroupNameDialogTitle)
                    .content(R.string.GroupProfileActivity_GroupNameDialogContent)
                    .inputType(
                            InputType.TYPE_CLASS_TEXT
                                    | InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                                    | InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                    .inputRange(1, 16)
                    .positiveText(R.string.Confirm)
                    .negativeText(R.string.Cancel)
                    .input(
                            null,
                            null,
                            false,
                            new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

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
                    .content(R.string.GroupProfileActivity_MyGroupNicknameDialogContent)
                    .inputType(
                            InputType.TYPE_CLASS_TEXT
                                    | InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                                    | InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                    .inputRange(0, 16)
                    .positiveText(R.string.Confirm)
                    .negativeText(R.string.Cancel)
                    .input(
                            null,
                            null,
                            false,
                            new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

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
    public void updateGroupInfo(GroupBean group) {
        mCollapsingToolbarLayout.setTitle(group.getName());
        mTvContentGroupName.setText(group.getName());
        if (TextUtils.isEmpty(group.getNotice())) {
            mTvContentGroupNotice.setText(R.string.None);
        } else {
            mTvContentGroupNotice.setText(group.getNotice());
        }
    }

    @Override
    public void updateMySelfInfo(GroupMemberBean groupMember) {
        mTvContentMyGroupNickname.setText(groupMember.getNicknameInGroup());
    }

    @Override
    public void showError(String error) {

    }

    @Override
    public void goBack() {
        finish();
    }
}
