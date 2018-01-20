package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.bean.ContactMessageBean;
import com.yzx.chat.util.GlideUtil;

import java.util.List;

/**
 * Created by YZX on 2018年01月18日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class ContactMessageAdapter extends BaseRecyclerViewAdapter<ContactMessageAdapter.ContactMessageHolder> {

    private List<ContactMessageBean> mContactMessageList;

    public ContactMessageAdapter(List<ContactMessageBean> contactMessageList) {
        mContactMessageList = contactMessageList;
    }

    @Override
    public ContactMessageHolder getViewHolder(ViewGroup parent, int viewType) {
        return new ContactMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_contact_message, parent, false));
    }

    @Override
    public void bindDataToViewHolder(ContactMessageHolder holder, int position) {
        ContactMessageBean contactMessage = mContactMessageList.get(position);
        holder.mTvName.setText(contactMessage.getNickname());
        holder.mTvReason.setText(contactMessage.getReason());
        int type = contactMessage.getType();
        if (type == ContactMessageBean.TYPE_REQUESTING) {
            holder.mBtnState.setEnabled(true);
        } else {
            holder.mBtnState.setEnabled(false);
        }
        switch (type) {
            case ContactMessageBean.TYPE_ADDED:
                holder.mBtnState.setText(R.string.ContactMessageAdapter_Added);
                break;
            case ContactMessageBean.TYPE_DISAGREE:
                holder.mBtnState.setText(R.string.ContactMessageAdapter_Disagree);
                break;
            case ContactMessageBean.TYPE_REFUSED:
                holder.mBtnState.setText(R.string.ContactMessageAdapter_Refused);
                break;
            case ContactMessageBean.TYPE_REQUESTING:
                holder.mBtnState.setText(R.string.ContactMessageAdapter_Requesting);
                break;
            case ContactMessageBean.TYPE_VERIFYING:
                holder.mBtnState.setText(R.string.ContactMessageAdapter_Verifying);
                break;
        }
        GlideUtil.loadFromUrl(mContext, holder.mIvAvatar, contactMessage.getAvatarUrl());

    }

    @Override
    public int getViewHolderCount() {
        return mContactMessageList == null ? 0 : mContactMessageList.size();
    }

    static final class ContactMessageHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        ImageView mIvAvatar;
        TextView mTvName;
        TextView mTvReason;
        Button mBtnState;

        ContactMessageHolder(View itemView) {
            super(itemView);
            mIvAvatar = itemView.findViewById(R.id.ContactMessageAdapter_mIvAvatar);
            mTvName = itemView.findViewById(R.id.ContactMessageAdapter_mTvName);
            mTvReason = itemView.findViewById(R.id.ContactMessageAdapter_mTvReason);
            mBtnState = itemView.findViewById(R.id.ContactMessageAdapter_mBtnState);
        }
    }
}
