package com.yzx.chat.presenter;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.GroupMemberBean;
import com.yzx.chat.contract.GroupProfileContract;
import com.yzx.chat.network.chat.GroupManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ResultCallback;

/**
 * Created by YZX on 2018年03月12日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupProfilePresenter implements GroupProfileContract.Presenter {

    private GroupProfileContract.View mGroupProfileView;
    private GroupManager mGroupManager;
    private String mGroupID;
    private Handler mHandler;

    @Override
    public void attachView(GroupProfileContract.View view) {
        mGroupProfileView = view;
        mGroupManager = IMClient.getInstance().groupManager();
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
        if (group != null) {
            mGroupID = groupID;
            String mySelfMemberID = IMClient.getInstance().userManager().getUserID();
            if (!TextUtils.isEmpty(mySelfMemberID)) {
                GroupMemberBean mySelf = mGroupManager.getGroupMember(groupID, mySelfMemberID);
                if (mySelf != null) {
                    mGroupProfileView.showGroupInfo(group, mySelf);
                    return;
                }
            }
        }
        mGroupProfileView.goBack();
    }

    @Override
    public void updateGroupName(final String newName) {
        mGroupManager.renameGroup(mGroupID, newName, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mGroupProfileView.showNewGroupName(newName);
                    }
                });
            }

            @Override
            public void onFailure(final String error) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mGroupProfileView.showError(error);
                    }
                });
            }
        });
    }

    @Override
    public void updateGroupNotice(final String newNotice) {
        mGroupManager.updateGroupNotice(mGroupID, newNotice, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mGroupProfileView.showNewGroupNotice(newNotice);
                    }
                });

            }

            @Override
            public void onFailure(final String error) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mGroupProfileView.showError(error);
                    }
                });
            }
        });
    }

    @Override
    public void updateMyGroupAlias(final String newAlias) {
        mGroupManager.updateMemberAlias(mGroupID, IMClient.getInstance().userManager().getUserID(), newAlias, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mGroupProfileView.showNewMyAlias(newAlias);
                    }
                });

            }

            @Override
            public void onFailure(final String error) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mGroupProfileView.showError(error);
                    }
                });
            }
        });
    }

    @Override
    public void quitGroup() {

    }

}
