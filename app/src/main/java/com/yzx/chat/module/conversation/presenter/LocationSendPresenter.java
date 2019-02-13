package com.yzx.chat.module.conversation.presenter;

import android.text.TextUtils;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.yzx.chat.R;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.module.conversation.contract.LocationSendContract;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.core.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年03月01日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class LocationSendPresenter implements LocationSendContract.Presenter {

    private static final String POI_TYPE = "汽车服务|汽车销售|汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|医疗保健服务|住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施";

    private LocationSendContract.View mLocationMapActivityView;
    private PoiSearch mPoiSearchLocation;
    private PoiSearch mPoiCurrentLocation;
    private String mCityCode;

    private boolean isSearchingMoreSearchLocation;

    private boolean isSearchingMoreCurrentLocation;

    @Override
    public void attachView(LocationSendContract.View view) {
        mLocationMapActivityView = view;
        mCityCode = "";
    }

    @Override
    public void detachView() {
        if (mPoiSearchLocation != null) {
            mPoiSearchLocation.setOnPoiSearchListener(null);
        }
        if (mPoiCurrentLocation != null) {
            mPoiCurrentLocation.setOnPoiSearchListener(null);
        }
        mLocationMapActivityView = null;
    }


    @Override
    public boolean hasCityCode() {
        return !TextUtils.isEmpty(mCityCode);
    }

    @Override
    public void searchMarkerLocation(double latitude, double longitude, final int pageNumber) {
        if (isSearchingMoreCurrentLocation) {
            return;
        }
        isSearchingMoreCurrentLocation = true;
        if (mPoiCurrentLocation != null) {
            mPoiCurrentLocation.setOnPoiSearchListener(null);
        }
        PoiSearch.Query query = new PoiSearch.Query("", POI_TYPE, "");
        query.setPageSize(Constants.SEARCH_LOCATION_PAGE_SIZE);
        query.setPageNum(pageNumber);
        mPoiCurrentLocation = new PoiSearch(mLocationMapActivityView.getContext(), query);
        mPoiCurrentLocation.setBound(new PoiSearch.SearchBound(new LatLonPoint(latitude, longitude), 1500));
        mPoiCurrentLocation.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int errorCode) {
                if (errorCode == 1000 && poiResult != null) {
                    List<PoiItem> poiItemList = poiResult.getPois();
                    if (poiItemList == null) {
                        poiItemList = new ArrayList<>(0);
                    }
                    if (poiItemList.size() == 0) {
                        mCityCode = "";
                    } else {
                        mCityCode = poiItemList.get(0).getCityCode();
                    }
                    boolean hasMore = poiItemList.size() >= Constants.SEARCH_LOCATION_PAGE_SIZE;
                    isSearchingMoreCurrentLocation = false;
                    if (pageNumber == 0) {
                        mLocationMapActivityView.showNewMarkerLocation(poiItemList, hasMore);
                    } else {
                        mLocationMapActivityView.showMoreMarkerLocation(poiItemList, hasMore);
                    }
                } else {
                    if (errorCode == 1804 || errorCode == 1806) {
                        mLocationMapActivityView.showError(AndroidUtil.getString(R.string.Error_NetworkUnavailable));
                    } else {
                        mLocationMapActivityView.showError(AndroidUtil.getString(R.string.Error_Server1));
                    }
                    LogUtil.e("search location fail,code:" + errorCode);
                }

            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int errorCode) {
                isSearchingMoreCurrentLocation = false;
            }
        });
        mPoiCurrentLocation.searchPOIAsyn();
    }


    @Override
    public void searchPOIByKeyword(String keyword, final int pageNumber) {
        if (isSearchingMoreSearchLocation) {
            return;
        }
        isSearchingMoreSearchLocation = true;
        if (mPoiSearchLocation != null) {
            mPoiSearchLocation.setOnPoiSearchListener(null);
        }
        PoiSearch.Query query = new PoiSearch.Query(keyword, POI_TYPE, mCityCode);
        query.setPageSize(Constants.SEARCH_LOCATION_PAGE_SIZE);
        query.setPageNum(pageNumber);
        mPoiSearchLocation = new PoiSearch(mLocationMapActivityView.getContext(), query);
        mPoiSearchLocation.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int errorCode) {
                if (errorCode == 1000 && poiResult != null) {
                    List<PoiItem> poiItemList = poiResult.getPois();
                    if (poiItemList == null) {
                        poiItemList = new ArrayList<>(0);
                    }
                    boolean hasMore = poiItemList.size() >= Constants.SEARCH_LOCATION_PAGE_SIZE;
                    isSearchingMoreSearchLocation = false;
                    if (pageNumber == 0) {
                        mLocationMapActivityView.showNewSearchLocation(poiItemList, hasMore);
                    } else {
                        mLocationMapActivityView.showMoreSearchLocation(poiItemList, hasMore);
                    }
                } else {
                    if (errorCode == 1804 || errorCode == 1806) {
                        mLocationMapActivityView.showError(AndroidUtil.getString(R.string.Error_NetworkUnavailable));
                    } else {
                        mLocationMapActivityView.showError(AndroidUtil.getString(R.string.Error_Server1));
                    }
                    LogUtil.e("search location fail,code:" + errorCode);
                }

            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int errorCode) {
                isSearchingMoreSearchLocation = false;
            }
        });
        mPoiSearchLocation.searchPOIAsyn();
    }
}
