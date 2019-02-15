package com.yzx.chat.module.conversation.view;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.NaviPara;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.util.BitmapUtil;
import com.yzx.chat.widget.listener.OnOnlySingleClickListener;
import com.yzx.chat.widget.view.RoundLinearLayout;

import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;

/**
 * Created by YZX on 2018年08月10日.
 * 如果你不给自己设限，世界上便没有限制你发挥的篱笆。
 */
public class LocationShareFragment extends BaseFragment {

    public static final String INTENT_EXTRA_POI = "POI";

    public static LocationShareFragment newInstance(PoiItem poiItem) {
        if (poiItem == null) {
            return null;
        }
        Bundle args = new Bundle();
        args.putParcelable(INTENT_EXTRA_POI, poiItem);
        LocationShareFragment fragment = new LocationShareFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private MapView mMapView;
    private TextView mTvShareLocationContent;
    private TextView mTvDistance;
    private TextView mTvShareLocationTitle;
    private TextView mTvMyLocation;
    private TextView mTvMarkerLocation;
    private ImageView mIvBack;
    private RoundLinearLayout mLlNavigation;
    private AMap mAMap;

    private PoiItem mCurrentPoi;
    private LatLng mMarkerLatLng;
    private LatLng mCurrentLatLng;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_location_share;
    }

    @Override
    protected void init(View parentView) {
        mMapView = parentView.findViewById(R.id.LocationShareFragment_mMapView);
        mTvShareLocationContent = parentView.findViewById(R.id.LocationShareFragment_mTvShareLocationContent);
        mTvDistance = parentView.findViewById(R.id.LocationShareFragment_mTvDistance);
        mTvShareLocationTitle = parentView.findViewById(R.id.LocationShareFragment_mTvShareLocationTitle);
        mLlNavigation = parentView.findViewById(R.id.LocationShareFragment_mLlNavigation);
        mIvBack = parentView.findViewById(R.id.LocationShareFragment_mIvBack);
        mTvMyLocation = parentView.findViewById(R.id.LocationShareFragment_mTvMyLocation);
        mTvMarkerLocation = parentView.findViewById(R.id.LocationShareFragment_mTvMarkerLocation);
        mCurrentPoi = Objects.requireNonNull(getArguments()).getParcelable(INTENT_EXTRA_POI);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        mLlNavigation.setRoundRadius(-1);
        mLlNavigation.setOnClickListener(mOnViewClickListener);
        mTvMyLocation.setOnClickListener(mOnViewClickListener);
        mTvMarkerLocation.setOnClickListener(mOnViewClickListener);
        mIvBack.setOnClickListener(mOnViewClickListener);
        mTvShareLocationTitle.setText(mCurrentPoi.getTitle());
        mTvShareLocationContent.setText(mCurrentPoi.getSnippet());

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
        mAMap.setMyLocationEnabled(true);
        mAMap.setOnMyLocationChangeListener(mOnMyLocationChangeListener);
        mAMap.moveCamera(CameraUpdateFactory.zoomTo(Constants.LOCATION_DEFAULT_ZOOM));

        UiSettings uiSettings = mAMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setScaleControlsEnabled(false);
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setLogoPosition(AMapOptions.LOGO_MARGIN_RIGHT);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(false);//可拖放性
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapUtil.drawableResToBitmap(mContext, R.drawable.ic_location_flag)));
        Marker mapMarker = mAMap.addMarker(markerOptions);
        LatLonPoint point = mCurrentPoi.getLatLonPoint();
        mMarkerLatLng = new LatLng(point.getLatitude(), point.getLongitude());
        mapMarker.setPosition(mMarkerLatLng);
        mAMap.moveCamera(CameraUpdateFactory.newLatLng(mMarkerLatLng));


        mMapView.onCreate(savedInstanceState);
    }

    private void moveToMyLocation() {
        if (mCurrentLatLng != null) {
            mAMap.animateCamera(CameraUpdateFactory.newLatLng(mCurrentLatLng));
        }
    }

    private void moveToMarkerLocation() {
        mAMap.animateCamera(CameraUpdateFactory.newLatLng(mMarkerLatLng));
    }

    private void tryNavigation() {
        NaviPara naviPara = new NaviPara();
        naviPara.setTargetPoint(mMarkerLatLng);
        try {
            AMapUtils.openAMapNavi(naviPara, mContext);
        } catch (AMapException e) {
            new MaterialDialog.Builder(mContext)
                    .content(e.getErrorMessage())
                    .positiveText("去下载")
                    .negativeText(R.string.Cancel)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            AMapUtils.getLatestAMapApp(mContext);
                        }
                    })
                    .build()
                    .show();
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

    private final View.OnClickListener mOnViewClickListener = new OnOnlySingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.LocationShareFragment_mLlNavigation:
                    tryNavigation();
                    break;
                case R.id.LocationShareFragment_mIvBack:
                    Objects.requireNonNull(getActivity()).finish();
                    break;
                case R.id.LocationShareFragment_mTvMyLocation:
                    moveToMyLocation();
                    break;
                case R.id.LocationShareFragment_mTvMarkerLocation:
                    moveToMarkerLocation();
                    break;
            }
        }
    };


    private final AMap.OnMyLocationChangeListener mOnMyLocationChangeListener = new AMap.OnMyLocationChangeListener() {

        @Override
        public void onMyLocationChange(Location location) {
            mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            float distance = AMapUtils.calculateLineDistance(mMarkerLatLng, mCurrentLatLng);
            if (distance < 1000) {
                mTvDistance.setText(String.format(Locale.getDefault(), "%d%s", (int) distance, getString(R.string.Unit_Meters)));
            } else {
                mTvDistance.setText(String.format(Locale.getDefault(), "%.2f%s", distance / 1000, getString(R.string.Unit_Kilometre)));
            }
        }
    };
}
