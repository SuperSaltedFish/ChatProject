package com.yzx.chat.core;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.JsonSyntaxException;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.core.entity.GroupMemberEntity;
import com.yzx.chat.core.listener.ResultCallback;
import com.yzx.chat.core.database.AbstractDao;
import com.yzx.chat.core.database.GroupDao;
import com.yzx.chat.core.database.GroupMemberDao;
import com.yzx.chat.core.net.ResponseHandler;
import com.yzx.chat.core.net.api.GroupApi;
import com.yzx.chat.core.entity.QuitGroupEntity;
import com.yzx.chat.core.net.ApiHelper;
import com.yzx.chat.core.util.CallbackUtil;
import com.yzx.chat.core.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import io.rong.imlib.RongIMClient;
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

    private AppClient mAppClient;
    private RongIMClient mRongIMClient;
    private Handler mUIHandler;

    private Map<String, GroupEntity> mGroupsMap;
    private GroupDao mGroupDao;
    private List<OnGroupOperationListener> mOnGroupOperationListeners;

    private GroupApi mGroupApi;

    GroupManager(AppClient appClient) {
        mAppClient = appClient;
        mRongIMClient = mAppClient.getRongIMClient();
        mUIHandler = new Handler(Looper.getMainLooper());

        mOnGroupOperationListeners = Collections.synchronizedList(new LinkedList<OnGroupOperationListener>());
        mGroupApi = ApiHelper.getProxyInstance(GroupApi.class);
        mGroupsMap = new HashMap<>(24);
    }

    void init(AbstractDao.ReadWriteHelper helper) {
        mGroupDao = new GroupDao(helper);
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

    void destroy() {
        mUIHandler.removeCallbacksAndMessages(null);
        mGroupsMap.clear();
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

    public void createGroup(String groupID, String[] membersID, ResultCallback<Void> callback) {
        mGroupApi.createGroup(groupID, membersID)
                .enqueue(new ResponseHandler<>(callback));

    }

    public void joinGroup(String groupID, @GroupApi.JoinType String joinType, ResultCallback<Void> callback) {
        mGroupApi.join(groupID, joinType)
                .enqueue(new ResponseHandler<>(callback));
    }

    public void addMember(String groupID, String[] membersID, ResultCallback<Void> callback) {
        mGroupApi.add(groupID, membersID)
                .enqueue(new ResponseHandler<>(callback));
    }

    public void quitGroup(final String groupID, final ResultCallback<Void> callback) {
        mGroupApi.quit(groupID)
                .enqueue(new ResponseHandler<>(new ResultCallback<QuitGroupEntity>() {
                    @Override
                    public void onResult(QuitGroupEntity result) {
                        GroupEntity group = mGroupsMap.get(groupID);
                        mGroupsMap.remove(groupID);
                        mAppClient.getConversationManager().removeConversation(Conversation.ConversationType.GROUP, groupID, null);
                        mAppClient.getConversationManager().clearConversationMessages(Conversation.ConversationType.GROUP, groupID, null);
                        for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                            listener.onQuitGroup(group);
                        }
                        CallbackUtil.callResult(null, callback);
                    }

                    @Override
                    public void onFailure(int code, String error) {
                        CallbackUtil.callFailure(code, error, callback);
                    }
                }));
    }

    public void renameGroup(String groupID, String newName, ResultCallback<Void> callback) {
        mGroupApi.rename(groupID, newName)
                .enqueue(new ResponseHandler<>(callback));
    }

    public void updateGroupNotice(String groupID, String newNotice, ResultCallback<Void> callback) {
        mGroupApi.updateNotice(groupID, newNotice)
                .enqueue(new ResponseHandler<>(callback));
    }

    public void updateMemberAlias(String groupID, String newAlias, ResultCallback<Void> callback) {
        mGroupApi.updateAlias(groupID, newAlias)
                .enqueue(new ResponseHandler<>(callback));
    }

    void onReceiveGroupNotificationMessage(GroupNotificationMessage groupNotification) {
        final String operation = groupNotification.getOperation();
        GroupMessageExtra extra = null;
        try {
            switch (operation) {
                case GROUP_OPERATION_CREATE:
                    extra = ApiHelper.GSON.fromJson(groupNotification.getExtra(), GroupMessageExtra.Created.class);
                    break;
                case GROUP_OPERATION_ADD:
                    extra = ApiHelper.GSON.fromJson(groupNotification.getExtra(), GroupMessageExtra.Add.class);
                    break;
                case GROUP_OPERATION_JOIN:
                    extra = ApiHelper.GSON.fromJson(groupNotification.getExtra(), GroupMessageExtra.Join.class);
                    break;
                case GROUP_OPERATION_QUIT:
                    extra = ApiHelper.GSON.fromJson(groupNotification.getExtra(), GroupMessageExtra.Quit.class);
                    break;
                case GROUP_OPERATION_RENAME:
                    extra = ApiHelper.GSON.fromJson(groupNotification.getExtra(), GroupMessageExtra.Rename.class);
                    break;
                case GROUP_OPERATION_BULLETIN:
                    extra = ApiHelper.GSON.fromJson(groupNotification.getExtra(), GroupMessageExtra.Bulletin.class);
                    break;
                case GROUP_OPERATION_ALIAS:
                    extra = ApiHelper.GSON.fromJson(groupNotification.getExtra(), GroupMessageExtra.Alias.class);
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
        mUIHandler.post(new Runnable() {
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
                            LogUtil.e("quitExtra.member == null || quitExtra.member.getUserInfo() == null");
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

    public void addGroupChangeListener(OnGroupOperationListener listener) {
        if (!mOnGroupOperationListeners.contains(listener)) {
            mOnGroupOperationListeners.add(listener);
        }
    }

    public void removeGroupChangeListener(OnGroupOperationListener listener) {
        mOnGroupOperationListeners.remove(listener);
    }


    static boolean insertAll(ArrayList<GroupEntity> groups, AbstractDao.ReadWriteHelper readWriteHelper) {
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
