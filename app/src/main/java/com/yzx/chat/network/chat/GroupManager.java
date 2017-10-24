package com.yzx.chat.network.chat;

import android.text.TextUtils;

import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupManager;
import com.hyphenate.chat.EMGroupOptions;
import com.hyphenate.chat.EMMucSharedFile;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by YZX on 2017年10月05日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class GroupManager {

    private GroupChangeListener mGroupChangeListener;
    private EMGroupChangeListener mEMGroupChangeListener;

    /**
     * 创建群组
     *
     * @param groupName  群组名称
     * @param desc       群组简介
     * @param allMembers 群组初始成员，如果只有自己传空数组即可
     * @param reason     邀请成员加入的reason(理由)
     *                   //     * @param option 群组类型选项，可以设置群组最大用户数(默认200)及群组类型@see {@link EMGroupManager.EMGroupStyle}
     *                   //     *               option.inviteNeedConfirm表示邀请对方进群是否需要对方同意，默认是需要用户同意才能加群的。
     *                   //     *               option.extField创建群时可以为群组设定扩展字段，方便个性化订制。
     * @return 创建好的group
     */
    public void createGroup(String groupName, String desc, String[] allMembers, String reason) {
        EMGroupOptions option = new EMGroupOptions();
        option.maxUsers = 200;
        option.style = EMGroupManager.EMGroupStyle.EMGroupStylePrivateMemberCanInvite;
//        EMGroupStylePrivateOnlyOwnerInvite——私有群，只有群主可以邀请人；
//        EMGroupStylePrivateMemberCanInvite——私有群，群成员也能邀请人进群；
//        EMGroupStylePublicJoinNeedApproval——公开群，加入此群除了群主邀请，只能通过申请加入此群；
//        EMGroupStylePublicOpenJoin ——公开群，任何人都能加入此群。

        try {
            EMGroup emGroup = EMClient.getInstance().groupManager().createGroup(groupName, desc, allMembers, reason, option);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //添加管理员权限
    public void addGroupAdmin(String groupId, String addAdminID) {
        try {
            EMGroup emGroup = EMClient.getInstance().groupManager().addGroupAdmin(groupId, addAdminID);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //移除管理员权限
    public void removeGroupAdmin(String groupId, String removeAdminID) {
        try {
            EMGroup emGroup = EMClient.getInstance().groupManager().removeGroupAdmin(groupId, removeAdminID);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //变更群组所有者
    public void changeOwner(String groupId, String newOwnerID) {
        try {
            EMGroup emGroup = EMClient.getInstance().groupManager().changeOwner(groupId, newOwnerID);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //群组加人
    public void addUsersToGroup(String groupId, String[] newMembers, boolean isAdmin) {
        try {
            if (isAdmin) {
                //群主加人调用此方法
                EMClient.getInstance().groupManager().addUsersToGroup(groupId, newMembers);//需异步处理
            } else {
                //私有群里，如果开放了群成员邀请，群成员邀请调用下面方法
                EMClient.getInstance().groupManager().inviteUser(groupId, newMembers, null);//需异步处理
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //群组踢人
    public void removeUserFromGroup(String groupId, String usernameID) {
        try {
            //把usernameID从群组里删除
            EMClient.getInstance().groupManager().removeUserFromGroup(groupId, usernameID);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //加入某个群组,只能用于加入公开群，不需要申请加入。
    public void joinPublicGroup(String groupId) {
        try {
            EMClient.getInstance().groupManager().joinGroup(groupId);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //加入某个群组,只能用于私人群，需要申请加入。
    public void joinPrivateGroup(String groupId, String reason) {
        try {
            //需要申请和验证才能加入的，即group.isMembersOnly()为true，调用下面方法
            EMClient.getInstance().groupManager().applyJoinToGroup(groupId, reason);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //退出群组
    public void leaveGroup(String groupId, String reason) {
        try {
            EMClient.getInstance().groupManager().leaveGroup(groupId);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //解散群组
    public void destroyGroup(String groupId) {
        try {
            EMClient.getInstance().groupManager().destroyGroup(groupId);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //获取指定数量的群成员
    public List<String> getGroupMembers(String groupId, int maxNumber) {
        try {
            List<String> memberList = new ArrayList<>(64);
            EMCursorResult<String> result = null;
            final int pageSize = 20;
            int number;
            do {
                result = EMClient.getInstance().groupManager().fetchGroupMembers(groupId, result != null ? result.getCursor() : "", pageSize);
                memberList.addAll(result.getData());
                number = memberList.size();
            }
            while (!TextUtils.isEmpty(result.getCursor()) && result.getData().size() == pageSize && number < maxNumber);
            while (number > maxNumber) {
                memberList.remove(--number);
            }
            return memberList;
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
        return null;
    }

    //修改群组名称
    public void changeGroupName(String groupId, String newName) {
        try {
            EMClient.getInstance().groupManager().changeGroupName(groupId, newName);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //修改群组名称
    public void changeGroupDescription(String groupId, String newDescription) {
        try {
            EMClient.getInstance().groupManager().changeGroupDescription(groupId, newDescription);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //根据群组ID获取群组基本信息
    public List<String> changeGroupDescription(String groupId, boolean isFromServer) {
        try {
            EMGroup group;
            if (isFromServer) {
                //根据群组ID从服务器获取群组基本信息
                group = EMClient.getInstance().groupManager().getGroupFromServer(groupId);
            } else {
                //根据群组ID从本地获取群组基本信息
                group = EMClient.getInstance().groupManager().getGroup(groupId);
            }
            String owner = group.getOwner();//获取群主
            List<String> members = group.getMembers();//获取内存中的群成员
            List<String> adminList = group.getAdminList();//获取管理员列表
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
        return null;
    }

    //屏蔽群消息,不允许 Owner 权限的调用
    public void blockGroupMessage(String groupId) {
        try {
            EMClient.getInstance().groupManager().blockGroupMessage(groupId);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //解除屏蔽群
    public void unblockGroupMessage(String groupId) {
        try {
            EMClient.getInstance().groupManager().unblockGroupMessage(groupId);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //将群成员拉入群组的黑名单,只有群主才能设置群的黑名单
    public void blockUser(String groupId, String blockUserID) {
        try {
            EMClient.getInstance().groupManager().blockUser(groupId, blockUserID);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //将用户从群组的黑名单移除,只有群主才能设置群的黑名单
    public void unblockUser(String groupId, String unblockUserID) {
        try {
            EMClient.getInstance().groupManager().unblockUser(groupId, unblockUserID);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //获取群组的黑名单用户列表
    public List<String> getBlockedUsers(String groupId) {
        try {
            return EMClient.getInstance().groupManager().getBlockedUsers(groupId);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
        return null;
    }

    //将群成员加入禁言列表中
    public void muteGroupMembers(String groupId, List<String> muteMembers) {
        try {
            EMGroup group = EMClient.getInstance().groupManager().muteGroupMembers(groupId, muteMembers, Integer.MAX_VALUE);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //将群成员移出禁言列表
    public void unMuteGroupMembers(String groupId, List<String> members) {
        try {
            EMClient.getInstance().groupManager().unMuteGroupMembers(groupId, members);//需异步处理
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    //获取群成员禁言列表
    public List<String> fetchGroupMuteList(String groupId, int pageNum, int pageSize) {
        try {
            Map<String, Long> map = EMClient.getInstance().groupManager().fetchGroupMuteList(groupId, pageNum, pageSize);
            if (map.size() != 0) {
                List<String> members = new ArrayList<>(map.size());
                for (Map.Entry<String, Long> entry : map.entrySet()) {
                    members.add(entry.getKey());
                }
                return members;
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setGroupChangeListener(GroupChangeListener listener) {
        if (mGroupChangeListener != null && listener != mGroupChangeListener) {
            EMClient.getInstance().groupManager().removeGroupChangeListener(mEMGroupChangeListener);
        }
        mGroupChangeListener = listener;
        if (mGroupChangeListener == null) {
            mEMGroupChangeListener = null;
            return;
        }
        mEMGroupChangeListener = new EMGroupChangeListener() {
            @Override
            public void onInvitationReceived(String groupId, String groupName, String inviter, String reason) {
                //接收到群组加入邀请
                mGroupChangeListener.onInvitationReceived(groupId,groupName,inviter,reason);
            }

            @Override
            public void onRequestToJoinReceived(String groupId, String groupName, String applyer, String reason) {
                //用户申请加入群
                mGroupChangeListener.onRequestToJoinReceived(groupId,groupName,applyer,reason);
            }

            @Override
            public void onRequestToJoinAccepted(String groupId, String groupName, String accepter) {
                //加群申请被同意
                mGroupChangeListener.onRequestToJoinAccepted(groupId,groupName,accepter);
            }

            @Override
            public void onRequestToJoinDeclined(String groupId, String groupName, String decliner, String reason) {
                //加群申请被拒绝
                mGroupChangeListener.onRequestToJoinDeclined(groupId,groupName,decliner,reason);
            }

            @Override
            public void onInvitationAccepted(String groupId, String inviter, String reason) {
                //群组邀请被同意
                mGroupChangeListener.onInvitationAccepted(groupId,inviter,reason);
            }

            @Override
            public void onInvitationDeclined(String groupId, String invitee, String reason) {
                //群组邀请被拒绝
                mGroupChangeListener.onInvitationDeclined(groupId,invitee,reason);
            }

            @Override
            public void onUserRemoved(String groupId, String groupName) {
                //当前登录用户被管理员移除出群组
                mGroupChangeListener.onUserRemoved(groupId,groupName);
            }

            @Override
            public void onGroupDestroyed(String groupId, String groupName) {
                //群组被解散。 sdk 会先删除本地的这个群组，之后通过此回调通知应用，此群组被删除了
                mGroupChangeListener.onGroupDestroyed(groupId,groupName);
            }

            @Override
            public void onAutoAcceptInvitationFromGroup(String groupId, String inviter, String inviteMessage) {
                //接收邀请时自动加入到群组的通知
                mGroupChangeListener.onAutoAcceptInvitationFromGroup(groupId,inviter,inviteMessage);
            }

            @Override
            public void onMuteListAdded(String groupId, final List<String> mutes, final long muteExpire) {
                //成员禁言的通知
                mGroupChangeListener.onMuteListAdded(groupId,mutes,muteExpire);
            }

            @Override
            public void onMuteListRemoved(String groupId, final List<String> mutes) {
                //成员从禁言列表里移除通知
                mGroupChangeListener.onMuteListRemoved(groupId,mutes);
            }

            @Override
            public void onAdminAdded(String groupId, String administrator) {
                //增加管理员的通知
                mGroupChangeListener.onAdminAdded(groupId,administrator);
            }

            @Override
            public void onAdminRemoved(String groupId, String administrator) {
                //管理员移除的通知
                mGroupChangeListener.onAdminRemoved(groupId,administrator);
            }

            @Override
            public void onOwnerChanged(String groupId, String newOwner, String oldOwner) {
                //群所有者变动通知
                mGroupChangeListener.onOwnerChanged(groupId,newOwner,oldOwner);
            }

            @Override
            public void onMemberJoined(final String groupId, final String member) {
                //群组加入新成员通知
                mGroupChangeListener.onMemberJoined(groupId,member);
            }

            @Override
            public void onMemberExited(final String groupId, final String member) {
                //群成员退出通知
                mGroupChangeListener.onMemberExited(groupId,member);
            }

            @Override
            public void onAnnouncementChanged(String groupId, String announcement) {
                //群公告变动通知
                mGroupChangeListener.onAnnouncementChanged(groupId,announcement);
            }

            @Override
            public void onSharedFileAdded(String groupId, EMMucSharedFile emMucSharedFile) {
                //增加共享文件的通知
                mGroupChangeListener.onSharedFileAdded(groupId,emMucSharedFile);
            }

            @Override
            public void onSharedFileDeleted(String groupId, String fileId) {
                //群共享文件删除通知
                mGroupChangeListener.onSharedFileDeleted(groupId,fileId);
            }
        };

        EMClient.getInstance().groupManager().addGroupChangeListener(mEMGroupChangeListener);
    }

    public interface GroupChangeListener {

        void onInvitationReceived(String groupId, String groupName, String inviter, String reason);
        //接收到群组加入邀请

        void onRequestToJoinReceived(String groupId, String groupName, String applyer, String reason);
        //用户申请加入群

        void onRequestToJoinAccepted(String groupId, String groupName, String accepter);
        //加群申请被同意

        void onRequestToJoinDeclined(String groupId, String groupName, String decliner, String reason);
        //加群申请被拒绝

        void onInvitationAccepted(String groupId, String inviter, String reason);
        //群组邀请被同意

        void onInvitationDeclined(String groupId, String invitee, String reason);
        //群组邀请被拒绝

        void onUserRemoved(String groupId, String groupName);
        //当前登录用户被管理员移除出群组

        void onGroupDestroyed(String groupId, String groupName);
        //群组被解散。 sdk 会先删除本地的这个群组，之后通过此回调通知应用，此群组被删除了

        void onAutoAcceptInvitationFromGroup(String groupId, String inviter, String inviteMessage);
        //接收邀请时自动加入到群组的通知

        void onMuteListAdded(String groupId, final List<String> mutes, final long muteExpire);
        //成员禁言的通知

        void onMuteListRemoved(String groupId, final List<String> mutes);
        //成员从禁言列表里移除通知

        void onAdminAdded(String groupId, String administrator);
        //增加管理员的通知

        void onAdminRemoved(String groupId, String administrator);
        //管理员移除的通知

        void onOwnerChanged(String groupId, String newOwner, String oldOwner);
        //群所有者变动通知

        void onMemberJoined(final String groupId, final String member);
        //群组加入新成员通知

        void onMemberExited(final String groupId, final String member);
        //群成员退出通知

        void onAnnouncementChanged(String groupId, String announcement);
        //群公告变动通知

        void onSharedFileAdded(String groupId, EMMucSharedFile emMucSharedFile);
        //增加共享文件的通知

        void onSharedFileDeleted(String groupId, String fileId);
        //群共享文件删除通知

    }

}
