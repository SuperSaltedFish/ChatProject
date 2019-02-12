package com.yzx.chat.module.group.presenter;

import android.os.Handler;

import com.yzx.chat.R;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.core.entity.GroupMemberEntity;
import com.yzx.chat.core.entity.UserEntity;
import com.yzx.chat.module.group.contract.CreateGroupContract;
import com.yzx.chat.core.GroupManager;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.listener.ResultCallback;
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
        mGroupManager = AppClient.getInstance().getGroupManager();
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
    public void createGroup(List<ContactEntity> members) {
        if (isCreating) {
            return;
        }
        mCreateGroupView.setEnableProgressDialog(true, AndroidUtil.getString(R.string.ProgressHint_Create));
        StringBuilder stringBuilder = new StringBuilder(64);
        String[] membersID = new String[members.size()];
        UserEntity user;
        for (int i = 0, count = members.size(); i < count; i++) {
            user = members.get(i).getUserProfile();
            stringBuilder.append(user.getNickname());
            stringBuilder.append("、");
            membersID[i] = user.getUserID();
        }
        stringBuilder.append(AppClient.getInstance().getUserManager().getUser().getNickname()).append("的群聊");

        mGroupManager.createGroup(stringBuilder.toString(), membersID, new ResultCallback<Void>() {
            @Override
            public void onResult(final Void result) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onFailure(AndroidUtil.getString(R.string.Error_Server1));
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
    public void addMembers(String groupID, List<ContactEntity> members) {
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
            public void onResult(final Void result) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onFailure(AndroidUtil.getString(R.string.Error_Server1));
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
        public void onCreatedGroup(GroupEntity group) {
            if (group.getOwner().equals(AppClient.getInstance().getUserManager().getUserID())) {
                mCreateGroupView.setEnableProgressDialog(false, null);
                mCreateGroupView.launchChatActivity(group);
                isCreating = false;
            }
        }

        @Override
        public void onQuitGroup(GroupEntity group) {

        }

        @Override
        public void onBulletinChange(GroupEntity group) {

        }

        @Override
        public void onNameChange(GroupEntity group) {

        }

        @Override
        public void onMemberAdded(GroupEntity group, String[] newMembersID) {
            if (Arrays.equals(mAddingMembersID, newMembersID)) {
                mCreateGroupView.setEnableProgressDialog(false, null);
                mCreateGroupView.launchChatActivity(group);
                isCreating = false;
            }
        }

        @Override
        public void onMemberJoin(GroupEntity group, String memberID) {

        }

        @Override
        public void onMemberQuit(GroupEntity group, GroupMemberEntity quitMember) {

        }

        @Override
        public void onMemberAliasChange(GroupEntity group, GroupMemberEntity member, String newAlias) {

        }
    };

}
