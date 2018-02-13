package com.yzx.chat.view.activity;


import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.contract.FindNewContactContract;
import com.yzx.chat.presenter.FindNewContactPresenter;
import com.yzx.chat.tool.IdentityManager;
import com.yzx.chat.widget.adapter.MaybeKnowAdapter;

import java.util.Locale;

public class FindNewContactActivity extends BaseCompatActivity<FindNewContactContract.Presenter> implements FindNewContactContract.View {

    private ConstraintLayout mClScanLayout;
    private SmartRefreshLayout mSmartRefreshLayout;
    private EditText mEtSearch;
    private TextView mTvSearchHint;
    private ProgressBar mPbSearch;
    private RecyclerView mRecyclerView;
    private LinearLayout mLlSearchHintLayout;
    private TextView mTvMyPhoneNumber;
    private MaybeKnowAdapter mAdapter;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_find_new_contact;
    }

    protected void init() {
        mClScanLayout = findViewById(R.id.FindNewContactActivity_mClScanLayout);
        mRecyclerView = findViewById(R.id.FindNewContactActivity_mRecyclerView);
        mEtSearch = findViewById(R.id.FindNewContactActivity_mEtSearch);
        mSmartRefreshLayout = findViewById(R.id.FindNewContactActivity_mSmartRefreshLayout);
        mLlSearchHintLayout = findViewById(R.id.FindNewContactActivity_mLlSearchHintLayout);
        mTvMyPhoneNumber = findViewById(R.id.FindNewContactActivity_mTvMyPhoneNumber);
        mTvSearchHint = findViewById(R.id.FindNewContactActivity_mTvSearchHint);
        mPbSearch = findViewById(R.id.FindNewContactActivity_mPbSearch);
        mAdapter = new MaybeKnowAdapter();
    }

    @Override
    protected void setup() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);

        mClScanLayout.setOnClickListener(mOnClScanLayoutClickListener);

        mEtSearch.setOnEditorActionListener(mOnEditorActionListener);

        setData();
    }

    private void setData() {
        UserBean user = IdentityManager.getInstance().getUser();
        mTvMyPhoneNumber.setText(String.format(Locale.getDefault(), "%s:%s", getString(R.string.FindNewContactActivity_MyPhoneNumber), user.getTelephone()));
    }

    private void enableSearchHint(boolean isEnable) {
        if (isEnable) {
            mSmartRefreshLayout.animate().translationY(mLlSearchHintLayout.getHeight()).start();
            mLlSearchHintLayout.setVisibility(View.VISIBLE);
        } else {
            mSmartRefreshLayout.animate().translationY(0).start();
            mLlSearchHintLayout.setVisibility(View.INVISIBLE);
        }
    }

    private final TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction())) {
                hideSoftKeyboard();
                String searchText = mEtSearch.getText().toString();
                if (!TextUtils.isEmpty(searchText)) {
                    mPresenter.searchUser(searchText);
                    enableSearchHint(true);
                    mTvSearchHint.setText(R.string.FindNewContactActivity_SearchProcess);
                    mPbSearch.setVisibility(View.VISIBLE);
                }
            }
            return true;
        }
    };

    private final View.OnClickListener mOnClScanLayoutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    @Override
    public FindNewContactContract.Presenter getPresenter() {
        return new FindNewContactPresenter();
    }

    @Override
    public void searchSuccess(UserBean user) {
        enableSearchHint(false);
        Intent intent = new Intent(this, StrangerProfileActivity.class);
        intent.putExtra(StrangerProfileActivity.INTENT_EXTRA_USER, user);
        startActivity(intent);
    }

    @Override
    public void searchNotExist() {
        mTvSearchHint.setText(R.string.FindNewContactActivity_SearchNotExist);
        mPbSearch.setVisibility(View.GONE);
    }

    @Override
    public void searchFail() {
        mTvSearchHint.setText(R.string.FindNewContactActivity_SearchFail);
        mPbSearch.setVisibility(View.GONE);
    }
}
