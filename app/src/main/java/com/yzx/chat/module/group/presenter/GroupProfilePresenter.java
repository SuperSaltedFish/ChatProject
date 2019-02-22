package com.yzx.chat.module.group.presenter;

import android.os.Handler;
import android.text.TextUtils;

import com.yzx.chat.R;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.ConversationManager;
import com.yzx.chat.core.GroupManager;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.core.entity.GroupMemberEntity;
import com.yzx.chat.module.conversation.presenter.ChatPresenter;
import com.yzx.chat.module.group.contract.GroupProfileContract;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.widget.listener.LifecycleMVPResultCallback;

import io.rong.imlib.model.Conversation;

/**
 * Created by YZX on 2018年03月12日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupProfilePresenter implements GroupProfileContract.Presenter {

    private GroupProfileContract.View mGroupProfileView;
    private GroupManager mGroupManager;
    private ConversationManager mConversationManager;
    private Conversation mConversation;
    private Handler mHandler;

    @Override
    public void attachView(GroupProfileContract.View view) {
        mGroupProfileView = view;
        mGroupManager = AppClient.getInstance().getGroupManager();
        mConversationManager = AppClient.getInstance().getConversationManager();
        mHandler = new Handler();
        mGroupManager.addGroupChangeListener(mOnGroupOperationListener);
    }

    @Override
    public void detachView() {
        mHandler.removeCallbacksAndMessages(null);
        mGroupManager.removeGroupChangeListener(mOnGroupOperationListener);
        mHandler=null;
        mGroupProfileView = null;
        mGroupManager = null;
    }

    @Override
    public void init(String groupID) {
        GroupEntity group = mGroupManager.getGroup(groupID);
        String mySelfMemberID = AppClient.getInstance().getUserManager().getUserID();
        if (group == null || TextUtils.isEmpty(mySelfMemberID)) {
            mGroupProfileView.goBack();
            return;
        }

        GroupMemberEntity mySelf = mGroupManager.getGroupMember(groupID, mySelfMemberID);
        if (mySelf == null) {
            mGroupProfileView.goBack();
            return;
        }
        mGroupProfileView.showGroupInfo(group, mySelf);
        mConversation = mConversationManager.getConversation(Conversation.ConversationType.GROUP, groupID);
        if (mConversation == null) {
            mConversation = new Conversation();
            mConversation.setTargetId(groupID);
            mConversation.setTop(false);
            mConversation.setConversationType(Conversation.ConversationType.GROUP);
        }
        mGroupProfileView.switchTopState(mConversation.isTop());
        mGroupProfileView.switchRemindState(  mConversationManager.isEnableConversationNotification(Conversation.ConversationType.GROUP, groupID));
    }

    @Override
    public GroupEntity getGroup() {
        return mGroupManager.getGroup(mConversation.getTargetId());
    }

    @Override
    public String getCurrentUserID() {
        return AppClient.getInstance().getUserManager().getUserID();
    }


    @Override
    public void updateGroupName(final String newName) {
        mGroupManager.renameGroup(mConversation.getTargetId(), newName, new LifecycleMVPResultCallback<Void>(mGroupProfileView) {
            @Override
            protected void onSuccess(Void result) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onFailure(0,AndroidHelper.getString(R.string.Error_Server1));
                    }
                }, 15000);
            }
        });
    }

    @Override
    public void updateGroupNotice(final String newNotice) {
        mGroupManager.updateGroupNotice(mConversation.getTargetId(), newNotice, new LifecycleMVPResultCallback<Void>(mGroupProfileView) {
            @Override
            protected void onSuccess(Void result) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onFailure(0,AndroidHelper.getString(R.string.Error_Server1));
                    }
                }, 15000);
            }
        });
    }

    @Override
    public void updateMyGroupAlias(final String newAlias) {
        mGroupManager.updateMemberAlias(mConversation.getTargetId(), newAlias, new LifecycleMVPResultCallback<Void>(mGroupProfileView) {
            @Override
            protected void onSuccess(Void result) {
                mGroupProfileView.showNewMyAlias(newAlias);
            }
        });
    }

    @Override
    public void quitGroup() {
        mGroupManager.quitGroup(mConversation.getTargetId(), new LifecycleMVPResultCallback<Void>(mGroupProfileView) {
            @Override
            protected void onSuccess(Void result) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onFailure(0,AndroidHelper.getString(R.string.Error_Server1));
                    }
                }, 15000);
            }
        });
    }

    @Override
    public boolean isMySelf(String userID) {
        return userID.equals(AppClient.getInstance().getUserManager().getUserID());
    }

    @Override
    public void enableConversationNotification(boolean isEnable) {
        mConversationManager.setEnableConversationNotification(mConversation.getConversationType(), mConversation.getTargetId(), isEnable,null);
    }

    @Override
    public void setConversationToTop(boolean isTop) {
        mConversationManager.setTopConversation(mConversation.getConversationType(), mConversation.getTargetId(), isTop,null);
    }

    @Override
    public void clearChatMessages() {
        mConversationManager.clearConversationMessages(mConversation.getConversationType(), mConversation.getTargetId(),null);
    }

    private final GroupManager.OnGroupOperationListener mOnGroupOperationListener = new GroupManager.OnGroupOperationListener() {
        @Override
        public void onCreatedGroup(GroupEntity group) {

        }

        @Override
        public void onQuitGroup(GroupEntity group) {
            if (mConversation.getTargetId().equals(group.getGroupID())) {
                if (mConversation.getTargetId().equals(ChatPresenter.sConversationID)) {
                    mGroupProfileView.finishChatActivity();
                }
                mGroupProfileView.goBack();
            }
        }

        @Override
        public void onBulletinChange(GroupEntity group) {
            if (mConversation.getTargetId().equals(group.getGroupID())) {
                mGroupProfileView.showNewGroupNotice(group.getNotice());
            }
        }

        @Override
        public void onNameChange(GroupEntity group) {
            if (mConversation.getTargetId().equals(group.getGroupID())) {
                mGroupProfileView.showNewGroupName(group.getName());
            }
        }

        @Override
        public void onMemberAdded(GroupEntity group, String[] newMembersID) {
            if (mConversation.getTargetId().equals(group.getGroupID())) {
                mGroupProfileView.showMembers(group.getMembers());
            }
        }

        @Override
        public void onMemberJoin(GroupEntity group, String memberID) {

        }

        @Override
        public void onMemberQuit(GroupEntity group, GroupMemberEntity quitMember) {
            if (mConversation.getTargetId().equals(group.getGroupID())) {
                mGroupProfileView.showMembers(group.getMembers());
            }
        }

        @Override
        public void onMemberAliasChange(GroupEntity group, GroupMemberEntity member, String newAlias) {
            if (mConversation.getTargetId().equals(group.getGroupID())) {
                if (member.getUserProfile().getUserID().equals(getCurrentUserID())) {
                    mGroupProfileView.showNewMyAlias(newAlias);
                }
                mGroupProfileView.showMembers(group.getMembers());
            }
        }
    };


}
