package com.yzx.chat.widget.listener;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by YZX on 2017年12月22日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class AutoCloseKeyboardScrollListener extends RecyclerView.OnScrollListener{


    private Activity mActivity;
    private InputMethodManager mInputManager;

    public AutoCloseKeyboardScrollListener(Activity activity) {
        mActivity = activity;
        mInputManager =  (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    }


    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if(newState!=RecyclerView.SCROLL_STATE_IDLE){
            if (mActivity.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                if (mActivity.getCurrentFocus() != null)
                    mInputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}
