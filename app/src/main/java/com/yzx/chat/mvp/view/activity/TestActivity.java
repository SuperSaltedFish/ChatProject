package com.yzx.chat.mvp.view.activity;


import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;


import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.configure.GlideApp;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.widget.view.GlideHexagonTransform;


public class TestActivity extends BaseCompatActivity {


    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        GlideApp.with(this).load(R.drawable.temp_share_image)
                .transform(new GlideHexagonTransform())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into((ImageView) findViewById(R.id.aaaa));
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

}


