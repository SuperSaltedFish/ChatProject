package com.yzx.chat.mvp.contract;

import android.net.Uri;

import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

/**
 * Created by YZX on 2018年05月17日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class VideoPlayContract {
    public interface View extends BaseView<Presenter> {
        void playVideo(String videoPath);

        void showProcess(int percent);

        void setEnableProgressDialog(boolean isEnable);

        void showError(String error);
    }


    public interface Presenter extends BasePresenter<View> {

        void downloadVideo(Uri videoUri);
    }
}
