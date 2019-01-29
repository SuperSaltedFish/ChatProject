package com.yzx.chat.core.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.yzx.chat.base.BaseResponseCallback;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.core.entity.GroupMemberEntity;
import com.yzx.chat.core.net.chat.IManagerHelper;
import com.yzx.chat.core.listener.ResultCallback;
import com.yzx.chat.database.AbstractDao;
import com.yzx.chat.database.GroupDao;
import com.yzx.chat.database.GroupMemberDao;
import com.yzx.chat.core.net.api.Group.GroupApi;
import com.yzx.chat.core.net.api.Group.QuitGroupBean;
import com.yzx.chat.core.net.api.JsonResponse;
import com.yzx.chat.core.net.framework.Call;
import com.yzx.chat.core.net.framework.NetworkExecutor;
import com.yzx.chat.tool.ApiHelper;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import io.rong.imlib.model.Conversation;
import io.rong.message.GroupNotificationMessage;

/**
 * Created by YZX on 2018年03月09日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupManager {

    public static final String GROUP_OPERATION_CREATE = "Create";//群组创建
    public static final String GROUP_OPERATION_ADD = "Add";//新成员加入（被动加入）
    public static final String GROUP_OPERATION_JOIN = "Join";//新成员加入（主动加入）
    public static final String GROUP_OPERATION_QUIT = "Quit";//成员退出
    public static final String GROUP_OPERATION_BULLETIN = "Bulletin";//修改公告
    public static final String GROUP_OPERATION_RENAME = "Rename";//群组重命名
    public static final String GROUP_OPERATION_ALIAS = "Alias";//成员备注改变

    private IManagerHelper mManagerHelper;
    private Map<String, GroupEntity> mGroupsMap;
    private GroupDao mGroupDao;
    private GroupMemberDao mGroupMemberDao;
    private List<OnGroupOperationListener> mOnGroupOperationListeners;

    private GroupApi mGroupApi;
    private Gson mGson;
    private NetworkExecutor mNetworkExecutor;
    private Call<JsonResponse<Void>> mRenameGroupCall;
    private Call<JsonResponse<Void>> mUpdateGroupNoticeCall;
    private Call<JsonResponse<Void>> mUpdateAliasCall;
    private Call<JsonResponse<QuitGroupBean>> mQuitGroupCall;
    private Call<JsonResponse<Void>> mCreateGroupCall;
    private Call<JsonResponse<Void>> mJoinGroupCall;
    private Call<JsonResponse<Void>> mAddMemberCall;

    GroupManager(IManagerHelper helper) {
        mManagerHelper = helper;
        mGroupDao = new GroupDao(mManagerHelper.getReadWriteHelper());
        mGroupMemberDao = new GroupMemberDao(mManagerHelper.getReadWriteHelper());
        mGson = new GsonBuilder().serializeNulls().create();
        mOnGroupOperationListeners = Collections.synchronizedList(new LinkedList<OnGroupOperationListener>());
        mNetworkExecutor = NetworkExecutor.getInstance();
        mGroupApi = (GroupApi) ApiHelper.getProxyInstance(GroupApi.class);
        mGroupsMap = new HashMap<>(24);
        List<GroupEntity> groups = mGroupDao.loadAllGroup();
        if (groups != null) {
            for (GroupEntity group : groups) {
                mGroupsMap.put(group.getGroupID(), group);
                List<GroupMemberEntity> members = group.getMembers();
                for (int ownerIndex = 0, size = members.size(); ownerIndex < size; ownerIndex++) {
                    if (group.getOwner().equals(members.get(ownerIndex).getUserProfile().getUserID())) {
                        Collections.swap(members, 0, ownerIndex);
                        break;
                    }
                }
            }
        }
    }

    public GroupEntity getGroup(String groupID) {
        return GroupEntity.copy(mGroupsMap.get(groupID));
    }

    public ArrayList<GroupEntity> getAllGroup() {
        if (mGroupsMap == null) {
            return null;
        }
        ArrayList<GroupEntity> groupList = new ArrayList<>(mGroupsMap.size() + 4);
        for (GroupEntity group : mGroupsMap.values()) {
            groupList.add(GroupEntity.copy(group));
        }
        return groupList;
    }

    public GroupMemberEntity getGroupMember(String groupID, String memberID) {
        GroupEntity group = getGroup(groupID);
        if (group == null) {
            return null;
        }

        for (GroupMemberEntity groupMember : group.getMembers()) {
            if (groupMember.getUserProfile().getUserID().equals(memberID)) {
                return GroupMemberEntity.copy(groupMember);
            }
        }
        return null;
    }

    public void createGroup(String groupID, String[] membersID, ResultCallback<Void> resultCallback) {
        AsyncUtil.cancelCall(mCreateGroupCall);
        mCreateGroupCall = mGroupApi.createGroup(groupID, membersID);
        mCreateGroupCall.setResponseCallback(new GroupOperationResponseCallback(resultCallback));
        mNetworkExecutor.submit(mCreateGroupCall);
    }

    public void joinGroup(String groupID, @GroupApi.JoinType String joinType, ResultCallback<Void> resultCallback) {
        AsyncUtil.cancelCall(mJoinGroupCall);
        mJoinGroupCall = mGroupApi.join(groupID, joinType);
        mJoinGroupCall.setResponseCallback(new GroupOperationResponseCallback(resultCallback));
        mNetworkExecutor.submit(mJoinGroupCall);
    }

    public void addMember(String groupID, String[] membersID, ResultCallback<Void> resultCallback) {
        AsyncUtil.cancelCall(mAddMemberCall);
        mAddMemberCall = mGroupApi.add(groupID, membersID);
        mAddMemberCall.setResponseCallback(new GroupOperationResponseCallback(resultCallback));
        mNetworkExecutor.submit(mAddMemberCall);
    }

    public void quitGroup(final String groupID, final ResultCallback<Void> resultCallback) {
        AsyncUtil.cancelCall(mQuitGroupCall);
        mQuitGroupCall = mGroupApi.quit(groupID);
        mQuitGroupCall.setResponseCallback(new BaseResponseCallback<QuitGroupBean>() {
            @Override
            protected void onSuccess(QuitGroupBean response) {
                GroupEntity group = mGroupsMap.get(groupID);
                mGroupsMap.remove(groupID);
                mManagerHelper.getConversationManager().removeConversation(Conversation.ConversationType.GROUP, groupID);
                mManagerHelper.getConversationManager().clearAllConversationMessages(Conversation.ConversationType.GROUP, groupID);
                for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                    listener.onQuitGroup(group);
                }
                if (resultCallback != null) {
                    resultCallback.onSuccess(null);
                }
            }

            @Override
            protected void onFailure(String message) {
                if (resultCallback != null) {
                    resultCallback.onFailure(message);
                }
            }
        });
        mNetworkExecutor.submit(mQuitGroupCall);
    }

    public void renameGroup(String groupID, String newName, ResultCallback<Void> resultCallback) {
        AsyncUtil.cancelCall(mRenameGroupCall);
        mRenameGroupCall = mGroupApi.rename(groupID, newName);
        mRenameGroupCall.setResponseCallback(new GroupOperationResponseCallback(resultCallback));
        mNetworkExecutor.submit(mRenameGroupCall);
    }

    public void updateGroupNotice(String groupID, String newNotice, ResultCallback<Void> resultCallback) {
        AsyncUtil.cancelCall(mUpdateGroupNoticeCall);
        mUpdateGroupNoticeCall = mGroupApi.updateNotice(groupID, newNotice);
        mUpdateGroupNoticeCall.setResponseCallback(new GroupOperationResponseCallback(resultCallback));
        mNetworkExecutor.submit(mUpdateGroupNoticeCall);
    }

    public void updateMemberAlias(String groupID, String newAlias, ResultCallback<Void> resultCallback) {
        AsyncUtil.cancelCall(mUpdateAliasCall);
        mUpdateAliasCall = mGroupApi.updateAlias(groupID, newAlias);
        mUpdateAliasCall.setResponseCallback(new GroupOperationResponseCallback(resultCallback));
        mNetworkExecutor.submit(mUpdateAliasCall);
    }

    void onReceiveGroupNotificationMessage(GroupNotificationMessage groupNotification) {
        final String operation = groupNotification.getOperation();
        GroupMessageExtra extra = null;
        try {
            switch (operation) {
                case GROUP_OPERATION_CREATE:
                    extra = mGson.fromJson(groupNotification.getExtra(), GroupMessageExtra.Created.class);
                    break;
                case GROUP_OPERATION_ADD:
                    extra = mGson.fromJson(groupNotification.getExtra(), GroupMessageExtra.Add.class);
                    break;
                case GROUP_OPERATION_JOIN:
                    extra = mGson.fromJson(groupNotification.getExtra(), GroupMessageExtra.Join.class);
                    break;
                case GROUP_OPERATION_QUIT:
                    extra = mGson.fromJson(groupNotification.getExtra(), GroupMessageExtra.Quit.class);
                    break;
                case GROUP_OPERATION_RENAME:
                    extra = mGson.fromJson(groupNotification.getExtra(), GroupMessageExtra.Rename.class);
                    break;
                case GROUP_OPERATION_BULLETIN:
                    extra = mGson.fromJson(groupNotification.getExtra(), GroupMessageExtra.Bulletin.class);
                    break;
                case GROUP_OPERATION_ALIAS:
                    extra = mGson.fromJson(groupNotification.getExtra(), GroupMessageExtra.Alias.class);
                    break;
                default:
                    LogUtil.e("unknown group operation:" + operation);
                    return;
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        if (extra == null) {
            LogUtil.e("GroupOperation:json to object(GroupMessageExtra) error,json content:" + groupNotification.getExtra());
            return;
        }
        if (extra.group == null) {
            LogUtil.e(" GroupOperation : group is empty");
            return;
        }
        if (!mGroupDao.replaceGroupAndMember(extra.group)) {
            LogUtil.e(" GroupOperation : replaceGroupAndMember fail");
            return;
        }
        mGroupsMap.put(extra.group.getGroupID(), GroupEntity.copy(extra.group));

        final GroupMessageExtra finalExtra = extra;
        mManagerHelper.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (operation) {
                    case GROUP_OPERATION_CREATE:
                        GroupMessageExtra.Created createdExtra = (GroupMessageExtra.Created) finalExtra;
                        if (createdExtra.group == null) {
                            LogUtil.e("createdExtra.group == null");
                            break;
                        }
                        for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                            listener.onCreatedGroup(createdExtra.group);
                        }
                        break;
                    case GROUP_OPERATION_ADD:
                        GroupMessageExtra.Add addExtra = (GroupMessageExtra.Add) finalExtra;
                        if (addExtra.group == null || addExtra.membersID == null) {
                            LogUtil.e("addExtra.group == null || addExtra.membersID == null");
                            break;
                        }
                        for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                            listener.onMemberAdded(addExtra.group, addExtra.membersID);
                        }
                        break;
                    case GROUP_OPERATION_JOIN:
                        GroupMessageExtra.Join joinExtra = (GroupMessageExtra.Join) finalExtra;
                        if (joinExtra.group == null || joinExtra.memberID == null) {
                            LogUtil.e("joinExtra.group == null || joinExtra.memberID == null");
                            break;
                        }
                        for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                            listener.onMemberJoin(joinExtra.group, joinExtra.memberID);
                        }
                        break;
                    case GROUP_OPERATION_QUIT:
                        GroupMessageExtra.Quit quitExtra = (GroupMessageExtra.Quit) finalExtra;
                        if (quitExtra.member == null || quitExtra.member.getUserProfile() == null) {
                            LogUtil.e("quitExtra.member == null || quitExtra.member.getUserProfile() == null");
                            break;
                        }
                        for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                            listener.onMemberQuit(quitExtra.group, quitExtra.member);
                        }
                        break;
                    case GROUP_OPERATION_RENAME:
                        GroupMessageExtra.Rename renameExtra = (GroupMessageExtra.Rename) finalExtra;
                        if (renameExtra.group == null) {
                            LogUtil.e("renameExtra.group == null");
                            break;
                        }
                        for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                            listener.onNameChange(renameExtra.group);
                        }
                        break;
                    case GROUP_OPERATION_BULLETIN:
                        GroupMessageExtra.Bulletin bulletinExtra = (GroupMessageExtra.Bulletin) finalExtra;
                        if (bulletinExtra.group == null) {
                            LogUtil.e("bulletinExtra.group == null");
                            break;
                        }
                        for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                            listener.onBulletinChange(bulletinExtra.group);
                        }
                        break;
                    case GROUP_OPERATION_ALIAS:
                        GroupMessageExtra.Alias aliasExtra = (GroupMessageExtra.Alias) finalExtra;
                        for (GroupMemberEntity groupMember : aliasExtra.group.getMembers()) {
                            if (groupMember.getUserProfile().getUserID().equals(aliasExtra.memberID)) {
                                for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                                    listener.onMemberAliasChange(aliasExtra.group, groupMember, aliasExtra.alias);
                                }
                                break;
                            }
                        }
                        break;
                }
            }
        });
    }

    void destroy() {
        AsyncUtil.cancelCall(mRenameGroupCall);
        AsyncUtil.cancelCall(mAddMemberCall);
        AsyncUtil.cancelCall(mUpdateGroupNoticeCall);
        AsyncUtil.cancelCall(mUpdateAliasCall);
        AsyncUtil.cancelCall(mQuitGroupCall);
        AsyncUtil.cancelCall(mJoinGroupCall);
        mGroupsMap.clear();
        mGroupsMap = null;
    }

    public void addGroupChangeListener(OnGroupOperationListener listener) {
        if (!mOnGroupOperationListeners.contains(listener)) {
            mOnGroupOperationListeners.add(listener);
        }
    }

    public void removeGroupChangeListener(OnGroupOperationListener listener) {
        mOnGroupOperationListeners.remove(listener);
    }


    static boolean update(ArrayList<GroupEntity> groups, AbstractDao.ReadWriteHelper readWriteHelper) {
        GroupDao groupDao = new GroupDao(readWriteHelper);
        GroupMemberDao groupMemberDao = new GroupMemberDao(readWriteHelper);
        groupMemberDao.cleanTable();
        groupDao.cleanTable();
        if (groupDao.insertAllGroupAndMember(groups)) {
            return true;
        } else {
            LogUtil.e("updateAllGroups fail");
            return false;
        }
    }

    private static class GroupOperationResponseCallback extends BaseResponseCallback<Void> {
        private ResultCallback<Void> resultCallback;

        GroupOperationResponseCallback(ResultCallback<Void> resultCallback) {
            this.resultCallback = resultCallback;
        }

        @Override
        protected void onSuccess(Void response) {
            resultCallback.onSuccess(null);
        }

        @Override
        protected void onFailure(final String message) {
            resultCallback.onFailure(message);
        }
    }


    public interface OnGroupOperationListener {
        void onCreatedGroup(GroupEntity group);

        void onQuitGroup(GroupEntity group);

        void onBulletinChange(GroupEntity group);

        void onNameChange(GroupEntity group);

        void onMemberAdded(GroupEntity group, String[] newMembersID);

        void onMemberJoin(GroupEntity group, String memberID);

        void onMemberQuit(GroupEntity group, GroupMemberEntity quitMember);

        void onMemberAliasChange(GroupEntity group, GroupMemberEntity member, String newAlias);
    }

    public static class GroupMessageExtra {
        public GroupEntity group;
        public String operatorUserID;

        public static final class Created extends GroupMessageExtra {

        }

        public static final class Add extends GroupMessageExtra {
            public String[] membersID;
        }

        public static final class Join extends GroupMessageExtra {
            public String memberID;
        }

        public static final class Quit extends GroupMessageExtra {
            public GroupMemberEntity member;
        }

        public static final class Rename extends GroupMessageExtra {

        }

        public static final class Bulletin extends GroupMessageExtra {

        }

        public static final class Alias extends GroupMessageExtra {
            public String memberID;
            public String alias;
        }
    }


}
