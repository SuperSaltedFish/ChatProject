package com.yzx.chat.mvp.view.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.mvp.contract.StrangerProfileContract;
import com.yzx.chat.mvp.presenter.StrangerProfilePresenter;
import com.yzx.chat.util.DateUtil;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.widget.view.ProgressDialog;

/**
 * Created by YZX on 2018年01月29日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class StrangerProfileActivity extends BaseCompatActivity<StrangerProfileContract.Presenter> implements StrangerProfileContract.View {

    public static final String INTENT_EXTRA_USER = "User";

    private EditText mEtVerifyContent;
    private UserBean mUserBean;
    private ProgressDialog mProgressDialog;
    private ImageView mIvAvatar;
    private TextView mTvNickname;
    private TextView mTvExplain;
    private TextView mTvContentNickname;
    private TextView mTvContentLocation;
    private TextView mTvContentBirthday;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_stranger_profile;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mEtVerifyContent = findViewById(R.id.StrangerProfileActivity_mEtVerifyContent);
        mTvContentNickname = findViewById(R.id.Profile_mTvContentNickname);
        mTvContentLocation = findViewById(R.id.Profile_mTvContentLocation);
        mTvContentBirthday = findViewById(R.id.Profile_mTvContentBirthday);
        mIvAvatar = findViewById(R.id.StrangerProfileActivity_mIvAvatar);
        mTvNickname = findViewById(R.id.StrangerProfileActivity_mTvNickname);
        mTvExplain = findViewById(R.id.StrangerProfileActivity_mTvExplain);
        mProgressDialog = new ProgressDialog(this, getString(R.string.ProgressHint_Send));
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setTitle(null);
        }


        setData();
    }

    private void setData() {
        mUserBean = getIntent().getParcelableExtra(INTENT_EXTRA_USER);
        if (mUserBean == null) {
            finish();
            return;
        }
        mTvNickname.setText(mUserBean.getNickname());
        mTvExplain.setText(mUserBean.getSignature());
        mTvContentNickname.setText(mUserBean.getNickname());
        if (TextUtils.isEmpty(mUserBean.getLocation())) {
            mTvContentLocation.setText(R.string.EditProfileActivity_NoSet);
        } else {
            mTvContentLocation.setText(mUserBean.getLocation());
        }
        String birthday = mUserBean.getBirthday();
        if (!TextUtils.isEmpty(mUserBean.getBirthday())) {
            birthday = DateUtil.isoFormatTo(getString(R.string.DateFormat_yyyyMMdd), birthday);
            if (!TextUtils.isEmpty(birthday)) {
                mTvContentBirthday.setText(birthday);
            } else {
                mTvContentBirthday.setText(R.string.EditProfileActivity_NoSet);
            }
        } else {
            mTvContentBirthday.setText(R.string.EditProfileActivity_NoSet);
        }
        GlideUtil.loadAvatarFromUrl(this,mIvAvatar,mUserBean.getAvatar());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_stranger_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.StrangerProfileMenu_Request){
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
            mPresenter.requestContact(mUserBean, mEtVerifyContent.getText().toString());
        }else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public StrangerProfileContract.Presenter getPresenter() {
        return new StrangerProfilePresenter();
    }

    @Override
    public void goBack() {
        mProgressDialog.dismiss();
        finish();
    }

    @Override
    public void showError(String error) {
        mProgressDialog.dismiss();
        showToast(error);
    }
}
