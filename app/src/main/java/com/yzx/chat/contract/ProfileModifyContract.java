package com.yzx.chat.contract;

        import com.yzx.chat.base.BasePresenter;
        import com.yzx.chat.base.BaseView;
        import com.yzx.chat.bean.UserBean;

/**
 * Created by YZX on 2018年02月12日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ProfileModifyContract {

    public interface View extends BaseView<Presenter> {
        void showError(String error);
        void goBack();
    }


    public interface Presenter extends BasePresenter<View> {
        UserBean getUserInfo();
        void updateProfile(UserBean user);
    }
}
