package com.yzx.chat.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.widget.adapter.GroupAdapter;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年03月09日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupListActivity extends BaseCompatActivity {

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
        mRvGroup.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRvGroup.setAdapter(mGroupAdapter);
        mRvGroup.setHasFixedSize(true);
        mRvGroup.addItemDecoration(new DividerItemDecoration(1, ContextCompat.getColor(this, R.color.divider_color_black)));
        mRvGroup.addOnItemTouchListener(mOnRvGroupItemClickListener);
    }


    private final OnRecyclerViewItemClickListener mOnRvGroupItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(final int position, RecyclerView.ViewHolder viewHolder) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        @Override
        public void onItemLongClick(int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY) {

        }

    };
}
