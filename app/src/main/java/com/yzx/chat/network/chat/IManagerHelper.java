package com.yzx.chat.network.chat;

import com.yzx.chat.database.AbstractDao;

/**
 * Created by YZX on 2018年07月02日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
interface IManagerHelper {

    ChatManager getChatManager();

    ContactManager getContactManager();

    ConversationManager getConversationManager();

    GroupManager getGroupManager();

    UserManager getUserManager();

    CryptoManager getCryptoManager();

    AbstractDao.ReadWriteHelper getReadWriteHelper();

    void runOnUiThread(Runnable runnable);

    void runOnWorkThread(Runnable runnable);

}
