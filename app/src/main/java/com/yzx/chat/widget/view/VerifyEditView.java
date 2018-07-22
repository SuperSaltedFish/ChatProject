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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.yzx.chat.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yzx on 2017年05月12日
 * 当你将信心放在自己身上时，你将永远充满力量
 */

public class VerifyEditView extends LinearLayout {

    private Context mContext;
    InputMethodManager mInputMethodManager;
    private List<EditText> mEditTextList;
    private OnInputListener mOnInputListener;

    private int mItemCount;
    private int mItemMinWidth;
    private int mItemSpace;
    private float mTextSize;
    private int mTextColor;
    private Drawable mItemBackground;
    private boolean isPasswordMode;

    private int mCurrentInputPosition;
    private StringBuilder mContent;

    public VerifyEditView(Context context) {
        this(context, null);
    }

    public VerifyEditView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerifyEditView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mInputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        setGravity(Gravity.CENTER);
        initDefault();
        update();
    }

    private void initDefault() {
        mItemSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 17, mContext.getResources().getDisplayMetrics());
        mItemMinWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, mContext.getResources().getDisplayMetrics());
        mTextColor = Color.BLACK;
        mItemBackground = null;
        isPasswordMode = false;
        mItemCount = 5;
        mEditTextList = new ArrayList<>();
        this.setOnClickListener(mOnClickListener);
    }

    private void update() {
        mEditTextList.clear();
        removeAllViews();
        if (mItemCount > 0) {
            for (int i = 0; i < mItemCount; i++) {
                EditText editItem = new EditItem(mContext);
                LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                if (i != 0) {
                    params.setMarginStart(mItemSpace);
                }
                editItem.setLayoutParams(params);
                editItem.setGravity(Gravity.CENTER);
                editItem.setSingleLine();
                editItem.setMinWidth(mItemMinWidth);
                editItem.setTextColor(mTextColor);
                editItem.setLongClickable(false);
                editItem.addTextChangedListener(mTextWatcher);
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
            mContent = new StringBuilder(mItemCount);
        }
    }

    public String getCurrentInputContent() {
        return mContent == null ? "" : mContent.toString();
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
            if (mCurrentInputPosition >= mItemCount) {
                return;
            }
            EditText editText = mEditTextList.get(mCurrentInputPosition);
            editText.requestFocus();
            if (mInputMethodManager != null) {
                mInputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    };

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (before == 0 && count == 0) {
                if (mCurrentInputPosition != 0) {
                    mCurrentInputPosition--;
                    EditText editText = mEditTextList.get(mCurrentInputPosition);
                    editText.requestFocus();
                    editText.setText("");
                    if (mOnInputListener != null) {
                        mOnInputListener.onInputChange(getCurrentInputContent());
                    }
                }
                return;
            } else if (before > 0 && count == 0) {
                mContent.deleteCharAt(mCurrentInputPosition);
            } else if (before == 0 && count > 0) {
                mContent.append(s);
                if (mCurrentInputPosition < mItemCount - 1) {
                    mCurrentInputPosition++;
                    mEditTextList.get(mCurrentInputPosition).requestFocus();
                } else if (mOnInputListener != null) {
                    mOnInputListener.onInputComplete(getCurrentInputContent());
                    return;
                }
            }
            if (mOnInputListener != null) {
                mOnInputListener.onInputChange(getCurrentInputContent());
            }

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public void setItemCount(int itemCount) {
        mItemCount = itemCount;
        update();
    }

    public void setItemSpace(int itemSpace) {
        mItemSpace = itemSpace;
        update();
    }

    public void setTextSize(float textSize) {
        mTextSize = textSize;
        update();
    }

    public void setTextColor(@ColorInt int textColor) {
        mTextColor = textColor;
        update();
    }

    public void setItemBackground(Drawable itemBackground) {
        mItemBackground = itemBackground;
        update();
    }

    public void setPasswordMode(boolean passwordMode) {
        isPasswordMode = passwordMode;
        update();
    }

    public void setOnInputListener(OnInputListener onInputListener) {
        mOnInputListener = onInputListener;
        update();
    }

    public void setItemMinWidth(int itemMinWidth) {
        mItemMinWidth = itemMinWidth;
        update();
    }

    private static class EditItem extends EditText {

        public EditItem(Context context) {
            super(context);
        }

        @Override
        public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
            BackInputConnection backInputConnection = new BackInputConnection(super.onCreateInputConnection(outAttrs), true);
            backInputConnection.setBackspaceListener(new BackInputConnection.BackspaceListener() {
                @Override
                public boolean onBackspace() {
                    if (TextUtils.isEmpty(getText())) {
                        setText("");
                        return true;
                    }
                    return false;
                }
            });
            return backInputConnection;
        }

    }

    public interface OnInputListener {
        void onInputComplete(String content);

        void onInputChange(String content);
    }


}
