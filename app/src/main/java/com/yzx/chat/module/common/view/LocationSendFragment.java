package com.yzx.chat.module.common.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.broadcast.BackPressedReceive;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.module.common.contract.LocationSendContract;
import com.yzx.chat.module.common.presenter.LocationSendPresenter;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.util.BitmapUtil;
import com.yzx.chat.widget.adapter.LocationAdapter;
import com.yzx.chat.widget.listener.AutoCloseKeyboardScrollListener;
import com.yzx.chat.widget.listener.OnOnlySingleClickListener;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by YZX on 2018年08月10日.
 * 如果你不给自己设限，世界上便没有限制你发挥的篱笆。
 */
public class LocationSendFragment extends BaseFragment<LocationSendContract.Presenter> implements LocationSendContract.View {

    private MapView mMapView;
    private AMap mAMap;
    private Marker mMapMarker;
    private Toolbar mToolbar;
    private MenuItem mSendMenuItem;
    private RecyclerView mRvSearch;
    private RecyclerView mRvMarker;
    private SearchView mSearchView;
    private ProgressBar mPbMarker;
    private FrameLayout mFlSearchLayout;
    private CardView mCvMarkerLayout;
    private View mSearchLocationFooterView;
    private View mMarkerLocationFooterView;
    private TextView mTvSearchLocationLoadMoreHint;
    private TextView mTvMarkerLocationLoadMoreHint;
    private TextView mTvSearchNoneHint;
    private ImageView mIvMyLocation;
    private LocationAdapter mSearchLocationAdapter;
    private LocationAdapter mMarkerLocationAdapter;
    private List<PoiItem> mMarkerLocationList;
    private List<PoiItem> mSearchLocationList;
    private LatLng mMarkerLocationLatLng;
    private LatLng mMyLocationLatLng;

    private Handler mMarkerHandler;
    private Handler mSearchHandler;

