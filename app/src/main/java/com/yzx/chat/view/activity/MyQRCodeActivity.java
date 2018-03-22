package com.yzx.chat.view.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.contract.MyQRCodeActivityContract;
import com.yzx.chat.presenter.MyQRCodeActivityPresenter;
import com.yzx.chat.util.QREncodingUtils;
import com.yzx.chat.widget.view.NineGridAvatarView;

/**
 * Created by YZX on 2018年02月24日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class MyQRCodeActivity extends BaseCompatActivity<MyQRCodeActivityContract.Presenter> implements MyQRCodeActivityContract.View {

    public static final String INTENT_EXTRA_GROUP_ID = "GroupID";
    public static final String INTENT_EXTRA_QR_TYPE = "Type";

    public static final int QR_CODE_TYPE_USER = 1;
    public static final int QR_CODE_TYPE_GROUP = 2;

    private NineGridAvatarView mIvAvatar;
    private ImageView mIvQRCode;
    private ProgressBar mProgressBar;
    private FrameLayout mFlScan;
    private FrameLayout mFlReset;
    private FrameLayout mFlSave;
    private TextView mTvLocation;
    private TextView mTvNickname;
    private TextView mTvHint;

    private String mGroupID;
    private int mCurrentQRCodeType;


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
        mTvLocation = findViewById(R.id.MyQRCodeActivity_mTvLocation);
        mTvNickname = findViewById(R.id.MyQRCodeActivity_mTvNickname);
        mTvHint = findViewById(R.id.MyQRCodeActivity_mTvHint);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        mCurrentQRCodeType = getIntent().getIntExtra(INTENT_EXTRA_QR_TYPE, 0);
        if (mCurrentQRCodeType != QR_CODE_TYPE_USER && mCurrentQRCodeType != QR_CODE_TYPE_GROUP) {
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mFlScan.setOnClickListener(mOnScanClickListener);
        mFlReset.setOnClickListener(mOnResetClickListener);
        mFlSave.setOnClickListener(mOnSaveClickListener);

        if (mCurrentQRCodeType == QR_CODE_TYPE_USER) {
            setTitle(R.string.MyQRCodeActivity_Title);
            UserBean user = mPresenter.getUserInfo();
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
            mIvAvatar.setImageUrlList(avatarList);
            mTvLocation.setText(user.getLocation());
            mTvNickname.setText(user.getNickname());
            mTvHint.setText(R.string.MyQRCodeActivity_QRCodeHint);
            mPresenter.updateUserQRCode();
        } else {
            setTitle(R.string.MyQRCodeActivity_Title2);
            mGroupID = getIntent().getStringExtra(INTENT_EXTRA_GROUP_ID);
            GroupBean group = mPresenter.getGroupInfo(mGroupID);
            if (group == null) {
                finish();
                return;
            }
            String avatarUri = group.getAvatarUrlFromMember();
            Object[] avatarList;
            if (TextUtils.isEmpty(avatarUri)) {
                avatarList = new Object[]{R.drawable.ic_avatar_default};
            } else {
                avatarList = avatarUri.split(",");
            }
            mIvAvatar.setImageUrlList(avatarList);
            mTvNickname.setText(group.getName());
            mTvHint.setText(R.string.MyQRCodeActivity_QRCodeHint2);
            mTvLocation.setVisibility(View.GONE);
            mPresenter.updateGroupQRCode(mGroupID);
        }

    }

    private final View.OnClickListener mOnScanClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private final View.OnClickListener mOnResetClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mProgressBar.setVisibility(View.VISIBLE);
            mIvQRCode.setVisibility(View.INVISIBLE);
            if (mCurrentQRCodeType == QR_CODE_TYPE_USER) {
                mPresenter.updateUserQRCode();
            } else {
                mPresenter.updateGroupQRCode(mGroupID);
            }
        }
    };

    private final View.OnClickListener mOnSaveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    @Override
    public MyQRCodeActivityContract.Presenter getPresenter() {
        return new MyQRCodeActivityPresenter();
    }

    @Override
    public void showQRCode(String content) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mIvQRCode.setVisibility(View.VISIBLE);
        mIvQRCode.setImageBitmap(QREncodingUtils.createQRCode(content, 200, 200, null));
    }

    @Override
    public void showError(String error) {
        showToast(error);
        finish();
    }
}
