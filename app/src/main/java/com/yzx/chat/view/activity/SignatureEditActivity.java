package com.yzx.chat.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;

/**
 * Created by YZX on 2018年02月09日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class SignatureEditActivity extends BaseCompatActivity {

    public static final String INTENT_EXTRA_SIGNATURE_CONTENT = "SignatureContent";
    public static final int RESULT_CODE = SignatureEditActivity.class.hashCode();

    private static final int MAX_SIGNATURE_LENGTH = 200;

    private EditText mEtSignatureContent;
    private TextView mTvTextLengthHint;
    private Button mBtnConfirm;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_signature_edit;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mEtSignatureContent = findViewById(R.id.SignatureEditActivity_mEtSignatureContent);
        mTvTextLengthHint = findViewById(R.id.SignatureEditActivity_mTvTextLengthHint);
        mBtnConfirm = findViewById(R.id.SignatureEditActivity_mBtnConfirm);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mEtSignatureContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_SIGNATURE_LENGTH)});
        mEtSignatureContent.addTextChangedListener(mTextWatcher);
        mBtnConfirm.setOnClickListener(mOnConfirmClickListener);

        String content = getIntent().getStringExtra(INTENT_EXTRA_SIGNATURE_CONTENT);
        if (TextUtils.isEmpty(content)) {
            mTvTextLengthHint.setText(String.valueOf(MAX_SIGNATURE_LENGTH));
        } else {
            mEtSignatureContent.setText(content);
            mTvTextLengthHint.setText(String.valueOf(MAX_SIGNATURE_LENGTH - content.length()));
        }

    }

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mTvTextLengthHint.setText(String.valueOf(MAX_SIGNATURE_LENGTH - s.length()));
        }
    };

    private final View.OnClickListener mOnConfirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.putExtra(INTENT_EXTRA_SIGNATURE_CONTENT, mEtSignatureContent.getText().toString());
            setResult(RESULT_CODE, intent);
            finish();
        }
    };
}
