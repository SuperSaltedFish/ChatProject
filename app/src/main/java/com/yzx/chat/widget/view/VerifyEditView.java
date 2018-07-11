package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yzx on 2017年05月12日
 * 当你将信心放在自己身上时，你将永远充满力量
 */

public class VerifyEditView extends LinearLayout {

    private Context mContext;
    private List<EditItem> mEditTextList;
    private OnInputListener mOnInputListener;

    private int mItemCount;
    private int mItemMinWidth;
    private int mItemSpace;
    private float mTextSize;
    private int mTextColor;
    private Drawable mItemBackground;
    private boolean isPasswordMode;

    public VerifyEditView(Context context) {
        this(context, null);
    }

    public VerifyEditView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerifyEditView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setGravity(Gravity.CENTER);
        initDefault();
        update();
    }

    private void initDefault() {
        mItemSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, mContext.getResources().getDisplayMetrics());
        mItemMinWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, mContext.getResources().getDisplayMetrics());
        mTextColor = Color.BLACK;
        mItemBackground = null;
        isPasswordMode = false;
        mItemCount = 5;
        mEditTextList = new ArrayList<>();
        this.setOnClickListener(mOnClickListener);
    }

    public void update() {
        mEditTextList.clear();
        removeAllViews();
        for (int i = 0; i < mItemCount; i++) {
            EditItem editItem = new EditItem(mContext, i);
            LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            if (i != 0) {
                params.setMarginStart(mItemSpace);
            }
            editItem.setLayoutParams(params);
            editItem.setGravity(Gravity.CENTER);
            editItem.setSingleLine();
            editItem.setMinWidth(mItemMinWidth);
            editItem.setTextColor(mTextColor);
            editItem.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
            editItem.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
            if (mItemBackground != null) {
                editItem.setBackground(mItemBackground);
            }
            if (isPasswordMode) {
                editItem.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            } else {
                editItem.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
            mEditTextList.add(editItem);
            addView(editItem);
        }
    }

    public String getCurrentInputContent(){
        StringBuilder builder = new StringBuilder(mEditTextList.size()+1);
        for (EditText ed : mEditTextList) {
            builder.append(ed.getText());
        }
        return builder.toString();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (int i = 0, count = getChildCount(); i < count; i++) {
            getChildAt(i).setEnabled(enabled);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText editText = mEditTextList.get(mItemCount - 1);
            for (EditText ed : mEditTextList) {
                if (TextUtils.isEmpty(ed.getText().toString())) {
                    editText = ed;
                    break;
                }
            }
            InputMethodManager inputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            editText.requestFocus();
            assert inputManager != null;
            inputManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    };

    public VerifyEditView setItemSpace(int itemSpace) {
        mItemSpace = itemSpace;
        return this;
    }

    public VerifyEditView setTextSize(float textSize) {
        mTextSize = textSize;
        return this;
    }

    public VerifyEditView setTextColor(@ColorInt int textColor) {
        mTextColor = textColor;
        return this;
    }

    public VerifyEditView setItemBackground(Drawable itemBackground) {
        mItemBackground = itemBackground;
        return this;
    }

    public VerifyEditView setPasswordMode(boolean passwordMode) {
        isPasswordMode = passwordMode;
        return this;
    }

    public VerifyEditView setOnInputListener(OnInputListener onInputListener) {
        mOnInputListener = onInputListener;
        return this;
    }

    public VerifyEditView setItemMinWidth(int itemMinWidth) {
        mItemMinWidth = itemMinWidth;
        return this;
    }

    private class EditItem extends EditText {
        private int mPosition;

        public EditItem(Context context, int position) {
            super(context);
            mPosition = position;
            addTextChangedListener(mTextWatcher);
        }

        @Override
        public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
            BackInputConnection backInputConnection = new BackInputConnection(super.onCreateInputConnection(outAttrs), true);
            backInputConnection.setBackspaceListener(new BackInputConnection.BackspaceListener() {
                @Override
                public boolean onBackspace() {
                    if (TextUtils.isEmpty(getText())) {
                        if (mPosition > 0) {
                            mEditTextList.get(mPosition - 1).requestFocus();
                            mEditTextList.get(mPosition - 1).setText("");
                        }
                    } else {
                        setText("");
                    }
                    return true;
                }
            });
            return backInputConnection;
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
                if (mOnInputListener != null) {
                    mOnInputListener.onInputChange(getCurrentInputContent());
                }
                if (!TextUtils.isEmpty(s)) {
                    if (mPosition + 1 < mEditTextList.size()) {
                        mEditTextList.get(mPosition + 1).requestFocus();
                    } else if (mOnInputListener != null) {
                        StringBuilder stringBuilder = new StringBuilder(mEditTextList.size());
                        for (int i = 0, size = mEditTextList.size(); i < size; i++) {
                            stringBuilder.append(mEditTextList.get(i).getText());
                        }
                        mOnInputListener.onInputComplete(stringBuilder.toString());
                    }

                }
            }
        };

    }

    public interface OnInputListener {
        void onInputComplete(String content);

        void onInputChange(String content);
    }


}
