package com.yzx.chat.network.chat;

import android.os.Parcel;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseResponseCallback;
import com.yzx.chat.bean.CreateGroupMemberBean;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.GroupMemberBean;
import com.yzx.chat.database.AbstractDao;
import com.yzx.chat.database.GroupDao;
import com.yzx.chat.database.GroupMemberDao;
import com.yzx.chat.network.api.Group.CreateGroupBean;
import com.yzx.chat.network.api.Group.GroupApi;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.NetworkExecutor;
import com.yzx.chat.tool.ApiHelper;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.rong.imlib.model.Message;
import io.rong.message.GroupNotificationMessage;

/**
 * Created by YZX on 2018年03月09日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupManager {

    public static final String GROUP_OPERATION_CREATE = "Create";//群组创建
    public static final String GROUP_OPERATION_ADD = "Add";//新成员加入
    public static final String GROUP_OPERATION_QUIT = "Quit";//旧成员退出

    private static final Set<String> GROUP_OPERATION_SET = new HashSet<>(Arrays.asList(GROUP_OPERATION_CREATE, GROUP_OPERATION_ADD, GROUP_OPERATION_QUIT));


    private IMClient.SubManagerCallback mSubManagerCallback;
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
    private Call<JsonResponse<CreateGroupBean>> mCreateGroupCall;
    private Call<JsonResponse<CreateGroupBean>> mAddMemberCall;

    GroupManager(IMClient.SubManagerCallback subManagerCallback, AbstractDao.ReadWriteHelper readWriteHelper) {
        if (subManagerCallback == null) {
            throw new NullPointerException("subManagerCallback can't be NULL");
        }
        mSubManagerCallback = subManagerCallback;
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
        return mGroupsMap.get(groupID);
    }

    public List<GroupBean> getAllGroup() {
        if (mGroupsMap == null) {
            return null;
        }
        List<GroupBean> groupList = new ArrayList<>(mGroupsMap.size() + 4);
        Parcel parcel;
        for (GroupBean group : mGroupsMap.values()) {
            parcel = Parcel.obtain();
            group.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            groupList.add(GroupBean.CREATOR.createFromParcel(parcel));
            parcel.recycle();
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
                return groupMember;
            }
        }
        return null;
    }

    public void createGroup(final String groupID, final List<CreateGroupMemberBean> memberList, final ResultCallback<GroupBean> resultCallback) {
        AsyncUtil.cancelCall(mCreateGroupCall);
        mCreateGroupCall = mGroupApi.createGroup(groupID, memberList);
        mCreateGroupCall.setResponseCallback(new BaseResponseCallback<CreateGroupBean>() {
            @Override
            protected void onSuccess(CreateGroupBean response) {
                GroupBean group = response.getGroup();
                if (mGroupDao.insertGroupAndMember(group)) {
                    mGroupsMap.put(group.getGroupID(), group);
                    for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                        listener.onCreatedGroup(group);
                    }

                    if (resultCallback != null) {
                        resultCallback.onSuccess(group);
                    }
                } else {
                    LogUtil.e("createGroup:Failure of operating database");
                    onFailure(AndroidUtil.getString(R.string.Server_Error2));
                }
            }

            @Override
            protected void onFailure(String message) {
                resultCallback.onFailure(message);
            }
        }, false);
        mNetworkExecutor.submit(mCreateGroupCall);
    }

    public void addMember(final String groupID, List<CreateGroupMemberBean> memberList, final ResultCallback<GroupBean> resultCallback) {
        AsyncUtil.cancelCall(mAddMemberCall);
        mAddMemberCall = mGroupApi.add(groupID, memberList);
        mAddMemberCall.setResponseCallback(new BaseResponseCallback<CreateGroupBean>() {
            @Override
            protected void onSuccess(CreateGroupBean response) {
                GroupBean group = response.getGroup();
                if (mGroupDao.replaceGroupAndMember(group)) {
                    mGroupsMap.put(group.getGroupID(), group);

                    for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                        listener.onMemberAdded(group, group.getMembers());
                    }

                    if (resultCallback != null) {
                        resultCallback.onSuccess(group);
                    }
                } else {
                    LogUtil.e("createGroup:Failure of operating database");
                    onFailure(AndroidUtil.getString(R.string.Server_Error2));
                }


            }

            @Override
            protected void onFailure(String message) {
                resultCallback.onFailure(message);
            }
        }, false);
        mNetworkExecutor.submit(mAddMemberCall);
    }

    public void quitGroup(final String groupID, final ResultCallback<Boolean> resultCallback) {
        AsyncUtil.cancelCall(mQuitGroupCall);
        mQuitGroupCall = mGroupApi.quit(groupID);
        mQuitGroupCall.setResponseCallback(new BaseResponseCallback<Void>() {
            @Override
            protected void onSuccess(Void response) {
                boolean success = mGroupDao.deleteGroupAndMember(groupID);
                if (!success) {
                    LogUtil.e("updateGroupName:Failure of operating database");
                }

                GroupBean group = mGroupsMap.remove(groupID);
                for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                    listener.onQuitGroup(group);
                }
                mSubManagerCallback.callConversationManager(ConversationManager.CALLBACK_CODE_ClEAR_AND_REMOVE_GROUP, groupID);

                if (resultCallback != null) {
                    resultCallback.onSuccess(success);
                }
            }

            @Override
            protected void onFailure(String message) {
                resultCallback.onFailure(message);
            }
        }, false);
        mNetworkExecutor.submit(mQuitGroupCall);
    }

    public void renameGroup(final String groupID, final String newName, final ResultCallback<Boolean> resultCallback) {
        AsyncUtil.cancelCall(mRenameGroupCall);
        mRenameGroupCall = mGroupApi.rename(groupID, newName);
        mRenameGroupCall.setResponseCallback(new BaseResponseCallback<Void>() {
            @Override
            protected void onSuccess(Void response) {
                boolean success = mGroupDao.updateGroupName(groupID, newName);
                if (!success) {
                    LogUtil.e("updateGroupName:Failure of operating database");
                }

                GroupBean group = mGroupsMap.get(groupID);
                if (success) {
                    if (group != null) {
                        group.setName(newName);
                        for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                            listener.onGroupInfoUpdated(group);
                        }
                    } else {
                        LogUtil.e("updateGroupName Failure in cache Failure");
                    }
                }


                if (resultCallback != null) {
                    resultCallback.onSuccess(success);
                }
            }

            @Override
            protected void onFailure(String message) {
                resultCallback.onFailure(message);
            }
        }, false);
        mNetworkExecutor.submit(mRenameGroupCall);
    }

    public void updateGroupNotice(final String groupID, final String newNotice, final ResultCallback<Boolean> resultCallback) {
        AsyncUtil.cancelCall(mUpdateGroupNoticeCall);
        mUpdateGroupNoticeCall = mGroupApi.updateNotice(groupID, newNotice);
        mUpdateGroupNoticeCall.setResponseCallback(new BaseResponseCallback<Void>() {
            @Override
            protected void onSuccess(Void response) {
                boolean success = mGroupDao.updateGroupNotice(groupID, newNotice);
                if (!success) {
                    LogUtil.e("updateGroupNotice:Failure of operating database");
                }

                GroupBean group = mGroupsMap.get(groupID);
                if (success) {
                    if (group != null) {
                        group.setNotice(newNotice);
                        for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                            listener.onGroupInfoUpdated(group);
                        }
                    } else {
                        LogUtil.e("updateGroupNotice Failure in cache Failure");
                    }
                }


                if (resultCallback != null) {
                    resultCallback.onSuccess(success);
                }
            }

            @Override
            protected void onFailure(String message) {
                resultCallback.onFailure(message);
            }
        }, false);
        mNetworkExecutor.submit(mUpdateGroupNoticeCall);
    }

    public void updateMemberAlias(final String groupID, final String memberID, final String newAlias, final ResultCallback<Boolean> resultCallback) {
        AsyncUtil.cancelCall(mUpdateAliasCall);
        mUpdateAliasCall = mGroupApi.updateAlias(groupID, newAlias);
        mUpdateAliasCall.setResponseCallback(new BaseResponseCallback<Void>() {
            @Override
            protected void onSuccess(Void response) {
                boolean success = mGroupMemberDao.updateMemberAlias(groupID, memberID, newAlias);
                if (!success) {
                    LogUtil.e("updateAlias:Failure of operating database");
                }

                if (success) {
                    GroupBean group = mGroupsMap.get(groupID);
                    GroupMemberBean groupMember = getGroupMember(groupID, memberID);
                    if (group != null && groupMember != null) {
                        groupMember.setAlias(newAlias);
                        for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                            listener.onMemberInfoUpdated(group, groupMember);
                        }
                    } else {
                        LogUtil.e("updateAlias Failure in cache Failure");
                    }
                }


                if (resultCallback != null) {
                    resultCallback.onSuccess(success);
                }
            }

            @Override
            protected void onFailure(String message) {
                resultCallback.onFailure(message);
            }
        }, false);
        mNetworkExecutor.submit(mUpdateAliasCall);
    }

    void onReceiveGroupNotificationMessage(Message message) {
        GroupNotificationMessage groupNotification = (GroupNotificationMessage) message.getContent();
        String operation = groupNotification.getOperation();
        if (!GROUP_OPERATION_SET.contains(operation)) {
            LogUtil.e("unknown group operation:" + operation);
            return;
        }
        GroupBean group;
        switch (groupNotification.getOperation()) {
            case GROUP_OPERATION_CREATE:
                GroupMessageExtra_Created extra_Create;
                try {
                    extra_Create = mGson.fromJson(groupNotification.getExtra(), GroupMessageExtra_Created.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    LogUtil.e("fromJson GroupMessageExtra_Created.class fail,json content:" + groupNotification.getExtra());
                    return;
                }
                if (extra_Create.group != null) {
                    if (!mGroupDao.insertGroupAndMember(extra_Create.group)) {
                        LogUtil.e(" GroupOperation : insertGroupAndMember fail");
                        return;
                    }
                } else {
                    LogUtil.e(" GroupOperation : group is empty");
                    return;
                }
                for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                    listener.onCreatedGroup(extra_Create.group);
                }
                break;
            case GROUP_OPERATION_ADD:
                GroupMessageExtra_Add extra_Add;
                try {
                    extra_Add = mGson.fromJson(groupNotification.getExtra(), GroupMessageExtra_Add.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    LogUtil.e("fromJson GroupMessageExtra_Add.class fail,json content:" + groupNotification.getExtra());
                    return;
                }
                if (extra_Add.members == null || extra_Add.members.size() == 0 || TextUtils.isEmpty(extra_Add.groupID)) {
                    LogUtil.e(" GroupOperation : groupMembers or groupID is empty");
                    return;
                }
                group = getGroup(extra_Add.groupID);
                if (group == null) {
                    LogUtil.e(" GroupOperation : getGroup is empty");
                    return;
                }
                group.getMembers().addAll(extra_Add.members);
                if (!mGroupMemberDao.replaceAll(group.getMembers())) {
                    LogUtil.e(" GroupOperation : replaceAll fail");
                    return;
                }
                for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                    listener.onMemberAdded(group, extra_Add.members);
                }
                break;
            case GROUP_OPERATION_QUIT:
                GroupMessageExtra_Quit extra_Quit;
                try {
                    extra_Quit = mGson.fromJson(groupNotification.getExtra(), GroupMessageExtra_Quit.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    LogUtil.e("fromJson GroupMessageExtra_Quit.class fail,json content:" + groupNotification.getExtra());
                    return;
                }
                if (TextUtils.isEmpty(extra_Quit.memberID) || TextUtils.isEmpty(extra_Quit.groupID)) {
                    LogUtil.e(" GroupOperation : memberID or groupID is empty");
                    return;
                }
                group = getGroup(extra_Quit.groupID);
                if (group == null) {
                    LogUtil.e(" GroupOperation : getGroup is empty");
                    return;
                }
                if (!mGroupMemberDao.deleteByKey(extra_Quit.groupID, extra_Quit.memberID)) {
                    LogUtil.e(" GroupOperation : deleteByKey fail");
                    return;
                }
                List<GroupMemberBean> groupMemberList = group.getMembers();
                GroupMemberBean groupMember;
                for (int i = 0, size = groupMemberList.size(); i < size; i++) {
                    groupMember = groupMemberList.get(i);
                    if (extra_Quit.memberID.equals(groupMember.getUserProfile().getUserID())) {
                        groupMemberList.remove(i);
                        for (OnGroupOperationListener listener : mOnGroupOperationListeners) {
                            listener.onMemberQuit(group, Collections.singletonList(groupMember));
                        }
                        break;
                    }
                }
                break;
        }

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


    public interface OnGroupOperationListener {
        void onCreatedGroup(GroupBean group);

        void onQuitGroup(GroupBean group);

        void onGroupInfoUpdated(GroupBean group);

        void onMemberAdded(GroupBean group, List<GroupMemberBean> groupMemberList);

        void onMemberInfoUpdated(GroupBean group, GroupMemberBean groupMember);

        void onMemberQuit(GroupBean group, List<GroupMemberBean> groupMemberList);

    }

    private static final class GroupMessageExtra_Created {
        String groupID;
        long version;
        GroupBean group;
    }

    private static final class GroupMessageExtra_Add {
        String groupID;
        long version;
        ArrayList<GroupMemberBean> members;
    }

    private static final class GroupMessageExtra_Quit {
        String groupID;
        long version;
        String memberID;
    }


}
