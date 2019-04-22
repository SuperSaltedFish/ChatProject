package com.yzx.chat.module.common.view;


import android.os.Bundle;

import com.amap.api.services.core.PoiItem;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.broadcast.BackPressedReceive;

/**
 * Created by YZX on 2018年02月28日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class LocationMapActivity extends BaseCompatActivity {

    public static final int RESULT_CODE = LocationMapActivity.class.hashCode();
    public static final String INTENT_EXTRA_POI = "POI";


    @Override
    protected int getLayoutID() {
        return R.layout.activity_location_map;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        setDisplayHomeAsUpEnabled(true);


        PoiItem poiItem = getIntent().getParcelableExtra(INTENT_EXTRA_POI);
        if (poiItem != null) {
            setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_LIGHT_BAR_STATUS);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, LocationShareFragment.newInstance(poiItem))
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new LocationSendFragment())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (!BackPressedReceive.sendBackPressedEvent(LocationMapActivity.class.getName())) {
            super.onBackPressed();
        }
    }
}
