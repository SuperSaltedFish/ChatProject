package com.yzx.chat.view.activity;

import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.adapter.GroupMembersAdapter;
import com.yzx.chat.widget.view.SpacesItemDecoration;

/**
 * Created by YZX on 2018年02月18日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class GroupProfileActivity extends BaseCompatActivity {

    private static final int GROUP_MEMBERS_LINE_MAX_COUNT = 5;

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private RecyclerView mRvGroupMembers;
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
    protected void init() {
        mCollapsingToolbarLayout = findViewById(R.id.GroupProfileActivity_mCollapsingToolbarLayout);
        mRvGroupMembers = findViewById(R.id.GroupProfileActivity_mRvGroupMembers);
        mFooterView = getLayoutInflater().inflate(R.layout.item_group_member_footer, (ViewGroup) getWindow().getDecorView(), false);
        mClGroupName = findViewById(R.id.ChatSetup_mClGroupName);
        mClMyGroupNickname = findViewById(R.id.ChatSetup_mClMyGroupNickname);
        mClQRCode = findViewById(R.id.ChatSetup_mClQRCode);
        mAdapter = new GroupMembersAdapter(GROUP_MEMBERS_LINE_MAX_COUNT);
    }

    @Override
    protected void setup() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mCollapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        mCollapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);

        mRvGroupMembers.addItemDecoration(new SpacesItemDecoration((int) AndroidUtil.dip2px(10), SpacesItemDecoration.VERTICAL, false, true));
        mRvGroupMembers.setLayoutManager(new GridLayoutManager(this, GROUP_MEMBERS_LINE_MAX_COUNT));
        mRvGroupMembers.setAdapter(mAdapter);
        mAdapter.addFooterView(mFooterView);

        mClGroupName.setOnClickListener(mOnGroupNameClickListener);
        mClMyGroupNickname.setOnClickListener(mOnMyGroupNicknameClickListener);
        mClQRCode.setOnClickListener(mOnQRCodeClickListener);
    }


    private final View.OnClickListener mOnGroupNameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private final View.OnClickListener mOnMyGroupNicknameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private final View.OnClickListener mOnQRCodeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
}
