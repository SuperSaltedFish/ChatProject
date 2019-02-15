package com.yzx.chat.module.conversation.contract;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

import io.rong.imlib.model.Message;

/**
 * Created by YZX on 2018年05月17日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VideoPlayContract {
    public interface View extends BaseView<Presenter> {
        void playVideo(String videoPath);

        void showProcess(int percent);
    }


    public interface Presenter extends BasePresenter<View> {

        void downloadVideo(Message message);
    }
}
