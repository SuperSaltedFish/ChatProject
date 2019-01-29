package com.yzx.chat.module.conversation.contract;

import android.content.Context;

import com.amap.api.services.core.PoiItem;
import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

import java.util.List;

/**
 * Created by YZX on 2018年03月01日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class LocationSendContract {
    public interface View extends BaseView<Presenter> {
        Context getContext();

        void showNewMarkerLocation(List<PoiItem> poiItemList, boolean hasMore);

        void showMoreMarkerLocation(List<PoiItem> poiItemList, boolean hasMore);

        void showNewSearchLocation(List<PoiItem> poiItemList, boolean hasMore);

        void showMoreSearchLocation(List<PoiItem> poiItemList, boolean hasMore);

        void showError(String error);
    }


    public interface Presenter extends BasePresenter<View> {

        boolean hasCityCode();

        void searchMarkerLocation(double latitude, double longitude, int pageNumber);

        void searchPOIByKeyword(String keyword, int pageNumber);

    }
}
