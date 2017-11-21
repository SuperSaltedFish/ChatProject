package com.yzx.chat.view.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.widget.adapter.NewContactAdapter;

public class FindNewContactActivity extends BaseCompatActivity {

    private RecyclerView mRvNewContact;
    private Toolbar mToolbar;
    private NewContactAdapter mAdapter;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_find_new_contact;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setView();
    }

    private void init() {
        mToolbar = findViewById(R.id.FindNewContactActivity_mToolbar);
        mRvNewContact = findViewById(R.id.FindNewContactActivity_mRvNewContact);
        mAdapter = new NewContactAdapter();
    }

    private void setView() {

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(null);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        mRvNewContact.setLayoutManager(layoutManager);
        mRvNewContact.setAdapter(mAdapter);
        mRvNewContact.setHasFixedSize(true);

    }
}
