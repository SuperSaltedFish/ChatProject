package com.yzx.chat.mvp.presenter;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.yzx.chat.R;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.GroupMemberBean;
import com.yzx.chat.mvp.contract.GroupProfileContract;
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
        mGroupManager = IMClient.getInstance().groupManager();
        mConversationManager = IMClient.getInstance().conversationManager();
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void detachView() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mGroupProfileView = null;
        mGroupManager = null;
    }

    @Override
    public void init(String groupID) {
        GroupBean group = mGroupManager.getGroup(groupID);
        String mySelfMemberID = IMClient.getInstance().userManager().getUserID();
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
        return IMClient.getInstance().userManager().getUserID();
    }


    @Override
    public void updateGroupName(final String newName) {
        mGroupProfileView.setEnableProgressDialog(true, AndroidUtil.getString(R.string.ProgressHint_Modify));
        mGroupManager.renameGroup(mConversation.getTargetId(), newName, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mGroupProfileView.showNewGroupName(newName);
                        mGroupProfileView.setEnableProgressDialog(false, null);
                    }
                });
            }

            @Override
            public void onFailure(final String error) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mGroupProfileView.setEnableProgressDialog(false, null);
                        mGroupProfileView.showError(error);
                    }
                });
            }
        });
    }

    @Override
    public void updateGroupNotice(final String newNotice) {
        mGroupProfileView.setEnableProgressDialog(true, AndroidUtil.getString(R.string.ProgressHint_Modify));
        mGroupManager.updateGroupNotice(mConversation.getTargetId(), newNotice, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mGroupProfileView.setEnableProgressDialog(false, null);
                        mGroupProfileView.showNewGroupNotice(newNotice);
                    }
                });

            }

            @Override
            public void onFailure(final String error) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mGroupProfileView.setEnableProgressDialog(false, null);
                        mGroupProfileView.showError(error);
                    }
                });
            }
        });
    }

    @Override
    public void updateMyGroupAlias(final String newAlias) {
        mGroupProfileView.setEnableProgressDialog(true, AndroidUtil.getString(R.string.ProgressHint_Modify));
        mGroupManager.updateMemberAlias(mConversation.getTargetId(), IMClient.getInstance().userManager().getUserID(), newAlias, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mGroupProfileView.setEnableProgressDialog(false, null);
                        mGroupProfileView.showNewMyAlias(newAlias);
                    }
                });

            }

            @Override
            public void onFailure(final String error) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mGroupProfileView.setEnableProgressDialog(false, null);
                        mGroupProfileView.showError(error);
                    }
                });
            }
        });
    }

    @Override
    public void quitGroup() {
        mGroupProfileView.setEnableProgressDialog(true, AndroidUtil.getString(R.string.ProgressHint_Quit));
        mGroupManager.quitGroup(mConversation.getTargetId(), new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (mConversation.getTargetId().equals(ChatPresenter.sConversationID)) {
                    mGroupProfileView.finishChatActivity();
                }
                mGroupProfileView.setEnableProgressDialog(false, null);
                mGroupProfileView.goBack();
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
        return userID.equals(IMClient.getInstance().userManager().getUserID());
    }

    @Override
    public void enableConversationNotification(boolean isEnable) {
        mConversationManager.enableConversationNotification(mConversation, isEnable);
    }

    @Override
    public void setConversationToTop(boolean isTop) {
        mConversationManager.setConversationTop(mConversation, isTop);
    }

    @Override
    public void clearChatMessages() {
        mConversationManager.clearAllConversationMessages(mConversation);
    }


}
