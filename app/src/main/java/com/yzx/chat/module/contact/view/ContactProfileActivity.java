package com.yzx.chat.module.contact.view;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactRemarkBean;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.module.conversation.view.ChatActivity;
import com.yzx.chat.module.contact.contract.ContactProfileContract;
import com.yzx.chat.module.contact.presenter.ContactProfilePresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.StringUtil;
import com.yzx.chat.widget.adapter.CenterCropImagePagerAdapter;
import com.yzx.chat.widget.adapter.ContactInfoPagerAdapter;
import com.yzx.chat.widget.view.FlowLayout;
import com.yzx.chat.widget.view.PageIndicator;
import com.yzx.chat.widget.view.ProgressDialog;

import java.util.ArrayList;
import java.util.List;


public class ContactProfileActivity extends BaseCompatActivity<ContactProfileContract.Presenter> implements ContactProfileContract.View {

    public static final String INTENT_EXTRA_CONTACT_ID = "ContactID";

    private ImageView mIvStartChat;
    private ImageView mIvAvatar;
    private ImageView mIvSexIcon;
    private TextView mTvTitle;
    private TextView mTvSignature;
    private TextView mTvNickname;
    private TextView mTvLocationAndAge;
    private TextView mTvLastLabel;
    private ProgressDialog mProgressDialog;
    private AppBarLayout mAppBarLayout;
    private FlowLayout mLabelFlowLayout;
    private TabLayout mTabLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private PageIndicator mPageIndicator;
    private ViewPager mVpBanner;
    private ViewPager mVpContactInfo;

    private CenterCropImagePagerAdapter mCropImagePagerAdapter;

