package com.yzx.chat.module.group.presenter;

import android.os.Handler;
import android.text.TextUtils;

import com.yzx.chat.R;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.GroupMemberBean;
import com.yzx.chat.module.conversation.presenter.ChatPresenter;
import com.yzx.chat.module.group.contract.GroupProfileContract;
import com.yzx.chat.network.chat.ConversationManager;
import com.yzx.chat.network.chat.GroupManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ResultCallback;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.LogUtil;

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
        mGroupManager = IMClient.getInstance().getGroupManager();
        mConversationManager = IMClient.getInstance().getConversationManager();
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
        GroupBean group = mGroupManager.getGroup(groupID);
        String mySelfMemberID = IMClient.getInstance().getUserManager().getUserID();
        if (group == null || TextUtils.isEmpty(mySelfMemberID)) {
            mGroupProfileView.goBack();
            return;
        }

        GroupMemberBean mySelf = mGroupManager.getGroupMember(groupID, mySelfMemberID);
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
        mConversationManager.isEnableConversationNotification(mConversation, new ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus result) {
                mGroupProfileView.switchRemindState(result == Conversation.ConversationNotificationStatus.DO_NOT_DISTURB);
            }

            @Override
            public void onFailure(String error) {
                LogUtil.e(error);
            }
        });
    }

    @Override
    public GroupBean getGroup() {
        return mGroupManager.getGroup(mConversation.getTargetId());
    }

    @Override
    public String getCurrentUserID() {
        return IMClient.getInstance().getUserManager().getUserID();
    }


    @Override
    public void updateGroupName(final String newName) {
        mGroupProfileView.setEnableProgressDialog(true, AndroidUtil.getString(R.string.ProgressHint_Modify));
        mGroupManager.renameGroup(mConversation.getTargetId(), newName, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onFailure(AndroidUtil.getString(R.string.Error_Server));
                    }
                }, 15000);
            }

            @Override
            public void onFailure(final String error) {
                mGroupProfileView.setEnableProgressDialog(false, null);
                mGroupProfileView.showError(error);
            }
        });
    }

    @Override
    public void updateGroupNotice(final String newNotice) {
        mGroupProfileView.setEnableProgressDialog(true, AndroidUtil.getString(R.string.ProgressHint_Modify));
        mGroupManager.updateGroupNotice(mConversation.getTargetId(), newNotice, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onFailure(AndroidUtil.getString(R.string.Error_Server));
                    }
                }, 15000);
            }

            @Override
            public void onFailure(final String error) {
                mGroupProfileView.setEnableProgressDialog(false, null);
                mGroupProfileView.showError(error);
            }
        });
    }

    @Override
    public void updateMyGroupAlias(final String newAlias) {
        mGroupProfileView.setEnableProgressDialog(true, AndroidUtil.getString(R.string.ProgressHint_Modify));
        mGroupManager.updateMemberAlias(mConversation.getTargetId(), newAlias, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                mGroupProfileView.setEnableProgressDialog(false, null);
                mGroupProfileView.showNewMyAlias(newAlias);
            }

            @Override
            public void onFailure(final String error) {
                mGroupProfileView.setEnableProgressDialog(false, null);
                mGroupProfileView.showError(error);
            }
        });
    }

    @Override
    public void quitGroup() {
        mGroupProfileView.setEnableProgressDialog(true, AndroidUtil.getString(R.string.ProgressHint_Quit));
        mGroupManager.quitGroup(mConversation.getTargetId(), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onFailure(AndroidUtil.getString(R.string.Error_Server));
                    }
                }, 15000);
            }

            @Override
            public void onFailure(String error) {
                mGroupProfileView.setEnableProgressDialog(false, null);
                mGroupProfileView.showError(error);
            }
        });
    }

    @Override
    public boolean isMySelf(String userID) {
        return userID.equals(IMClient.getInstance().getUserManager().getUserID());
    }

    @Override
    public void enableConversationNotification(boolean isEnable) {
        mConversationManager.setEnableConversationNotification(mConversation.getConversationType(), mConversation.getTargetId(), isEnable);
    }

    @Override
    public void setConversationToTop(boolean isTop) {
        mConversationManager.setConversationTop(mConversation.getConversationType(), mConversation.getTargetId(), isTop);
    }

    @Override
    public void clearChatMessages() {
        mConversationManager.clearAllConversationMessages(mConversation.getConversationType(), mConversation.getTargetId());
    }

    private final GroupManager.OnGroupOperationListener mOnGroupOperationListener = new GroupManager.OnGroupOperationListener() {
        @Override
        public void onCreatedGroup(GroupBean group) {

        }

        @Override
        public void onQuitGroup(GroupBean group) {
            if (mConversation.getTargetId().equals(group.getGroupID())) {
                mGroupProfileView.setEnableProgressDialog(false, null);
                if (mConversation.getTargetId().equals(ChatPresenter.sConversationID)) {
                    mGroupProfileView.finishChatActivity();
                }
                mGroupProfileView.goBack();
            }
        }

        @Override
        public void onBulletinChange(GroupBean group) {
            if (mConversation.getTargetId().equals(group.getGroupID())) {
                mGroupProfileView.setEnableProgressDialog(false, null);
                mGroupProfileView.showNewGroupNotice(group.getNotice());
            }
        }

        @Override
        public void onNameChange(GroupBean group) {
            if (mConversation.getTargetId().equals(group.getGroupID())) {
                mGroupProfileView.setEnableProgressDialog(false, null);
                mGroupProfileView.showNewGroupName(group.getName());
            }
        }

        @Override
        public void onMemberAdded(GroupBean group, String[] newMembersID) {
            if (mConversation.getTargetId().equals(group.getGroupID())) {
                mGroupProfileView.setEnableProgressDialog(false, null);
                mGroupProfileView.showMembers(group.getMembers());
            }
        }

        @Override
        public void onMemberJoin(GroupBean group, String memberID) {

        }

        @Override
        public void onMemberQuit(GroupBean group, GroupMemberBean quitMember) {
            if (mConversation.getTargetId().equals(group.getGroupID())) {
                mGroupProfileView.setEnableProgressDialog(false, null);
                mGroupProfileView.showMembers(group.getMembers());
            }
        }

        @Override
        public void onMemberAliasChange(GroupBean group, GroupMemberBean member, String newAlias) {
            if (mConversation.getTargetId().equals(group.getGroupID())) {
                if (member.getUserProfile().getUserID().equals(getCurrentUserID())) {
                    mGroupProfileView.setEnableProgressDialog(false, null);
                    mGroupProfileView.showNewMyAlias(newAlias);
                }
                mGroupProfileView.showMembers(group.getMembers());
            }
        }
    };


}
