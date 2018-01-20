package com.yzx.chat.view.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.bean.ContactMessageBean;
import com.yzx.chat.contract.ContactMessageContract;
import com.yzx.chat.presenter.ContactMessagePresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.widget.adapter.ContactMessageAdapter;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.DividerItemDecoration;
import com.yzx.chat.widget.view.OverflowMenuShowHelper;
import com.yzx.chat.widget.view.OverflowPopupMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年01月18日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class ContactMessageActivity extends BaseCompatActivity<ContactMessageContract.Presenter> implements ContactMessageContract.View {

    private RecyclerView mRecyclerView;
    private Button mBtnFindNewContact;
    private View mFooterView;
    private TextView mTvLoadMoreHint;
    private OverflowPopupMenu mContactMessageMenu;
    private ContactMessageAdapter mAdapter;
    private List<ContactMessageBean> mContactMessageList;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_contact_message;
    }


    protected void init() {
        mBtnFindNewContact = findViewById(R.id.ContactMessageActivity_mBtnFindNewContact);
        mRecyclerView = findViewById(R.id.ContactMessageActivity_mRecyclerView);
        mFooterView = getLayoutInflater().inflate(R.layout.view_load_more, (ViewGroup) getWindow().getDecorView(), false);
        mTvLoadMoreHint = mFooterView.findViewById(R.id.LoadMoreView_mTvLoadMoreHint);
        mContactMessageMenu = new OverflowPopupMenu(this);
        mContactMessageList = new ArrayList<>(32);
        mAdapter = new ContactMessageAdapter(mContactMessageList);
    }

    @Override
    protected void setup() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(1, ContextCompat.getColor(this, R.color.divider_color_black)));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnItemTouchListener(mOnRecyclerViewItemClickListener);

        mBtnFindNewContact.setOnClickListener(mOnBtnAddNewContactClickListener);

        mAdapter.setScrollToBottomListener(mOnScrollToBottomListener);

        mPresenter.loadMoreContactMessage(Integer.MAX_VALUE);

        setOverflowMenu();
    }

    private void setOverflowMenu() {
        mContactMessageMenu.setWidth((int) AndroidUtil.dip2px(128));
        mContactMessageMenu.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.theme_background_color)));
        mContactMessageMenu.setElevation(AndroidUtil.dip2px(2));
        mContactMessageMenu.inflate(R.menu.menu_contact_message_overflow);
        mContactMessageMenu.setOnMenuItemClickListener(new OverflowPopupMenu.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, int menuID) {
                int index = (int) mRecyclerView.getTag();
                if (menuID == R.id.ContactMessageMenu_Delete) {
                    mPresenter.removeContactMessage(mContactMessageList.get(index));
                }
            }
        });
    }

    private final OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {

        }

        @Override
        public void onItemLongClick(int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY) {
            mRecyclerView.setTag(position);
            OverflowMenuShowHelper.show(viewHolder.itemView, mContactMessageMenu, mRecyclerView.getHeight(), (int) touchX, (int) touchY);
        }
    };

    private final BaseRecyclerViewAdapter.OnScrollToBottomListener mOnScrollToBottomListener = new BaseRecyclerViewAdapter.OnScrollToBottomListener() {
        @Override
        public void OnScrollToBottom() {
            if (mPresenter.isLoadingMore()) {
                return;
            }
            if (mPresenter.hasMoreMessage()) {
                mTvLoadMoreHint.setText(getString(R.string.LoadMoreHint_Loading));
                mPresenter.loadMoreContactMessage(mContactMessageList.get(mContactMessageList.size() - 1).getIndexID());
            } else {
                mTvLoadMoreHint.setText(getString(R.string.LoadMoreHint_NoMore));
            }
        }
    };

    private final View.OnClickListener mOnBtnAddNewContactClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(ContactMessageActivity.this, FindNewContactActivity.class));
        }
    };

    @Override
    public ContactMessageContract.Presenter getPresenter() {
        return new ContactMessagePresenter();
    }

    @Override
    public void addContactMessageToList(ContactMessageBean contactMessage) {
        mAdapter.notifyItemInsertedEx(0);
        mContactMessageList.add(0, contactMessage);
    }

    @Override
    public void removeContactMessageFromList(ContactMessageBean contactMessage) {
        int removePosition = mContactMessageList.indexOf(contactMessage);
        if (removePosition >= 0) {
            mAdapter.notifyItemRemovedEx(removePosition);
            mContactMessageList.remove(removePosition);
        } else {
            LogUtil.e("remove contactMessageItem fail in ui");
        }
    }

    @Override
    public void updateAllContactMessageList(DiffUtil.DiffResult diffResult, List<ContactMessageBean> newDataList) {
        diffResult.dispatchUpdatesTo(new BaseRecyclerViewAdapter.ListUpdateCallback(mAdapter));
        mContactMessageList.clear();
        mContactMessageList.addAll(newDataList);
    }

    @Override
    public void addMoreContactMessageToList(List<ContactMessageBean> contactMessageList, boolean isHasMore) {
        if (contactMessageList != null && contactMessageList.size() != 0) {
            mAdapter.notifyItemRangeInsertedEx(mContactMessageList.size(), contactMessageList.size());
            mContactMessageList.addAll(contactMessageList);
        }
        if (!isHasMore) {
            mTvLoadMoreHint.setText(getString(R.string.LoadMoreHint_NoMore));
        } else if (contactMessageList == null) {
            mTvLoadMoreHint.setText(getString(R.string.LoadMoreHint_LoadFail));
        }
    }

    @Override
    public void enableLoadMoreHint(boolean isEnable) {
        if (isEnable) {
            mAdapter.addFooterView(mFooterView);
        } else {
            mAdapter.addFooterView(null);
        }
    }
}
