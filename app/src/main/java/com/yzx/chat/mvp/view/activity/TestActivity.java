package com.yzx.chat.mvp.view.activity;

import android.os.Bundle;
import android.view.View;

import com.amap.api.maps.MapView;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.tool.DirectoryHelper;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.view.RoundLinearLayout;


public class TestActivity extends BaseCompatActivity {

    private MapView mMapView;
    private RoundLinearLayout mLlLine;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mMapView = findViewById(R.id.LocationMapActivity_mMapView);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_LIGHT_BAR_STATUS);
        String path = DirectoryHelper.getPublicTempPath()+"map_style.data";
        if(AndroidUtil.rawResToLocalFile(R.raw.map_style,path)){
            mMapView.getMap().setCustomMapStylePath(path);
            mMapView.getMap().setMapCustomEnable(true);
        }


        mMapView.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    public void onClick(View v) {


    }

    public void onClick1(View v) {

    }
}


