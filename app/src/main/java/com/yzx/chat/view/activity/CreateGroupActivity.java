package com.yzx.chat.view.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.contract.CreateGroupContract;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.presenter.CreateGroupPresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.adapter.CreateGroupAdapter;
import com.yzx.chat.widget.view.CircleImageView;
import com.yzx.chat.widget.view.FlowLayout;
import com.yzx.chat.widget.view.IndexBarView;
import com.yzx.chat.widget.view.LetterSegmentationItemDecoration;
import com.yzx.chat.widget.view.ProgressDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年02月22日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class CreateGroupActivity extends BaseCompatActivity<CreateGroupContract.Presenter> implements CreateGroupContract.View {


    private RecyclerView mRecyclerView;
    private View mHeaderView;
    private CreateGroupAdapter mCreateGroupAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private LetterSegmentationItemDecoration mLetterSegmentationItemDecoration;
    private IndexBarView mIndexBarView;
    private TextView mTvIndexBarHint;
    private FlowLayout mFlowLayout;
    private Button mBtnConfirm;
    private ProgressDialog mProgressDialog;
    private List<ContactBean> mContactList;
    private List<ContactBean> mSelectedContactList;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_create_group;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mRecyclerView = findViewById(R.id.CreateGroupActivity_mRecyclerView);
        mIndexBarView = findViewById(R.id.CreateGroupActivity_mIndexBarView);
        mTvIndexBarHint = findViewById(R.id.CreateGroupActivity_mTvIndexBarHint);
        mBtnConfirm = findViewById(R.id.ProfileModifyActivity_mBtnConfirm);
        mHeaderView = getLayoutInflater().inflate(R.layout.item_create_group_header, (ViewGroup) getWindow().getDecorView(), false);
        mFlowLayout = mHeaderView.findViewById(R.id.CreateGroupActivity_mFlowLayout);
        mContactList = IMClient.getInstance().contactManager().getAllContacts();
        if (mContactList == null) {
            return;
        }
        mProgressDialog = new ProgressDialog(this, getString(R.string.CreateGroupActivity_Creating));
        mSelectedContactList = new ArrayList<>(32);
        mCreateGroupAdapter = new CreateGroupAdapter(mContactList);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (mContactList == null) {
            finish();
            return;
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mFlowLayout.setItemSpace((int) AndroidUtil.dip2px(4));
        mFlowLayout.setLineSpace((int) AndroidUtil.dip2px(4));

        mLetterSegmentationItemDecoration = new LetterSegmentationItemDecoration();
        mLetterSegmentationItemDecoration.setLineColor(ContextCompat.getColor(this, R.color.divider_color_black));
        mLetterSegmentationItemDecoration.setLineWidth(1);
        mLetterSegmentationItemDecoration.setTextColor(ContextCompat.getColor(this, R.color.divider_color_black));
        mLetterSegmentationItemDecoration.setTextSize(AndroidUtil.sp2px(16));

        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mCreateGroupAdapter);
        mRecyclerView.setHasFixedSize(true);
        // mContactRecyclerView.setItemAnimator(new NoAnimations());
        mRecyclerView.addItemDecoration(mLetterSegmentationItemDecoration);

        mIndexBarView.setSelectedTextColor(ContextCompat.getColor(this, R.color.text_secondary_color_black));
        mIndexBarView.setOnTouchSelectedListener(mIndexBarSelectedListener);


        mCreateGroupAdapter.setHeaderView(mHeaderView);
        mCreateGroupAdapter.setOnItemSelectedChangeListener(mOnItemSelectedChangeListener);

        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        mBtnConfirm.setOnClickListener(mOnConfirmClickListener);
    }

    private final View.OnClickListener mOnConfirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPresenter.createGroup(mSelectedContactList);
        }
    };

    private final CreateGroupAdapter.OnItemSelectedChangeListener mOnItemSelectedChangeListener = new CreateGroupAdapter.OnItemSelectedChangeListener() {
        @Override
        public void onItemSelectedChange(int position, boolean isSelect) {
            if (isSelect) {
                mSelectedContactList.add(mContactList.get(position - 1));
                CircleImageView avatar = new CircleImageView(CreateGroupActivity.this);
                avatar.setId(position);
                avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
                avatar.setImageResource(R.drawable.temp_head_image);
                mFlowLayout.addView(avatar, new ViewGroup.MarginLayoutParams((int) AndroidUtil.dip2px(40), (int) AndroidUtil.dip2px(40)));
            } else {
                mSelectedContactList.remove(mContactList.get(position - 1));
                View needRemoveView = mFlowLayout.findViewById(position);
                if (needRemoveView != null) {
                    mFlowLayout.removeView(needRemoveView);
                }
            }
            mBtnConfirm.setEnabled(mSelectedContactList.size() > 0);
        }
    };

    private final IndexBarView.OnTouchSelectedListener mIndexBarSelectedListener = new IndexBarView.OnTouchSelectedListener() {
        @Override
        public void onSelected(int position, String text) {
            final int scrollPosition = mCreateGroupAdapter.findPositionByLetter(text);
            if (scrollPosition >= 0) {
                mRecyclerView.scrollToPosition(scrollPosition);
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        int firstPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
                        if (scrollPosition > firstPosition) {
                            View childView = mRecyclerView.getChildAt(scrollPosition - firstPosition);
                            int scrollY = childView.getTop() - mLetterSegmentationItemDecoration.getSpace();
                            mRecyclerView.scrollBy(0, scrollY);
                        }
                    }
                });
            }
            mTvIndexBarHint.setVisibility(View.VISIBLE);
            mTvIndexBarHint.setText(text);
        }

        @Override
        public void onCancelSelected() {
            mTvIndexBarHint.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onMove(int offsetPixelsY) {
            int startOffset = mTvIndexBarHint.getHeight() / 2;
            if (startOffset > offsetPixelsY) {
                mTvIndexBarHint.setTranslationY(0);
            } else if (offsetPixelsY > mIndexBarView.getHeight() - startOffset) {
                mTvIndexBarHint.setTranslationY(mIndexBarView.getHeight() - startOffset * 2);
            } else {
                mTvIndexBarHint.setTranslationY(offsetPixelsY - startOffset);
            }
        }
    };

    @Override
    public CreateGroupContract.Presenter getPresenter() {
        return new CreateGroupPresenter();
    }


    @Override
    public void showError(String error) {

    }

    @Override
    public void goBack() {

    }
}
