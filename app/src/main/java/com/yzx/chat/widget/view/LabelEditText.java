package com.yzx.chat.widget.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.appcompat.widget.AppCompatEditText;

/**
 * Created by YZX on 2018年01月23日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class LabelEditText extends AppCompatEditText {

    private BackInputConnection.BackspaceListener mBackspaceListener;
    private BackInputConnection mBackInputConnection;

    public LabelEditText(Context context) {
        super(context);
    }

    public LabelEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LabelEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        if(mBackInputConnection==null){
            mBackInputConnection = new BackInputConnection(super.onCreateInputConnection(outAttrs), true);
        }
        mBackInputConnection.setBackspaceListener(mBackspaceListener);
        return mBackInputConnection;
    }

    public void setBackspaceListener(BackInputConnection.BackspaceListener backspaceListener) {
        mBackspaceListener = backspaceListener;
        if(mBackInputConnection!=null){
            mBackInputConnection.setBackspaceListener(mBackspaceListener);
        }
    }
}
