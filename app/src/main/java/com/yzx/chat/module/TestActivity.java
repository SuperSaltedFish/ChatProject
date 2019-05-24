package com.yzx.chat.module;

import android.Manifest;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;

import java.util.Locale;


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

        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.locale = Locale.ENGLISH; //设置语言
        resources.updateConfiguration(config, dm);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    public void onClick1(View v) {
        requestPermissionsInCompatMode(new String[]{Manifest.permission.CAMERA}, 100);

    }


}


