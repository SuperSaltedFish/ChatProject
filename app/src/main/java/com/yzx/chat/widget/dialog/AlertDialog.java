package com.yzx.chat.widget.dialog;

import android.content.Context;
import android.content.DialogInterface;

import com.yzx.chat.R;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

/**
 * Created by YZX on 2019年05月06日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class AlertDialog {

    private androidx.appcompat.app.AlertDialog.Builder mBuilder;
    private androidx.appcompat.app.AlertDialog mDialog;

    public AlertDialog(@NonNull Context context) {
        mBuilder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.AlertDialogStyle);
    }

    public AlertDialog setTitle(@StringRes int title) {
        return setTitle(mBuilder.getContext().getString(title));
    }

    public AlertDialog setTitle(String title) {
        mBuilder.setTitle(title);
        return this;
    }

    public AlertDialog setMessage(@StringRes int message) {
        return setMessage(mBuilder.getContext().getString(message));
    }

    public AlertDialog setMessage(String message) {
        mBuilder.setMessage(message);
        return this;
    }

    public AlertDialog setNegativeButton(@StringRes int text, DialogInterface.OnClickListener listener) {
        return setNegativeButton(mBuilder.getContext().getString(text), listener);
    }

    public AlertDialog setNegativeButton(String text, DialogInterface.OnClickListener listener) {
        mBuilder.setNegativeButton(text, listener);
        return this;
    }

    public AlertDialog setPositiveButton(@StringRes int text, DialogInterface.OnClickListener listener) {
        return setPositiveButton(mBuilder.getContext().getString(text), listener);
    }

    public AlertDialog setPositiveButton(String text, DialogInterface.OnClickListener listener) {
        mBuilder.setPositiveButton(text, listener);
        return this;
    }

    public void show() {
        mDialog = mBuilder.show();
    }

    public void dismiss() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

}
