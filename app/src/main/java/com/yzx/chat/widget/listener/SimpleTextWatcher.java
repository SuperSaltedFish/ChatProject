package com.yzx.chat.widget.listener;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by YZX on 2018年06月27日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public abstract class SimpleTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
