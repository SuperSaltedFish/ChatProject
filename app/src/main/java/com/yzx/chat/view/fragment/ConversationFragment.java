package com.yzx.chat.view.fragment;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.yzx.chat.R;
import com.yzx.chat.contract.ConversationContract;
import com.yzx.chat.presenter.ConversationPresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.view.activity.ChatActivity;
import com.yzx.chat.widget.adapter.ConversationAdapter;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.widget.listener.AutoEnableOverScrollListener;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
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
    private ConversationAdapter mAdapter;
    private Toolbar mToolbar;
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
        mToolbar = parentView.findViewById(R.id.ConversationFragment_mToolbar);
        mSmartRefreshLayout = parentView.findViewById(R.id.ConversationFragment_mSmartRefreshLayout);
        mIvEmptyHintImage = parentView.findViewById(R.id.ConversationFragment_mIvEmptyHintImage);
        mITvEmptyHintText = parentView.findViewById(R.id.ConversationFragment_mITvEmptyHintText);
        mConversationMenu = new OverflowPopupMenu(mContext);
        mConversationList = new ArrayList<>(128);
        mAdapter = new ConversationAdapter(mConversationList);
        mAutoEnableOverScrollListener = new AutoEnableOverScrollListener(mSmartRefreshLayout);
    }

    @Override
    protected void setView() {
        mToolbar.setTitle("微信");
        mToolbar.inflateMenu(R.menu.menu_conversation_overflow);
        mToolbar.setOnMenuItemClickListener(mOnOptionsItemSelectedListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnItemTouchListener(mOnRecyclerViewItemClickListener);
        mRecyclerView.addOnScrollListener(mAutoEnableOverScrollListener);

        setOverflowMenu();

    }

    private void setOverflowMenu() {
        mConversationMenu.setWidth((int) AndroidUtil.dip2px(128));
        mConversationMenu.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(mContext, R.color.theme_background_color)));
        mConversationMenu.setElevation(AndroidUtil.dip2px(2));
        mConversationMenu.inflate(R.menu.menu_conversation_overflow);
        mConversationMenu.setOnMenuItemClickListener(new OverflowPopupMenu.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, int menuID) {
                int index = (int) mRecyclerView.getTag();
                Conversation conversation = mConversationList.get(index);
                switch (menuID) {
                    case R.id.ConversationMenu_Top:
                        mPresenter.setConversationToTop(conversation, !conversation.isTop());
                        break;
                    case R.id.ConversationMenu_Remove:
                        mPresenter.removeConversation(conversation);
                        break;
                    case R.id.ConversationMenu_Clean:
                        mPresenter.clearChatMessages(conversation);
                        break;
                }
            }
        });
    }

    @Override
    protected void onFirstVisible() {
        mPresenter.refreshAllConversations();
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

    private final Toolbar.OnMenuItemClickListener mOnOptionsItemSelectedListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {

            }
            return true;
        }
    };

    private final OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(final int position, RecyclerView.ViewHolder viewHolder) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(mContext, ChatActivity.class);
                    intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION, mConversationList.get(position));
                    ActivityOptionsCompat compat = ActivityOptionsCompat.makeCustomAnimation(mContext, R.anim.avtivity_slide_in_right, R.anim.activity_slide_out_left);
                    startActivity(intent, compat.toBundle());
                }
            });
        }

        @Override
        public void onItemLongClick(int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY) {
            if (mConversationList.get(position).isTop()) {
                mConversationMenu.findMenuById(R.id.ConversationMenu_Top).setTitle(R.string.ConversationMenu_CancelTop);
            } else {
                mConversationMenu.findMenuById(R.id.ConversationMenu_Top).setTitle(R.string.ConversationMenu_Top);
            }
            View anchor = viewHolder.itemView;
            int menuWidth = mConversationMenu.getWidth();
            int menuHeight = mConversationMenu.getHeight();
            int offsetY = -(anchor.getHeight() - (int) touchY % viewHolder.itemView.getHeight());
            int offsetX;
            if (touchX > AndroidUtil.getScreenWidth() / 2) {
                offsetX = (int) touchX - menuWidth;
                if (mRecyclerView.getHeight() / 2 > touchY) {
                    mConversationMenu.setAnimationStyle(R.style.PopupMenuAnimation_Right_Top);
                } else {
                    offsetY -= menuHeight;
                    mConversationMenu.setAnimationStyle(R.style.PopupMenuAnimation_Right_Bottom);
                }
            } else {
                offsetX = (int) touchX;
                if (mRecyclerView.getHeight() / 2 > touchY) {
                    mConversationMenu.setAnimationStyle(R.style.PopupMenuAnimation_Left_Top);
                } else {
                    offsetY -= menuHeight;
                    mConversationMenu.setAnimationStyle(R.style.PopupMenuAnimation_Left_Bottom);
                }
            }
            mRecyclerView.setTag(position);
            mConversationMenu.showAsDropDown(anchor, offsetX, offsetY);
        }

    };

    @Override
    public ConversationContract.Presenter getPresenter() {
        return new ConversationPresenter();
    }


    @Override
    public void updateConversationListView(DiffUtil.DiffResult diffResult, List<Conversation> newConversationList) {
        diffResult.dispatchUpdatesTo(mAdapter);
        mConversationList.clear();
        mConversationList.addAll(newConversationList);
        enableEmptyListHint(mConversationList.size() == 0);
        mConversationMenu.dismiss();
    }


    @Override
    public void removeConversationItem(Conversation conversation) {
        for (int i = 0, size = mConversationList.size(); i < size; i++) {
            if (mConversationList.get(i).getTargetId().equals(conversation.getTargetId())) {
                mConversationList.remove(i);
                mAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }
}
