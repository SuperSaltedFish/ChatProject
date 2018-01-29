package com.yzx.chat.view.activity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.contract.StrangerProfileContract;
import com.yzx.chat.presenter.StrangerProfilePresenter;

/**
 * Created by YZX on 2018年01月29日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class StrangerProfileActivity extends BaseCompatActivity<StrangerProfileContract.Presenter> implements StrangerProfileContract.View {

    public static final String INTENT_EXTRA_USER = "User";

    private EditText mEtVerifyContent;
    private Button mBtnConfirm;
    private UserBean mUserBean;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_stranger_profile;
    }

    @Override
    protected void init() {
        mEtVerifyContent = findViewById(R.id.StrangerProfileActivity_mEtVerifyContent);
        mBtnConfirm = findViewById(R.id.StrangerProfileActivity_mBtnConfirm);
    }

    @Override
    protected void setup() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setTitle(null);
        }

        mBtnConfirm.setOnClickListener(mOnConfirmClickListener);

        setData();
    }

    private void setData() {
        mUserBean = getIntent().getParcelableExtra(INTENT_EXTRA_USER);
        if (mUserBean == null) {
            return;
        }
    }

    private final View.OnClickListener mOnConfirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPresenter.requestContact(mUserBean.getUserID(), mEtVerifyContent.getText().toString());
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
}