    private ArrayList<Object> mPicUrlList;
    private String mContactID;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_contact_profile;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mAppBarLayout = findViewById(R.id.ContactProfileActivity_mAppBarLayout);
        mIvStartChat = findViewById(R.id.ContactProfileActivity_mIvStartChat);
        mTvNickname = findViewById(R.id.ContactProfileActivity_mTvNickname);
        mTvSignature = findViewById(R.id.ContactProfileActivity_mTvSignature);
        mIvAvatar = findViewById(R.id.ContactProfileActivity_mIvAvatar);
        mIvSexIcon = findViewById(R.id.ContactProfileActivity_mIvSexIcon);
        mTvTitle = findViewById(R.id.ContactProfileActivity_mTvTitle);
        mTvLocationAndAge = findViewById(R.id.ContactProfileActivity_mTvLocationAndAge);
        mLabelFlowLayout = findViewById(R.id.ContactProfileActivity_mLabelFlowLayout);
        mTabLayout = findViewById(R.id.ContactProfileActivity_mTabLayout);
        mPageIndicator = findViewById(R.id.ContactProfileActivity_mPageIndicator);
        mVpBanner = findViewById(R.id.ContactProfileActivity_mVpBanner);
        mVpContactInfo = findViewById(R.id.ContactProfileActivity_mVpContactInfo);
        mCollapsingToolbarLayout = findViewById(R.id.ContactProfileActivity_mCollapsingToolbarLayout);
        mTvLastLabel = (TextView) getLayoutInflater().inflate(R.layout.item_label_normal, mLabelFlowLayout, false);
        mProgressDialog = new ProgressDialog(this, getString(R.string.ProgressHint_Delete));
        mContactID = getIntent().getStringExtra(INTENT_EXTRA_CONTACT_ID);
        mPicUrlList = new ArrayList<>(6);
        mCropImagePagerAdapter = new CenterCropImagePagerAdapter(mPicUrlList);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (TextUtils.isEmpty(mContactID)) {
            LogUtil.e("contactID==NULL");
            finish();
            return;
        }
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS);
        setTitle(null);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fillTestData();

        mPageIndicator.setIndicatorColorSelected(Color.WHITE);
        mPageIndicator.setIndicatorColorUnselected(ContextCompat.getColor(this, R.color.backgroundColorWhiteLight));
        mPageIndicator.setIndicatorRadius((int) AndroidUtil.dip2px(3));
        mPageIndicator.setupWithViewPager(mVpBanner);

        mVpBanner.setAdapter(mCropImagePagerAdapter);
        mVpContactInfo.setAdapter(new ContactInfoPagerAdapter(getSupportFragmentManager(), mContactID, getResources().getStringArray(R.array.ContactProfile_TabTitle)));

        mTabLayout.setupWithViewPager(mVpContactInfo);

        mLabelFlowLayout.setLineSpace((int) AndroidUtil.dip2px(8));
        mLabelFlowLayout.setItemSpace((int) AndroidUtil.dip2px(8));

        int size = (int) AndroidUtil.dip2px(10);
        Drawable drawable = getDrawable(R.drawable.ic_add);
        if (drawable != null) {
            drawable.setTint(Color.WHITE);
            drawable.setBounds(0, 0, size, size);
        }
        mTvLastLabel.setText(R.string.EditContactLabelActivity_Title);
        mTvLastLabel.setCompoundDrawables(null, null, drawable, null);
        mTvLastLabel.setPadding(size, mTvLastLabel.getPaddingTop(), size * 2 / 3, mTvLastLabel.getPaddingBottom());
        mTvLastLabel.setCompoundDrawablePadding(size / 4);

        mTvTitle.setAlpha(0);
        mIvStartChat.setOnClickListener(mOnViewClickListener);
        mLabelFlowLayout.setOnClickListener(mOnViewClickListener);
        mAppBarLayout.addOnOffsetChangedListener(mOnOffsetChangedListener);
        mPresenter.init(mContactID);
    }


    private void fillTestData() {
        mPicUrlList.add(R.drawable.temp_image_1);
        mPicUrlList.add(R.drawable.temp_image_2);
        mPicUrlList.add(R.drawable.temp_image_3);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ContactMenu_ReviseRemarks:
                startRemarkInfoActivity();
                break;
            case R.id.ContactMenu_DeleteContact:
                mPresenter.deleteContact();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == EditContactTagsActivity.RESULT_CODE) {
            ContactBean contact = mPresenter.getContact();
            ArrayList<String> newTags = data.getStringArrayListExtra(EditContactTagsActivity.INTENT_EXTRA_LABEL);
            ArrayList<String> oldTags = contact.getRemark().getTags();
            if (!StringUtil.isEquals(newTags, oldTags, true)) {
                contact.getRemark().setTags(newTags);
                mPresenter.saveRemarkInfo(contact);
            }
        }
    }

    private void startRemarkInfoActivity() {
        Intent intent = new Intent(this, RemarkInfoActivity.class);
        intent.putExtra(RemarkInfoActivity.INTENT_EXTRA_CONTACT, mPresenter.getContact());
        startActivityForResult(intent, 0);
    }

    private void startChatActivity() {
        Intent intent = new Intent(ContactProfileActivity.this, ChatActivity.class);
        intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION_ID, mContactID);
        intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION_TYPE_CODE, ChatActivity.CONVERSATION_PRIVATE);
        startActivity(intent);
        finish();
    }

    private void startEditContactLabelActivity() {
        Intent intent = new Intent(ContactProfileActivity.this, EditContactTagsActivity.class);
        intent.putExtra(EditContactTagsActivity.INTENT_EXTRA_LABEL, mPresenter.getContact().getRemark().getTags());
        intent.putExtra(EditContactTagsActivity.INTENT_EXTRA_SELECTABLE_LABEL, mPresenter.getAllTags());
        startActivityForResult(intent, 0);
    }

    private final View.OnClickListener mOnViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ContactProfileActivity_mIvStartChat:
                    startChatActivity();
                    break;
                case R.id.ContactProfileActivity_mLabelFlowLayout:
                    startEditContactLabelActivity();
                    break;

            }
        }
    };

    private AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener = new AppBarLayout.OnOffsetChangedListener() {
        private boolean isHide;

        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            if (mCollapsingToolbarLayout.getHeight() + verticalOffset < mCollapsingToolbarLayout.getScrimVisibleHeightTrigger()) {
                if (!isHide) {
                    isHide = true;
                    mIvAvatar.animate().alpha(0).scaleX(0).scaleY(0).setDuration(200);
                    mTvTitle.animate().alpha(1).setDuration(200);
                }
            } else {
                if (isHide) {
                    isHide = false;
                    mIvAvatar.animate().alpha(1).scaleX(1).scaleY(1).setDuration(200);
                    mTvTitle.animate().alpha(0).setDuration(200);
                }
            }
        }
    };

    @Override
    public ContactProfileContract.Presenter getPresenter() {
        return new ContactProfilePresenter();
    }

    @Override
    public void updateContactInfo(ContactBean contact) {
        mTvTitle.setText(contact.getName());
        mTvNickname.setText(contact.getUserProfile().getNickname());
        UserBean user = contact.getUserProfile();
        mIvSexIcon.setSelected(user.getSex() == UserBean.SEX_WOMAN);
        StringBuilder locationAndAge = new StringBuilder();
        locationAndAge.append(user.getAge());
        if (!TextUtils.isEmpty(user.getLocation())) {
            locationAndAge.append(" · ").append(user.getLocation());
        }
        mTvLocationAndAge.setText(locationAndAge.toString());
        if (TextUtils.isEmpty(user.getSignature())) {
            mTvSignature.setText(null);
            mTvSignature.setVisibility(View.GONE);
        } else {
            mTvSignature.setText(user.getSignature());
            mTvSignature.setVisibility(View.VISIBLE);
        }
        GlideUtil.loadAvatarFromUrl(this, mIvAvatar, user.getAvatar());
        mLabelFlowLayout.removeAllViews();

        ContactRemarkBean contactRemark = contact.getRemark();
        List<String> tags = contactRemark.getTags();
        if (tags != null && tags.size() != 0) {
            for (String tag : tags) {
                TextView label = (TextView) getLayoutInflater().inflate(R.layout.item_label_normal, mLabelFlowLayout, false);
                label.setText(tag);
                mLabelFlowLayout.addView(label);
            }
        }
        mLabelFlowLayout.addView(mTvLastLabel);
    }


    @Override
    public void showError(String error) {
        showToast(error);
    }

    @Override
    public void goBack() {
        finish();
    }

    @Override
    public void setEnableProgressDialog(boolean isEnable) {
        if (isEnable) {
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }

}
