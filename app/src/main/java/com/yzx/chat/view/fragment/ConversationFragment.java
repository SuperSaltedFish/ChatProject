package com.yzx.chat.view.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.yzx.chat.R;
import com.yzx.chat.view.activity.HomeActivity;
import com.yzx.chat.widget.adapter.ConversationAdapter;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.bean.ConversationBean;
import com.yzx.chat.widget.listener.AutoEnableOverScrollListener;
import com.yzx.chat.widget.listener.OnRecyclerViewClickListener;
import com.yzx.chat.test.ConversationTestData;
import com.yzx.chat.util.DensityUtil;
import com.yzx.chat.widget.view.HomeOverflowPopupWindow;

import java.util.Collections;
import java.util.List;

/**
 * Created by YZX on 2017年06月03日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ConversationFragment extends BaseFragment {

    private RecyclerView mRecyclerView;
    private SmartRefreshLayout mSmartRefreshLayout;
    private ConversationAdapter mAdapter;
    private Toolbar mToolbar;
    private HomeOverflowPopupWindow mOverflowPopupWindow;
    private AutoEnableOverScrollListener mAutoEnableOverScrollListener;
    private List<ConversationBean> mConversationList = ConversationTestData.getTestData();

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_conversation;
    }

    @Override
    protected void init(View parentView) {
        mRecyclerView = (RecyclerView) parentView.findViewById(R.id.ConversationFragment_mRecyclerView);
        mToolbar = (Toolbar) parentView.findViewById(R.id.ConversationFragment_mToolbar);
        mSmartRefreshLayout = (SmartRefreshLayout) parentView.findViewById(R.id.ConversationFragment_mSmartRefreshLayout);
        mOverflowPopupWindow = new HomeOverflowPopupWindow(mContext, mToolbar, R.menu.menu_home_overflow);
        mAdapter = new ConversationAdapter(mContext, mConversationList);
        mAutoEnableOverScrollListener = new AutoEnableOverScrollListener(mSmartRefreshLayout);
    }

    @Override
    protected void setView() {
        mToolbar.setTitle("微信");
        mToolbar.inflateMenu(R.menu.menu_home_top);
        mToolbar.setOnMenuItemClickListener(onOptionsItemSelectedListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnItemTouchListener(mOnRecyclerViewClickListener);
        mRecyclerView.addOnScrollListener(mAutoEnableOverScrollListener);
        new ItemTouchHelper(mItemTouchCallback).attachToRecyclerView(mRecyclerView);

    }


    private final Toolbar.OnMenuItemClickListener onOptionsItemSelectedListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.HomeMenu_more:
                    mOverflowPopupWindow.show();
                    break;
                default:
                    return false;
            }
            return true;
        }
    };

    private final OnRecyclerViewClickListener mOnRecyclerViewClickListener = new OnRecyclerViewClickListener() {

        @Override
        public void onItemClick(int position, View itemView) {
            requestActivity(HomeActivity.FRAGMENT_REQUEST_START_ACTIVITY, itemView);
        }

        @Override
        public void onItemLongClick(int position, View itemView) {

        }
    };

    private final ItemTouchHelper.Callback mItemTouchCallback = new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mConversationList, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mConversationList, i, i - 1);
                }
            }
            mAdapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        }


        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                viewHolder.itemView.setBackground(new ColorDrawable(Color.WHITE));
                viewHolder.itemView.setTranslationZ(DensityUtil.dip2px(4));
                mAutoEnableOverScrollListener.setEnableOverScroll(false);
            }
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setBackground(null);
            viewHolder.itemView.setTranslationZ(0);
            mAutoEnableOverScrollListener.setEnableOverScroll(true);
        }

    };

}
