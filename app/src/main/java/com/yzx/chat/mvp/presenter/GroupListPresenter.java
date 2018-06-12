package com.yzx.chat.mvp.presenter;

import android.os.Handler;
import android.support.v7.util.DiffUtil;

import com.yzx.chat.base.DiffCalculate;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.GroupMemberBean;
import com.yzx.chat.mvp.contract.GroupListContract;
import com.yzx.chat.network.chat.GroupManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.BackstageAsyncTask;
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
    private LoadAllGroupTask mLoadAllGroupTask;
    private List<GroupBean> mGroupList;
    private Handler mHandler;

    @Override
    public void attachView(GroupListContract.View view) {
        mGroupListView = view;
        mGroupList = new ArrayList<>(24);
        mHandler = new Handler();
        IMClient.getInstance().groupManager().addGroupChangeListener(mOnGroupOperationListener);
    }

    @Override
    public void detachView() {
        mHandler.removeCallbacksAndMessages(null);
        AsyncUtil.cancelTask(mLoadAllGroupTask);
        IMClient.getInstance().groupManager().removeGroupChangeListener(mOnGroupOperationListener);
        mGroupList.clear();
        mGroupList = null;
        mGroupListView = null;
        mHandler = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadAllGroup() {
        LogUtil.e("loadAllGroup");
        AsyncUtil.cancelTask(mLoadAllGroupTask);
        mLoadAllGroupTask = new LoadAllGroupTask(this);
        mLoadAllGroupTask.execute(mGroupList);
    }

    private void loadAllGroupComplete(DiffUtil.DiffResult diffResult) {
        mGroupListView.showGroupList(diffResult, mGroupList);
    }

    private final GroupManager.OnGroupOperationListener mOnGroupOperationListener = new GroupManager.OnGroupOperationListener() {
        @Override
        public void onCreatedGroup(GroupBean group) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    loadAllGroup();
                }
            });
        }

        @Override
        public void onQuitGroup(final GroupBean group) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mGroupList.remove(group);
                    mGroupListView.removeGroupItem(group);
                }
            });
        }

        @Override
        public void onGroupInfoUpdated(final GroupBean group) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    int index = mGroupList.indexOf(group);
                    if (index >= 0) {
                        GroupBean old = mGroupList.get(index);
                        if (!old.getName().equals(group.getName())) {
                            loadAllGroup();
                        } else {
                            mGroupList.set(index, group);
                            mGroupListView.refreshGroupItem(group);
                        }
                    } else {
                        loadAllGroup();
                    }
                }
            });
        }

        @Override
        public void onMemberAdded(GroupBean group, List<GroupMemberBean> groupMemberList) {
            onGroupInfoUpdated(group);
        }

        @Override
        public void onMemberInfoUpdated(GroupBean group, GroupMemberBean groupMember) {

        }

        @Override
        public void onMemberQuit(GroupBean group, List<GroupMemberBean> groupMemberList) {
            onGroupInfoUpdated(group);
        }
    };

    private static class LoadAllGroupTask extends BackstageAsyncTask<GroupListPresenter, List<GroupBean>, DiffUtil.DiffResult> {

        LoadAllGroupTask(GroupListPresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(List<GroupBean>[] oldList) {
            List<GroupBean> newGroupList = IMClient.getInstance().groupManager().getAllGroup();
            List<GroupBean> oldGroupList = oldList[0];

            Collections.sort(newGroupList, new Comparator<GroupBean>() {
                @Override
                public int compare(GroupBean o1, GroupBean o2) {
                    if (o1 == null || o2 == null) {
                        return 0;
                    }
                    return PinYinUtil.getPinYinAbbreviation(o1.getName(), false).compareTo(PinYinUtil.getPinYinAbbreviation(o2.getName(), false));
                }
            });

            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCalculate<GroupBean>(oldGroupList, newGroupList) {
                @Override
                public boolean isItemEquals(GroupBean oldItem, GroupBean newItem) {
                    return oldItem.getGroupID().equals(newItem.getGroupID());
                }

                @Override
                public boolean isContentsEquals(GroupBean oldItem, GroupBean newItem) {
                    return !(!oldItem.getName().equals(newItem.getName()) || oldItem.getMembers().size() != newItem.getMembers().size());
                }
            }, true);
            oldGroupList.clear();
            oldGroupList.addAll(newGroupList);
            return diffResult;
        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult, GroupListPresenter lifeDependentObject) {
            super.onPostExecute(diffResult, lifeDependentObject);
            lifeDependentObject.loadAllGroupComplete(diffResult);
        }
    }
}
