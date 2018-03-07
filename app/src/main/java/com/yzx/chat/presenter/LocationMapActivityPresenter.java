package com.yzx.chat.presenter;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.autonavi.amap.mapcore2d.Inner_3dMap_location;
import com.yzx.chat.R;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.contract.LocationMapActivityContract;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年03月01日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class LocationMapActivityPresenter implements LocationMapActivityContract.Presenter {

    private static final String POI_TYPE = "汽车服务|汽车销售|汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|医疗保健服务|住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施";

    private LocationMapActivityContract.View mLocationMapActivityView;
    private PoiSearch mPoiSearchLocation;
    private PoiSearch mPoiCurrentLocation;
    private String mCityCode;

    private int mSearchLocationPageNum;
    private boolean mHasMoreSearchLocation;
    private boolean isSearchingMoreSearchLocation;

    private int mCurrentLocationPageNum;
    private boolean mHasMoreCurrentLocation;
    private boolean isSearchingMoreCurrentLocation;

    @Override
    public void attachView(LocationMapActivityContract.View view) {
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
    public void searchCurrentLocation(double latitude, double longitude) {
        isSearchingMoreCurrentLocation = true;
        if (mPoiCurrentLocation != null) {
            mPoiCurrentLocation.setOnPoiSearchListener(null);
        }
        mCurrentLocationPageNum = 1;
        mHasMoreCurrentLocation = true;
        PoiSearch.Query query = new PoiSearch.Query("", POI_TYPE, "");
        query.setPageSize(Constants.SEARCH_LOCATION_PAGE_SIZE);
        query.setPageNum(mCurrentLocationPageNum);
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
                    if (poiItemList.size() < Constants.SEARCH_LOCATION_PAGE_SIZE) {
                        mHasMoreCurrentLocation = false;
                    }
                    isSearchingMoreCurrentLocation = false;
                    mLocationMapActivityView.showNewCurrentLocation(poiItemList);
                } else {
                    if (errorCode == 1804 || errorCode == 1806) {
                        mLocationMapActivityView.showError(AndroidUtil.getString(R.string.NetworkUnavailable));
                    } else {
                        mLocationMapActivityView.showError(AndroidUtil.getString(R.string.Server_Error));
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
    public void searchCurrentMoreLocation(double latitude, double longitude) {
        if (isSearchingMoreCurrentLocation) {
            return;
        }
        isSearchingMoreCurrentLocation = true;
        if (mPoiCurrentLocation != null) {
            mPoiCurrentLocation.setOnPoiSearchListener(null);
        }
        mCurrentLocationPageNum++;
        PoiSearch.Query query = new PoiSearch.Query("", POI_TYPE, "");
        query.setPageSize(Constants.SEARCH_LOCATION_PAGE_SIZE);
        query.setPageNum(mCurrentLocationPageNum);
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
                    if (poiItemList.size() < Constants.SEARCH_LOCATION_PAGE_SIZE) {
                        mHasMoreCurrentLocation = false;
                    }
                    isSearchingMoreCurrentLocation = false;
                    mLocationMapActivityView.showMoreCurrentLocation(poiItemList, mHasMoreCurrentLocation);
                } else {
                    if (errorCode == 1804 || errorCode == 1806) {
                        mLocationMapActivityView.showError(AndroidUtil.getString(R.string.NetworkUnavailable));
                    } else {
                        mLocationMapActivityView.showError(AndroidUtil.getString(R.string.Server_Error));
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
    public void searchPOIByKeyword(String keyword) {
        isSearchingMoreSearchLocation = true;
        if (mPoiSearchLocation != null) {
            mPoiSearchLocation.setOnPoiSearchListener(null);
        }
        mSearchLocationPageNum = 1;
        mHasMoreSearchLocation = true;
        PoiSearch.Query query = new PoiSearch.Query(keyword, POI_TYPE, mCityCode);
        query.setPageSize(Constants.SEARCH_LOCATION_PAGE_SIZE);
        query.setPageNum(mSearchLocationPageNum);
        mPoiSearchLocation = new PoiSearch(mLocationMapActivityView.getContext(), query);
        mPoiSearchLocation.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int errorCode) {
                if (errorCode == 1000 && poiResult != null) {
                    List<PoiItem> poiItemList = poiResult.getPois();
                    if (poiItemList == null) {
                        poiItemList = new ArrayList<>(0);
                    }
                    if (poiItemList.size() < Constants.SEARCH_LOCATION_PAGE_SIZE) {
                        mHasMoreSearchLocation = false;
                    }
                    isSearchingMoreSearchLocation = false;
                    mLocationMapActivityView.showNewSearchLocation(poiItemList);
                } else {
                    if (errorCode == 1804 || errorCode == 1806) {
                        mLocationMapActivityView.showError(AndroidUtil.getString(R.string.NetworkUnavailable));
                    } else {
                        mLocationMapActivityView.showError(AndroidUtil.getString(R.string.Server_Error));
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
        PoiSearch.Query query = new PoiSearch.Query(keyword, POI_TYPE, mCityCode);
        query.setPageSize(Constants.SEARCH_LOCATION_PAGE_SIZE);
        query.setPageNum(mSearchLocationPageNum);
        mPoiSearchLocation = new PoiSearch(mLocationMapActivityView.getContext(), query);
        mPoiSearchLocation.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int errorCode) {
                if (errorCode == 1000 && poiResult != null) {
                    List<PoiItem> poiItemList = poiResult.getPois();
                    if (poiItemList == null) {
                        poiItemList = new ArrayList<>(0);
                    }
                    if (poiItemList.size() < Constants.SEARCH_LOCATION_PAGE_SIZE) {
                        mHasMoreSearchLocation = false;
                    }
                    isSearchingMoreSearchLocation = false;
                    mLocationMapActivityView.showMoreSearchLocation(poiItemList, mHasMoreSearchLocation);
                } else {
                    if (errorCode == 1804 || errorCode == 1806) {
                        mLocationMapActivityView.showError(AndroidUtil.getString(R.string.NetworkUnavailable));
                    } else {
                        mLocationMapActivityView.showError(AndroidUtil.getString(R.string.Server_Error));
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
