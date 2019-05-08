package com.yzx.chat.module.conversation.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.module.conversation.contract.ConversationContract;
import com.yzx.chat.module.conversation.presenter.ConversationPresenter;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.widget.adapter.ConversationAdapter;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.DividerItemDecoration;
import com.yzx.chat.widget.view.OverflowMenuShowHelper;
import com.yzx.chat.widget.view.OverflowPopupMenu;

import java.util.List;
import java.util.Objects;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.rong.imlib.model.Conversation;

/**
 * Created by YZX on 2017年06月03日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ConversationFragment extends BaseFragment<ConversationContract.Presenter> implements ConversationContract.View {

    public static final String TAG = ConversationFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private ImageView mIvEmptyHintImage;
    private TextView mITvEmptyHintText;
    private ConversationAdapter mAdapter;
    private Toolbar mToolbar;
    private View mHeaderView;
    private OverflowPopupMenu mConversationMenu;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_conversation;
    }

    @Override
    protected void init(View parentView) {
        mRecyclerView = parentView.findViewById(R.id.mRecyclerView);
        mToolbar = parentView.findViewById(R.id.Default_mToolbar);
        mIvEmptyHintImage = parentView.findViewById(R.id.mIvEmptyHintImage);
        mITvEmptyHintText = parentView.findViewById(R.id.mITvEmptyHintText);
        mHeaderView = LayoutInflater.from(mContext).inflate(R.layout.item_conversation_header, (ViewGroup) parentView, false);
        mConversationMenu = new OverflowPopupMenu(mContext);
        mAdapter = new ConversationAdapter();
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        mToolbar.setTitle(R.string.app_name);
        mToolbar.setBackground(null);
        mToolbar.setElevation(0);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(mContext, R.anim.layout_alpha));
        mRecyclerView.addItemDecoration(new DividerItemDecoration((int) AndroidHelper.dip2px(1), ContextCompat.getColor(mContext, R.color.dividerColor), DividerItemDecoration.HORIZONTAL));
        mRecyclerView.addOnItemTouchListener(mOnRecyclerViewItemClickListener);
        ((DefaultItemAnimator) (Objects.requireNonNull(mRecyclerView.getItemAnimator()))).setSupportsChangeAnimations(false);

        setOverflowMenu();
        setEnableDisconnectionHint(!mPresenter.isConnectedToServer());
    }

    private void setOverflowMenu() {
        mConversationMenu.setWidth((int) AndroidHelper.dip2px(152));
        mConversationMenu.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mConversationMenu.setTitleTextColor(ContextCompat.getColor(mContext, R.color.textColorPrimary));
        mConversationMenu.setElevation(AndroidHelper.dip2px(4));
        mConversationMenu.inflate(R.menu.menu_conversation_overflow);
        mConversationMenu.setOnMenuItemClickListener(new OverflowPopupMenu.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, int menuID) {
                Conversation conversation = (Conversation) mRecyclerView.getTag();
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

    private void setEnableEmptyListHint(boolean isEnable) {
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
        public void onItemClick(final int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY) {
            Conversation conversation;
            if (!mAdapter.isHasHeaderView()) {
                conversation = mAdapter.getItem(position);
            } else if (position != 0) {
                conversation = mAdapter.getItem(position - 1);
            } else return;
            switch (conversation.getConversationType()) {
                case PRIVATE:
                    ChatActivity.startActivity(mContext, conversation.getTargetId(), Conversation.ConversationType.PRIVATE);
                    break;
                case GROUP:
                    ChatActivity.startActivity(mContext, conversation.getTargetId(), Conversation.ConversationType.GROUP);
                    break;
            }
        }

        @Override
        public void onItemLongClick(int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY) {
            Conversation conversation;
            if (!mAdapter.isHasHeaderView()) {
                conversation = mAdapter.getItem(position);
            } else if (position != 0) {
                conversation = mAdapter.getItem(position - 1);
            } else return;
            mRecyclerView.setTag(conversation);
            if (conversation.isTop()) {
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
    public void showConversationList(List<Conversation> conversationList) {
        setEnableEmptyListHint(conversationList.isEmpty());
        mAdapter.submitList(conversationList);
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
