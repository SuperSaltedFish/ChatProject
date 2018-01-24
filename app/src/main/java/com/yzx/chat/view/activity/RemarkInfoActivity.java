package com.yzx.chat.view.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactRemarkBean;
import com.yzx.chat.contract.RemarkInfoContract;
import com.yzx.chat.presenter.RemarkInfoPresenter;
import com.yzx.chat.util.LogUtil;

import java.util.ArrayList;

/**
 * Created by YZX on 2018年01月15日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class RemarkInfoActivity extends BaseCompatActivity<RemarkInfoContract.Presenter> implements RemarkInfoContract.View {

    public static final String INTENT_EXTRA_CONTACT = "Contact";

    private Button mBtnConfirm;

    private ContactBean mContactBean;
    private EditText mEtRemarkName;
    private EditText mEtTelephone;
    private EditText mEtDescription;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_remark_info;
    }

    protected void init() {
        mBtnConfirm = findViewById(R.id.ImageSelectorActivity_mBtnConfirm);
        mEtRemarkName = findViewById(R.id.ImageSelectorActivity_mEtRemarkName);
        mEtTelephone = findViewById(R.id.ImageSelectorActivity_mEtTelephone);
        mEtDescription = findViewById(R.id.ImageSelectorActivity_mEtDescription);
    }

    @Override
    protected void setup() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mBtnConfirm.setOnClickListener(mOnConfirmClickListener);

        setData();
    }


    private void setData() {
        mContactBean = getIntent().getParcelableExtra(INTENT_EXTRA_CONTACT);
        if (mContactBean == null || mContactBean.getRemark() == null) {
            LogUtil.e("mContactBean == null");
            finish();
        } else {
            ContactRemarkBean contactRemark = mContactBean.getRemark();
            mEtRemarkName.setText(contactRemark.getRemarkName());
            mEtDescription.setText(contactRemark.getDescription());
            if (contactRemark.getTelephone() == 0) {
                mEtTelephone.setText(null);
            } else {
                mEtTelephone.setText(String.valueOf(contactRemark.getTelephone()));
            }
        }
    }

    private final View.OnClickListener mOnConfirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ContactRemarkBean contactRemark = mContactBean.getRemark();
            contactRemark.setDescription(mEtDescription.getText().toString());
            contactRemark.setRemarkName(mEtRemarkName.getText().toString());
            if (!TextUtils.isEmpty(mEtTelephone.getText())) {
                contactRemark.setTelephone(Integer.parseInt(mEtTelephone.getText().toString()));
            }
            mPresenter.save(mContactBean);
            finish();
        }
    };

    @Override
    public RemarkInfoContract.Presenter getPresenter() {
        return new RemarkInfoPresenter();
    }
}
