package com.yzx.chat.module.conversation.presenter;

import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.module.conversation.contract.ChatContract;
import com.yzx.chat.core.ChatManager;
import com.yzx.chat.core.ContactManager;
import com.yzx.chat.core.ConversationManager;
import com.yzx.chat.core.AppClient;
import com.yzx.chat.core.extra.VideoMessage;
import com.yzx.chat.tool.NotificationHelper;
import com.yzx.chat.core.util.LogUtil;

import java.io.File;
import java.util.List;
import java.util.Locale;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.FileMessage;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

/**
 * Created by YZX on 2017年11月10日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class ChatPresenter implements ChatContract.Presenter {

    public static volatile String sConversationID;

    private ChatContract.View mChatView;
    private Handler mHandler;
    private AppClient mAppClient;

    private String mConversationID;
    private Conversation.ConversationType mConversationType;
    private String mMessageDraft;
    private boolean mHasMoreMessage;
    private boolean mIsLoadingMore;
    private boolean mIsConversationStateChange;

    @Override
    public void attachView(ChatContract.View view) {
        mChatView = view;
        mHandler = new Handler();
        mAppClient = AppClient.getInstance();
    }

    @Override
    public void detachView() {
        reset();
        mChatView = null;
        mHandler = null;
    }

    @Override
    public ContactEntity initPrivateChat(String conversationID) {
        ContactEntity contact = mAppClient.getContactManager().getContact(conversationID);
        if (contact != null) {
            mConversationID = conversationID;
            mConversationType = Conversation.ConversationType.PRIVATE;
            mChatView.showChatTitle(contact.getName());
            init();
            return contact;
        }
        return null;
    }

    @Override
    public GroupEntity initGroupChat(String conversationID) {
        GroupEntity group = mAppClient.getGroupManager().getGroup(conversationID);
        if (group != null) {
            mConversationID = conversationID;
            mConversationType = Conversation.ConversationType.GROUP;
            mChatView.showChatTitle(group.getName());
            init();
            return group;
        }
        return null;
    }

    @Override
    public String getConversationID() {
        return mConversationID;
    }

    private void init() {
        reset();
        Conversation conversation = mAppClient.getConversationManager().getConversation(mConversationType, mConversationID);
        if (conversation == null) {
            conversation = Conversation.obtain(mConversationType, mConversationID, "");
        } else {
            mMessageDraft = conversation.getDraft();
        }

        sConversationID = mConversationID;
        mChatView.clearMessage();
        if (conversation.getUnreadMessageCount() != 0) {
            mAppClient.getConversationManager().clearConversationUnreadStatus(mConversationType, mConversationID,null);
        }
        List<Message> messageList = mAppClient.getChatManager().getHistoryMessagesBlock(mConversationType, mConversationID, -1, Constants.CHAT_MESSAGE_PAGE_SIZE);
        mHasMoreMessage = messageList != null && messageList.size() >= Constants.CHAT_MESSAGE_PAGE_SIZE;
        mChatView.enableLoadMoreHint(mHasMoreMessage);
        mChatView.addNewMessage(messageList);
        mAppClient.getChatManager().addOnMessageReceiveListener(mOnChatMessageReceiveListener, sConversationID);
        mAppClient.getChatManager().addOnMessageSendStateChangeListener(mOnMessageSendListener, sConversationID);
        mAppClient.getContactManager().addContactChangeListener(mOnContactChangeListener);
        mAppClient.getConversationManager().addConversationStateChangeListener(mOnConversationChangeListener);
        NotificationHelper.getInstance().cancelNotification(mConversationID.hashCode());
    }

    private void reset() {
        mAppClient.getChatManager().removeOnMessageReceiveListener(mOnChatMessageReceiveListener);
        mAppClient.getChatManager().removeOnMessageSendStateChangeListener(mOnMessageSendListener);
        mAppClient.getContactManager().removeContactChangeListener(mOnContactChangeListener);
        mAppClient.getConversationManager().removeConversationStateChangeListener(mOnConversationChangeListener);
        mHandler.removeCallbacksAndMessages(null);
        sConversationID = null;
        mHasMoreMessage = true;
        mIsConversationStateChange = false;
        mIsLoadingMore = false;
    }

    @Override
    public void resendMessage(Message message) {
        sendMessage(message);
    }


    @Override
    public void sendTextMessage(String content) {
        sendMessage(TextMessage.obtain(content.trim()));
    }

    @Override
    public void sendVoiceMessage(String filePath, long timeLengthMs) {
        sendMessage(VoiceMessage.obtain(Uri.fromFile(new File(filePath)), (int) timeLengthMs));
    }

    @Override
    public void sendImageMessage(String imagePath, boolean isOriginal) {
        Uri uri = Uri.parse("file://" + imagePath);
        sendMessage(ImageMessage.obtain(uri, uri, isOriginal));
    }

    @Override
    public void sendLocationMessage(PoiItem poi) {
        LatLonPoint latLonPoint = poi.getLatLonPoint();
        double latitude = latLonPoint.getLatitude();
        double longitude = latLonPoint.getLongitude();
        String url = String.format(Locale.getDefault(), Constants.URL_MAP_IMAGE_FORMAT, longitude, latitude);
        String title = poi.getTitle();
        String address = poi.getSnippet();
        LocationMessage locationMessage = LocationMessage.obtain(latitude, longitude, title + "/" + address, Uri.parse(url));
        locationMessage.setExtra(poi.getPoiId());
        sendMessage(locationMessage);
    }

    @Override
    public void sendVideoMessage(String filePath) {
        Uri uri = Uri.parse("file://" + filePath);
        sendMessage(VideoMessage.obtain(uri));
    }

    @Override
    public void sendFileMessage(String filePath) {
        Uri uri = Uri.parse("file://" + filePath);
        sendMessage(FileMessage.obtain(uri));
    }

    @Override
    public void loadMoreMessage(int lastMessageID) {
        mIsLoadingMore = true;
        mAppClient.getChatManager().getHistoryMessages(
                mConversationType, mConversationID,
                lastMessageID,
                Constants.CHAT_MESSAGE_PAGE_SIZE,
                new RongIMClient.ResultCallback<List<Message>>() {
                    @Override
                    public void onSuccess(final List<Message> messages) {
                        if (messages == null || messages.size() < Constants.CHAT_MESSAGE_PAGE_SIZE) {
                            mHasMoreMessage = false;
                        }
                        loadMoreComplete(messages);
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                        LogUtil.e(errorCode.getMessage());
                        loadMoreComplete(null);
                    }
                });
    }

    @Override
    public boolean isLoadingMore() {
        return mIsLoadingMore;
    }

    @Override
    public boolean hasMoreMessage() {
        return mHasMoreMessage;
    }

    @Override
    public void setVoiceMessageAsListened(Message message) {
        mAppClient.getChatManager().setVoiceMessageAsListened(message);
    }

    @Override
    public void saveMessageDraft(String draft) {
        if (TextUtils.equals(mMessageDraft, draft) && !mIsConversationStateChange) {
            return;
        }
        mAppClient.getConversationManager().saveConversationDraft(mConversationType, mConversationID, draft,null);
    }

    @Override
    public String getMessageDraft() {
        return mMessageDraft;
    }

    private void sendMessage(MessageContent messageContent) {
        sendMessage(Message.obtain(mConversationID, mConversationType, messageContent));
    }

    private void sendMessage(Message message) {
        mAppClient.getChatManager().sendMessage(message);
    }

    private void loadNewComplete(final Message message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mChatView.addNewMessage(message);
            }
        });
    }

    private void loadMoreComplete(final List<Message> messages) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mIsLoadingMore = false;
                mChatView.addMoreMessage(messages, mHasMoreMessage);
            }
        });
    }


    private final ChatManager.OnChatMessageReceiveListener mOnChatMessageReceiveListener = new ChatManager.OnChatMessageReceiveListener() {

        @Override
        public void onChatMessageReceived(Message message, int untreatedCount) {
            loadNewComplete(message);
            mIsConversationStateChange = true;
        }
    };

    private final ChatManager.OnMessageSendListener mOnMessageSendListener = new ChatManager.OnMessageSendListener() {
        @Override
        public void onAttached(Message message) {
            mChatView.addNewMessage(message);
            mIsConversationStateChange = true;
        }

        @Override
        public void onProgress(Message message, int progress) {

        }

        @Override
        public void onSuccess(Message message) {
            mChatView.updateMessage(message);
        }

        @Override
        public void onError(Message message) {
            mChatView.updateMessage(message);
        }

        @Override
        public void onCanceled(Message message) {
            mChatView.updateMessage(message);
        }
    };

    private final ConversationManager.OnConversationChangeListener mOnConversationChangeListener = new ConversationManager.OnConversationChangeListener() {
        @Override
        public void onConversationChange(final Conversation conversation, int typeCode) {
            if (conversation.getTargetId().equals(sConversationID)) {
                switch (typeCode) {
                    case ConversationManager.UPDATE_TYPE_CLEAR_MESSAGE:
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mChatView.clearMessage();
                                mChatView.enableLoadMoreHint(false);
                                mHasMoreMessage = false;
                            }
                        });
                        break;
                    case ConversationManager.UPDATE_TYPE_REMOVE:
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mChatView.goBack();
                            }
                        });
                        break;
                }
            }
        }
    };

    private final ContactManager.OnContactChangeListener mOnContactChangeListener = new ContactManager.OnContactChangeListener() {
        @Override
        public void onContactAdded(ContactEntity contact) {

        }

        @Override
        public void onContactDeleted(String contactID) {
            if (TextUtils.equals(mConversationID, contactID)) {
                mChatView.goBack();
            }
        }

        @Override
        public void onContactUpdate(ContactEntity contact) {
            if (TextUtils.equals(mConversationID, contact.getUserInfo().getUserID())) {
                mChatView.showChatTitle(contact.getName());
            }
        }
    };

}
