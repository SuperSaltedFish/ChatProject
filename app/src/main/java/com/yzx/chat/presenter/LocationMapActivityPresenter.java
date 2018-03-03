package com.yzx.chat.presenter;

import com.amap.api.maps2d.model.LatLng;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.contract.LocationMapActivityContract;
import com.yzx.chat.util.LogUtil;

import java.util.List;

/**
 * Created by YZX on 2018年03月01日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class LocationMapActivityPresenter implements LocationMapActivityContract.Presenter {

    private LocationMapActivityContract.View mLocationMapActivityView;
    private PoiSearch mPoiSearchLocation;
    private PoiSearch mPoiCurrentLocation;

    private int mSearchLocationPageNum;
    private boolean mHasMoreSearchLocation;
    private boolean isSearchingMoreSearchLocation;

    private int mCurrentLocationPageNum;
    private boolean mHasMoreCurrentLocation;
    private boolean isSearchingMoreCurrentLocation;

    @Override
    public void attachView(LocationMapActivityContract.View view) {
        mLocationMapActivityView = view;
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
    public void searchCurrentLocation(LatLng latLng) {
        isSearchingMoreCurrentLocation = true;
        if (mPoiCurrentLocation != null) {
            mPoiCurrentLocation.setOnPoiSearchListener(null);
        }
        mCurrentLocationPageNum = 1;
        mHasMoreCurrentLocation = true;
        PoiSearch.Query query = new PoiSearch.Query(keyword, "", "深圳");
        query.setPageSize(Constants.SEARCH_LOCATION_PAGE_SIZE);
        query.setPageNum(mCurrentLocationPageNum);
        mPoiCurrentLocation = new PoiSearch(mLocationMapActivityView.getContext(), query);
        mPoiCurrentLocation.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int errorCode) {
                if (errorCode == 1000 && poiResult != null) {
                    List<PoiItem> poiItemList = poiResult.getPois();
                    if (poiItemList.size() < Constants.SEARCH_LOCATION_PAGE_SIZE) {
                        mHasMoreCurrentLocation = false;
                    }
                    isSearchingMoreCurrentLocation = false;
                    mLocationMapActivityView.showNewCurrentLocation(poiItemList);
                } else {
                    LogUtil.e("search new fail,code:" + errorCode);
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
    public void searchMoreLocation(LatLng latLng) {

    }

    @Override
    public void searchPOIByKeyword(String keyword) {
        isSearchingMoreSearchLocation = true;
        if (mPoiSearchLocation != null) {
            mPoiSearchLocation.setOnPoiSearchListener(null);
        }
        mSearchLocationPageNum = 1;
        mHasMoreSearchLocation = true;
        PoiSearch.Query query = new PoiSearch.Query(keyword, "", "深圳");
        query.setPageSize(Constants.SEARCH_LOCATION_PAGE_SIZE);
        query.setPageNum(mSearchLocationPageNum);
        mPoiSearchLocation = new PoiSearch(mLocationMapActivityView.getContext(), query);
        mPoiSearchLocation.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int errorCode) {
                if (errorCode == 1000 && poiResult != null) {
                    List<PoiItem> poiItemList = poiResult.getPois();
                    if (poiItemList.size() < Constants.SEARCH_LOCATION_PAGE_SIZE) {
                        mHasMoreSearchLocation = false;
                    }
                    isSearchingMoreSearchLocation = false;
                    mLocationMapActivityView.showNewSearchLocation(poiItemList);
                } else {
                    LogUtil.e("search new fail,code:" + errorCode);
                }

            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int errorCode) {
                isSearchingMoreSearchLocation = false;
            }
        });
        mPoiSearchLocation.searchPOIAsyn();
    }

    @Override
    public void searchMorePOIByKeyword(String keyword) {
        if (isSearchingMoreSearchLocation) {
            return;
        }
        isSearchingMoreSearchLocation = true;
        if (mPoiSearchLocation != null) {
            mPoiSearchLocation.setOnPoiSearchListener(null);
        }
        mSearchLocationPageNum++;
        PoiSearch.Query query = new PoiSearch.Query(keyword, "", "深圳");
        query.setPageSize(Constants.SEARCH_LOCATION_PAGE_SIZE);
        query.setPageNum(mSearchLocationPageNum);
        mPoiSearchLocation = new PoiSearch(mLocationMapActivityView.getContext(), query);
        mPoiSearchLocation.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int errorCode) {
                if (errorCode == 1000 && poiResult != null) {
                    List<PoiItem> poiItemList = poiResult.getPois();
                    if (poiItemList == null || poiItemList.size() < Constants.SEARCH_LOCATION_PAGE_SIZE) {
                        mHasMoreSearchLocation = false;
                    }
                    isSearchingMoreSearchLocation = false;
                    mLocationMapActivityView.showMoreSearchLocation(poiItemList, mHasMoreSearchLocation);
                } else {
                    LogUtil.e("search more fail,code:" + errorCode);
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
