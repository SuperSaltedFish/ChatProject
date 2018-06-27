package com.yzx.chat.mvp.presenter;

import android.os.Handler;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.CreateGroupMemberBean;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.mvp.contract.CreateGroupContract;
import com.yzx.chat.network.chat.GroupManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ResultCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年02月27日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class CreateGroupPresenter implements CreateGroupContract.Presenter {

    private CreateGroupContract.View mCreateGroupView;
    private GroupManager mGroupManager;

    private boolean isCreating;

    @Override
    public void attachView(CreateGroupContract.View view) {
        mCreateGroupView = view;
        mGroupManager = IMClient.getInstance().groupManager();
    }

    @Override
    public void detachView() {
        mGroupManager = null;
        mCreateGroupView = null;

    }

    @Override
    public void createGroup(List<ContactBean> members) {
        if (isCreating) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder(64);
        List<CreateGroupMemberBean> memberList = new ArrayList<>(members.size());
        CreateGroupMemberBean groupMember;
        for (int i = 0, count = members.size(); i < count; i++) {
            stringBuilder.append(members.get(i).getUserProfile().getNickname());
            stringBuilder.append("、");
            groupMember = new CreateGroupMemberBean();
            groupMember.setUserID(members.get(i).getUserProfile().getUserID());
            memberList.add(groupMember);
        }
        stringBuilder.append(IMClient.getInstance().userManager().getUser().getNickname()).append("的群聊");

        mGroupManager.createGroup(stringBuilder.toString(), memberList, new ResultCallback<GroupBean>() {
            @Override
            public void onSuccess(final GroupBean result) {
                mCreateGroupView.launchChatActivity(result);
                isCreating = false;
            }

            @Override
            public void onFailure(final String error) {
                mCreateGroupView.showError(error);
                isCreating = false;
            }
        });
        isCreating = true;
    }

    @Override
    public void addMembers(String groupID, List<ContactBean> members) {
        if (isCreating) {
            return;
        }
        List<CreateGroupMemberBean> memberList = new ArrayList<>(members.size());
        CreateGroupMemberBean groupMember;
        for (int i = 0, count = members.size(); i < count; i++) {
            groupMember = new CreateGroupMemberBean();
            groupMember.setUserID(members.get(i).getUserProfile().getUserID());
            memberList.add(groupMember);
        }
        mGroupManager.addMember(groupID, memberList, new ResultCallback<GroupBean>() {
            @Override
            public void onSuccess(final GroupBean result) {
                mCreateGroupView.launchChatActivity(result);
                isCreating = false;
            }

            @Override
            public void onFailure(final String error) {
                mCreateGroupView.showError(error);
                isCreating = false;
            }
        });
        isCreating = true;
    }

}
