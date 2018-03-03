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
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.PoiItem;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.contract.LocationMapActivityContract;
import com.yzx.chat.presenter.LocationMapActivityPresenter;
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
    private View mSearchLocationFooterView;
    private View mCurrentLocationFooterView;
    private TextView mTvSearchLocationLoadMoreHint;
    private TextView mTvCurrentLocationLoadMoreHint;
    private RecyclerView mRvSearchLocation;
    private RecyclerView mRvCurrentLocation;
    private ConstraintLayout mClLocationLayout;
    private LocationAdapter mCurrentLocationAdapter;
    private LocationAdapter mSearchLocationAdapter;
    private Handler mSearchHandler;
    private Marker mMapMarker;
    private List<PoiItem> mCurrentLocationList;
    private List<PoiItem> mSearchTipList;

    private boolean isPositionComplete;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_location_map;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mMapView = findViewById(R.id.LocationMapActivity_mMapView);
        mRvSearchLocation = findViewById(R.id.LocationMapActivity_mRvSearchLocation);
        mClLocationLayout = findViewById(R.id.LocationMapActivity_mClLocationLayout);
        mRvCurrentLocation = findViewById(R.id.LocationMapActivity_mRvCurrentLocation);
        mSearchLocationFooterView = getLayoutInflater().inflate(R.layout.view_load_more, (ViewGroup) getWindow().getDecorView(), false);
        mCurrentLocationFooterView = getLayoutInflater().inflate(R.layout.view_load_more, (ViewGroup) getWindow().getDecorView(), false);
        mTvSearchLocationLoadMoreHint = mSearchLocationFooterView.findViewById(R.id.LoadMoreView_mTvLoadMoreHint);
        mTvCurrentLocationLoadMoreHint = mCurrentLocationFooterView.findViewById(R.id.LoadMoreView_mTvLoadMoreHint);
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
        mSearchLocationAdapter.setScrollToBottomListener(mOnSearchScrollToBottomListener);

        mRvCurrentLocation.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRvCurrentLocation.setAdapter(mCurrentLocationAdapter);
        mRvCurrentLocation.setHasFixedSize(true);
        mRvCurrentLocation.addItemDecoration(new DividerItemDecoration(1, ContextCompat.getColor(this, R.color.divider_color_black)));

        setupMap(savedInstanceState);

    }


    private void setupMap(Bundle savedInstanceState) {
        AMap aMap = mMapView.getMap();
        aMap.setMyLocationStyle(new MyLocationStyle()
                .myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
                .myLocationIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_location_point)))
                .radiusFillColor(ContextCompat.getColor(this, R.color.location_radius_fill_color))
                .strokeColor(ContextCompat.getColor(this, R.color.colorAccent)));

        aMap.setMyLocationEnabled(true);
        aMap.setOnMyLocationChangeListener(mOnMyLocationChangeListener);
        aMap.setOnCameraChangeListener(mOnCameraChangeListener);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(4));

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(false);//可拖放性
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.temp_head_image));
        mMapMarker = aMap.addMarker(markerOptions);
        mMapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mMapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mMapMarker.setPositionByPixels(mMapView.getWidth() >> 1, mMapView.getHeight() >> 1);
            }
        });

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
                    showNewSearchLocation(null);
                } else {
                    mSearchHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mPresenter.searchPOIByKeyword(newText);
                        }
                    }, 250);
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
            isPositionComplete = true;
        }
    };

    private final AMap.OnCameraChangeListener mOnCameraChangeListener = new AMap.OnCameraChangeListener() {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
        }

        @Override
        public void onCameraChangeFinish(final CameraPosition cameraPosition) {
            if (isPositionComplete) {
                mSearchHandler.removeCallbacksAndMessages(null);
                mSearchHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPresenter.searchCurrentLocation(cameraPosition.target);
                    }
                }, 200);
            }
        }
    };

    private final BaseRecyclerViewAdapter.OnScrollToBottomListener mOnSearchScrollToBottomListener = new BaseRecyclerViewAdapter.OnScrollToBottomListener() {
        @Override
        public void OnScrollToBottom() {
            mPresenter.searchMorePOIByKeyword(mSearchAutoComplete.getText().toString());
        }
    };

    @Override
    public LocationMapActivityContract.Presenter getPresenter() {
        return new LocationMapActivityPresenter();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showNewCurrentLocation(List<PoiItem> poiItemList) {
        mSearchLocationAdapter.notifyDataSetChanged();
        mSearchTipList.clear();
        if (poiItemList != null && poiItemList.size() > 0) {
            mSearchTipList.addAll(poiItemList);
        }
    }

    @Override
    public void showMoreCurrentLocation(List<PoiItem> poiItemList, boolean hasMore) {

    }

    @Override
    public void showNewSearchLocation(List<PoiItem> poiItemList) {
        mSearchLocationAdapter.setFooterView(null);
        if (!mSearchLocationAdapter.isHasFooterView() && (poiItemList != null && poiItemList.size() >= Constants.SEARCH_LOCATION_PAGE_SIZE)) {
            mTvSearchLocationLoadMoreHint.setText(R.string.LoadMoreHint_LoadingMore);
            mSearchLocationAdapter.setFooterView(mSearchLocationFooterView);
        }
        mSearchLocationAdapter.notifyDataSetChanged();
        mSearchTipList.clear();
        if (poiItemList != null && poiItemList.size() > 0) {
            mSearchTipList.addAll(poiItemList);
        }
    }

    @Override
    public void showMoreSearchLocation(List<PoiItem> poiItemList, boolean hasMore) {
        if (hasMore) {
            mTvSearchLocationLoadMoreHint.setText(R.string.LoadMoreHint_LoadingMore);
        } else {
            mTvSearchLocationLoadMoreHint.setText(R.string.LoadMoreHint_NoMore);
        }
        mSearchLocationAdapter.notifyItemRangeInsertedEx(mSearchTipList.size(), poiItemList.size());
        mSearchTipList.addAll(poiItemList);
    }

}
