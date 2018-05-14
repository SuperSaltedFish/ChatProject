package com.yzx.chat.network.chat;

import com.yzx.chat.util.LogUtil;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

/**
 * Created by YZX on 2017年12月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class ConversationManager {

    static final int CALLBACK_CODE_UPDATE_UNREAD = 0;

    public static final int UPDATE_TYPE_SET_TOP = 1;
    public static final int UPDATE_TYPE_CLEAR_UNREAD_STATUS = 2;
    public static final int UPDATE_TYPE_REMOVE = 3;
    public static final int UPDATE_TYPE_SAVE_DRAFT = 4;
    public static final int UPDATE_TYPE_CLEAR_MESSAGE = 5;
    public static final int UPDATE_TYPE_NOTIFICATION = 6;

    private RongIMClient mRongIMClient;
    private IMClient.SubManagerCallback mSubManagerCallback;

    private List<OnConversationStateChangeListener> mConversationStateChangeListeners;

    ConversationManager(IMClient.SubManagerCallback subManagerCallback) {
        if (subManagerCallback == null) {
            throw new NullPointerException("subManagerCallback can't be NULL");
        }
        mSubManagerCallback = subManagerCallback;
        mConversationStateChangeListeners = Collections.synchronizedList(new LinkedList<OnConversationStateChangeListener>());
        mRongIMClient = RongIMClient.getInstance();
    }

    public List<Conversation> getAllConversations() {
        return getAllConversations(IMClient.SUPPORT_CONVERSATION_TYPE);
    }

    public List<Conversation> getAllConversations(Conversation.ConversationType... type) {
        return mRongIMClient.getConversationList(type);
    }

    public Conversation getConversation(Conversation.ConversationType type, String targetId) {
        return mRongIMClient.getConversation(type, targetId);
    }

    public void setConversationTop(final Conversation conversation, boolean isTop) {
        mRongIMClient.setConversationToTop(conversation.getConversationType(), conversation.getTargetId(), isTop, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    callbackConversationChange(getConversation(conversation.getConversationType(), conversation.getTargetId()), UPDATE_TYPE_SET_TOP);
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

    public void clearConversationUnreadStatus(final Conversation conversation) {
        mRongIMClient.clearMessagesUnreadStatus(conversation.getConversationType(), conversation.getTargetId(), new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    mSubManagerCallback.conversationManagerCallback(CALLBACK_CODE_UPDATE_UNREAD, null);
                    callbackConversationChange(getConversation(conversation.getConversationType(), conversation.getTargetId()), UPDATE_TYPE_CLEAR_UNREAD_STATUS);
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

    public void removeConversation( Conversation conversation) {
        removeConversation(conversation,true);
    }

    public void removeConversation(final Conversation conversation,final boolean isCallbackListener) {
        final boolean isUpdateUnreadState = conversation.getUnreadMessageCount() > 0;
        mRongIMClient.removeConversation(conversation.getConversationType(), conversation.getTargetId(), new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    if(isCallbackListener) {
                        callbackConversationChange(conversation, UPDATE_TYPE_REMOVE);
                    }
                    if (isUpdateUnreadState) {
                        mSubManagerCallback.conversationManagerCallback(CALLBACK_CODE_UPDATE_UNREAD, null);
                    }
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

    public void saveConversationDraft(final Conversation conversation, final String draft) {
        mRongIMClient.saveTextMessageDraft(conversation.getConversationType(), conversation.getTargetId(), draft, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    callbackConversationChange(getConversation(conversation.getConversationType(), conversation.getTargetId()), UPDATE_TYPE_SAVE_DRAFT);
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

    public void clearAllConversationMessages(final Conversation conversation) {
        final boolean isUpdateUnreadState = conversation.getUnreadMessageCount() > 0;
        mRongIMClient.deleteMessages(conversation.getConversationType(), conversation.getTargetId(), new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                LogUtil.e("clearAllConversationMessages " + aBoolean.toString());
                callbackConversationChange(getConversation(conversation.getConversationType(), conversation.getTargetId()), UPDATE_TYPE_CLEAR_MESSAGE);
                if (isUpdateUnreadState) {
                    mSubManagerCallback.conversationManagerCallback(CALLBACK_CODE_UPDATE_UNREAD, null);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtil.e(errorCode.getMessage());
            }
        });
    }

    public void enableConversationNotification(final Conversation conversation, boolean isEnable) {
        Conversation.ConversationNotificationStatus status = isEnable ? Conversation.ConversationNotificationStatus.NOTIFY : Conversation.ConversationNotificationStatus.DO_NOT_DISTURB;
        mRongIMClient.setConversationNotificationStatus(conversation.getConversationType(), conversation.getTargetId(), status, new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {
                callbackConversationChange(getConversation(conversation.getConversationType(), conversation.getTargetId()), UPDATE_TYPE_NOTIFICATION);
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
                if(callback!=null){
                    callback.onSuccess(conversationNotificationStatus);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                if(callback!=null){
                    callback.onFailure(errorCode.getMessage());
                }
            }
        });
    }

    public void addConversationStateChangeListener(OnConversationStateChangeListener listener) {
        if (!mConversationStateChangeListeners.contains(listener)) {
            mConversationStateChangeListeners.add(listener);
        }
    }

    public void removeConversationStateChangeListener(OnConversationStateChangeListener listener) {
        mConversationStateChangeListeners.remove(listener);
    }

    private void callbackConversationChange(Conversation conversation, int typeCode) {
        for (OnConversationStateChangeListener listener : mConversationStateChangeListeners) {
            listener.onConversationStateChange(conversation, typeCode);
        }
    }

    void destroy(){
        mConversationStateChangeListeners.clear();
        mConversationStateChangeListeners = null;
    }

    public interface OnConversationStateChangeListener {
        void onConversationStateChange(Conversation conversation, int typeCode);
    }
}
