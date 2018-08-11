package com.yzx.chat.mvp.view.fragment;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.PoiItem;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.mvp.contract.LocationSendContract;
import com.yzx.chat.mvp.presenter.LocationSendPresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.BitmapUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.widget.adapter.LocationAdapter;
import com.yzx.chat.widget.listener.AutoCloseKeyboardScrollListener;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.listener.SimpleTextWatcher;
import com.yzx.chat.widget.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年08月10日.
 * 如果你不给自己设限，世界上便没有限制你发挥的篱笆。
 */
public class LocationSendFragment extends BaseFragment<LocationSendContract.Presenter> implements LocationSendContract.View {

    private MapView mMapView;
    private EditText mEtSearch;
    private AMap mAMap;
    private PopupWindow mSearchPopupWindow;
    private RecyclerView mRvSearch;
    private RecyclerView mRvMarkert;
    private FrameLayout mFlSearchLayout;
    private CardView mCvMarkerLayout;
    private ProgressBar mPbMarker;
    private View mSearchLocationFooterView;
    private View mMarkerLocationFooterView;
    private TextView mTvSearchLocationLoadMoreHint;
    private TextView mTvMarkerLocationLoadMoreHint;
    private LocationAdapter mSearchLocationAdapter;
    private LocationAdapter mMarkerLocationAdapter;
    private List<PoiItem> mMarkerLocationList;
    private List<PoiItem> mSearchLocationList;
    private LatLng mMarkerLocationLatLng;

    private Handler mMarkerHandler;
    private Handler mSearchHandler;

