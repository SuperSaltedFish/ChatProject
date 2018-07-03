package com.yzx.chat.mvp.presenter;

import android.os.Handler;

import com.yzx.chat.R;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.GroupMemberBean;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.mvp.contract.CreateGroupContract;
import com.yzx.chat.network.chat.GroupManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ResultCallback;
import com.yzx.chat.util.AndroidUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by YZX on 2018年02月27日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class CreateGroupPresenter implements CreateGroupContract.Presenter {

    private CreateGroupContract.View mCreateGroupView;
    private GroupManager mGroupManager;
    private Handler mHandler;

    private boolean isCreating;
    private boolean isAdding;
    private String[] mAddingMembersID;

    @Override
    public void attachView(CreateGroupContract.View view) {
        mCreateGroupView = view;
        mHandler = new Handler();
        mGroupManager = IMClient.getInstance().getGroupManager();
        mGroupManager.addGroupChangeListener(mOnGroupOperationListener);
    }

    @Override
    public void detachView() {
        mGroupManager.removeGroupChangeListener(mOnGroupOperationListener);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mGroupManager = null;
        mCreateGroupView = null;

    }

    @Override
    public void createGroup(List<ContactBean> members) {
        if (isCreating) {
            return;
        }
        mCreateGroupView.setEnableProgressDialog(true, AndroidUtil.getString(R.string.ProgressHint_Create));
        StringBuilder stringBuilder = new StringBuilder(64);
        String[] membersID = new String[members.size()];
        UserBean user;
        for (int i = 0, count = members.size(); i < count; i++) {
            user = members.get(i).getUserProfile();
            stringBuilder.append(user.getNickname());
            stringBuilder.append("、");
            membersID[i] = user.getUserID();
        }
        stringBuilder.append(IMClient.getInstance().getUserManager().getUser().getNickname()).append("的群聊");

        mGroupManager.createGroup(stringBuilder.toString(), membersID, new ResultCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onFailure(AndroidUtil.getString(R.string.Server_Error));
                    }
                }, 15000);
            }

            @Override
            public void onFailure(final String error) {
                mCreateGroupView.setEnableProgressDialog(false, null);
                mCreateGroupView.showError(error);
                isCreating = false;
            }
        });
        isCreating = true;
    }

    @Override
    public void addMembers(String groupID, List<ContactBean> members) {
        if (isAdding) {
            return;
        }
        mCreateGroupView.setEnableProgressDialog(true, AndroidUtil.getString(R.string.ProgressHint_Add));
        mAddingMembersID = new String[members.size()];
        for (int i = 0, count = members.size(); i < count; i++) {
            mAddingMembersID[i] = members.get(i).getUserProfile().getUserID();
        }
        mGroupManager.addMember(groupID, mAddingMembersID, new ResultCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onFailure(AndroidUtil.getString(R.string.Server_Error));
                    }
                }, 15000);
            }

            @Override
            public void onFailure(final String error) {
                mCreateGroupView.setEnableProgressDialog(false, null);
                mCreateGroupView.showError(error);
                isAdding = false;
            }
        });
        isAdding = true;
    }

    private final GroupManager.OnGroupOperationListener mOnGroupOperationListener = new GroupManager.OnGroupOperationListener() {
        @Override
        public void onCreatedGroup(GroupBean group) {
            if (group.getOwner().equals(IMClient.getInstance().getUserManager().getUserID())) {
                mCreateGroupView.setEnableProgressDialog(false, null);
                mCreateGroupView.launchChatActivity(group);
                isCreating = false;
            }
        }

        @Override
        public void onQuitGroup(GroupBean group) {

        }

        @Override
        public void onBulletinChange(GroupBean group) {

        }

        @Override
        public void onNameChange(GroupBean group) {

        }

        @Override
        public void onMemberAdded(GroupBean group, String[] newMembersID) {
            if (Arrays.equals(mAddingMembersID, newMembersID)) {
                mCreateGroupView.setEnableProgressDialog(false, null);
                mCreateGroupView.launchChatActivity(group);
                isCreating = false;
            }
        }

        @Override
        public void onMemberJoin(GroupBean group, String memberID) {

        }

        @Override
        public void onMemberQuit(GroupBean group, GroupMemberBean quitMember) {

        }

        @Override
        public void onMemberAliasChange(GroupBean group, GroupMemberBean member, String newAlias) {

        }
    };

}
