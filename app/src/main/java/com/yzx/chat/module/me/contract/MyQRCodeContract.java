package com.yzx.chat.module.me.contract;

import android.graphics.Bitmap;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.UserBean;

/**
 * Created by YZX on 2018年02月26日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class MyQRCodeContract {
    public interface View extends BaseView<Presenter> {

        void showQRCode(String content);

        void showHint(String hint);

        void showErrorHint(String hint);

        void setEnableProgressBar(boolean isEnable);
    }


    public interface Presenter extends BasePresenter<View> {

        UserBean getUserInfo();

        GroupBean getGroupInfo(String groupID);

        void updateUserQRCode();

        void updateGroupQRCode(String groupID);

        void saveQRCodeToLocal(Bitmap bitmap,String id);

    }
}
