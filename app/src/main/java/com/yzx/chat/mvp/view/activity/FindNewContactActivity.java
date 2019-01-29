package com.yzx.chat.mvp.view.activity;


import android.Manifest;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.module.contact.view.ContactProfileActivity;
import com.yzx.chat.module.group.view.CreateGroupActivity;
import com.yzx.chat.mvp.contract.FindNewContactContract;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.mvp.presenter.FindNewContactPresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.adapter.MaybeKnowAdapter;
import com.yzx.chat.widget.listener.SimpleTextWatcher;
import com.yzx.chat.widget.view.SpacesItemDecoration;

import java.util.Locale;


public class FindNewContactActivity extends BaseCompatActivity<FindNewContactContract.Presenter> implements FindNewContactContract.View {

    private ConstraintLayout mClScan;

    private EditText mEtSearch;
    private RecyclerView mRecyclerView;
    private TextView mTvMyPhoneNumber;
    private ImageView mIvSearchIcon;
    private ImageView mIvEnterIcon;
    private ConstraintLayout mClCreateGroup;
    private MaybeKnowAdapter mAdapter;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_find_new_contact;
    }

    protected void init(Bundle savedInstanceState) {
        mClScan = findViewById(R.id.FindNewContactActivity_mClScan);
        mRecyclerView = findViewById(R.id.FindNewContactActivity_mRecyclerView);
        mEtSearch = findViewById(R.id.FindNewContactActivity_mEtSearch);
        mTvMyPhoneNumber = findViewById(R.id.FindNewContactActivity_mTvMyPhoneNumber);
        mClCreateGroup = findViewById(R.id.FindNewContactActivity_mClCreateGroup);
        mIvSearchIcon = findViewById(R.id.FindNewContactActivity_mIvSearchIcon);
        mIvEnterIcon = findViewById(R.id.FindNewContactActivity_mIvEnterIcon);
        mAdapter = new MaybeKnowAdapter();
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this,R.color.colorAccent)));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration((int) AndroidUtil.dip2px(8),SpacesItemDecoration.HORIZONTAL));

        mClScan.setOnClickListener(mOnScanLayoutClickListener);
        mClCreateGroup.setOnClickListener(mOnCreateGroupClickListener);
        mTvMyPhoneNumber.setOnClickListener(mOnMyPhoneClickListener);

        setInputListener();
        setData();
    }

    private void setInputListener() {
        mEtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction())) {
                    hideSoftKeyboard();
                    String searchText = mEtSearch.getText().toString();
                    if (!TextUtils.isEmpty(searchText)) {
                        mPresenter.searchUser(searchText);
                    }
                }
                return true;
            }
        });

        mEtSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mIvSearchIcon.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                } else {
                    mIvSearchIcon.setImageTintList(ColorStateList.valueOf(Color.argb(196,255,255,255)));
                }
            }

        });

        mEtSearch.addTextChangedListener(new SimpleTextWatcher(){
            @Override
            public void afterTextChanged(Editable s) {
                mIvEnterIcon.setVisibility(s.length()>0?View.VISIBLE:View.INVISIBLE);
            }
        });
    }

    private void setData() {
        UserBean user = IMClient.getInstance().getUserManager().getUser();
        mTvMyPhoneNumber.setText(String.format(Locale.getDefault(), "%s:%s", getString(R.string.FindNewContactActivity_MyPhoneNumber), user.getTelephone()));
    }

    @Override
    protected void onRequestPermissionsResult(int requestCode, boolean isSuccess, String[] deniedPermissions) {
        if(isSuccess){
            Intent intent = new Intent(FindNewContactActivity.this, QrCodeScanActivity.class);
            startActivity(intent);
        }
    }

    private final View.OnClickListener mOnMyPhoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(FindNewContactActivity.this, MyQRCodeActivity.class);
            intent.putExtra(MyQRCodeActivity.INTENT_EXTRA_QR_TYPE, MyQRCodeActivity.QR_CODE_TYPE_USER);
            startActivity(intent);
        }
    };

    private final View.OnClickListener mOnCreateGroupClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(FindNewContactActivity.this, CreateGroupActivity.class));
        }
    };

    private final View.OnClickListener mOnScanLayoutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            requestPermissionsInCompatMode(new String[]{Manifest.permission.CAMERA},0);
        }
    };

    @Override
    public FindNewContactContract.Presenter getPresenter() {
        return new FindNewContactPresenter();
    }

    @Override
    public void searchSuccess(UserBean user, boolean isContact) {
        if (isContact) {
            Intent intent = new Intent(this, ContactProfileActivity.class);
            intent.putExtra(ContactProfileActivity.INTENT_EXTRA_CONTACT_ID, user.getUserID());
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, StrangerProfileActivity.class);
            intent.putExtra(StrangerProfileActivity.INTENT_EXTRA_USER, user);
            startActivity(intent);
        }
    }

    @Override
    public void searchNotExist() {
//        mTvSearchHint.setText(R.string.FindNewContactActivity_SearchNotExist);
//        mPbSearch.setVisibility(View.GONE);

    }

    @Override
    public void searchFail() {
//        mTvSearchHint.setText(R.string.FindNewContactActivity_SearchFail);
//        mPbSearch.setVisibility(View.GONE);

    }


    public  void sendData(){

    }

    public  void sendHeart(){

    }
}
