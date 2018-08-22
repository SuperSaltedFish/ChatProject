package com.yzx.chat.mvp.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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


public class EditContactTagsActivity extends BaseCompatActivity {

    public static final String INTENT_EXTRA_LABEL = "Label";
    public static final String INTENT_EXTRA_SELECTABLE_LABEL = "SelectableLabel";
    public static final int RESULT_CODE = 1;

    private FlowLayout mFlowLayoutSelected;
    private FlowLayout mFlowLayoutSelectable;
    private LabelEditText mEtInput;
    private TextView mTvSelectionLabelTitle;
    private Drawable mCloseDrawable;
    private ArrayList<String> mLabels;
    private ArrayList<String> mSelectableLabels;

    private int mCurrentSelectedID = -1;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_edit_contact_tags;
    }

    @Override
    protected void onPause() {
        super.onPause();
        super.hideSoftKeyboard();
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mFlowLayoutSelected = findViewById(R.id.EditContactTagsActivity_mFlowLayoutSelected);
        mFlowLayoutSelectable = findViewById(R.id.EditContactTagsActivity_mFlowLayoutSelectable);
        mEtInput = findViewById(R.id.EditContactTagsActivity_mEtInput);
        mTvSelectionLabelTitle = findViewById(R.id.EditContactTagsActivity_mTvSelectionLabelTitle);
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

        mFlowLayoutSelected.setOnClickListener(mOnFlowLayoutClickListener);
        mFlowLayoutSelected.setItemSpace((int) AndroidUtil.dip2px(8));
        mFlowLayoutSelected.setLineSpace((int) AndroidUtil.dip2px(4));

        mFlowLayoutSelectable.setItemSpace((int) AndroidUtil.dip2px(8));
        mFlowLayoutSelectable.setLineSpace((int) AndroidUtil.dip2px(4));

        int size = (int) AndroidUtil.dip2px(12);
        mCloseDrawable.setBounds(0, 0, size, size);
        mCloseDrawable.setTint(Color.WHITE);

        setData();
    }

    private void setData() {
        Intent intent = getIntent();
        mLabels = intent.getStringArrayListExtra(INTENT_EXTRA_LABEL);
        mSelectableLabels = intent.getStringArrayListExtra(INTENT_EXTRA_SELECTABLE_LABEL);

        if (mSelectableLabels != null && mSelectableLabels.size() > 0) {
            for (String tag : mSelectableLabels) {
                addSelectableLabelTextView(tag);
            }
        } else {
            mTvSelectionLabelTitle.setVisibility(View.INVISIBLE);
        }

        if (mLabels == null) {
            mLabels = new ArrayList<>(6);
        } else {
            if (mLabels.size() != 0) {
                for (String tag : mLabels) {
                    addNewLabelTextView(tag, false);
                }
            }
        }
    }

    private void tryAddNewLabelTextView() {
        String labelContent = mEtInput.getText().toString();
        if (TextUtils.isEmpty(labelContent)) {
            return;
        }
        addNewLabelTextView(labelContent, true);
        mEtInput.setText("");
    }

    private void addNewLabelTextView(String labelContent, boolean isChecked) {
        if (isChecked) {
            if (mLabels.contains(labelContent)) {
                showToast(getString(R.string.EditContactLabelActivity_LabelExists));
                return;
            } else {
                mLabels.add(labelContent);
            }
        }
        TextView textView = (TextView) getLayoutInflater().inflate(R.layout.item_label_selected, mFlowLayoutSelected, false);
        textView.setText(labelContent);
        int lastIndex = mFlowLayoutSelected.getChildCount();
        if (lastIndex != 0) {
            lastIndex--;
        }
        textView.setId(textView.hashCode());
        textView.setOnClickListener(mOnLabelClickListener);
        mFlowLayoutSelected.addView(textView, lastIndex);

        if (mSelectableLabels != null && mSelectableLabels.size() > 0) {
            int index = mSelectableLabels.indexOf(labelContent);
            if (index >= 0) {
                mFlowLayoutSelectable.getChildAt(index).setSelected(true);
            }
        }
    }

    private void removeLabel(TextView labelView) {
        String label = labelView.getText().toString();
        mFlowLayoutSelected.removeView(labelView);
        mLabels.remove(label);

        if (mSelectableLabels != null && mSelectableLabels.size() > 0) {
            int index = mSelectableLabels.indexOf(label);
            if (index >= 0) {
                mFlowLayoutSelectable.getChildAt(index).setSelected(false);
            }
        }
    }

    private void removeLabel(String label) {
        for (int i = 0, count = mFlowLayoutSelected.getChildCount() - 1; i < count; i++) {
            TextView labelView = (TextView) mFlowLayoutSelected.getChildAt(i);
            if (label.contentEquals(labelView.getText())) {
                removeLabel(labelView);
                return;
            }
        }
    }

    private void addSelectableLabelTextView(String labelContent) {
        TextView textView = (TextView) getLayoutInflater().inflate(R.layout.item_label_selectable, mFlowLayoutSelected, false);
        textView.setText(labelContent);
        textView.setOnClickListener(mOnSelectionLabelClickListener);
        mFlowLayoutSelectable.addView(textView);
    }


    private void setLabelSelected(TextView labelView, boolean isSelected) {
        if (labelView == null || labelView.isSelected() == isSelected) {
            return;
        }
        if (isSelected) {
            labelView.setCompoundDrawablePadding((int) AndroidUtil.dip2px(2));
            labelView.setCompoundDrawables(null, null, mCloseDrawable, null);
            labelView.setPadding(labelView.getPaddingStart(), labelView.getPaddingTop(), (int) AndroidUtil.dip2px(6), labelView.getPaddingBottom());
        } else {
            labelView.setPadding(labelView.getPaddingStart(), labelView.getPaddingTop(), labelView.getPaddingStart(), labelView.getPaddingBottom());
            labelView.setCompoundDrawables(null, null, null, null);
        }
        labelView.setSelected(isSelected);
        mCurrentSelectedID = labelView.getId();
    }

    private void confirm() {
        String mCurrentInputContent = mEtInput.getText().toString();
        if (!TextUtils.isEmpty(mCurrentInputContent) && !mLabels.contains(mCurrentInputContent)) {
            mLabels.add(mCurrentInputContent);
        }
        Intent intent = new Intent();
        intent.putStringArrayListExtra(INTENT_EXTRA_LABEL, mLabels);
        setResult(RESULT_CODE, intent);
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

    private final View.OnClickListener mOnFlowLayoutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            tryAddNewLabelTextView();
        }
    };

    private final View.OnClickListener mOnLabelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == mCurrentSelectedID) {
                removeLabel((TextView) v);
                mCurrentSelectedID = -1;
            } else {
                if (mCurrentSelectedID > 0) {
                    setLabelSelected((TextView) mFlowLayoutSelected.findViewById(mCurrentSelectedID), false);
                }
                setLabelSelected((TextView) v, true);
            }
        }
    };

    private final View.OnClickListener mOnSelectionLabelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView view = (TextView) v;
            String label = view.getText().toString();
            if (mLabels.contains(label)) {
                removeLabel(label);
            } else {
                addNewLabelTextView(label, true);
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
                setLabelSelected((TextView) mFlowLayoutSelected.findViewById(mCurrentSelectedID), false);
                mCurrentSelectedID = -1;
            }
        }
    };

    private final TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction())) {
                tryAddNewLabelTextView();
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
                int childCount = mFlowLayoutSelected.getChildCount();
                if (childCount <= 1) {
                    return false;
                }
                if (mCurrentSelectedID > 0) {
                    View selectedView = mFlowLayoutSelected.findViewById(mCurrentSelectedID);
                    if (mFlowLayoutSelected.indexOfChild(selectedView) == childCount - 2) {
                        removeLabel((TextView) selectedView);
                    } else {
                        setLabelSelected((TextView) selectedView, false);
                        setLabelSelected((TextView) mFlowLayoutSelected.getChildAt(childCount - 2), true);
                    }
                } else {
                    setLabelSelected((TextView) mFlowLayoutSelected.getChildAt(childCount - 2), true);
                }
            }
            return true;
        }
    };
}
