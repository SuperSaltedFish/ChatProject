package com.yzx.chat.mvp.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.mvp.contract.GroupListContract;
import com.yzx.chat.mvp.presenter.GroupListPresenter;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.widget.adapter.GroupAdapter;
import com.yzx.chat.widget.listener.ImageAutoLoadScrollListener;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年03月09日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupListActivity extends BaseCompatActivity<GroupListContract.Presenter> implements GroupListContract.View {

    private RecyclerView mRvGroup;
    private GroupAdapter mGroupAdapter;

    private List<GroupBean> mGroupList;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_group_list;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mRvGroup = findViewById(R.id.GroupListActivity_mRvGroup);
        mGroupList = new ArrayList<>(32);
        mGroupAdapter = new GroupAdapter(mGroupList);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        mRvGroup.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRvGroup.setAdapter(mGroupAdapter);
        mRvGroup.setHasFixedSize(true);
        mRvGroup.addItemDecoration(new DividerItemDecoration(1, ContextCompat.getColor(this, R.color.dividerColorBlack),DividerItemDecoration.ORIENTATION_HORIZONTAL));
        mRvGroup.addOnItemTouchListener(mOnRvGroupItemClickListener);
        mRvGroup.addOnScrollListener(new ImageAutoLoadScrollListener());

        mPresenter.loadAllGroup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_list, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.GroupListMenu_Create) {
            startActivity(new Intent(this, CreateGroupActivity.class));
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }


    private final OnRecyclerViewItemClickListener mOnRvGroupItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(final int position, RecyclerView.ViewHolder viewHolder) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(GroupListActivity.this, GroupProfileActivity.class);
                    intent.putExtra(GroupProfileActivity.INTENT_EXTRA_GROUP_ID, mGroupList.get(position).getGroupID());
                    startActivity(intent);
                }
            });
        }

        @Override
        public void onItemLongClick(int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY) {

        }

    };

    @Override
    public GroupListContract.Presenter getPresenter() {
        return new GroupListPresenter();
    }


    @Override
    public void showGroupList(DiffUtil.DiffResult diffResult, List<GroupBean> groupList) {
        diffResult.dispatchUpdatesTo(new BaseRecyclerViewAdapter.ListUpdateCallback(mGroupAdapter));
        mGroupList.clear();
        mGroupList.addAll(groupList);
    }

    @Override
    public void refreshGroupItem(GroupBean group) {
        int index = mGroupList.indexOf(group);
        if (index >= 0) {
            mGroupAdapter.notifyItemChangedEx(index);
            mGroupList.set(index, group);
        } else {
            LogUtil.e("refreshGroupItem fail from ui");
        }
    }

    @Override
    public void removeGroupItem(GroupBean group) {
        int index = mGroupList.indexOf(group);
        if (index >= 0) {
            mGroupAdapter.notifyItemRemovedEx(index);
            mGroupList.remove(index);
        } else {
            LogUtil.e("removeGroupItem fail from ui");
        }
    }
}
