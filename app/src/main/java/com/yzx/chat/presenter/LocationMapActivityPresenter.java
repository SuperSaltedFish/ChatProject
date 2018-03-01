package com.yzx.chat.presenter;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.yzx.chat.contract.LocationMapActivityContract;

/**
 * Created by YZX on 2018年03月01日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class LocationMapActivityPresenter implements LocationMapActivityContract.Presenter {

    private LocationMapActivityContract.View mLocationMapActivityView;
    private PoiSearch mPoiSearch;

    @Override
    public void attachView(LocationMapActivityContract.View view) {
        mLocationMapActivityView = view;
    }

    @Override
    public void detachView() {
        if (mPoiSearch != null) {
            mPoiSearch.setOnPoiSearchListener(null);
        }
        mLocationMapActivityView = null;
    }

    @Override
    public void searchPOIByKeyword(String keyword) {
        if (mPoiSearch != null) {
            mPoiSearch.setOnPoiSearchListener(null);
        }
        PoiSearch.Query query = new PoiSearch.Query(keyword, "", "深圳");
        query.setPageSize(10);
        query.setPageNum(0);
        mPoiSearch = new PoiSearch(mLocationMapActivityView.getContent(), query);
        mPoiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int errorCode) {
                mLocationMapActivityView.showNewSearchContent(poiResult.getPois());

            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int errorCode) {

            }
        });
        mPoiSearch.searchPOIAsyn();
    }
}
