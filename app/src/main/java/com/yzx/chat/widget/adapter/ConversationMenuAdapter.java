package com.yzx.chat.widget.adapter;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yzx.chat.util.AndroidUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年12月25日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class ConversationMenuAdapter extends BaseAdapter {

    private List<String> mMenuTitleList ;

    {
        mMenuTitleList = new ArrayList<>();
        mMenuTitleList.add("指定");
        mMenuTitleList.add("指定定");
        mMenuTitleList.add("指定指定定");
        mMenuTitleList.add("指定指定");
    }

    @Override
    public int getCount() {
        return mMenuTitleList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMenuTitleList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            TextView textView = new TextView(parent.getContext());
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) AndroidUtil.dip2px(40)));
            convertView = textView;
        }
        TextView itemMenu = (TextView) convertView;
        itemMenu.setText(mMenuTitleList.get(position));
        return convertView;
    }

}
