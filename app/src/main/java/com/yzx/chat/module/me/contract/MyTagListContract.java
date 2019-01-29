package com.yzx.chat.module.me.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.bean.TagBean;

import java.util.List;

/**
 * Created by YZX on 2018年06月26日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class MyTagListContract {

    public interface View extends BaseView<Presenter> {
       void showAllTags(List<TagBean> tagList);
    }


    public interface Presenter extends BasePresenter<View> {
        void loadAllTagList();

    }
}
