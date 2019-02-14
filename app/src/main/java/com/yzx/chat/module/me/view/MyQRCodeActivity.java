package com.yzx.chat.module.me.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.module.me.contract.MyQRCodeContract;
import com.yzx.chat.module.me.presenter.MyQRCodePresenter;
import com.yzx.chat.module.common.view.QrCodeScanActivity;
import com.yzx.chat.util.QRUtils;
import com.yzx.chat.widget.view.NineGridAvatarView;

/**
 * Created by YZX on 2018年02月24日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class MyQRCodeActivity extends BaseCompatActivity<MyQRCodeContract.Presenter> implements MyQRCodeContract.View {

    public static final String INTENT_EXTRA_GROUP_ID = "GroupID";
    public static final String INTENT_EXTRA_QR_TYPE = "Type";

    public static final int QR_CODE_TYPE_USER = 1;
    public static final int QR_CODE_TYPE_GROUP = 2;

    private NineGridAvatarView mIvAvatar;
    private ImageView mIvQRCode;
    private ImageView mIvUserInfoIcon;
    private ProgressBar mProgressBar;
    private FrameLayout mFlScan;
    private FrameLayout mFlReset;
    private FrameLayout mFlSave;
    private TextView mTvUserInfo;
    private TextView mTvNickname;
    private TextView mTvHint;
    private TextView mTvErrorHint;

    private String mGroupID;
    private int mCurrentQRCodeType;
    private ConstraintLayout mClQRCodeLayout;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_my_qrcode;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mIvAvatar = findViewById(R.id.MyQRCodeActivity_mIvAvatar);
        mIvQRCode = findViewById(R.id.MyQRCodeActivity_mIvQRCode);
        mProgressBar = findViewById(R.id.MyQRCodeActivity_mProgressBar);
        mFlScan = findViewById(R.id.MyQRCodeActivity_mFlScan);
        mFlReset = findViewById(R.id.MyQRCodeActivity_mFlReset);
        mFlSave = findViewById(R.id.MyQRCodeActivity_mFlSave);
        mTvUserInfo = findViewById(R.id.MyQRCodeActivity_mTvUserInfo);
        mTvNickname = findViewById(R.id.MyQRCodeActivity_mTvNickname);
        mClQRCodeLayout = findViewById(R.id.MyQRCodeActivity_mClQRCodeLayout);
        mTvHint = findViewById(R.id.MyQRCodeActivity_mTvHint);
        mIvUserInfoIcon = findViewById(R.id.MyQRCodeActivity_mIvUserInfoIcon);
        mTvErrorHint = findViewById(R.id.MyQRCodeActivity_mTvErrorHint);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (mCurrentQRCodeType == QR_CODE_TYPE_USER) {
                actionBar.setTitle(R.string.MyQRCodeActivity_Title);
            } else {
                actionBar.setTitle(R.string.MyQRCodeActivity_Title2);
            }
        }

        mFlScan.setOnClickListener(mOnViewClickListener);
        mFlReset.setOnClickListener(mOnViewClickListener);
        mFlSave.setOnClickListener(mOnViewClickListener);

        setData();
    }

    private void setData() {
        mCurrentQRCodeType = getIntent().getIntExtra(INTENT_EXTRA_QR_TYPE, 0);
        if (mCurrentQRCodeType != QR_CODE_TYPE_USER && mCurrentQRCodeType != QR_CODE_TYPE_GROUP) {
            finish();
            return;
        }
        if (mCurrentQRCodeType == QR_CODE_TYPE_USER) {
            setTitle(R.string.MyQRCodeActivity_Title);
            UserEntity user = mPresenter.getUserInfo();
            if (user == null) {
                finish();
                return;
            }
            String avatarUri = user.getAvatar();
            Object[] avatarList;
            if (TextUtils.isEmpty(avatarUri)) {
                avatarList = new Object[]{R.drawable.ic_avatar_default};
            } else {
                avatarList = avatarUri.split(",");
            }
            mIvUserInfoIcon.setImageResource(R.drawable.selector_src_sex);
            if (user.getSex() == UserEntity.SEX_WOMAN) {
                mIvUserInfoIcon.setSelected(true);
                mTvUserInfo.setText(R.string.Woman);
            } else {
                mIvUserInfoIcon.setSelected(false);
                mTvUserInfo.setText(R.string.Man);
            }
            mIvAvatar.setImageUrlList(avatarList);
            mTvNickname.setText(user.getNickname());
            mTvHint.setText(R.string.MyQRCodeActivity_QRCodeHint);
            mTvUserInfo.setText(R.string.Woman);
            if (!TextUtils.isEmpty(user.getLocation())) {
                mTvUserInfo.setText(mTvUserInfo.getText() + " · " + user.getLocation());
            }
        } else {
            setTitle(R.string.MyQRCodeActivity_Title2);
            mGroupID = getIntent().getStringExtra(INTENT_EXTRA_GROUP_ID);
            GroupEntity group = mPresenter.getGroupInfo(mGroupID);
            if (group == null) {
                finish();
                return;
            }
            String avatarUri = group.getAvatarUrlFromMembers();
            Object[] avatarList;
            if (TextUtils.isEmpty(avatarUri)) {
                avatarList = new Object[]{R.drawable.ic_avatar_default};
            } else {
                avatarList = avatarUri.split(",");
            }
            mIvAvatar.setImageUrlList(avatarList);
            mTvNickname.setText(group.getName());
            mTvHint.setText(R.string.MyQRCodeActivity_QRCodeHint2);
            mIvUserInfoIcon.setImageResource(R.drawable.ic_friend);
            mIvUserInfoIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent)));
            mTvUserInfo.setText(group.getMembers().size() + getString(R.string.People));
        }

        resetQRCode();
    }

    private void resetQRCode() {
        mIvQRCode.setImageBitmap(null);
        mTvErrorHint.setText(null);
        mProgressBar.setVisibility(View.VISIBLE);
        if (mCurrentQRCodeType == QR_CODE_TYPE_USER) {
            mPresenter.updateUserQRCode();
        } else {
            mPresenter.updateGroupQRCode(mGroupID);
        }
    }

    private void saveQRCode() {
        mClQRCodeLayout.setDrawingCacheEnabled(true);
        Bitmap bitmap = mClQRCodeLayout.getDrawingCache();
        float scale = 800f / bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(scale, scale);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        if (mCurrentQRCodeType == QR_CODE_TYPE_USER) {
            mPresenter.saveQRCodeToLocal(bitmap, mPresenter.getUserInfo().getUserID());
        } else {
            mPresenter.saveQRCodeToLocal(bitmap, mGroupID);
        }
        bitmap.recycle();
        mClQRCodeLayout.setDrawingCacheEnabled(false);
    }

    private final View.OnClickListener mOnViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.MyQRCodeActivity_mFlScan:
                    startActivity(new Intent(MyQRCodeActivity.this, QrCodeScanActivity.class));
                    break;
                case R.id.MyQRCodeActivity_mFlReset:
                    resetQRCode();
                    break;
                case R.id.MyQRCodeActivity_mFlSave:
                    saveQRCode();
                    break;
            }
        }
    };


    @Override
    public MyQRCodeContract.Presenter getPresenter() {
        return new MyQRCodePresenter();
    }

    @Override
    public void showQRCode(String content) {
        mIvQRCode.setImageBitmap(QRUtils.createQRCode(content, 280, 280, null));
    }

    @Override
    public void showHint(String error) {
        showToast(error);
    }

    @Override
    public void showErrorHint(String hint) {
        mTvErrorHint.setVisibility(View.VISIBLE);
        mTvErrorHint.setText(hint);
    }

    @Override
    public void setEnableProgressBar(boolean isEnable) {
        if (isEnable) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}
