package com.yzx.chat.mvp.view.activity;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
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
import com.yzx.chat.mvp.contract.MyQRCodeContract;
import com.yzx.chat.mvp.presenter.MyQRCodePresenter;
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
    private ProgressBar mProgressBar;
    private FrameLayout mFlScan;
    private FrameLayout mFlReset;
    private FrameLayout mFlSave;
    private TextView mTvLocation;
    private TextView mTvNickname;
    private TextView mTvHint;

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
        mTvLocation = findViewById(R.id.MyQRCodeActivity_mTvLocation);
        mTvNickname = findViewById(R.id.MyQRCodeActivity_mTvNickname);
        mClQRCodeLayout = findViewById(R.id.MyQRCodeActivity_mClQRCodeLayout);
        mTvHint = findViewById(R.id.MyQRCodeActivity_mTvHint);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        mCurrentQRCodeType = getIntent().getIntExtra(INTENT_EXTRA_QR_TYPE, 0);
        if (mCurrentQRCodeType != QR_CODE_TYPE_USER && mCurrentQRCodeType != QR_CODE_TYPE_GROUP) {
            finish();
            return;
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (mCurrentQRCodeType == QR_CODE_TYPE_USER) {
                actionBar.setTitle(R.string.MyQRCodeActivity_Title);
            }else {
                actionBar.setTitle(R.string.MyQRCodeActivity_Title2);
            }
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
    };

    @Override
    public MyQRCodeContract.Presenter getPresenter() {
        return new MyQRCodePresenter();
    }

    @Override
    public void showQRCode(String content) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mIvQRCode.setVisibility(View.VISIBLE);
        mIvQRCode.setImageBitmap(QRUtils.createQRCode(content, 200, 200, null));
    }

    @Override
    public void showHint(String error) {
        showToast(error);
    }
}
