package com.yzx.chat.network.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.yzx.chat.base.BaseResponseCallback;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.GroupMemberBean;
import com.yzx.chat.database.AbstractDao;
import com.yzx.chat.database.GroupDao;
import com.yzx.chat.database.GroupMemberDao;
import com.yzx.chat.network.api.Group.GroupApi;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.NetworkExecutor;
import com.yzx.chat.tool.ApiHelper;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import io.rong.imlib.model.Message;
import io.rong.message.GroupNotificationMessage;

/**
 * Created by YZX on 2018年03月09日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupManager {

    public static final String GROUP_OPERATION_CREATE = "Create";//群组创建
    public static final String GROUP_OPERATION_ADD = "Add";//新成员加入
    public static final String GROUP_OPERATION_QUIT = "Quit";//成员退出
    public static final String GROUP_OPERATION_BULLETIN = "Bulletin";//修改公告
    public static final String GROUP_OPERATION_RENAME = "Rename";//群组重命名
    public static final String GROUP_OPERATION_ALIAS = "Alias";//成员备注改变

    private IMClient.CallbackHelper mCallbackHelper;
    private Map<String, GroupBean> mGroupsMap;
    private GroupDao mGroupDao;
    private GroupMemberDao mGroupMemberDao;
    private List<OnGroupOperationListener> mOnGroupOperationListeners;

    private GroupApi mGroupApi;
    private Gson mGson;
    private NetworkExecutor mNetworkExecutor;
    private Call<JsonResponse<Void>> mRenameGroupCall;
    private Call<JsonResponse<Void>> mUpdateGroupNoticeCall;
    private Call<JsonResponse<Void>> mUpdateAliasCall;
    private Call<JsonResponse<Void>> mQuitGroupCall;
    private Call<JsonResponse<Void>> mCreateGroupCall;
    private Call<JsonResponse<Void>> mAddMemberCall;

    GroupManager(IMClient.CallbackHelper callbackHelper, AbstractDao.ReadWriteHelper readWriteHelper) {
        if (callbackHelper == null) {
            throw new NullPointerException("subManagerCallback can't be NULL");
        }
        mCallbackHelper = callbackHelper;
        mGroupDao = new GroupDao(readWriteHelper);
        mGroupMemberDao = new GroupMemberDao(readWriteHelper);
        mGson = new GsonBuilder().serializeNulls().create();
        mOnGroupOperationListeners = Collections.synchronizedList(new LinkedList<OnGroupOperationListener>());
        mNetworkExecutor = NetworkExecutor.getInstance();
        mGroupApi = (GroupApi) ApiHelper.getProxyInstance(GroupApi.class);
        mGroupsMap = new HashMap<>(24);
        List<GroupBean> groups = mGroupDao.loadAllGroup();
        if (groups != null) {
            for (GroupBean group : groups) {
                mGroupsMap.put(group.getGroupID(), group);
                List<GroupMemberBean> members = group.getMembers();
                for (int ownerIndex = 0, size = members.size(); ownerIndex < size; ownerIndex++) {
                    if (group.getOwner().equals(members.get(ownerIndex).getUserProfile().getUserID())) {
                        Collections.swap(members, 0, ownerIndex);
                        break;
                    }
                }
            }
        }
    }

    public GroupBean getGroup(String groupID) {
        return GroupBean.copy(mGroupsMap.get(groupID));
    }

    public ArrayList<GroupBean> getAllGroup() {
        if (mGroupsMap == null) {
            return null;
        }
        ArrayList<GroupBean> groupList = new ArrayList<>(mGroupsMap.size() + 4);
        for (GroupBean group : mGroupsMap.values()) {
            groupList.add(GroupBean.copy(group));
        }
        return groupList;
    }

    public GroupMemberBean getGroupMember(String groupID, String memberID) {
        GroupBean group = getGroup(groupID);
        if (group == null) {
            return null;
        }

        for (GroupMemberBean groupMember : group.getMembers()) {
            if (groupMember.getUserProfile().getUserID().equals(memberID)) {
                return GroupMemberBean.copy(groupMember);
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

    public void addMember(String groupID, String[] membersID, ResultCallback<Void> resultCallback) {
        AsyncUtil.cancelCall(mAddMemberCall);
        mAddMemberCall = mGroupApi.add(groupID, membersID);
        mAddMemberCall.setResponseCallback(new GroupOperationResponseCallback(resultCallback));
        mNetworkExecutor.submit(mAddMemberCall);
    }

    public void quitGroup(String groupID, ResultCallback<Void> resultCallback) {
        AsyncUtil.cancelCall(mQuitGroupCall);
        mQuitGroupCall = mGroupApi.quit(groupID);
        mQuitGroupCall.setResponseCallback(new GroupOperationResponseCallback(resultCallback));
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

    void onReceiveGroupNotificationMessage(Message message) {
        GroupNotificationMessage groupNotification = (GroupNotificationMessage) message.getContent();
        final String operation = groupNotification.getOperation();
        GroupMessageExtra extra = null;
        try {
            switch (operation) {
                case GROUP_OPERATION_CREATE:
                    extra = mGson.fromJson(groupNotification.getMessage(), GroupMessageExtra.Created.class);
                    break;
                case GROUP_OPERATION_ADD:
                    extra = mGson.fromJson(groupNotification.getMessage(), GroupMessageExtra.Add.class);
                    break;
                case GROUP_OPERATION_QUIT:
                    extra = mGson.fromJson(groupNotification.getMessage(), GroupMessageExtra.Quit.class);
                    break;
                case GROUP_OPERATION_RENAME:
                    extra = mGson.fromJson(groupNotification.getMessage(), GroupMessageExtra.Rename.class);
                    break;
                case GROUP_OPERATION_BULLETIN:
                    extra = mGson.fromJson(groupNotification.getMessage(), GroupMessageExtra.Bulletin.class);
                    break;
                case GROUP_OPERATION_ALIAS:
                    extra = mGson.fromJson(groupNotification.getMessage(), GroupMessageExtra.Alias.class);
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
        mGroupsMap.put(extra.group.getGroupID(), GroupBean.copy(extra.group));

        final GroupMessageExtra finalExtra = extra;
        mCallbackHelper.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (operation) {
                    case GROUP_OPERATION_CREATE:
                        GroupMessageExtra.Created createdExtra = (GroupMessageExtra.Created) finalExtra;
                        for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                            listener.onCreatedGroup(createdExtra.group);
                        }
                        break;
                    case GROUP_OPERATION_ADD:
                        GroupMessageExtra.Add addExtra = (GroupMessageExtra.Add) finalExtra;
                        for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                            listener.onMemberAdded(addExtra.group, addExtra.membersID);
                        }
                        break;
                    case GROUP_OPERATION_QUIT:
                        GroupMessageExtra.Quit quitExtra = (GroupMessageExtra.Quit) finalExtra;
                        if (quitExtra.member.getUserProfile().getUserID().equals(mCallbackHelper.getCurrentUserID())) {
                            for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                                listener.onQuitGroup(quitExtra.group);
                            }
                        } else {
                            for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                                listener.onMemberQuit(quitExtra.group, quitExtra.member);
                            }
                        }
                        break;
                    case GROUP_OPERATION_RENAME:
                        GroupMessageExtra.Rename renameExtra = (GroupMessageExtra.Rename) finalExtra;
                        for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                            listener.onNameChange(renameExtra.group, renameExtra.name);
                        }
                        break;
                    case GROUP_OPERATION_BULLETIN:
                        GroupMessageExtra.Bulletin bulletinExtra = (GroupMessageExtra.Bulletin) finalExtra;
                        for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                            listener.onBulletinChange(bulletinExtra.group, bulletinExtra.bulletin);
                        }
                        break;
                    case GROUP_OPERATION_ALIAS:
                        GroupMessageExtra.Alias aliasExtra = (GroupMessageExtra.Alias) finalExtra;
                        for (GroupMemberBean groupMember : aliasExtra.group.getMembers()) {
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


    static boolean update(ArrayList<GroupBean> groups, AbstractDao.ReadWriteHelper readWriteHelper) {
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
        void onCreatedGroup(GroupBean group);

        void onQuitGroup(GroupBean group);

        void onBulletinChange(GroupBean group, String newBulletin);

        void onNameChange(GroupBean group, String newName);

        void onMemberAdded(GroupBean group, String[] newMembersID);

        void onMemberQuit(GroupBean group, GroupMemberBean quitMember);

        void onMemberAliasChange(GroupBean group, GroupMemberBean member, String newAlias);
    }

    public static class GroupMessageExtra {
        public GroupBean group;
        public String operatorUserId;

        public static final class Created extends GroupMessageExtra {

        }

        public static final class Add extends GroupMessageExtra {
            public String[] membersID;
        }

        public static final class Quit extends GroupMessageExtra {
            public GroupMemberBean member;
        }

        public static final class Rename extends GroupMessageExtra {
            public String name;
        }

        public static final class Bulletin extends GroupMessageExtra {
            public String bulletin;
        }

        public static final class Alias extends GroupMessageExtra {
            public String memberID;
            public String alias;
        }
    }


}
