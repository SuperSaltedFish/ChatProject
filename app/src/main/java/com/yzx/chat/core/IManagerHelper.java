package com.yzx.chat.core;

import com.yzx.chat.core.manager.ChatManager;
import com.yzx.chat.core.manager.ContactManager;
import com.yzx.chat.core.manager.ConversationManager;
import com.yzx.chat.core.manager.ConfigurationManager;
import com.yzx.chat.core.manager.GroupManager;
import com.yzx.chat.core.manager.UserManager;
import com.yzx.chat.core.database.AbstractDao;

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

    ConfigurationManager getConfigurationManager();

    AbstractDao.ReadWriteHelper getReadWriteHelper();

    void runOnUiThread(Runnable runnable);

    void runOnWorkThread(Runnable runnable);

}
