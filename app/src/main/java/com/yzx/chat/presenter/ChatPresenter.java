package com.yzx.chat.presenter;

import android.net.Uri;
import android.os.Handler;

import com.yzx.chat.configure.Constants;
import com.yzx.chat.contract.ChatContract;
import com.yzx.chat.tool.ChatClientManager;
import com.yzx.chat.util.LogUtil;

import java.io.File;
import java.util.List;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

/**
 * Created by YZX on 2017年11月10日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class ChatPresenter implements ChatContract.Presenter {

    public static String sConversationID;

    private ChatContract.View mChatView;
    private Handler mHandler;
    private ChatClientManager mChatManager;

    private Conversation mConversation;
    private boolean mHasMoreMessage;
    private boolean mIsLoadingMore;


    @Override
    public void attachView(ChatContract.View view) {
        mChatView = view;
        mHandler = new Handler();
        mChatManager = ChatClientManager.getInstance();
    }

    @Override
    public void detachView() {
        mChatManager.removeOnMessageReceiveListener(mOnChatMessageReceiveListener);
        mHandler.removeCallbacksAndMessages(null);
        mChatView = null;
        mHandler = null;
        sConversationID = null;
    }

    @Override
    public void init(Conversation conversation) {
        mConversation = conversation;
        sConversationID = mConversation.getTargetId();
        mHasMoreMessage = true;
        mIsLoadingMore = false;
        mChatManager.addOnMessageReceiveListener(mOnChatMessageReceiveListener, sConversationID);
        mChatManager.clearConversationUnreadStatus(mConversation.getConversationType(), mConversation.getTargetId());
        List<Message> messageList = mChatManager.getHistoryMessages(mConversation.getConversationType(), mConversation.getTargetId(), -1, Constants.CHAT_MESSAGE_PAGE_SIZE);
        if (messageList == null || messageList.size() == 0) {
            mHasMoreMessage = false;
            return;
        }

        if (messageList.size() < Constants.CHAT_MESSAGE_PAGE_SIZE) {
            mHasMoreMessage = false;
        }
        mChatView.addNewMessage(messageList);
    }

    @Override
    public void resendMessage(Message message) {
        sendMessage(message);
    }


    @Override
    public void sendTextMessage(String content) {
        sendMessage(TextMessage.obtain("我是消息内容"));
    }

    @Override
    public void sendVoiceMessage(String filePath, int timeLength) {
        VoiceMessage.obtain(Uri.fromFile(new File(filePath)), timeLength / 1000);
        sendMessage(VoiceMessage.obtain(Uri.fromFile(new File(filePath)), timeLength / 1000));
    }

    @Override
    public void loadMoreMessage(int lastMessageID) {
        mIsLoadingMore = true;
        mChatManager.asyncGetHistoryMessages(
                mConversation.getConversationType(),
                mConversation.getTargetId(),
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

    private void sendMessage(MessageContent messageContent) {
        sendMessage(Message.obtain(mConversation.getTargetId(), Conversation.ConversationType.PRIVATE, messageContent));
    }

    private void sendMessage(Message message) {
        mChatManager.sendMessage(message);
        mChatView.addNewMessage(message);
    }

    private void updateMessage(Message message) {
//        List<EMMessage> messageList = mChatView.getAllMessage();
//        for (int position = 0, length = messageList.size(); position < length; position++) {
//            if (messageList.get(position).getMsgId().equals(message.getMsgId())) {
//                final int finalPosition = position;
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        mChatView.updateMessage(finalPosition);
//                    }
//                });
//                break;
//            }
//        }
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


    private final ChatClientManager.OnChatMessageReceiveListener mOnChatMessageReceiveListener = new ChatClientManager.OnChatMessageReceiveListener() {

        @Override
        public void onChatMessageReceived(Message message, int untreatedCount) {
            loadNewComplete(message);
        }
    };

}
