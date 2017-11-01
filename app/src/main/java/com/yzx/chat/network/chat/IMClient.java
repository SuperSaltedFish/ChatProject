package com.yzx.chat.network.chat;

/**
 * Created by YZX on 2017年10月04日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class IMClient {

    private ChatManager mChatManager;

    private GroupManager mGroupManager;

    private ContactManager mContactManager;

    private static IMClient sClient = new IMClient();

    public static IMClient getInstance() {
        return sClient;
    }

    public ChatManager getChatManager() {
        return mChatManager;
    }

    public GroupManager getGroupManager() {
        return mGroupManager;
    }

    public ContactManager getContactManager() {
        return mContactManager;
    }
}
