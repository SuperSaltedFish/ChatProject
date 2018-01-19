package com.yzx.chat.view.activity;

import android.os.Bundle;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.widget.adapter.ContactMessageAdapter;
import com.yzx.chat.widget.view.DividerItemDecoration;

/**
 * Created by YZX on 2018年01月18日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class ContactMessageActivity extends BaseCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ContactMessageAdapter mAdapter;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_contact_message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
        setView();
    }

    private void init() {
        mToolbar = findViewById(R.id.ContactMessageActivity_mToolbar);
        mRecyclerView = findViewById(R.id.ContactMessageActivity_mRecyclerView);
        mAdapter = new ContactMessageAdapter();
    }

    private void setView() {
        setSupportActionBar(mToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(1, ContextCompat.getColor(this,R.color.divider_color_black)));
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
}
