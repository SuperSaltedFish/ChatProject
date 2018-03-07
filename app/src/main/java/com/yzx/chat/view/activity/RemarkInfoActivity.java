package com.yzx.chat.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactRemarkBean;
import com.yzx.chat.contract.RemarkInfoContract;
import com.yzx.chat.presenter.RemarkInfoPresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.widget.view.ClearEditText;
import com.yzx.chat.widget.view.FlowLayout;

import java.util.ArrayList;
import java.util.List;

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
    private TextView mTvLabelHint;
    private FlowLayout mLabelFlowLayout;
    private LinearLayout mLlTelephoneLayout;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_remark_info;
    }

    protected void init(Bundle savedInstanceState) {
        mBtnConfirm = findViewById(R.id.RemarkInfoActivity_mBtnConfirm);
        mEtRemarkName = findViewById(R.id.RemarkInfoActivity_mEtRemarkName);
        mEtTelephone = findViewById(R.id.RemarkInfoActivity_mEtTelephone);
        mEtDescription = findViewById(R.id.RemarkInfoActivity_mEtDescription);
        mLabelFlowLayout = findViewById(R.id.RemarkInfoActivity_mLabelFlowLayout);
        mTvLabelHint = findViewById(R.id.RemarkInfoActivity_mTvLabelHint);
        mLlTelephoneLayout = findViewById(R.id.RemarkInfoActivity_mLlTelephoneLayout);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mBtnConfirm.setOnClickListener(mOnConfirmClickListener);
        mLabelFlowLayout.setOnClickListener(mOnLabelFlowLayoutClickListener);
        mEtTelephone.setOnEditorActionListener(mOnEditorActionListener);
        setData();
    }


    private void setData() {
        mContactBean = getIntent().getParcelableExtra(INTENT_EXTRA_CONTACT);
        if (mContactBean == null || mContactBean.getRemark() == null || mContactBean.getUserProfile() == null) {
            LogUtil.e("mContactBean == null");
            finish();
        } else {
            ContactRemarkBean contactRemark = mContactBean.getRemark();
            mEtRemarkName.setText(mContactBean.getName());
            mEtDescription.setText(contactRemark.getDescription());
            List<String> telephones = contactRemark.getTelephone();
            if (telephones != null && telephones.size() > 0) {
                for (String telephone : telephones) {
                    addTelephoneInput(telephone);
                }
            }
            setTags(contactRemark.getTags());
        }
    }

    private void setTags(ArrayList<String> tags) {
        mLabelFlowLayout.removeAllViews();
        if (tags != null && tags.size() != 0) {
            mLabelFlowLayout.setLineSpace((int) AndroidUtil.dip2px(8));
            mLabelFlowLayout.setItemSpace((int) AndroidUtil.dip2px(8));
            for (String tag : tags) {
                TextView label = (TextView) getLayoutInflater().inflate(R.layout.item_label, mLabelFlowLayout, false);
                label.setText(tag);
                mLabelFlowLayout.addView(label);
            }
            mContactBean.getRemark().setTags(tags);
        } else {
            mLabelFlowLayout.addView(mTvLabelHint);
        }
    }

    private void addTelephoneInput(CharSequence content) {
        ClearEditText editText = (ClearEditText) getLayoutInflater().inflate(R.layout.item_telephone_edit, mLlTelephoneLayout, false);
        editText.setText(content);
        editText.addTextChangedListener(new TelephoneTextWatcher(editText));
        editText.setClearIconVisible(true);
        mLlTelephoneLayout.addView(editText, mLlTelephoneLayout.getChildCount() - 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ModifyContactLabelActivity.RESULT_CODE && data != null) {
            setTags(data.getStringArrayListExtra(ModifyContactLabelActivity.INTENT_EXTRA_LABEL));
        }
    }

    private final TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction())) {
                if (!TextUtils.isEmpty(v.getText())) {
                    addTelephoneInput(v.getText());
                    v.setText("");
                }
            }
            return true;
        }
    };

    private final View.OnClickListener mOnLabelFlowLayoutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(RemarkInfoActivity.this, ModifyContactLabelActivity.class);
            ArrayList<String> tags = mContactBean.getRemark().getTags();
            if (tags != null && tags.size() != 0) {
                intent.putStringArrayListExtra(ModifyContactLabelActivity.INTENT_EXTRA_LABEL, tags);
            }
            startActivityForResult(intent, 0);

        }
    };

    private final View.OnClickListener mOnConfirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ContactRemarkBean contactRemark = mContactBean.getRemark();
            contactRemark.setDescription(mEtDescription.getText().toString());
            contactRemark.setRemarkName(mEtRemarkName.getText().toString());
            ArrayList<String> telephones = new ArrayList<>();
            for (int i = 0, count = mLlTelephoneLayout.getChildCount(); i < count; i++) {
                EditText editText = (EditText) mLlTelephoneLayout.getChildAt(i);
                String telephone = editText.getText().toString();
                if (!TextUtils.isEmpty(telephone)) {
                    telephones.add(telephone);
                }
            }
            contactRemark.setTelephone(telephones);
            mPresenter.save(mContactBean);
            finish();
        }
    };

    @Override
    public RemarkInfoContract.Presenter getPresenter() {
        return new RemarkInfoPresenter();
    }

    private final class TelephoneTextWatcher implements TextWatcher {
        private EditText mEditText;

        TelephoneTextWatcher(EditText editText) {
            mEditText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s) && mEditText != null) {
                mLlTelephoneLayout.removeView(mEditText);
                showSoftKeyboard(mEtTelephone);
            }
        }
    }
}
