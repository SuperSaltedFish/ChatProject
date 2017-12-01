package com.yzx.chat.widget.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by YZX on 2017年12月01日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class EmotionPanelLinearLayout extends LinearLayout {

    public EmotionPanelLinearLayout(Context context) {
        this(context,null);
    }

    public EmotionPanelLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public EmotionPanelLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setHeight(@Px int height){
       ViewGroup.LayoutParams  params = getLayoutParams();
       if(params==null){
           params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,height);
       }else {
           params.height = height;
       }
       setLayoutParams(params);
    }
}