    private int mSearchLocationPageNumber;
    private int mMarkerLocationPageNumber;
    private boolean isFirstLocateComplete;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_location__send;
    }

    @Override
    protected void init(View parentView) {
        mMapView = parentView.findViewById(R.id.LocationSendFragment_mMapView);
        mEtSearch = parentView.findViewById(R.id.LocationSendFragment_mEtSearch);
        mFlSearchLayout = parentView.findViewById(R.id.LocationSendFragment_mFlSearchLayout);
        mRvMarkert = parentView.findViewById(R.id.LocationSendFragment_mRvMarkerLocation);
        mPbMarker = parentView.findViewById(R.id.LocationSendFragment_mPbMarker);
        mCvMarkerLayout = parentView.findViewById(R.id.LocationSendFragment_mCvMarkerLayout);
        mSearchLocationFooterView = getLayoutInflater().inflate(R.layout.view_load_more, (ViewGroup) parentView, false);
        mMarkerLocationFooterView = getLayoutInflater().inflate(R.layout.view_load_more, (ViewGroup) parentView, false);
        mTvSearchLocationLoadMoreHint = mSearchLocationFooterView.findViewById(R.id.LoadMoreView_mTvLoadMoreHint);
        mTvMarkerLocationLoadMoreHint = mMarkerLocationFooterView.findViewById(R.id.LoadMoreView_mTvLoadMoreHint);
        mMarkerHandler = new Handler();
        mSearchHandler = new Handler();
        mRvSearch = new RecyclerView(mContext);
        mSearchLocationList = new ArrayList<>(32);
        mMarkerLocationList = new ArrayList<>(32);
        mSearchLocationAdapter = new LocationAdapter(mSearchLocationList);
        mMarkerLocationAdapter = new LocationAdapter(mMarkerLocationList);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        mRvMarkert.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRvMarkert.setAdapter(mMarkerLocationAdapter);
        mRvMarkert.setHasFixedSize(true);
        mRvMarkert.addItemDecoration(new DividerItemDecoration(1, ContextCompat.getColor(mContext, R.color.dividerColorBlack), DividerItemDecoration.ORIENTATION_HORIZONTAL));
        mRvMarkert.addOnItemTouchListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {

            }
        });
        mMarkerLocationAdapter.setScrollToBottomListener(new BaseRecyclerViewAdapter.OnScrollToBottomListener() {
            @Override
            public void OnScrollToBottom() {
                if (mMarkerLocationPageNumber != -1) {
                    mMarkerHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mPresenter.searchMarkerLocation(mMarkerLocationLatLng.latitude, mMarkerLocationLatLng.longitude, mMarkerLocationPageNumber);
                        }
                    });
                }
            }
        });

        mRvSearch.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRvSearch.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mRvSearch.setRecycledViewPool(mRvMarkert.getRecycledViewPool());
        mRvSearch.setAdapter(mSearchLocationAdapter);
        mRvSearch.addOnScrollListener(new AutoCloseKeyboardScrollListener((Activity) mContext));
        mRvSearch.addOnItemTouchListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(final int position, RecyclerView.ViewHolder viewHolder) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mSearchPopupWindow.dismiss();
                    }
                });
            }
        });
        mSearchLocationAdapter.setScrollToBottomListener(new BaseRecyclerViewAdapter.OnScrollToBottomListener() {
            @Override
            public void OnScrollToBottom() {
                if (mSearchLocationPageNumber != -1) {
                    mSearchHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mPresenter.searchPOIByKeyword(mEtSearch.getText().toString(), mSearchLocationPageNumber);
                        }
                    });
                }
            }
        });

        mEtSearch.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(final Editable s) {
                mSearchHandler.removeCallbacksAndMessages(null);
                mSearchHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSearchLocationPageNumber = 0;
                        mPresenter.searchPOIByKeyword(s.toString(), mSearchLocationPageNumber);
                    }
                }, 200);
            }
        });

        final int searchPopupWindowWidth = (int) (AndroidUtil.getScreenWidth() - AndroidUtil.dip2px(16));
        mSearchPopupWindow = new PopupWindow(mContext);
        mSearchPopupWindow.setAnimationStyle(-1);
        mSearchPopupWindow.setWidth(searchPopupWindowWidth);
        mSearchPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mSearchPopupWindow.setContentView(mRvSearch);
        mSearchPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mSearchPopupWindow.setElevation(AndroidUtil.dip2px(8));
        mSearchPopupWindow.setOutsideTouchable(true);
        mSearchPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        mSearchPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        initMap();

        mMapView.onCreate(savedInstanceState);
    }

    private void initMap() {
        mAMap = mMapView.getMap();
        mAMap.setMyLocationStyle(new MyLocationStyle()
                .myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
                .interval(Constants.LOCATION_INTERVAL)
                .myLocationIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_location_point)))
                .radiusFillColor(Color.TRANSPARENT)
                .strokeColor(Color.TRANSPARENT));
        if (AndroidUtil.rawResToLocalFile(R.raw.map_style, Constants.LOCATION_STYLE_FILE_PATH)) {
            mAMap.setCustomMapStylePath(Constants.LOCATION_STYLE_FILE_PATH);
            mAMap.setMapCustomEnable(true);
        }
        mAMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            private LatLng mMyLocation;

            @Override
            public void onMyLocationChange(Location location) {
                LogUtil.e(location.toString());
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (latLng.latitude == 0 && latLng.longitude == 0) {
                    return;
                }
                if (mMyLocation == null || Math.abs(latLng.latitude - mMyLocation.latitude) >= 0.3 || Math.abs(latLng.longitude - mMyLocation.longitude) >= 0.3) {
                    mMyLocation = latLng;
                    mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMyLocation, Constants.LOCATION_DEFAULT_ZOOM));
                }
                isFirstLocateComplete = true;
            }
        });
        mAMap.setMyLocationEnabled(true);
        mAMap.moveCamera(CameraUpdateFactory.zoomTo(Constants.LOCATION_DEFAULT_ZOOM));
        mAMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                if (isFirstLocateComplete) {
                    mMarkerLocationLatLng = cameraPosition.target;
                    mMarkerHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMarkerLocationList.clear();
                            mMarkerLocationAdapter.setFooterView(null);
                            mMarkerLocationAdapter.notifyDataSetChanged();
                            mPbMarker.setVisibility(View.VISIBLE);
                            mMarkerLocationPageNumber = 0;
                            mPresenter.searchMarkerLocation(mMarkerLocationLatLng.latitude, mMarkerLocationLatLng.longitude, mMarkerLocationPageNumber);

                        }
                    }, 200);
                }
            }
        });

        UiSettings uiSettings = mAMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setScaleControlsEnabled(false);
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setLogoPosition(AMapOptions.LOGO_MARGIN_RIGHT);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(false);//可拖放性
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapUtil.drawableResToBitmap(mContext, R.drawable.ic_location_flag)));
        final Marker mapMarker = mAMap.addMarker(markerOptions);
        mMapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mMapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mapMarker.setPositionByPixels(mMapView.getWidth() / 2, (mCvMarkerLayout.getTop() + mFlSearchLayout.getBottom()) / 2);
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public LocationSendContract.Presenter getPresenter() {
        return new LocationSendPresenter();
    }


    @Override
    public void showNewMarkerLocation(List<PoiItem> poiItemList, boolean hasMore) {
        mPbMarker.setVisibility(View.INVISIBLE);
        mMarkerLocationAdapter.setFooterView(null);
        if (poiItemList != null) {
            if (poiItemList.size() == 0) {
                mMarkerLocationPageNumber = -1;
                mTvMarkerLocationLoadMoreHint.setText(R.string.LoadMoreHint_None);
            } else if (hasMore) {
                mTvMarkerLocationLoadMoreHint.setText(R.string.LoadMoreHint_LoadingMore);
            } else {
                mMarkerLocationPageNumber = -1;
                mTvMarkerLocationLoadMoreHint.setText(R.string.LoadMoreHint_Default);
            }
            mMarkerLocationAdapter.setFooterView(mMarkerLocationFooterView);
        }
        mMarkerLocationAdapter.notifyDataSetChanged();
        mMarkerLocationAdapter.setSelectedPosition(0);
        mMarkerLocationList.clear();
        if (poiItemList != null && poiItemList.size() > 0) {
            mMarkerLocationList.addAll(poiItemList);
            //  mSendMenuItem.setEnabled(true);
        } else {
            // mSendMenuItem.setEnabled(false);
        }

    }

    @Override
    public void showMoreMarkerLocation(List<PoiItem> poiItemList, boolean hasMore) {
        if (!hasMore) {
            mMarkerLocationPageNumber = -1;
        }
        if (hasMore) {
            mTvMarkerLocationLoadMoreHint.setText(R.string.LoadMoreHint_LoadingMore);
        } else {
            mTvMarkerLocationLoadMoreHint.setText(R.string.LoadMoreHint_NoMore);
            mMarkerLocationPageNumber = -1;
        }
        mMarkerLocationAdapter.notifyItemRangeInsertedEx(mMarkerLocationList.size(), poiItemList.size());
        mMarkerLocationList.addAll(poiItemList);
    }

    @Override
    public void showNewSearchLocation(List<PoiItem> poiItemList, boolean hasMore) {
        mSearchLocationAdapter.notifyDataSetChanged();
        mSearchLocationAdapter.setSelectedPosition(0);
        mSearchLocationList.clear();
        if (poiItemList != null && poiItemList.size() > 0) {
            mSearchLocationList.addAll(poiItemList);
        }
        mSearchPopupWindow.showAsDropDown(mFlSearchLayout, 0, 0, Gravity.START);
    }

    @Override
    public void showMoreSearchLocation(List<PoiItem> poiItemList, boolean hasMore) {

    }

    @Override
    public void showError(String error) {

    }
}
