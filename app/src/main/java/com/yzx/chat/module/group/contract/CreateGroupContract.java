package com.yzx.chat.module.group.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.GroupEntity;

import java.util.List;

/**
 * Created by YZX on 2018年02月27日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class CreateGroupContract {
    public interface View extends BaseView<Presenter> {
        void launchChatActivity(GroupEntity group);
    }

    public interface Presenter extends BasePresenter<View> {
        void createGroup(List<ContactEntity> members);

        void addMembers(String groupID, List<ContactEntity> members);
    }
}
