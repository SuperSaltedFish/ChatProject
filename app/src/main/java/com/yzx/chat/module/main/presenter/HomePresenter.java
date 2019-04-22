package com.yzx.chat.module.main.presenter;

import com.yzx.chat.R;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.ChatManager;
import com.yzx.chat.core.ContactManager;
import com.yzx.chat.core.ConversationManager;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.ContactOperationEntity;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.core.extra.ContactNotificationMessageEx;
import com.yzx.chat.module.contact.view.NotificationMessageActivity;
import com.yzx.chat.module.conversation.presenter.ChatPresenter;
import com.yzx.chat.module.conversation.view.ChatActivity;
import com.yzx.chat.module.main.contract.HomeContract;
import com.yzx.chat.tool.ActivityHelper;
import com.yzx.chat.tool.NotificationHelper;
import com.yzx.chat.util.AndroidHelper;

import io.rong.imlib.model.Message;
import io.rong.message.GroupNotificationMessage;

/**
 * Created by YZX on 2017年11月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class HomePresenter implements HomeContract.Presenter {

    private HomeContract.View mHomeView;
    private AppClient mAppClient;

    @Override
    public void attachView(HomeContract.View view) {
        mHomeView = view;
        mAppClient = AppClient.getInstance();
        mAppClient.getConversationManager().addConversationUnreadCountListener(mOnConversationUnreadCountListener);
        mAppClient.getChatManager().addOnMessageReceiveListener(mOnChatMessageReceiveListener, null);
        mAppClient.getContactManager().addContactOperationUnreadCountChangeListener(mOnContactOperationUnreadCountChangeListener);
        mAppClient.getContactManager().addContactOperationListener(mOnContactOperationListener);
    }

    @Override
    public void detachView() {
        mAppClient.getConversationManager().removeConversationUnreadCountListener(mOnConversationUnreadCountListener);
        mAppClient.getChatManager().removeOnMessageReceiveListener(mOnChatMessageReceiveListener);
        mAppClient.getContactManager().removeContactOperationUnreadCountChangeListener(mOnContactOperationUnreadCountChangeListener);
        mAppClient.getContactManager().removeContactOperationListener(mOnContactOperationListener);
        mHomeView = null;
        mAppClient = null;
    }


    @Override
    public void loadUnreadCount() {
        mHomeView.updateMessageUnreadBadge(mAppClient.getConversationManager().getConversationUnreadCount());
        mHomeView.updateContactUnreadBadge(mAppClient.getContactManager().getContactUnreadCount());
    }

    private final ConversationManager.OnConversationUnreadCountListener mOnConversationUnreadCountListener = new ConversationManager.OnConversationUnreadCountListener() {
        @Override
        public void OnConversationUnreadCountChange(final int count) {
            mHomeView.updateMessageUnreadBadge(count);
        }
    };

    private final ContactManager.OnContactOperationUnreadCountChangeListener mOnContactOperationUnreadCountChangeListener = new ContactManager.OnContactOperationUnreadCountChangeListener() {
        @Override
        public void onContactOperationUnreadCountChange(final int count) {
            mHomeView.updateContactUnreadBadge(count);
        }
    };

    private final ChatManager.OnChatMessageReceiveListener mOnChatMessageReceiveListener = new ChatManager.OnChatMessageReceiveListener() {
        @Override
        public void onChatMessageReceived(final Message message, int untreatedCount) {
            Class activityClass = ActivityHelper.getStackTopActivityClass();
            if (activityClass == ChatActivity.class && message.getTargetId().equals(ChatPresenter.sConversationID)) {
                return;
            }
            switch (message.getConversationType()) {
                case PRIVATE:
                    ContactEntity contact = AppClient.getInstance().getContactManager().getContact(message.getTargetId());
                    if (contact != null && !(message.getContent() instanceof ContactNotificationMessageEx)) {
                        NotificationHelper.getInstance().showPrivateMessageNotification(message, contact, !ActivityHelper.isAppForeground());
                    }
                    break;
                case GROUP:
                    GroupEntity group = AppClient.getInstance().getGroupManager().getGroup(message.getTargetId());
                    if (group != null && !(message.getContent() instanceof GroupNotificationMessage)) {
                        NotificationHelper.getInstance().showGroupMessageNotification(message, group, !ActivityHelper.isAppForeground());
                    }
                    break;
            }
        }
    };

    private final ContactManager.OnContactOperationListener mOnContactOperationListener = new ContactManager.OnContactOperationListener() {
        @Override
        public void onContactOperationReceive(final ContactOperationEntity contactOperation) {
            Class activityClass = ActivityHelper.getStackTopActivityClass();
            if (activityClass == NotificationMessageActivity.class) {
                return;
            }
            String hintContent;
            switch (contactOperation.getType()) {
                case ContactManager.CONTACT_OPERATION_REQUEST:
                    hintContent = AndroidHelper.getString(R.string.ContactOperationAdapter_RequestReason);
                    break;
                case ContactManager.CONTACT_OPERATION_REJECT:
                    hintContent = AndroidHelper.getString(R.string.ContactOperationAdapter_RejuctReason);
                    break;
                default:
                    return;
            }
            NotificationHelper.getInstance().showContactOperationNotification(contactOperation, hintContent, !ActivityHelper.isAppForeground());
        }

        @Override
        public void onContactOperationUpdate(ContactOperationEntity contactOperation) {

        }

        @Override
        public void onContactOperationRemove(final String contactOperationID) {

        }
    };

}
