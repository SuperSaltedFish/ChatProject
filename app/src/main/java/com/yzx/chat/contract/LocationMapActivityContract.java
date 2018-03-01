package com.yzx.chat.contract;

import android.content.Context;

import com.amap.api.services.core.PoiItem;
import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

import java.util.List;

/**
 * Created by YZX on 2018年03月01日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class LocationMapActivityContract {
    public interface View extends BaseView<Presenter> {
        Context getContent();

        void showNewSearchContent(List<PoiItem> poiItemList);
    }


    public interface Presenter extends BasePresenter<View> {
        void searchPOIByKeyword(String keyword);
    }
}
