package com.yzx.chat.view.activity;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.PoiItem;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.contract.LocationMapActivityContract;
import com.yzx.chat.presenter.LocationMapActivityPresenter;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.widget.adapter.LocationAdapter;
import com.yzx.chat.widget.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年02月28日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class LocationMapActivity extends BaseCompatActivity<LocationMapActivityContract.Presenter> implements LocationMapActivityContract.View {

    private MapView mMapView;
    private SearchView mSearchView;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private RecyclerView mRvSearchLocation;
    private ConstraintLayout mClLocationLayout;
    private LocationAdapter mCurrentLocationAdapter;
    private LocationAdapter mSearchLocationAdapter;
    private Handler mSearchHandler;
    private List<PoiItem> mCurrentLocationList;
    private List<PoiItem> mSearchTipList;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_location_map;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mMapView = findViewById(R.id.LocationMapActivity_mMapView);
        mRvSearchLocation = findViewById(R.id.LocationMapActivity_mRvSearchLocation);
        mClLocationLayout = findViewById(R.id.LocationMapActivity_mClLocationLayout);
        mCurrentLocationList = new ArrayList<>(20);
        mSearchTipList = new ArrayList<>(20);
        mCurrentLocationAdapter = new LocationAdapter(mCurrentLocationList);
        mSearchLocationAdapter = new LocationAdapter(mSearchTipList);
        mSearchHandler = new Handler();
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mRvSearchLocation.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRvSearchLocation.setAdapter(mSearchLocationAdapter);
        mRvSearchLocation.setHasFixedSize(true);
        mRvSearchLocation.addItemDecoration(new DividerItemDecoration(1, ContextCompat.getColor(this, R.color.divider_color_black)));

        AMap aMap = mMapView.getMap();
        aMap.setMyLocationStyle(new MyLocationStyle()
                .myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
                .myLocationIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_location_point))));
        aMap.setMyLocationEnabled(true);
        aMap.setOnMyLocationChangeListener(mOnMyLocationChangeListener);
        aMap.setOnCameraChangeListener(mOnCameraChangeListener);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(3));

        UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);

        mMapView.onCreate(savedInstanceState);

    }

    private void setupSearchView() {
        mSearchView.setQueryHint("请输入搜索内容...");
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClLocationLayout.setVisibility(View.GONE);
                mRvSearchLocation.setVisibility(View.VISIBLE);
            }
        });
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mClLocationLayout.setVisibility(View.VISIBLE);
                mRvSearchLocation.setVisibility(View.GONE);
                return false;
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                mSearchHandler.removeCallbacksAndMessages(null);
                if (TextUtils.isEmpty(newText)) {
                    showNewSearchContent(null);
                } else {
                    mSearchHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mPresenter.searchPOIByKeyword(newText);
                        }
                    }, 300);
                }
                return false;
            }
        });

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_location_map, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchAutoComplete = mSearchView.findViewById(R.id.search_src_text);
        setupSearchView();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!mSearchView.isIconified()) {
                    try {
                        mSearchAutoComplete.setText("");
                        mSearchView.setIconified(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    super.onOptionsItemSelected(item);
                }

                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    private final AMap.OnMyLocationChangeListener mOnMyLocationChangeListener = new AMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LogUtil.e(location.toString());
        }
    };

    private final AMap.OnCameraChangeListener mOnCameraChangeListener = new AMap.OnCameraChangeListener() {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {

        }

        @Override
        public void onCameraChangeFinish(CameraPosition cameraPosition) {
            LogUtil.e("onCameraChangeFinish " + cameraPosition.toString());
        }
    };

    @Override
    public LocationMapActivityContract.Presenter getPresenter() {
        return new LocationMapActivityPresenter();
    }

    @Override
    public Context getContent() {
        return this;
    }

    @Override
    public void showNewSearchContent(List<PoiItem> poiItemList) {
        mSearchLocationAdapter.notifyDataSetChanged();
        mSearchTipList.clear();
        if (poiItemList != null && poiItemList.size() > 0) {
            mSearchTipList.addAll(poiItemList);
        }
    }
    //showmore
}
