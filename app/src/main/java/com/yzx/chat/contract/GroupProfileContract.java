package com.yzx.chat.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.GroupMemberBean;

/**
 * Created by YZX on 2018年03月12日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupProfileContract {

    public interface View extends BaseView<Presenter> {
        void showGroupInfo(GroupBean group, GroupMemberBean mySelf);

        void showNewGroupName(String newGroupName);

        void showNewGroupNotice(String newGroupNotice);

        void showNewMyAlias(String newAlias);

        void showError(String error);

        void goBack();
    }


    public interface Presenter extends BasePresenter<View> {
        void init(String groupID);

        GroupBean getGroup();

        void updateGroupName(String newName);

        void updateGroupNotice(String newNotice);

        void updateMyGroupAlias(String newAlias);

        void quitGroup();

        boolean isMySelf(String userID);
    }
}
