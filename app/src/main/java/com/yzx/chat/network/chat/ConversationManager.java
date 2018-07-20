package com.yzx.chat.network.chat;

import com.yzx.chat.util.LogUtil;

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

    private RongIMClient mRongIMClient;

    private List<OnConversationStateChangeListener> mConversationStateChangeListeners;
    private List<OnConversationUnreadCountListener> mConversationUnreadCountListeners;

    private volatile int mUnreadChatMessageCount;

    ConversationManager() {
        mConversationUnreadCountListeners = Collections.synchronizedList(new LinkedList<OnConversationUnreadCountListener>());
        mConversationStateChangeListeners = Collections.synchronizedList(new LinkedList<OnConversationStateChangeListener>());
        mRongIMClient = RongIMClient.getInstance();
    }

    public List<Conversation> getAllConversations() {
        return getAllConversations(SUPPORT_CONVERSATION_TYPE);
    }

    public List<Conversation> getAllConversations(Conversation.ConversationType... type) {
        return mRongIMClient.getConversationList(type);
    }

    public Conversation getConversation(Conversation.ConversationType type, String targetId) {
        return mRongIMClient.getConversation(type, targetId);
    }

    public void setConversationTop(final Conversation.ConversationType type, final String targetId, boolean isTop) {
        mRongIMClient.setConversationToTop(type, targetId, isTop, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    callbackConversationChange(getConversation(type, targetId), UPDATE_TYPE_SET_TOP);
                } else {
                    LogUtil.e("setConversationTop fail");
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtil.e(errorCode.getMessage());
            }
        });
    }

    public void clearConversationUnreadStatus(final Conversation.ConversationType type, final String targetId) {
        mRongIMClient.clearMessagesUnreadStatus(type, targetId, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    callbackConversationChange(getConversation(type, targetId), UPDATE_TYPE_CLEAR_UNREAD_STATUS);
                    updateChatUnreadCount();
                } else {
                    LogUtil.e("clearMessagesUnreadStatus error");
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtil.e(errorCode.getMessage());
            }
        });
    }

    public void updateConversationTitle(final Conversation.ConversationType type, final String targetId, String title) {
        mRongIMClient.updateConversationInfo(type, targetId, title, "null", new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    callbackConversationChange(getConversation(type, targetId), UPDATE_TYPE_UPDATE);
                } else {
                    LogUtil.e("updateConversationTitle error");
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtil.e(errorCode.getMessage());
            }
        });
    }

    public void removeConversation(Conversation.ConversationType type, String targetId) {
        removeConversation(type, targetId, true);
    }

    public void removeConversation(final Conversation.ConversationType type, final String targetId, final boolean isCallbackListener) {
        mRongIMClient.removeConversation(type, targetId, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    if (isCallbackListener) {
                        callbackConversationChange(Conversation.obtain(type, targetId, null), UPDATE_TYPE_REMOVE);
                    }
                    updateChatUnreadCount();
                } else {
                    LogUtil.e("removeConversation fail");
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtil.e(errorCode.getMessage());
            }
        });
    }

    public void saveConversationDraft(final Conversation.ConversationType type, final String targetId, final String draft) {
        mRongIMClient.saveTextMessageDraft(type, targetId, draft, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    callbackConversationChange(getConversation(type, targetId), UPDATE_TYPE_SAVE_DRAFT);
                } else {
                    LogUtil.e("saveConversationDraft fail");
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtil.e(errorCode.getMessage());
            }
        });
    }

    public void clearAllConversationMessages(final Conversation.ConversationType type, final String targetId) {
        mRongIMClient.deleteMessages(type, targetId, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    callbackConversationChange(getConversation(type, targetId), UPDATE_TYPE_CLEAR_MESSAGE);
                    updateChatUnreadCount();
                } else {
                    LogUtil.e("saveConversationDraft fail");
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtil.e(errorCode.getMessage());
            }
        });
    }

    public void setEnableConversationNotification(final Conversation.ConversationType type, final String targetId, boolean isEnable) {
        Conversation.ConversationNotificationStatus status = isEnable ? Conversation.ConversationNotificationStatus.NOTIFY : Conversation.ConversationNotificationStatus.DO_NOT_DISTURB;
        mRongIMClient.setConversationNotificationStatus(type, targetId, status, new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {
                callbackConversationChange(getConversation(type, targetId), UPDATE_TYPE_NOTIFICATION_CHANGE);
                updateChatUnreadCount();
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtil.e(errorCode.getMessage());
            }
        });
    }

    public void isEnableConversationNotification(Conversation conversation, final ResultCallback<Conversation.ConversationNotificationStatus> callback) {
        mRongIMClient.getConversationNotificationStatus(conversation.getConversationType(), conversation.getTargetId(), new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {
                if (callback != null) {
                    callback.onSuccess(conversationNotificationStatus);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                if (callback != null) {
                    callback.onFailure(errorCode.getMessage());
                } else {
                    LogUtil.e(errorCode.getMessage());
                }
            }
        });
    }

    public int getConversationUnreadCount() {
        return mUnreadChatMessageCount;
    }

    public void updateChatUnreadCount() {
        mRongIMClient.getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                if (conversations == null || conversations.size() == 0) {
                    updateUnreadCount(0);
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
                    updateUnreadCount(0);
                    return;
                }

                Conversation[] conversationArray = new Conversation[conversations.size()];
                conversations.toArray(conversationArray);
                mRongIMClient.getTotalUnreadCount(new RongIMClient.ResultCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer integer) {
                        updateUnreadCount(integer);
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

            void updateUnreadCount(int newCount) {
                if (mUnreadChatMessageCount != newCount) {
                    mUnreadChatMessageCount = newCount;
                    for (OnConversationUnreadCountListener listener : mConversationUnreadCountListeners) {
                        listener.OnConversationUnreadCountChange(mUnreadChatMessageCount);
                    }
                }
            }
        }, SUPPORT_CONVERSATION_TYPE);

    }

    public void addConversationStateChangeListener(OnConversationStateChangeListener listener) {
        if (!mConversationStateChangeListeners.contains(listener)) {
            mConversationStateChangeListeners.add(listener);
        }
    }

    public void removeConversationStateChangeListener(OnConversationStateChangeListener listener) {
        mConversationStateChangeListeners.remove(listener);
    }

    public void addConversationUnreadCountListener(OnConversationUnreadCountListener listener) {
        if (!mConversationUnreadCountListeners.contains(listener)) {
            mConversationUnreadCountListeners.add(listener);
        }
    }

    public void removeConversationUnreadCountListener(OnConversationUnreadCountListener listener) {
        mConversationUnreadCountListeners.remove(listener);
    }


    private void callbackConversationChange(Conversation conversation, int typeCode) {
        for (OnConversationStateChangeListener listener : mConversationStateChangeListeners) {
            listener.onConversationStateChange(conversation, typeCode);
        }
    }

    void destroy() {
        mConversationUnreadCountListeners.clear();
        mConversationUnreadCountListeners = null;
        mConversationStateChangeListeners.clear();
        mConversationStateChangeListeners = null;
    }

    public interface OnConversationUnreadCountListener {
        void OnConversationUnreadCountChange(int count);
    }

    public interface OnConversationStateChangeListener {
        void onConversationStateChange(Conversation conversation, int typeCode);
    }
}
