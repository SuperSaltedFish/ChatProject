package com.yzx.chat.view.activity;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;

/**
 * Created by YZX on 2018年01月29日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class StrangerProfileActivity extends BaseCompatActivity {


    @Override
    protected int getLayoutID() {
        return R.layout.activity_stranger_profile;
    }

    @Override
    protected void init() {

    }

    @Override
    protected void setup() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setTitle(null);
        }

    }
}
