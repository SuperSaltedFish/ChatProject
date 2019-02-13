package com.yzx.chat.module.group.presenter;

import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.core.entity.GroupMemberEntity;
import com.yzx.chat.module.group.contract.GroupListContract;
import com.yzx.chat.core.GroupManager;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.util.LogUtil;
import com.yzx.chat.util.PinYinUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by YZX on 2018年03月10日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class GroupListPresenter implements GroupListContract.Presenter {

    private GroupListContract.View mGroupListView;
    private List<GroupEntity> mGroupList;

    @Override
    public void attachView(GroupListContract.View view) {
        mGroupListView = view;
        AppClient.getInstance().getGroupManager().addGroupChangeListener(mOnGroupOperationListener);
    }

    @Override
    public void detachView() {
        AppClient.getInstance().getGroupManager().removeGroupChangeListener(mOnGroupOperationListener);
        mGroupList = null;
        mGroupListView = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadAllGroup() {
        List<GroupEntity> groupList = AppClient.getInstance().getGroupManager().getAllGroup();
        if (groupList == null) {
            mGroupList = new ArrayList<>(2);
        } else {
            mGroupList = new ArrayList<>(groupList.size() + 2);
            mGroupList.addAll(groupList);
        }
        mGroupListView.showAllGroupList(mGroupList);
    }

    private final GroupManager.OnGroupOperationListener mOnGroupOperationListener = new GroupManager.OnGroupOperationListener() {
        @Override
        public void onCreatedGroup(GroupEntity group) {
            mGroupList.add(group);
            sortGroup(mGroupList);
            mGroupListView.showNewGroup(group, mGroupList.indexOf(group));
        }

        @Override
        public void onQuitGroup(final GroupEntity group) {
            int position = mGroupList.indexOf(group);
            if (position >= 0) {
                mGroupList.remove(position);
                mGroupListView.hideGroup(position);
            } else {
                LogUtil.e("onQuitGroup:remove group fail");
            }
        }

        @Override
        public void onBulletinChange(GroupEntity group) {

        }

        @Override
        public void onNameChange(GroupEntity group) {
            int oldPosition = mGroupList.indexOf(group);
            if (oldPosition >= 0) {
                mGroupList.set(oldPosition, group);
                sortGroup(mGroupList);
                mGroupListView.showAllGroupList(mGroupList);
            } else {
                LogUtil.e("onNameChange:search group fail");
            }
        }

        @Override
        public void onMemberAdded(GroupEntity group, String[] newMembersID) {
            int oldPosition = mGroupList.indexOf(group);
            if (oldPosition >= 0) {
                mGroupList.set(oldPosition, group);
                mGroupListView.refreshGroup(group, oldPosition);
            } else {
                LogUtil.e("onMemberAdded:search group fail");
            }
        }

        @Override
        public void onMemberJoin(GroupEntity group, String memberID) {

        }

        @Override
        public void onMemberQuit(GroupEntity group, GroupMemberEntity quitMember) {
            int oldPosition = mGroupList.indexOf(group);
            if (oldPosition >= 0) {
                mGroupList.set(oldPosition, group);
                mGroupListView.refreshGroup(group, oldPosition);
            } else {
                LogUtil.e("onMemberQuit:search group fail");
            }
        }

        @Override
        public void onMemberAliasChange(GroupEntity group, GroupMemberEntity member, String newAlias) {

        }
    };

    private static void sortGroup(List<GroupEntity> groupList) {
        Collections.sort(groupList, new Comparator<GroupEntity>() {
            @Override
            public int compare(GroupEntity o1, GroupEntity o2) {
                if (o1 == null || o2 == null) {
                    return 0;
                }
                return PinYinUtil.getPinYinAbbreviation(o1.getName(), false).compareTo(PinYinUtil.getPinYinAbbreviation(o2.getName(), false));
            }
        });
    }
}
