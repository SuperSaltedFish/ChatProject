package com.yzx.chat.mvp.view.activity;


import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.widget.view.VisualizerView;


public class TestActivity extends BaseCompatActivity {


    VisualizerView visualizerView;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        visualizerView = findViewById(R.id.ddd);
        visualizerView.setStrokeColor(ContextCompat.getColor(this, R.color.colorAccent));
    }

    @Override
    protected void setup(Bundle savedInstanceState) {

    }


    public void onClick(View v) {

    }

    public void onClick1(View v) {

    }
}


