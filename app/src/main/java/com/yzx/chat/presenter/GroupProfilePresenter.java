package com.yzx.chat.presenter;

import android.text.TextUtils;

import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.GroupMemberBean;
import com.yzx.chat.contract.GroupProfileContract;
import com.yzx.chat.network.chat.GroupManager;
import com.yzx.chat.network.chat.IMClient;

/**
 * Created by YZX on 2018年03月12日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupProfilePresenter implements GroupProfileContract.Presenter {

    private GroupProfileContract.View mGroupProfileView;
    private GroupManager mGroupManager;

    @Override
    public void attachView(GroupProfileContract.View view) {
        mGroupProfileView = view;
        mGroupManager = IMClient.getInstance().groupManager();
    }

    @Override
    public void detachView() {
        mGroupProfileView = null;
        mGroupManager = null;
    }

    @Override
    public void init(String groupID) {
        GroupBean group = mGroupManager.getGroup(groupID);
        if (group != null) {
            String mySelfMemberID = IMClient.getInstance().userManager().getUserID();
            if (!TextUtils.isEmpty(mySelfMemberID)) {
                GroupMemberBean groupMember = mGroupManager.getGroupMenber(groupID, mySelfMemberID);
                if (groupMember != null) {
                    mGroupProfileView.updateGroupInfo(group);
                    mGroupProfileView.updateMySelfInfo(groupMember);
                    return;
                }
            }
        }
        mGroupProfileView.goBack();
    }
}
