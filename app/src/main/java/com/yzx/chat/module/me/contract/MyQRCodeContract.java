package com.yzx.chat.module.me.contract;

import android.graphics.Bitmap;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.core.entity.UserEntity;

/**
 * Created by YZX on 2018年02月26日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class MyQRCodeContract {
    public interface View extends BaseView<Presenter> {

        void showQRCode(String content);

        void showHint(String hint);
    }


    public interface Presenter extends BasePresenter<View> {

        UserEntity getUserInfo();

        GroupEntity getGroupInfo(String groupID);

        void updateUserQRCode();

        void updateGroupQRCode(String groupID);

        void saveQRCodeToLocal(Bitmap bitmap,String id);

    }
}
