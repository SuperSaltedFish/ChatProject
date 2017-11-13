package com.yzx.chat.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.yzx.chat.R;
import com.yzx.chat.contract.ConversationContract;
import com.yzx.chat.presenter.ConversationPresenter;
import com.yzx.chat.tool.AndroidTool;
import com.yzx.chat.view.activity.ChatActivity;
import com.yzx.chat.widget.adapter.ConversationAdapter;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.bean.ConversationBean;
import com.yzx.chat.widget.listener.AutoEnableOverScrollListener;
import com.yzx.chat.widget.listener.OnRecyclerViewClickListener;
import com.yzx.chat.widget.view.HomeOverflowPopupWindow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by YZX on 2017年06月03日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ConversationFragment extends BaseFragment<ConversationContract.Presenter> implements ConversationContract.View {

    private RecyclerView mRecyclerView;
    private SmartRefreshLayout mSmartRefreshLayout;
    private ConversationAdapter mAdapter;
    private Toolbar mToolbar;
    private HomeOverflowPopupWindow mOverflowPopupWindow;
    private AutoEnableOverScrollListener mAutoEnableOverScrollListener;
    private List<ConversationBean> mConversationList;

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
        mConversationList = new ArrayList<>(64);
        mAdapter = new ConversationAdapter(mConversationList);
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

    @Override
    public void onFirstVisible() {
        mPresenter.refreshAllConversation(mConversationList);
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

        @SuppressWarnings("unchecked")
        @Override
        public void onItemClick(int position, View itemView) {
            String conversationID = mConversationList.get(position).getConversationID();
            mPresenter.markConversationAsRead(conversationID);
            mPresenter.refreshAllConversation(mConversationList);
            Intent intent = new Intent(mContext, ChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra(ChatActivity.INTENT_CONVERSATION_ID,conversationID);
//            Activity activity = (Activity) mContext;
//            ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                    activity,
//                    new Pair<>(activity.findViewById(R.id.HomeActivity_mBottomNavigationView), ChatActivity.SHARED_ELEMENTS_BOTTOM_LAYOUT),
//                    new Pair<>(itemView, ChatActivity.SHARED_ELEMENTS_CONTENT));
//            ActivityCompat.startActivity(mContext, intent, activityOptions.toBundle());
            mContext.startActivity(intent);
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
                viewHolder.itemView.setTranslationZ(AndroidTool.dip2px(4));
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

    @Override
    public ConversationContract.Presenter getPresenter() {
        return new ConversationPresenter();
    }


    @Override
    public List<ConversationBean> getOldConversationList() {
        return mConversationList;
    }

    @Override
    public void updateListView(DiffUtil.DiffResult diffResult, List<ConversationBean> newConversationList) {
        diffResult.dispatchUpdatesTo(mAdapter);
        mConversationList.clear();
        mConversationList.addAll(newConversationList);
    }
}
