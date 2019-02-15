package com.yzx.chat.module.contact.view;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.ContactOperationEntity;
import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.module.contact.contract.ContactOperationContract;
import com.yzx.chat.module.contact.presenter.ContactOperationPresenter;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.widget.adapter.ContactOperationAdapter;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.DividerItemDecoration;
import com.yzx.chat.widget.view.OverflowMenuShowHelper;
import com.yzx.chat.widget.view.OverflowPopupMenu;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by YZX on 2018年01月18日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class ContactOperationFragment extends BaseFragment<ContactOperationContract.Presenter> implements ContactOperationContract.View {

    private RecyclerView mRecyclerView;
    private View mFooterView;
    private TextView mTvLoadMoreHint;
    private OverflowPopupMenu mContactOperationMenu;
    private ContactOperationAdapter mAdapter;
    private List<ContactOperationEntity> mContactOperationList;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_contact_operation;
    }

    @Override
    protected void init(View parentView) {
        mRecyclerView = parentView.findViewById(R.id.ContactOperationFragment_mRecyclerView);
        mFooterView = getLayoutInflater().inflate(R.layout.view_load_more, (ViewGroup) parentView, false);
        mTvLoadMoreHint = mFooterView.findViewById(R.id.LoadMoreView_mTvLoadMoreHint);
        mContactOperationMenu = new OverflowPopupMenu(mContext);
        mContactOperationList = new ArrayList<>(32);
        mAdapter = new ContactOperationAdapter(mContactOperationList);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(1, ContextCompat.getColor(mContext, R.color.dividerColorBlack), DividerItemDecoration.HORIZONTAL));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnItemTouchListener(mOnRecyclerViewItemClickListener);
        ((DefaultItemAnimator) (mRecyclerView.getItemAnimator())).setSupportsChangeAnimations(false);

        mAdapter.setOnContactRequestListener(mOnContactRequestListener);

        mTvLoadMoreHint.setText(R.string.LoadMoreHint_Default);

        setOverflowMenu();

        setData();
    }


    private void setOverflowMenu() {
        mContactOperationMenu.setWidth((int) AndroidHelper.dip2px(128));
        mContactOperationMenu.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(mContext, R.color.backgroundColorWhite)));
        mContactOperationMenu.setElevation(AndroidHelper.dip2px(2));
        mContactOperationMenu.inflate(R.menu.menu_contact_message_overflow);
        mContactOperationMenu.setOnMenuItemClickListener(new OverflowPopupMenu.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, int menuID) {
                int index = (int) mRecyclerView.getTag();
                if (menuID == R.id.ContactOperationMenu_Delete) {
                    mPresenter.removeContactOperation(mContactOperationList.get(index));
                }
            }
        });
    }

    private void setData() {
        mPresenter.init();
    }

    public void enableLoadMoreHint(boolean isEnable) {
        if (isEnable) {
            mAdapter.setFooterView(mFooterView);
        } else {
            mAdapter.setFooterView(null);
        }
    }


    private final OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {

        }

        @Override
        public void onItemLongClick(int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY) {
            mRecyclerView.setTag(position);
            OverflowMenuShowHelper.show(viewHolder.itemView, mContactOperationMenu, mRecyclerView.getHeight(), (int) touchX, (int) touchY);
        }
    };

    private final ContactOperationAdapter.OnContactRequestListener mOnContactRequestListener = new ContactOperationAdapter.OnContactRequestListener() {
        @Override
        public void onAcceptRequest(int position) {
            mPresenter.acceptContactRequest(mContactOperationList.get(position));
        }

        @Override
        public void onRefusedRequest(int position) {
            mPresenter.refusedContactRequest(mContactOperationList.get(position));
        }

        @Override
        public void enterDetails(int position) {
            ContactOperationEntity contactOperation = mContactOperationList.get(position);
            ContactEntity contact = mPresenter.findContact(contactOperation.getUserID());
            Intent intent;
            if (contact == null) {
                intent = new Intent(mContext, StrangerProfileActivity.class);
                intent.putExtra(StrangerProfileActivity.INTENT_EXTRA_CONTENT_OPERATION, mContactOperationList.get(position));
            } else {
                intent = new Intent(mContext, ContactProfileActivity.class);
                intent.putExtra(ContactProfileActivity.INTENT_EXTRA_CONTACT_ID, contact.getUserProfile().getUserID());
            }
            startActivity(intent);
        }
    };

    @Override
    public ContactOperationContract.Presenter getPresenter() {
        return new ContactOperationPresenter();
    }

    @Override
    public void addContactOperationToList(ContactOperationEntity ContactOperation) {
        mAdapter.notifyItemInsertedEx(0);
        mContactOperationList.add(0, ContactOperation);
        enableLoadMoreHint(mContactOperationList.size() > 12);
    }

    @Override
    public void removeContactOperationFromList(ContactOperationEntity ContactOperation) {
        int removePosition = mContactOperationList.indexOf(ContactOperation);
        if (removePosition >= 0) {
            mAdapter.notifyItemRemovedEx(removePosition);
            mContactOperationList.remove(removePosition);
            enableLoadMoreHint(mContactOperationList.size() > 12);
        } else {
            LogUtil.e("remove ContactOperationItem fail in ui");
        }
    }

    @Override
    public void updateContactOperationFromList(ContactOperationEntity ContactOperation) {
        int index = mContactOperationList.indexOf(ContactOperation);
        if (index < 0) {
            LogUtil.e("update fail from ContactOperationList");
        } else {
            mAdapter.notifyItemChangedEx(index);
            mContactOperationList.set(index, ContactOperation);
        }
    }

    @Override
    public void updateAllContactOperationList(DiffUtil.DiffResult diffResult, List<ContactOperationEntity> newDataList) {
        diffResult.dispatchUpdatesTo(new BaseRecyclerViewAdapter.ListUpdateCallback(mAdapter));
        mContactOperationList.clear();
        mContactOperationList.addAll(newDataList);
        enableLoadMoreHint(mContactOperationList.size() > 12);
    }

}
