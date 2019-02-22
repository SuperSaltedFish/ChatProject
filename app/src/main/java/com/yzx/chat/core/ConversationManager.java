package com.yzx.chat.core;

import android.os.Handler;
import android.os.Looper;

import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.GroupEntity;
import com.yzx.chat.core.listener.ResultCallback;
import com.yzx.chat.core.util.CallbackUtil;
import com.yzx.chat.core.util.LogUtil;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

/**
 * Created by YZX on 2017年12月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ConversationManager {

    private static final Conversation.ConversationType[] SUPPORT_CONVERSATION_TYPE = {Conversation.ConversationType.PRIVATE, Conversation.ConversationType.GROUP};

    public static final int UPDATE_TYPE_SET_TOP = 1;
    public static final int UPDATE_TYPE_CLEAR_UNREAD_STATUS = 2;
    public static final int UPDATE_TYPE_REMOVE = 3;
    public static final int UPDATE_TYPE_SAVE_DRAFT = 4;
    public static final int UPDATE_TYPE_CLEAR_MESSAGE = 5;
    public static final int UPDATE_TYPE_NOTIFICATION_CHANGE = 6;
    public static final int UPDATE_TYPE_UPDATE = 7;
    public static final int UPDATE_LIST_CHANGE = 8;

    private AppClient mAppClient;
    private RongIMClient mRongIMClient;
    private Handler mUIHandler;

    private List<OnConversationChangeListener> mConversationChangeListeners;
    private List<OnConversationUnreadCountListener> mConversationUnreadCountListeners;

    private volatile int mUnreadChatMessageCount;

    ConversationManager(AppClient appClient) {
        mAppClient = appClient;
        mRongIMClient = mAppClient.getRongIMClient();
        mConversationUnreadCountListeners = Collections.synchronizedList(new LinkedList<OnConversationUnreadCountListener>());
        mConversationChangeListeners = Collections.synchronizedList(new LinkedList<OnConversationChangeListener>());
        mRongIMClient = RongIMClient.getInstance();
        mUIHandler = new Handler(Looper.getMainLooper());
    }

    public List<Conversation> getAllConversationsBlock() {
        List<Conversation> conversationList = mRongIMClient.getConversationList(SUPPORT_CONVERSATION_TYPE);
        if (conversationList != null) {
            for (Conversation conversation : conversationList) {
                setupTitleAndAvatar(conversation);
            }
        }
        return conversationList;
    }

    public void getAllConversations(final ResultCallback<List<Conversation>> callback) {
        mRongIMClient.getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                if (conversations != null) {
                    for (Conversation conversation : conversations) {
                        setupTitleAndAvatar(conversation);
                    }
                }
                CallbackUtil.callResult(conversations, callback);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                CallbackUtil.callFailure(errorCode.getValue(), errorCode.getMessage(), callback);
            }
        }, SUPPORT_CONVERSATION_TYPE);
    }

    public Conversation getConversation(Conversation.ConversationType type, String targetId) {
        Conversation conversation = mRongIMClient.getConversation(type, targetId);
        if (conversation != null) {
            setupTitleAndAvatar(conversation);
        }
        return conversation;
    }

    private void setupTitleAndAvatar(Conversation conversation) {
        String conversationID = conversation.getTargetId();
        switch (conversation.getConversationType()) {
            case PRIVATE:
                ContactEntity contactEntity = mAppClient.getContactManager().getContact(conversationID);
                if (contactEntity != null) {
                    conversation.setConversationTitle(contactEntity.getName());
                    conversation.setPortraitUrl(contactEntity.getUserProfile().getAvatar());
                }
                break;
            case GROUP:
                GroupEntity group = mAppClient.getGroupManager().getGroup(conversationID);
                if (group != null) {
                    conversation.setConversationTitle(group.getName());
                    conversation.setPortraitUrl(group.getAvatarUrlFromMembers());
                }
                break;
        }
    }

    public void setTopConversation(final Conversation.ConversationType type, final String targetId, boolean isTop, final ResultCallback<Void> callback) {
        mRongIMClient.setConversationToTop(type, targetId, isTop, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    callbackConversationChange(getConversation(type, targetId), UPDATE_TYPE_SET_TOP);
                } else {
                    LogUtil.e("setTopConversation fail");
                }
                CallbackUtil.callResult(null, callback);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                CallbackUtil.callFailure(errorCode.getValue(), errorCode.getMessage(), callback);
                LogUtil.e(errorCode.getMessage());
            }
        });
    }

    public void clearConversationUnreadStatus(final Conversation.ConversationType type, final String targetId, final ResultCallback<Void> callback) {
        mRongIMClient.clearMessagesUnreadStatus(type, targetId, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    updateChatUnreadCount();
                    callbackConversationChange(getConversation(type, targetId), UPDATE_TYPE_CLEAR_UNREAD_STATUS);
                } else {
                    LogUtil.e("clearMessagesUnreadStatus error");
                }
                CallbackUtil.callResult(null, callback);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                CallbackUtil.callFailure(errorCode.getValue(), errorCode.getMessage(), callback);
                LogUtil.e(errorCode.getMessage());
            }
        });
    }

    public void removeConversation(final Conversation.ConversationType type, final String targetId, final ResultCallback<Void> callback) {
        final Conversation conversation = getConversation(type, targetId);
        mRongIMClient.removeConversation(type, targetId, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    callbackConversationChange(conversation, UPDATE_TYPE_REMOVE);
                    updateChatUnreadCount();
                } else {
                    LogUtil.e("removeConversation fail");
                }
                CallbackUtil.callResult(null, callback);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                CallbackUtil.callFailure(errorCode.getValue(), errorCode.getMessage(), callback);
                LogUtil.e(errorCode.getMessage());
            }
        });
    }

    public void saveConversationDraft(final Conversation.ConversationType type, final String targetId, final String draft, final ResultCallback<Void> callback) {
        mRongIMClient.saveTextMessageDraft(type, targetId, draft, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    callbackConversationChange(getConversation(type, targetId), UPDATE_TYPE_SAVE_DRAFT);
                } else {
                    LogUtil.e("saveConversationDraft fail");
                }
                CallbackUtil.callResult(null, callback);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                CallbackUtil.callFailure(errorCode.getValue(), errorCode.getMessage(), callback);
                LogUtil.e(errorCode.getMessage());
            }
        });
    }

    public void clearConversationMessages(final Conversation.ConversationType type, final String targetId, final ResultCallback<Void> callback) {
        mRongIMClient.deleteMessages(type, targetId, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    callbackConversationChange(getConversation(type, targetId), UPDATE_TYPE_CLEAR_MESSAGE);
                    updateChatUnreadCount();
                } else {
                    LogUtil.e("saveConversationDraft fail");
                }
                CallbackUtil.callResult(null, callback);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                CallbackUtil.callFailure(errorCode.getValue(), errorCode.getMessage(), callback);
                LogUtil.e(errorCode.getMessage());
            }
        });
    }

    public void setEnableConversationNotification(final Conversation.ConversationType type, final String targetId, boolean isEnable, final ResultCallback<Void> callback) {
        Conversation.ConversationNotificationStatus status = isEnable ? Conversation.ConversationNotificationStatus.NOTIFY : Conversation.ConversationNotificationStatus.DO_NOT_DISTURB;
        mRongIMClient.setConversationNotificationStatus(type, targetId, status, new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {
                callbackConversationChange(getConversation(type, targetId), UPDATE_TYPE_NOTIFICATION_CHANGE);
                updateChatUnreadCount();
                CallbackUtil.callResult(null, callback);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                CallbackUtil.callFailure(errorCode.getValue(), errorCode.getMessage(), callback);
                LogUtil.e(errorCode.getMessage());
            }
        });
    }

    public boolean isEnableConversationNotification(final Conversation.ConversationType type, final String targetId) {
        Conversation conversation = getConversation(type, targetId);
        if (conversation != null) {
            return conversation.getNotificationStatus() == Conversation.ConversationNotificationStatus.NOTIFY;
        }
        return false;
    }

    public int getConversationUnreadCount() {
        return mUnreadChatMessageCount;
    }

    void updateConversationTitle(final Conversation.ConversationType type, final String targetId, String title) {
        callbackConversationChange(getConversation(type, targetId), UPDATE_TYPE_UPDATE);
    }

    void onConversationListChange() {
        callbackConversationChange(null, UPDATE_LIST_CHANGE);
    }

    void updateChatUnreadCount() {
        mRongIMClient.getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                if (conversations == null || conversations.size() == 0) {
                    callUpdateUnreadCountChange(0);
                    return;
                }
                Iterator<Conversation> it = conversations.iterator();
                while (it.hasNext()) {
                    Conversation conversation = it.next();
                    if (conversation.getNotificationStatus() == Conversation.ConversationNotificationStatus.DO_NOT_DISTURB) {
                        it.remove();
                    }
                }
                if (conversations.size() == 0) {
                    callUpdateUnreadCountChange(0);
                    return;
                }

                Conversation[] conversationArray = new Conversation[conversations.size()];
                conversations.toArray(conversationArray);
                mRongIMClient.getTotalUnreadCount(new RongIMClient.ResultCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer integer) {
                        callUpdateUnreadCountChange(integer);
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                        LogUtil.e(errorCode.getMessage());
                    }
                }, conversationArray);

            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtil.e(errorCode.getMessage());
            }

            void callUpdateUnreadCountChange(int newCount) {
                if (mUnreadChatMessageCount != newCount) {
                    mUnreadChatMessageCount = newCount;
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            for (OnConversationUnreadCountListener listener : mConversationUnreadCountListeners) {
                                listener.OnConversationUnreadCountChange(mUnreadChatMessageCount);
                            }
                        }
                    });
                }
            }
        }, SUPPORT_CONVERSATION_TYPE);
    }

    public void addConversationStateChangeListener(OnConversationChangeListener listener) {
        if (!mConversationChangeListeners.contains(listener)) {
            mConversationChangeListeners.add(listener);
        }
    }

    public void removeConversationStateChangeListener(OnConversationChangeListener listener) {
        mConversationChangeListeners.remove(listener);
    }

    public void addConversationUnreadCountListener(OnConversationUnreadCountListener listener) {
        if (!mConversationUnreadCountListeners.contains(listener)) {
            mConversationUnreadCountListeners.add(listener);
        }
    }

    public void removeConversationUnreadCountListener(OnConversationUnreadCountListener listener) {
        mConversationUnreadCountListeners.remove(listener);
    }


    void callbackConversationChange(final Conversation conversation, final int typeCode) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                for (OnConversationChangeListener listener : mConversationChangeListeners) {
                    listener.onConversationChange(conversation, typeCode);
                }
            }
        });
    }

    void destroy() {
        mUIHandler.removeCallbacksAndMessages(null);
        mConversationUnreadCountListeners.clear();
        mConversationUnreadCountListeners = null;
        mConversationChangeListeners.clear();
        mConversationChangeListeners = null;
    }

    public interface OnConversationUnreadCountListener {
        void OnConversationUnreadCountChange(int count);
    }

    public interface OnConversationChangeListener {
        void onConversationChange(Conversation conversation, int typeCode);
    }
}
