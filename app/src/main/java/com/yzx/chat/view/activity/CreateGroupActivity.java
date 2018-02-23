package com.yzx.chat.view.activity;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.adapter.CreateGroupAdapter;
import com.yzx.chat.widget.view.IndexBarView;
import com.yzx.chat.widget.view.LetterSegmentationItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年02月22日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class CreateGroupActivity extends BaseCompatActivity {


    private RecyclerView mRecyclerView;
    private View mHeaderView;
    private CreateGroupAdapter mCreateGroupAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private LetterSegmentationItemDecoration mLetterSegmentationItemDecoration;
    private IndexBarView mIndexBarView;
    private List<ContactBean> mContactList;
    private List<ContactBean> mSelectedContactList;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_create_group;
    }

    @Override
    protected void init() {
        mRecyclerView = findViewById(R.id.CreateGroupActivity_mRecyclerView);
        mIndexBarView = findViewById(R.id.CreateGroupActivity_mIndexBarView);
        mHeaderView = getLayoutInflater().inflate(R.layout.item_create_group_header, (ViewGroup) getWindow().getDecorView(), false);
        mContactList = IMClient.getInstance().contactManager().getAllContacts();
        if (mContactList == null) {
            return;
        }
        mSelectedContactList = new ArrayList<>(32);
        mCreateGroupAdapter = new CreateGroupAdapter(mContactList);
    }

    @Override
    protected void setup() {
        if (mContactList == null) {
            finish();
            return;
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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

        mIndexBarView.setSelectedTextColor(ContextCompat.getColor(this, R.color.text_secondary_color_black));
       // mIndexBarView.setOnTouchSelectedListener(mIndexBarSelectedListener);


        mCreateGroupAdapter.addHeaderView(mHeaderView);
        mCreateGroupAdapter.setOnItemSelectedChangeListener(mOnItemSelectedChangeListener);
    }

    private final CreateGroupAdapter.OnItemSelectedChangeListener mOnItemSelectedChangeListener = new CreateGroupAdapter.OnItemSelectedChangeListener() {
        @Override
        public void onItemSelectedChange(int position, boolean isSelect) {
            if (isSelect) {
                mSelectedContactList.add(mContactList.get(position - 1));
            } else {
                mSelectedContactList.remove(mContactList.get(position - 1));
            }
        }
    };
}
