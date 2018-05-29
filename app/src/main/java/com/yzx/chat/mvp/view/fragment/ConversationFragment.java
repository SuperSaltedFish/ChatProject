package com.yzx.chat.mvp.view.fragment;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.mvp.contract.ConversationContract;
import com.yzx.chat.mvp.presenter.ConversationPresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.mvp.view.activity.ChatActivity;
import com.yzx.chat.widget.adapter.ConversationAdapter;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.widget.listener.AutoEnableOverScrollListener;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.DividerItemDecoration;
import com.yzx.chat.widget.view.OverflowMenuShowHelper;
import com.yzx.chat.widget.view.OverflowPopupMenu;

import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.model.Conversation;

/**
 * Created by YZX on 2017年06月03日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ConversationFragment extends BaseFragment<ConversationContract.Presenter> implements ConversationContract.View {

    public static final String TAG = ConversationFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private SmartRefreshLayout mSmartRefreshLayout;
    private ImageView mIvEmptyHintImage;
    private TextView mITvEmptyHintText;
    private FrameLayout mFlToolbarLayout;
    private ConversationAdapter mAdapter;
    private Toolbar mToolbar;
    private View mHeaderView;
    private OverflowPopupMenu mConversationMenu;
    private AutoEnableOverScrollListener mAutoEnableOverScrollListener;
    private List<Conversation> mConversationList;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_conversation;
    }

    @Override
    protected void init(View parentView) {
        mRecyclerView = parentView.findViewById(R.id.ConversationFragment_mRecyclerView);
        mToolbar = parentView.findViewById(R.id.Default_mToolbar);
        mSmartRefreshLayout = parentView.findViewById(R.id.FindNewContactActivity_mSmartRefreshLayout);
        mIvEmptyHintImage = parentView.findViewById(R.id.ConversationFragment_mIvEmptyHintImage);
        mITvEmptyHintText = parentView.findViewById(R.id.ConversationFragment_mITvEmptyHintText);
        mFlToolbarLayout = parentView.findViewById(R.id.ConversationFragment_mFlToolbarLayout);
        mHeaderView = LayoutInflater.from(mContext).inflate(R.layout.item_conversation_header, (ViewGroup) parentView, false);
        mConversationMenu = new OverflowPopupMenu(mContext);
        mConversationList = new ArrayList<>(128);
        mAdapter = new ConversationAdapter(mConversationList);
        mAutoEnableOverScrollListener = new AutoEnableOverScrollListener(mSmartRefreshLayout);
    }

    @Override
    protected void setup() {
        mToolbar.setTitle(R.string.app_name);
//        mToolbar.inflateMenu(R.menu.menu_conversation_overflow);
//        mToolbar.setOnMenuItemClickListener(mOnOptionsItemSelectedListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(1, ContextCompat.getColor(mContext, R.color.dividerColorBlack),DividerItemDecoration.ORIENTATION_HORIZONTAL));
        mRecyclerView.addOnItemTouchListener(mOnRecyclerViewItemClickListener);
        mRecyclerView.addOnScrollListener(mAutoEnableOverScrollListener);
        ((DefaultItemAnimator) (mRecyclerView.getItemAnimator())).setSupportsChangeAnimations(false);

        setOverflowMenu();
        setEnableDisconnectionHint(!mPresenter.isConnectedToServer());
    }

    private void setOverflowMenu() {
        mConversationMenu.setWidth((int) AndroidUtil.dip2px(128));
        mConversationMenu.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(mContext, R.color.backgroundColorWhite)));
        mConversationMenu.setElevation(AndroidUtil.dip2px(2));
        mConversationMenu.inflate(R.menu.menu_conversation_overflow);
        mConversationMenu.setOnMenuItemClickListener(new OverflowPopupMenu.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, int menuID) {
                int index = (int) mRecyclerView.getTag();
                Conversation conversation = mConversationList.get(index);
                switch (menuID) {
                    case R.id.ConversationMenu_Top:
                        mPresenter.setConversationTop(conversation, !conversation.isTop());
                        break;
                    case R.id.ConversationMenu_Remove:
                        mPresenter.deleteConversation(conversation);
                        break;
                    case R.id.ConversationMenu_Clean:
                        mPresenter.clearConversationMessages(conversation);
                        break;
                }
            }
        });
    }

    @Override
    protected void onFirstVisible() {
        mPresenter.refreshAllConversations();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (mParentView != null) {
            mFlToolbarLayout.setFitsSystemWindows(!hidden);
            mFlToolbarLayout.requestApplyInsets();
        }
        super.onHiddenChanged(hidden);
    }

    private void enableEmptyListHint(boolean isEnable) {
        if (isEnable) {
            mIvEmptyHintImage.setVisibility(View.VISIBLE);
            mITvEmptyHintText.setVisibility(View.VISIBLE);
        } else {
            mIvEmptyHintImage.setVisibility(View.INVISIBLE);
            mITvEmptyHintText.setVisibility(View.INVISIBLE);
        }
    }

    private final OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(final int position, RecyclerView.ViewHolder viewHolder) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(mContext, ChatActivity.class);
                    if (!mAdapter.isHasHeaderView()) {
                        intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION, mConversationList.get(position));
                    } else if (position != 0) {
                        intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION, mConversationList.get(position - 1));
                    } else return;
                    startActivity(intent);
                }
            });
        }

        @Override
        public void onItemLongClick(int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY) {
            if (mAdapter.isHasHeaderView()) {
                position--;
            }
            mRecyclerView.setTag(position);
            if (mConversationList.get(position).isTop()) {
                mConversationMenu.findMenuById(R.id.ConversationMenu_Top).setTitle(R.string.ConversationMenu_CancelTop);
            } else {
                mConversationMenu.findMenuById(R.id.ConversationMenu_Top).setTitle(R.string.ConversationMenu_Top);
            }

            OverflowMenuShowHelper.show(viewHolder.itemView, mConversationMenu, mRecyclerView.getHeight(), (int) touchX, (int) touchY);
        }

    };

    @Override
    public ConversationContract.Presenter getPresenter() {
        return new ConversationPresenter();
    }


    @Override
    public void updateConversationsFromUI(DiffUtil.DiffResult diffResult, List<Conversation> newConversationList) {
        diffResult.dispatchUpdatesTo(new BaseRecyclerViewAdapter.ListUpdateCallback(mAdapter));
        mConversationList.clear();
        mConversationList.addAll(newConversationList);
        enableEmptyListHint(mConversationList.size() == 0);
        mConversationMenu.dismiss();
    }


    @Override
    public void removeConversationFromUI(Conversation conversation) {
        for (int i = 0, size = mConversationList.size(); i < size; i++) {
            if (mConversationList.get(i).getTargetId().equals(conversation.getTargetId())) {
                mConversationList.remove(i);
                mAdapter.notifyItemRemoved(i);
                break;
            }
        }
        enableEmptyListHint(mConversationList.size() == 0);
    }

    @Override
    public void setEnableDisconnectionHint(boolean isEnable) {
        if (mAdapter == null) {
            return;
        }
        if (isEnable) {
            mAdapter.setHeaderView(mHeaderView);
        } else {
            mAdapter.setHeaderView(null);
        }
    }


}
