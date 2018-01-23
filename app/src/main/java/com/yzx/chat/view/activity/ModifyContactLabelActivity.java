package com.yzx.chat.view.activity;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.widget.view.BackInputConnection;
import com.yzx.chat.widget.view.FlowLayout;
import com.yzx.chat.widget.view.LabelEditText;

/**
 * Created by YZX on 2018年01月23日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class ModifyContactLabelActivity extends BaseCompatActivity {

    private FlowLayout mFlowLayout;
    private LabelEditText mEtInput;

    private int mCurrentSelectedID = -1;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_modify_contact_label;
    }

    @Override
    protected void init() {
        mFlowLayout = findViewById(R.id.ModifyContactLabelActivity_mFlowLayout);
        mEtInput = findViewById(R.id.ModifyContactLabelActivity_mEtInput);
    }

    @Override
    protected void setup() {
        mEtInput.setBackspaceListener(mBackspaceListener);
        mEtInput.setOnEditorActionListener(mOnEditorActionListener);
        mFlowLayout.setOnClickListener(mOnFlowLayoutClickListener);
    }

    private void addNewLabelTextView(CharSequence labelContent) {
        TextView textView = (TextView) getLayoutInflater().inflate(R.layout.item_profile_label, mFlowLayout, false);
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
    private void setLabelSelected(TextView labelView,boolean isSelected){
        if(labelView==null||labelView.isSelected()==isSelected){
            return;
        }
        if(isSelected){
            labelView.setText(labelView.getText() + " x");
        }else {
            labelView.setText(labelView.getText().toString().replace(" x",""));
        }
        labelView.setSelected(isSelected);
        mCurrentSelectedID = labelView.getId();
    }


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
                    setLabelSelected((TextView) mFlowLayout.findViewById(mCurrentSelectedID),false);
                }
                setLabelSelected((TextView) v,true);
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
                if(childCount<=1){
                    return false;
                }
                if(mCurrentSelectedID>0){
                    View selectedView = mFlowLayout.findViewById(mCurrentSelectedID);
                    if(mFlowLayout.indexOfChild(selectedView)==childCount-2){
                        mFlowLayout.removeView(selectedView);
                    }else {
                        setLabelSelected((TextView) selectedView,false);
                        setLabelSelected((TextView) mFlowLayout.getChildAt(childCount-2),true);
                    }
                }else {
                    setLabelSelected((TextView) mFlowLayout.getChildAt(childCount-2),true);
                }
            }
            return true;
        }
    };
}
