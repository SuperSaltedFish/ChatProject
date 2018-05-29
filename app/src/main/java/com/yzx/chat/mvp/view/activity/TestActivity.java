package com.yzx.chat.mvp.view.activity;


import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.util.GlideUtil;


public class TestActivity extends BaseCompatActivity {


    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {


    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    public void onClick(View v) {

    }

}


