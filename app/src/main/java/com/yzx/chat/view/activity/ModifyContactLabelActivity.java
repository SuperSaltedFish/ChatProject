package com.yzx.chat.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.view.BackInputConnection;
import com.yzx.chat.widget.view.FlowLayout;
import com.yzx.chat.widget.view.LabelEditText;

import java.util.ArrayList;

/**
 * Created by YZX on 2018年01月23日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class ModifyContactLabelActivity extends BaseCompatActivity {

    public static final String INTENT_EXTRA_LABEL = "Label";
    public static final int RESULT_CODE = 1;

    private FlowLayout mFlowLayout;
    private LabelEditText mEtInput;
    private Button mBtnConfirm;
    private Drawable mCloseDrawable;

    private int mCurrentSelectedID = -1;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_modify_contact_label;
    }

    @Override
    protected void onPause() {
        super.onPause();
        super.hideSoftKeyboard();
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mFlowLayout = findViewById(R.id.ModifyContactLabelActivity_mFlowLayout);
        mEtInput = findViewById(R.id.ModifyContactLabelActivity_mEtInput);
        mBtnConfirm = findViewById(R.id.ModifyContactLabelActivity_mBtnConfirm);
        mCloseDrawable = getDrawable(R.drawable.ic_close);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mEtInput.setBackspaceListener(mBackspaceListener);
        mEtInput.setOnEditorActionListener(mOnEditorActionListener);
        mEtInput.addTextChangedListener(mTextWatcher);
        mFlowLayout.setOnClickListener(mOnFlowLayoutClickListener);
        mFlowLayout.setItemSpace((int) AndroidUtil.dip2px(8));
        mFlowLayout.setLineSpace((int) AndroidUtil.dip2px(4));

        int size = (int) AndroidUtil.dip2px(16);
        mCloseDrawable.setBounds(0, 0, size, size);
        mCloseDrawable.setTint(Color.WHITE);

        mBtnConfirm.setOnClickListener(mOnConfirmClickListener);

        setData();
    }

    private void setData() {
        Intent intent = getIntent();
        ArrayList<String> tags = intent.getStringArrayListExtra(INTENT_EXTRA_LABEL);
        if (tags != null && tags.size() != 0) {
            for (String tag : tags) {
                addNewLabelTextView(tag);
            }
        }
    }

    private void addNewLabelTextView(CharSequence labelContent) {
        TextView textView = (TextView) getLayoutInflater().inflate(R.layout.item_label, mFlowLayout, false);
        textView.setText(labelContent);
        int lastIndex = mFlowLayout.getChildCount();
        if (lastIndex != 0) {
            lastIndex--;
        }
        textView.setId(textView.hashCode());
        textView.setOnClickListener(mOnLabelClickListener);
        mFlowLayout.addView(textView, lastIndex);
        mEtInput.setText("");
    }


    private void setLabelSelected(TextView labelView, boolean isSelected) {
        if (labelView == null || labelView.isSelected() == isSelected) {
            return;
        }
        if (isSelected) {
            labelView.setCompoundDrawables(null, null, mCloseDrawable, null);
            labelView.setPadding(labelView.getPaddingStart(), labelView.getPaddingTop(), (int) AndroidUtil.dip2px(4), labelView.getPaddingBottom());
        } else {
            labelView.setPadding(labelView.getPaddingStart(), labelView.getPaddingTop(), labelView.getPaddingStart(), labelView.getPaddingBottom());
            labelView.setCompoundDrawables(null, null, null, null);
        }
        labelView.setSelected(isSelected);
        mCurrentSelectedID = labelView.getId();
    }

    private final View.OnClickListener mOnConfirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ArrayList<String> tags = new ArrayList<>();
            for (int i = 0, count = mFlowLayout.getChildCount() - 1; i < count; i++) {
                TextView labelView = (TextView) mFlowLayout.getChildAt(i);
                tags.add(labelView.getText().toString());
            }
            Intent intent = new Intent();
            intent.putStringArrayListExtra(INTENT_EXTRA_LABEL, tags);
            setResult(RESULT_CODE, intent);
            finish();
        }
    };


    private final View.OnClickListener mOnFlowLayoutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CharSequence labelContent = mEtInput.getText();
            if (TextUtils.isEmpty(labelContent)) {
                return;
            }
            addNewLabelTextView(labelContent);
        }
    };

    private final View.OnClickListener mOnLabelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == mCurrentSelectedID) {
                mFlowLayout.removeView(v);
                mCurrentSelectedID = -1;
            } else {
                if (mCurrentSelectedID > 0) {
                    setLabelSelected((TextView) mFlowLayout.findViewById(mCurrentSelectedID), false);
                }
                setLabelSelected((TextView) v, true);
            }
        }
    };

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mCurrentSelectedID > 0) {
                setLabelSelected((TextView) mFlowLayout.findViewById(mCurrentSelectedID), false);
                mCurrentSelectedID = -1;
            }
        }
    };


    private final TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction())) {
                CharSequence labelContent = mEtInput.getText();
                if (!TextUtils.isEmpty(labelContent)) {
                    addNewLabelTextView(labelContent);
                }
            }
            return true;
        }
    };

    private final BackInputConnection.BackspaceListener mBackspaceListener = new BackInputConnection.BackspaceListener() {
        @Override
        public boolean onBackspace() {
            CharSequence labelContent = mEtInput.getText();
            if (!TextUtils.isEmpty(labelContent)) {
                return false;
            } else {
                int childCount = mFlowLayout.getChildCount();
                if (childCount <= 1) {
                    return false;
                }
                if (mCurrentSelectedID > 0) {
                    View selectedView = mFlowLayout.findViewById(mCurrentSelectedID);
                    if (mFlowLayout.indexOfChild(selectedView) == childCount - 2) {
                        mFlowLayout.removeView(selectedView);
                    } else {
                        setLabelSelected((TextView) selectedView, false);
                        setLabelSelected((TextView) mFlowLayout.getChildAt(childCount - 2), true);
                    }
                } else {
                    setLabelSelected((TextView) mFlowLayout.getChildAt(childCount - 2), true);
                }
            }
            return true;
        }
    };
}
