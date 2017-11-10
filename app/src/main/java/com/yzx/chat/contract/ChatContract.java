package com.yzx.chat.contract;

import com.hyphenate.chat.EMMessage;
import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

import java.util.List;

/**
 * Created by YZX on 2017年11月10日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class ChatContract {
    public interface View extends BaseView<Presenter> {

        void startShow(List<EMMessage> messageList);
        void showMore(List<EMMessage> messageList);
    }


    public interface Presenter extends BasePresenter<View> {

        void initMessage(String conversationID);
        void loadMoreMessage(String startID,int count);

    }
}
