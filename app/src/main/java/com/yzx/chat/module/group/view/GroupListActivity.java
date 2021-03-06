package com.yzx.chat.module.group.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.module.group.contract.GroupListContract;
import com.yzx.chat.module.group.presenter.GroupListPresenter;
import com.yzx.chat.widget.adapter.GroupAdapter;
import com.yzx.chat.widget.listener.ImageAutoLoadScrollListener;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by YZX on 2018年03月09日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupListActivity extends BaseCompatActivity<GroupListContract.Presenter> implements GroupListContract.View {

    private RecyclerView mRvGroup;
    private GroupAdapter mGroupAdapter;

    private List<GroupEntity> mGroupList;

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
        setTitle(R.string.GroupListActivity_Title);
        setDisplayHomeAsUpEnabled(true);


        mRvGroup.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRvGroup.setAdapter(mGroupAdapter);
        mRvGroup.setHasFixedSize(true);
        mRvGroup.addItemDecoration(new DividerItemDecoration(1, ContextCompat.getColor(this, R.color.dividerColor), DividerItemDecoration.HORIZONTAL));
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
        public void onItemClick(final int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY) {
            Intent intent = new Intent(GroupListActivity.this, GroupProfileActivity.class);
            intent.putExtra(GroupProfileActivity.INTENT_EXTRA_GROUP_ID, mGroupList.get(position).getGroupID());
            startActivity(intent);
        }
    };

    @Override
    public GroupListContract.Presenter getPresenter() {
        return new GroupListPresenter();
    }

    @Override
    public void showAllGroupList(List<GroupEntity> groupList) {
        mGroupList.clear();
        mGroupList.addAll(groupList);
        mGroupAdapter.notifyDataSetChanged();
    }

    @Override
    public void showNewGroup(GroupEntity group, int position) {
        mGroupList.add(position, group);
        mGroupAdapter.notifyItemInsertedEx(position);
    }

    @Override
    public void hideGroup(int position) {
        mGroupList.remove(position);
        mGroupAdapter.notifyItemRemovedEx(position);
    }

    @Override
    public void refreshGroup(GroupEntity group, int position) {
        mGroupList.set(position, group);
        mGroupAdapter.notifyItemChangedEx(position);
    }
}
