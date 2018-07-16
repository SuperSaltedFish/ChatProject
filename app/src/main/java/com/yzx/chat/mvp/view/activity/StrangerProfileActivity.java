package com.yzx.chat.mvp.view.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.ContactOperationBean;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.configure.GlideApp;
import com.yzx.chat.mvp.contract.StrangerProfileContract;
import com.yzx.chat.mvp.presenter.StrangerProfilePresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.view.GlideSemicircleTransform;
import com.yzx.chat.widget.view.ProgressDialog;

/**
 * Created by YZX on 2018年01月29日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class StrangerProfileActivity extends BaseCompatActivity<StrangerProfileContract.Presenter> implements StrangerProfileContract.View {

    public static final String INTENT_EXTRA_USER = "User";
    public static final String INTENT_EXTRA_CONTENT_OPERATION = "ContactOperation";

    private EditText mEtReason;
    private ProgressDialog mProgressDialog;
    private TextView mTvNickname;
    private TextView mTvSignature;
    private TextView mTvUserInfo;
    private ImageView mIvSexIcon;
    private ImageView mIvPicture;
    private Button mBtnConfirm;

    private UserBean mUserBean;
    private ContactOperationBean mContactOperationBean;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_stranger_profile;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mEtReason = findViewById(R.id.StrangerProfileActivity_mEtReason);
        mTvNickname = findViewById(R.id.StrangerProfileActivity_mTvNickname);
        mTvSignature = findViewById(R.id.StrangerProfileActivity_mTvSignature);
        mTvUserInfo = findViewById(R.id.StrangerProfileActivity_mTvUserInfo);
        mIvSexIcon = findViewById(R.id.StrangerProfileActivity_mIvSexIcon);
        mIvPicture = findViewById(R.id.StrangerProfileActivity_mIvPicture);
        mBtnConfirm = findViewById(R.id.StrangerProfileActivity_mBtnConfirm);
        mProgressDialog = new ProgressDialog(this, getString(R.string.ProgressHint_Send));
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setTitle(null);
        }

        mBtnConfirm.setOnClickListener(mOnConfirmClickListener);

        setData();
    }

    private void setData() {
        mUserBean = getIntent().getParcelableExtra(INTENT_EXTRA_USER);
        mContactOperationBean = getIntent().getParcelableExtra(INTENT_EXTRA_CONTENT_OPERATION);
        if (mUserBean != null && mContactOperationBean == null) {
            mEtReason.setEnabled(true);
            mBtnConfirm.setText(R.string.StrangerProfileActivity_RequestAddContact);
        } else if (mUserBean == null && mContactOperationBean != null) {
            mUserBean = mContactOperationBean.getUser();
            if (TextUtils.isEmpty(mContactOperationBean.getReason())) {
                mEtReason.setText(R.string.None);
            } else {
                mEtReason.setText(mContactOperationBean.getReason());
            }
            mEtReason.setEnabled(false);
            mBtnConfirm.setText(R.string.StrangerProfileActivity_AcceptAdd);
        } else {
            finish();
            return;
        }
        mTvNickname.setText(mUserBean.getNickname());
        mTvSignature.setText(mUserBean.getSignature());
        int sexBgID;
        if (mUserBean.getSex() == UserBean.SEX_WOMAN) {
            mIvSexIcon.setSelected(true);
            mTvUserInfo.setText(R.string.Woman);
            sexBgID = R.drawable.src_sex_woman;
        } else {
            mIvSexIcon.setSelected(false);
            mTvUserInfo.setText(R.string.Man);
            sexBgID = R.drawable.src_sex_man;
        }

        GlideApp.with(this)
                .load(sexBgID)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .transforms(new GlideSemicircleTransform(AndroidUtil.dip2px(40), Color.WHITE))
                .into(mIvPicture);

        mTvUserInfo.setText(mTvUserInfo.getText() + " · " + mUserBean.getAge());
        if (!TextUtils.isEmpty(mUserBean.getLocation())) {
            mTvUserInfo.setText(mTvUserInfo.getText() + " · " + mUserBean.getLocation());
        }
    }

    private final View.OnClickListener mOnConfirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mContactOperationBean != null) {
                mPresenter.acceptContactRequest(mContactOperationBean);
            } else if (mUserBean != null) {
                mPresenter.requestContact(mUserBean, mEtReason.getText().toString());
            }
        }
    };

    @Override
    public StrangerProfileContract.Presenter getPresenter() {
        return new StrangerProfilePresenter();
    }

    @Override
    public void goBack() {
        finish();
    }

    @Override
    public void showError(String error) {
        showToast(error);
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
