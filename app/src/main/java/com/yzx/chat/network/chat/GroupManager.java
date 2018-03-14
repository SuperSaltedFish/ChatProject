package com.yzx.chat.network.chat;

import android.os.Parcel;

import com.yzx.chat.base.BaseHttpCallback;
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
import com.yzx.chat.util.PinYinUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by YZX on 2018年03月09日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupManager {

    private IMClient.SubManagerCallback mSubManagerCallback;
    private Map<String, GroupBean> mGroupsMap;
    private GroupDao mGroupDao;
    private GroupMemberDao mGroupMemberDao;
    private List<OnGroupChangeListener> mOnGroupChangeListeners;

    private GroupApi mGroupApi;
    private NetworkExecutor mNetworkExecutor;
    private Call<JsonResponse<Void>> mRenameGroup;
    private Call<JsonResponse<Void>> mUpdateGroupNotice;
    private Call<JsonResponse<Void>> mUpdateAlias;
    private Call<JsonResponse<Void>> mQuitGroup;

    GroupManager(IMClient.SubManagerCallback subManagerCallback, AbstractDao.ReadWriteHelper readWriteHelper) {
        if (subManagerCallback == null) {
            throw new NullPointerException("subManagerCallback can't be NULL");
        }
        mSubManagerCallback = subManagerCallback;
        mGroupDao = new GroupDao(readWriteHelper);
        mGroupMemberDao = new GroupMemberDao(readWriteHelper);
        mOnGroupChangeListeners = Collections.synchronizedList(new LinkedList<OnGroupChangeListener>());
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
        List<GroupBean> groupList = new ArrayList<>(mGroupsMap.size()+4);
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

    public void quitGroup(final String groupID, final ResultCallback<Boolean> resultCallback) {
        AsyncUtil.cancelCall(mQuitGroup);
        mQuitGroup = mGroupApi.quit(groupID);
        mQuitGroup.setCallback(new BaseHttpCallback<Void>() {
            @Override
            protected void onSuccess(Void response) {
                boolean success = mGroupDao.deleteGroupAndMember(groupID);
                if (!success) {
                    LogUtil.e("updateGroupName:Failure of operating database");
                }

                GroupBean group = mGroupsMap.remove(groupID);
                for (OnGroupChangeListener listener : mOnGroupChangeListeners) {
                    listener.onGroupQuit(group);
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
        mNetworkExecutor.submit(mQuitGroup);
    }

    public void renameGroup(final String groupID, final String newName, final ResultCallback<Boolean> resultCallback) {
        AsyncUtil.cancelCall(mRenameGroup);
        mRenameGroup = mGroupApi.rename(groupID, newName);
        mRenameGroup.setCallback(new BaseHttpCallback<Void>() {
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
                    } else {
                        LogUtil.e("updateGroupName Failure in cache Failure");
                    }
                }

                for (OnGroupChangeListener listener : mOnGroupChangeListeners) {
                    listener.onGroupUpdated(group);
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
        mNetworkExecutor.submit(mRenameGroup);
    }

    public void updateGroupNotice(final String groupID, final String newNotice, final ResultCallback<Boolean> resultCallback) {
        AsyncUtil.cancelCall(mUpdateGroupNotice);
        mUpdateGroupNotice = mGroupApi.updateNotice(groupID, newNotice);
        mUpdateGroupNotice.setCallback(new BaseHttpCallback<Void>() {
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
                    } else {
                        LogUtil.e("updateGroupNotice Failure in cache Failure");
                    }
                }

                for (OnGroupChangeListener listener : mOnGroupChangeListeners) {
                    listener.onGroupUpdated(group);
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
        mNetworkExecutor.submit(mUpdateGroupNotice);
    }

    public void updateMemberAlias(final String groupID, final String memberID, final String newAlias, final ResultCallback<Boolean> resultCallback) {
        AsyncUtil.cancelCall(mUpdateAlias);
        mUpdateAlias = mGroupApi.updateAlias(groupID, newAlias);
        mUpdateAlias.setCallback(new BaseHttpCallback<Void>() {
            @Override
            protected void onSuccess(Void response) {
                boolean success = mGroupMemberDao.updateMemberAlias(groupID, memberID, newAlias);
                if (!success) {
                    LogUtil.e("updateAlias:Failure of operating database");
                }

                if (success) {
                    GroupMemberBean groupMember = getGroupMember(groupID, memberID);
                    if (groupMember != null) {
                        groupMember.setAlias(newAlias);
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
        mNetworkExecutor.submit(mUpdateAlias);
    }

    void destroy() {
        AsyncUtil.cancelCall(mRenameGroup);
        AsyncUtil.cancelCall(mUpdateGroupNotice);
        AsyncUtil.cancelCall(mUpdateAlias);
        AsyncUtil.cancelCall(mQuitGroup);
        mGroupsMap.clear();
        mGroupsMap = null;
    }

    public void addGroupChangeListener(OnGroupChangeListener listener) {
        if (!mOnGroupChangeListeners.contains(listener)) {
            mOnGroupChangeListeners.add(listener);
        }
    }

    public void removeGroupChangeListener(OnGroupChangeListener listener) {
        mOnGroupChangeListeners.remove(listener);
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


    public interface OnGroupChangeListener {
        void onGroupCreated(GroupBean group);

        void onGroupQuit(GroupBean group);

        void onGroupUpdated(GroupBean group);
    }

}
