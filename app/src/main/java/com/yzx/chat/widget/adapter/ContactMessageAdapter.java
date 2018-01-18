package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;

/**
 * Created by YZX on 2018年01月18日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class ContactMessageAdapter extends BaseRecyclerViewAdapter<ContactMessageAdapter.ContactMessageHolder> {


    @Override
    public ContactMessageHolder getViewHolder(ViewGroup parent, int viewType) {
        return new ContactMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_contact_message, parent, false));
    }

    @Override
    public void bindDataToViewHolder(ContactMessageHolder holder, int position) {

    }

    @Override
    public int getViewHolderCount() {
        return 5;
    }

    static final class ContactMessageHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        ContactMessageHolder(View itemView) {
            super(itemView);
        }
    }
}
