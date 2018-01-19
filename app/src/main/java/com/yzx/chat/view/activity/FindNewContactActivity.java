package com.yzx.chat.view.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.contract.FindNewContactContract;
import com.yzx.chat.presenter.FindNewContactPresenter;
import com.yzx.chat.widget.adapter.MaybeKnowAdapter;

public class FindNewContactActivity extends BaseCompatActivity<FindNewContactContract.Presenter> implements FindNewContactContract.View {

    private RecyclerView mRecyclerView;
    private Toolbar mToolbar;
    private MaybeKnowAdapter mAdapter;

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
        mRecyclerView = findViewById(R.id.FindNewContactActivity_mRecyclerView);
        mAdapter = new MaybeKnowAdapter();
    }

    private void setView() {

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public FindNewContactContract.Presenter getPresenter() {
        return new FindNewContactPresenter();
    }
}
