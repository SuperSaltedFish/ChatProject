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
import com.yzx.chat.bean.ContactOperationBean;
import com.yzx.chat.contract.ContactOperationContract;
import com.yzx.chat.presenter.ContactOperationPresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.widget.adapter.ContactOperationAdapter;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.DividerItemDecoration;
import com.yzx.chat.widget.view.OverflowMenuShowHelper;
import com.yzx.chat.widget.view.OverflowPopupMenu;
import com.yzx.chat.widget.view.ProgressDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年01月18日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class ContactOperationActivity extends BaseCompatActivity<ContactOperationContract.Presenter> implements ContactOperationContract.View {


    private RecyclerView mRecyclerView;
    private Button mBtnFindNewContact;
    private View mFooterView;
    private TextView mTvLoadMoreHint;
    private OverflowPopupMenu mContactOperationMenu;
    private ProgressDialog mProgressDialog;
    private ContactOperationAdapter mAdapter;
    private List<ContactOperationBean> mContactOperationList;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_contact_message;
    }


    protected void init() {
        mBtnFindNewContact = findViewById(R.id.ContactOperationActivity_mBtnFindNewContact);
        mRecyclerView = findViewById(R.id.ContactOperationActivity_mRecyclerView);
        mFooterView = getLayoutInflater().inflate(R.layout.view_load_more, (ViewGroup) getWindow().getDecorView(), false);
        mTvLoadMoreHint = mFooterView.findViewById(R.id.LoadMoreView_mTvLoadMoreHint);
        mProgressDialog = new ProgressDialog(this, getString(R.string.ContactOperationActivity_ProgressHint));
        mContactOperationMenu = new OverflowPopupMenu(this);
        mContactOperationList = new ArrayList<>(32);
        mAdapter = new ContactOperationAdapter(mContactOperationList);
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

        mAdapter.setOnAcceptContactRequestListener(mOnAcceptContactRequestListener);

        mTvLoadMoreHint.setText(R.string.LoadMoreHint_Default);

        setOverflowMenu();

        setData();
    }

    private void setOverflowMenu() {
        mContactOperationMenu.setWidth((int) AndroidUtil.dip2px(128));
        mContactOperationMenu.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.theme_background_color_white)));
        mContactOperationMenu.setElevation(AndroidUtil.dip2px(2));
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
            mAdapter.addFooterView(mFooterView);
        } else {
            mAdapter.addFooterView(null);
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

    private final ContactOperationAdapter.OnAcceptContactRequestListener mOnAcceptContactRequestListener = new ContactOperationAdapter.OnAcceptContactRequestListener() {
        @Override
        public void onAcceptContactRequest(int position) {
            mPresenter.acceptContactRequest(mContactOperationList.get(position));
        }
    };


    private final View.OnClickListener mOnBtnAddNewContactClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(ContactOperationActivity.this, FindNewContactActivity.class));
        }
    };

    @Override
    public ContactOperationContract.Presenter getPresenter() {
        return new ContactOperationPresenter();
    }

    @Override
    public void addContactOperationToList(ContactOperationBean ContactOperation) {
        mAdapter.notifyItemInsertedEx(0);
        mContactOperationList.add(0, ContactOperation);
        enableLoadMoreHint(mContactOperationList.size() > 12);
    }

    @Override
    public void removeContactOperationFromList(ContactOperationBean ContactOperation) {
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
    public void updateContactOperationFromList(ContactOperationBean ContactOperation) {
        int index = mContactOperationList.indexOf(ContactOperation);
        if (index < 0) {
            LogUtil.e("update fail from ContactOperationList");
        } else {
            mAdapter.notifyItemChangedEx(index);
            mContactOperationList.set(index, ContactOperation);
        }
    }

    @Override
    public void updateAllContactOperationList(DiffUtil.DiffResult diffResult, List<ContactOperationBean> newDataList) {
        diffResult.dispatchUpdatesTo(new BaseRecyclerViewAdapter.ListUpdateCallback(mAdapter));
        mContactOperationList.clear();
        mContactOperationList.addAll(newDataList);
        enableLoadMoreHint(mContactOperationList.size() > 12);
    }

    @Override
    public void enableProgressDialog(boolean isEnable) {
        if (isEnable) {
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        } else {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void showError(String error) {
        showToast(error);
    }
}
