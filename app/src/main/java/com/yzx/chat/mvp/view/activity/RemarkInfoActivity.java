package com.yzx.chat.mvp.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactRemarkBean;
import com.yzx.chat.mvp.contract.RemarkInfoContract;
import com.yzx.chat.mvp.presenter.RemarkInfoPresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.widget.view.ClearEditText;
import com.yzx.chat.widget.view.FlowLayout;

import java.util.ArrayList;

/**
 * Created by YZX on 2018年01月15日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class RemarkInfoActivity extends BaseCompatActivity<RemarkInfoContract.Presenter> implements RemarkInfoContract.View {

    public static final String INTENT_EXTRA_CONTACT = "Contact";

    private ContactBean mContactBean;
    private EditText mEtRemarkName;
    private EditText mEtTelephone;
    private EditText mEtDescription;
    private TextView mTvLabelHint;
    private FlowLayout mLabelFlowLayout;
    private LinearLayout mLlTelephoneLayout;
    private ArrayList<String> mTags;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_remark_info;
    }

    protected void init(Bundle savedInstanceState) {
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
        mLabelFlowLayout.setLineSpace((int) AndroidUtil.dip2px(8));
        mLabelFlowLayout.setItemSpace((int) AndroidUtil.dip2px(8));
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
            setTelephones(contactRemark.getTelephone());
            mTags = contactRemark.getTags();
            setTags(mTags);
        }
    }

    private void setTags(ArrayList<String> tags) {
        mLabelFlowLayout.removeAllViews();
        if (tags != null && tags.size() != 0) {
            for (String tag : tags) {
                TextView label = (TextView) getLayoutInflater().inflate(R.layout.item_label_selected, mLabelFlowLayout, false);
                label.setText(tag);
                mLabelFlowLayout.addView(label);
            }
        } else {
            mLabelFlowLayout.addView(mTvLabelHint);
        }
    }

    private ArrayList<String> getTags() {
        ArrayList<String> tags = null;
        for (int i = 0, count = mLabelFlowLayout.getChildCount(); i < count; i++) {
            TextView labelView = (TextView) mLabelFlowLayout.getChildAt(i);
            if (labelView != mTvLabelHint) {
                String label = labelView.getText().toString();
                if (!TextUtils.isEmpty(label)) {
                    if (tags == null) {
                        tags = new ArrayList<>(count + 2);
                    }
                    tags.add(label);
                }
            }
        }
        return tags;
    }

    private void setTelephones(ArrayList<String> telephones) {
        if (telephones != null && telephones.size() > 0) {
            for (String telephone : telephones) {
                addTelephoneInput(telephone);
            }
        }
    }

    private ArrayList<String> getTelephones() {
        ArrayList<String> telephones = null;
        for (int i = 0, count = mLlTelephoneLayout.getChildCount(); i < count; i++) {
            EditText editText = (EditText) mLlTelephoneLayout.getChildAt(i);
            String telephone = editText.getText().toString();
            if (!TextUtils.isEmpty(telephone)) {
                if (telephones == null) {
                    telephones = new ArrayList<>(count + 2);
                }
                telephones.add(telephone);
            }
        }
        return telephones;
    }

    private void addTelephoneInput(CharSequence content) {
        ClearEditText editText = (ClearEditText) getLayoutInflater().inflate(R.layout.item_telephone_edit, mLlTelephoneLayout, false);
        editText.setText(content);
        editText.addTextChangedListener(new TelephoneTextWatcher(editText));
        editText.setClearIconVisible(true);
        mLlTelephoneLayout.addView(editText, mLlTelephoneLayout.getChildCount() - 1);
    }

    private void confirm() {
        ContactRemarkBean contactRemark = mContactBean.getRemark();
        String remarkName = mEtRemarkName.getText().toString();
        String description = mEtDescription.getText().toString();
        ArrayList<String> newTelephones = getTelephones();
        ArrayList<String> newTags =mTags;
        ArrayList<String> oldTelephones = contactRemark.getTelephone();
        ArrayList<String> oldTags = contactRemark.getTags();
        boolean isChanged = false;

        if (!remarkName.equals(contactRemark.getRemarkName())) {
            if (!remarkName.equals(mContactBean.getUserProfile().getNickname())) {
                isChanged = true;
                contactRemark.setRemarkName(remarkName);
            } else if (!TextUtils.isEmpty(contactRemark.getRemarkName())) {
                isChanged = true;
                contactRemark.setRemarkName(null);
            }
        }
        if (isChanged || !description.equals(contactRemark.getDescription())) {
            isChanged = true;
            contactRemark.setDescription(description);
        }
        if (isChanged || !isEquals(newTelephones, oldTelephones)) {
            isChanged = true;
            contactRemark.setTelephone(newTelephones);
        }
        if (isChanged || !isEquals(newTags, oldTags)) {
            isChanged = true;
            contactRemark.setTags(newTags);
        }
        if (isChanged) {
            mPresenter.save(mContactBean);
        }

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_simple_confirm, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.SimpleConfirmMenu_Confirm) {
            confirm();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == EditContactLabelActivity.RESULT_CODE && data != null) {
            mTags = data.getStringArrayListExtra(EditContactLabelActivity.INTENT_EXTRA_LABEL);
            setTags(mTags);
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
            Intent intent = new Intent(RemarkInfoActivity.this, EditContactLabelActivity.class);
            intent.putStringArrayListExtra(EditContactLabelActivity.INTENT_EXTRA_LABEL, mContactBean.getRemark().getTags());
            intent.putStringArrayListExtra(EditContactLabelActivity.INTENT_EXTRA_SELECTABLE_LABEL, mPresenter.getAllTags());
            startActivityForResult(intent, 0);

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

    private static boolean isEquals(ArrayList<String> a1, ArrayList<String> a2) {
        if (a1 == a2) {
            return true;
        }
        if (a1 == null && a2 != null && a2.size() == 0) {
            return true;
        }
        if (a2 == null && a1 != null && a1.size() == 0) {
            return true;
        }
        if (a1 != null && a2 != null && a1.size() == 0 && a1.size() == a2.size()) {
            return true;
        }
        if (a1 != null && a2 != null && a1.size() == a2.size() && a1.containsAll(a2)) {
            return true;
        }
        return false;
    }

}
