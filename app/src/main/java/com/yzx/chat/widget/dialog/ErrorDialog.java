package com.yzx.chat.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import cn.swiftpass.standardwallet.R;
import cn.swiftpass.standardwallet.widget.listener.OnOnlySingleClickListener;

/**
 * Created by 叶智星 on 2018年09月21日.
 * 每一个不曾起舞的日子，都是对生命的辜负。
 */
public class ErrorDialog extends Dialog {

    private Button mBtnCancel;
    private TextView mTvError;

    private View.OnClickListener mOnCancelClickListener;

    public ErrorDialog(@NonNull Context context) {
        super(context,R.style.AlertDialogStyle);
        init();
    }


    private void init() {
        setContentView(R.layout.dialog_error);
        mBtnCancel = findViewById(R.id.ErrorDialog_mBtnCancel);
        mTvError = findViewById(R.id.ErrorDialog_mTvHint);

        mBtnCancel.setOnClickListener(new OnOnlySingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            if (mOnCancelClickListener != null) {
                mOnCancelClickListener.onClick(v);
            }
            if (isShowing()) {
                dismiss();
            }
        }
        });
    }

    public ErrorDialog setContentText(String content) {
        mTvError.setText(content);
        return this;
    }

    public ErrorDialog setNegativeButton(String title, View.OnClickListener listener) {
        mBtnCancel.setText(title);
        mOnCancelClickListener = listener;
        return this;
    }

}
