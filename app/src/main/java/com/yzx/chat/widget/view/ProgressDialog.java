package com.yzx.chat.widget.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.yzx.chat.R;

/**
 * Created by YZX on 2018年02月12日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ProgressDialog extends Dialog {

    private TextView mTvHint;
    private Context mContext;


    public ProgressDialog(@NonNull Context context, CharSequence hintText) {
        this(context, hintText, false);

    }

    public ProgressDialog(@NonNull Context context, CharSequence hintText, boolean isCancelable) {
        super(context);
        mContext = context;
        setContentView(R.layout.dialog_progress);
        setCancelable(isCancelable);
        mTvHint = findViewById(R.id.ProgressDialog_mTvHint);
        mTvHint.setText(hintText);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.bg_progress_dialog));
        }
    }

    public void setHintText(CharSequence hintText) {
        mTvHint.setText(hintText);
    }

    @Override
    public void show() {
        if(!isShowing()){
            super.show();
        }
    }


    public void show(String hintText) {
        mTvHint.setText(hintText);
        show();
    }
}
