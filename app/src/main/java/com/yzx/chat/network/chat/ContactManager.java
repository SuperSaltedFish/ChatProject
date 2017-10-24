package com.yzx.chat.network.chat;

import com.hyphenate.EMContactListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;

/**
 * Created by YZX on 2017年10月04日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ContactManager {

    private ContactListener mContactListener;
    private EMContactListener mEMContactListener;

    //添加好友
    public void addContact(String toAddUsername, String reason) {
        //参数为要添加的好友的username和添加理由
        try {
            EMClient.getInstance().contactManager().addContact(toAddUsername, reason);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //删除好友
    public void deleteContact(String toAddUsername) {
        try {
            EMClient.getInstance().contactManager().deleteContact(toAddUsername);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //同意好友请求
    public void acceptInvitation(String toAddUsername) {
        try {
            EMClient.getInstance().contactManager().acceptInvitation(toAddUsername);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //拒绝好友请求
    public void declineInvitation(String toAddUsername) {
        try {
            EMClient.getInstance().contactManager().declineInvitation(toAddUsername);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //监听好友状态事件
    public void setContactListener(ContactListener listener) {
        if (mContactListener != listener && listener != null) {
            EMClient.getInstance().contactManager().removeContactListener(mEMContactListener);
        }
        mContactListener = listener;
        if (mContactListener == null) {
            mEMContactListener = null;
            return;
        }
        mEMContactListener = new EMContactListener() {
            @Override
            public void onContactInvited(String username, String reason) {
                mContactListener.onContactInvited(username, reason);
            }

            //好友请求被同意
            @Override
            public void onFriendRequestAccepted(String s) {
                mContactListener.onFriendRequestAccepted(s);
            }

            //好友请求被拒绝
            @Override
            public void onFriendRequestDeclined(String s) {
                mContactListener.onFriendRequestDeclined(s);
            }

            @Override
            public void onContactDeleted(String username) {
                mContactListener.onContactDeleted(username);
            }


            @Override
            public void onContactAdded(String username) {
                mContactListener.onContactAdded(username);
            }
        };
        EMClient.getInstance().contactManager().setContactListener(mEMContactListener);
    }


    //从服务器获取黑名单列表
    public List<String> getBlackListFromServer() {
        try {
            return EMClient.getInstance().contactManager().getBlackListFromServer();
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
        return null;
    }

    //从本地db获取黑名单列表
    public List<String> getBlackListUsernames() {
        return EMClient.getInstance().contactManager().getBlackListUsernames();
    }

    //把用户加入到黑名单
    public void addUserToBlackList(String toAddUsername) {
        try {
            EMClient.getInstance().contactManager().addUserToBlackList(toAddUsername, true);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //把用户从黑名单中移除
    public void removeUserFromBlackList(String toRemoveUsername) {
        try {
            EMClient.getInstance().contactManager().removeUserFromBlackList(toRemoveUsername);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    public interface ContactListener {
        //好友请求被同意
        void onFriendRequestAccepted(String username);

        //好友请求被拒绝
        void onFriendRequestDeclined(String username);

        //收到好友邀请
        void onContactInvited(String username, String reason);

        //被删除时回调此方法
        void onContactDeleted(String username);

        //增加了联系人时回调此方法
        void onContactAdded(String username);
    }
}