    private int mSearchLocationPageNumber;
    private int mMarkerLocationPageNumber;
    private boolean isFirstLocateComplete;
    private boolean isAnimating;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_location_send;
    }

    @Override
    protected void init(View parentView) {
        mMapView = parentView.findViewById(R.id.mMapView);
        mRvMarker = parentView.findViewById(R.id.mRvMarkerLocation);
        mPbMarker = parentView.findViewById(R.id.mPbMarker);
        mToolbar = parentView.findViewById(R.id.Default_mToolbar);
        mFlSearchLayout = parentView.findViewById(R.id.mFlSearchLayout);
        mRvSearch = parentView.findViewById(R.id.mRvSearch);
        mTvSearchNoneHint = parentView.findViewById(R.id.mTvSearchNoneHint);
        mCvMarkerLayout = parentView.findViewById(R.id.mCvMarkerLayout);
        mIvMyLocation = parentView.findViewById(R.id.mIvMyLocation);
        mSearchLocationFooterView = getLayoutInflater().inflate(R.layout.view_load_more, (ViewGroup) parentView, false);
        mMarkerLocationFooterView = getLayoutInflater().inflate(R.layout.view_load_more, (ViewGroup) parentView, false);
        mTvSearchLocationLoadMoreHint = mSearchLocationFooterView.findViewById(R.id.LoadMoreView_mTvLoadMoreHint);
        mTvMarkerLocationLoadMoreHint = mMarkerLocationFooterView.findViewById(R.id.LoadMoreView_mTvLoadMoreHint);
        mMarkerHandler = new Handler();
        mSearchHandler = new Handler();
        mSearchLocationList = new ArrayList<>(32);
        mMarkerLocationList = new ArrayList<>(32);
        mSearchLocationAdapter = new LocationAdapter(mSearchLocationList);
        mMarkerLocationAdapter = new LocationAdapter(mMarkerLocationList);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        mToolbar.setTitle(R.string.LocationMapActivity_Title);
        mRvMarker.setLayoutManager(new LinearLayoutManager(mContext));
        mRvMarker.setAdapter(mMarkerLocationAdapter);
        mRvMarker.setHasFixedSize(true);
        mRvMarker.addItemDecoration(new DividerItemDecoration(1, ContextCompat.getColor(mContext, R.color.dividerColor), DividerItemDecoration.HORIZONTAL));
        mRvMarker.addOnItemTouchListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(int position, RecyclerView.ViewHolder viewHolder,float touchX, float touchY) {
                mMarkerLocationAdapter.setSelectedPosition(position);
                PoiItem poiItem = mMarkerLocationList.get(position);
                LatLonPoint point = poiItem.getLatLonPoint();
                isAnimating = true;
                mAMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(point.getLatitude(), point.getLongitude()), mAMap.getCameraPosition().zoom), null);
            }
        });
        mMarkerLocationAdapter.setScrollToBottomListener(new BaseRecyclerViewAdapter.OnScrollToBottomListener() {
            @Override
            public void OnScrollToBottom() {
                if (mMarkerLocationPageNumber != -1) {
                    mMarkerHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mPresenter.searchMarkerLocation(mMarkerLocationLatLng.latitude, mMarkerLocationLatLng.longitude, ++mMarkerLocationPageNumber);
                        }
                    });
                }
            }
        });

        mRvSearch.setLayoutManager(new LinearLayoutManager(mContext));
        mRvSearch.setRecycledViewPool(mRvMarker.getRecycledViewPool());
        mRvSearch.setAdapter(mSearchLocationAdapter);
        mRvSearch.addItemDecoration(new DividerItemDecoration(1, ContextCompat.getColor(mContext, R.color.dividerColor), DividerItemDecoration.HORIZONTAL));
        mRvSearch.addOnScrollListener(new AutoCloseKeyboardScrollListener((Activity) mContext));
        mRvSearch.addOnItemTouchListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(final int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY) {
                PoiItem poiItem = mSearchLocationList.get(position);
                LatLonPoint point = poiItem.getLatLonPoint();
                mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(point.getLatitude(), point.getLongitude()), mAMap.getCameraPosition().zoom));
                resetInput();
            }
        });
        mSearchLocationAdapter.setSelectedPosition(-1);
        mSearchLocationAdapter.setScrollToBottomListener(new BaseRecyclerViewAdapter.OnScrollToBottomListener() {
            @Override
            public void OnScrollToBottom() {
                if (mSearchLocationPageNumber != -1) {
                    mSearchHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mPresenter.searchPOIByKeyword(mSearchView.getQuery().toString(), ++mSearchLocationPageNumber);
                        }
                    });
                }
            }
        });
        mIvMyLocation.setOnClickListener(new OnOnlySingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (mMyLocationLatLng != null) {
                    mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMyLocationLatLng, mAMap.getCameraPosition().zoom));
                }
            }
        });


        initToolbar();
        initMap();

        mMapView.onCreate(savedInstanceState);
    }

    private void initToolbar() {
        mToolbar.inflateMenu(R.menu.menu_location_send);
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mToolbar.setNavigationOnClickListener(new OnOnlySingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Objects.requireNonNull(getActivity()).onBackPressed();
            }
        });
        mSendMenuItem = mToolbar.getMenu().findItem(R.id.LocationSendMenu_Send);
        mSendMenuItem.setEnabled(false);
        mSendMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                sendLocation();
                return true;
            }
        });
        MenuItem searchItem = mToolbar.getMenu().findItem(R.id.LocationSendMenu_Search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                mSearchHandler.removeCallbacksAndMessages(null);
                if (TextUtils.isEmpty(newText)) {
                    mSearchLocationList.clear();
                    mSearchLocationAdapter.notifyDataSetChanged();
                    dismissSearchContent();
                } else {
                    mSearchHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mSearchLocationPageNumber = 0;
                            mPresenter.searchPOIByKeyword(newText, mSearchLocationPageNumber);
                        }
                    }, 200);
                }
                return false;
            }
        });
    }

    private void initMap() {
        mAMap = mMapView.getMap();
        mAMap.setMyLocationStyle(new MyLocationStyle()
                .myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
                .interval(Constants.LOCATION_INTERVAL)
                .myLocationIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_location_point)))
                .radiusFillColor(Color.TRANSPARENT)
                .strokeColor(Color.TRANSPARENT));
        if (AndroidHelper.rawResToLocalFile(R.raw.map_style, Constants.LOCATION_STYLE_FILE_PATH)) {
            mAMap.setCustomMapStylePath(Constants.LOCATION_STYLE_FILE_PATH);
            mAMap.setMapCustomEnable(true);
        }
        mAMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {

            @Override
            public void onMyLocationChange(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (latLng.latitude == 0 && latLng.longitude == 0) {
                    return;
                }
                if (mMyLocationLatLng == null || Math.abs(latLng.latitude - mMyLocationLatLng.latitude) >= 0.3 || Math.abs(latLng.longitude - mMyLocationLatLng.longitude) >= 0.3) {
                    mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, Constants.LOCATION_DEFAULT_ZOOM));
                    isFirstLocateComplete = true;
                }
                mMyLocationLatLng = latLng;
            }
        });
        mAMap.moveCamera(CameraUpdateFactory.zoomTo(Constants.LOCATION_DEFAULT_ZOOM));
        mAMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                if (!isAnimating && isFirstLocateComplete) {
                    mMarkerLocationLatLng = mMapMarker.getPosition();
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
        mAMap.setOnMapTouchListener(new AMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                isAnimating = false;
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    hideSoftKeyboard();
                    resetInput();
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
        mMapMarker = mAMap.addMarker(markerOptions);
        mMapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mMapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int centerX = mMapView.getWidth() / 2;
                int centerY = (mToolbar.getBottom() + mCvMarkerLayout.getTop()) / 2 - (int) AndroidHelper.dip2px(16);
                mMapMarker.setPositionByPixels(centerX, centerY);
                mAMap.setPointToCenter(centerX, centerY);
                mAMap.setMyLocationEnabled(true);
            }
        });
    }

    private void showSearchContent() {
        mFlSearchLayout.setVisibility(View.VISIBLE);
        mCvMarkerLayout.setVisibility(View.GONE);
        mIvMyLocation.setVisibility(View.GONE);
    }

    private void dismissSearchContent() {
        mFlSearchLayout.setVisibility(View.GONE);
        mCvMarkerLayout.setVisibility(View.VISIBLE);
        mIvMyLocation.setVisibility(View.VISIBLE);
    }

    private void resetInput() {
        mSearchView.setQuery(null, false);
        mSearchView.setIconified(true);
    }

    private void sendLocation() {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent();
            intent.putExtra(LocationMapActivity.INTENT_EXTRA_POI, mMarkerLocationList.get(mMarkerLocationAdapter.getSelectedPosition()));
            activity.setResult(LocationMapActivity.RESULT_CODE, intent);
            activity.finish();
        }
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
        BackPressedReceive.registerBackPressedListener(mBackPressedListener);

    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        BackPressedReceive.unregisterBackPressedListener(mBackPressedListener);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mMarkerHandler.removeCallbacksAndMessages(null);
        mSearchHandler.removeCallbacksAndMessages(null);
        mMapView.onDestroy();
        mMapMarker.destroy();
    }

    private final BackPressedReceive.BackPressedListener mBackPressedListener = new BackPressedReceive.BackPressedListener() {
        @Override
        public boolean onBackPressed(String eventName) {
            if (eventName.equals(LocationMapActivity.class.getName())) {
                if (!mSearchView.isIconified()) {
                    resetInput();
                    return true;
                }
            }
            return false;
        }
    };

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
            mSendMenuItem.setEnabled(true);
        } else {
            mSendMenuItem.setEnabled(false);
        }
    }

    @Override
    public void showMoreMarkerLocation(List<PoiItem> poiItemList, boolean hasMore) {
        if (hasMore) {
            mTvMarkerLocationLoadMoreHint.setText(R.string.LoadMoreHint_LoadingMore);
        } else {
            mTvMarkerLocationLoadMoreHint.setText(R.string.LoadMoreHint_NoMore);
            mMarkerLocationPageNumber = -1;
        }

        if (poiItemList.size() > 0) {
            mMarkerLocationAdapter.notifyItemRangeInsertedEx(mMarkerLocationList.size(), poiItemList.size());
            mMarkerLocationList.addAll(poiItemList);
        }
    }

    @Override
    public void showNewSearchLocation(List<PoiItem> poiItemList, boolean hasMore) {
        mSearchLocationAdapter.setFooterView(null);
        mSearchLocationAdapter.notifyDataSetChanged();
        mSearchLocationList.clear();
        if (poiItemList != null) {
            if (poiItemList.size() == 0) {
                mSearchLocationPageNumber = -1;
                mTvSearchLocationLoadMoreHint.setText(R.string.LoadMoreHint_None);
            } else if (hasMore) {
                mTvSearchLocationLoadMoreHint.setText(R.string.LoadMoreHint_LoadingMore);
            } else {
                mSearchLocationPageNumber = -1;
                mTvSearchLocationLoadMoreHint.setText(R.string.LoadMoreHint_Default);
            }
            mSearchLocationAdapter.setFooterView(mSearchLocationFooterView);
        }
        if (poiItemList != null && poiItemList.size() > 0) {
            mSearchLocationList.addAll(poiItemList);
            mTvSearchNoneHint.setVisibility(View.INVISIBLE);
        } else {
            mTvSearchNoneHint.setVisibility(View.VISIBLE);
        }
        showSearchContent();
    }

    @Override
    public void showMoreSearchLocation(List<PoiItem> poiItemList, boolean hasMore) {
        if (hasMore) {
            mTvSearchLocationLoadMoreHint.setText(R.string.LoadMoreHint_LoadingMore);
        } else {
            mTvSearchLocationLoadMoreHint.setText(R.string.LoadMoreHint_NoMore);
            mSearchLocationPageNumber = -1;
        }

        if (poiItemList.size() > 0) {
            mSearchLocationAdapter.notifyItemRangeInsertedEx(mSearchLocationList.size(), poiItemList.size());
            mSearchLocationList.addAll(poiItemList);
        }
    }
}
