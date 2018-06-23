package com.yzx.chat.mvp.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactRemarkBean;

import java.util.ArrayList;

/**
 * Created by YZX on 2018年01月24日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class RemarkInfoContract {

    public interface View extends BaseView<Presenter> {

    }


    public interface Presenter extends BasePresenter<View> {
        void save(ContactBean contact);

        ArrayList<String> getAllTags();
    }
}
