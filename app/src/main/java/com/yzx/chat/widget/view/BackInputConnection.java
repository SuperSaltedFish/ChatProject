package com.yzx.chat.widget.view;

import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

/**
 * Created by YZX on 2018年01月10日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class BackInputConnection extends InputConnectionWrapper {

    private BackspaceListener mBackspaceListener;

    public BackInputConnection(InputConnection target, boolean mutable) {
        super(target, mutable);
    }

    public interface BackspaceListener {
        /**
         * @return true 代表消费了这个事件
         */
        boolean onBackspace();
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        if (mBackspaceListener != null) {
            if (mBackspaceListener.onBackspace()) {
                return true;
            }
        }

        return super.deleteSurroundingText(beforeLength, afterLength);
    }

    public void setBackspaceListener(BackspaceListener backspaceListener) {
        this.mBackspaceListener = backspaceListener;
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mBackspaceListener != null && mBackspaceListener.onBackspace()) {
                return true;
            }
        }
        return super.sendKeyEvent(event);
    }
}
