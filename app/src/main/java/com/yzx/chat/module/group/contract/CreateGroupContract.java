package com.yzx.chat.module.group.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.GroupBean;

import java.util.List;

/**
 * Created by YZX on 2018年02月27日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class CreateGroupContract {
    public interface View extends BaseView<Presenter> {
        void setEnableProgressDialog(boolean isEnable,String hintContent);

        void showError(String error);

        void launchChatActivity(GroupBean group);
    }

    public interface Presenter extends BasePresenter<View> {
        void createGroup(List<ContactBean> members);

        void addMembers(String groupID, List<ContactBean> members);
    }
}
