package com.yzx.chat.module.contact.view;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.ContactOperationEntity;
import com.yzx.chat.module.contact.contract.ContactOperationContract;
import com.yzx.chat.module.contact.presenter.ContactOperationPresenter;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.widget.adapter.ContactOperationAdapter;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.DividerItemDecoration;
import com.yzx.chat.widget.view.OverflowMenuShowHelper;
import com.yzx.chat.widget.view.OverflowPopupMenu;

import java.util.List;
import java.util.Objects;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by YZX on 2018年01月18日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class ContactOperationFragment extends BaseFragment<ContactOperationContract.Presenter> implements ContactOperationContract.View {

    private RecyclerView mRecyclerView;
    private OverflowPopupMenu mContactOperationMenu;
    private ContactOperationAdapter mAdapter;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_contact_operation;
    }

    @Override
    protected void init(View parentView) {
        mRecyclerView = parentView.findViewById(R.id.ContactOperationFragment_mRecyclerView);
        mContactOperationMenu = new OverflowPopupMenu(mContext);
        mAdapter = new ContactOperationAdapter();
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
        ((DefaultItemAnimator) (Objects.requireNonNull(mRecyclerView.getItemAnimator()))).setSupportsChangeAnimations(false);

        mAdapter.setOnContactRequestListener(mOnContactRequestListener);

        setOverflowMenu();

        mPresenter.loadAllAndMakeAllAsRead();
    }


    private void setOverflowMenu() {
        mContactOperationMenu.setWidth((int) AndroidHelper.dip2px(128));
        mContactOperationMenu.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(mContext, R.color.backgroundColorWhite)));
        mContactOperationMenu.setElevation(AndroidHelper.dip2px(2));
        mContactOperationMenu.inflate(R.menu.menu_contact_message_overflow);
        mContactOperationMenu.setOnMenuItemClickListener(new OverflowPopupMenu.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, int menuID) {
                ContactOperationEntity contactOperation = (ContactOperationEntity) mRecyclerView.getTag();
                if (menuID == R.id.ContactOperationMenu_Delete) {
                    mPresenter.removeContactOperation(contactOperation);
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
            mRecyclerView.setTag(mAdapter.getItem(position));
            OverflowMenuShowHelper.show(viewHolder.itemView, mContactOperationMenu, mRecyclerView.getHeight(), (int) touchX, (int) touchY);
        }
    };

    private final ContactOperationAdapter.OnContactRequestListener mOnContactRequestListener = new ContactOperationAdapter.OnContactRequestListener() {
        @Override
        public void onAcceptRequest(int position) {
            mPresenter.acceptContactRequest(mAdapter.getItem(position));
        }

        @Override
        public void onRefusedRequest(int position) {
            mPresenter.refusedContactRequest(mAdapter.getItem(position));
        }

        @Override
        public void enterDetails(int position) {
            ContactOperationEntity contactOperation = mAdapter.getItem(position);
            ContactEntity contact = mPresenter.findContact(contactOperation.getUserInfo().getUserID());
            Intent intent;
            if (contact == null) {
                intent = new Intent(mContext, StrangerProfileActivity.class);
                intent.putExtra(StrangerProfileActivity.INTENT_EXTRA_CONTENT_OPERATION, contactOperation);
                startActivity(intent);
            } else {
                ContactProfileActivity.startActivity(mContext,contact.getContactID());
            }
        }
    };

    @Override
    public ContactOperationContract.Presenter getPresenter() {
        return new ContactOperationPresenter();
    }

    @Override
    public void showContactOperation(List<ContactOperationEntity> contactOperationList) {
        mAdapter.submitList(contactOperationList);
    }
}
