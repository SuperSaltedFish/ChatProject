package com.yzx.chat.widget.adapter;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.bean.ContactOperationBean;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.util.GlideUtil;

import java.util.List;

/**
 * Created by YZX on 2018年01月18日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class ContactOperationAdapter extends BaseRecyclerViewAdapter<ContactOperationAdapter.ContactMessageHolder> {

    private List<ContactOperationBean> mContactOperationList;
    private OnContactRequestListener mOnContactRequestListener;

    public ContactOperationAdapter(List<ContactOperationBean> contactOperationList) {
        mContactOperationList = contactOperationList;
    }

    @Override
    public ContactMessageHolder getViewHolder(ViewGroup parent, int viewType) {
        return new ContactMessageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_contact_message, parent, false));
    }

    @Override
    public void bindDataToViewHolder(ContactMessageHolder holder, int position) {
        ContactOperationBean contactMessage = mContactOperationList.get(position);
        holder.setOnContactRequestListener(mOnContactRequestListener);
        holder.mTvName.setText(contactMessage.getUser().getNickname());
        String reason = contactMessage.getReason();
        if (TextUtils.isEmpty(reason)) {
            holder.mTvReason.setText(R.string.ContactOperationAdapter_DefaultReason);
        } else {
            holder.mTvReason.setText(contactMessage.getReason());
        }
        String type = contactMessage.getType();
        if (ContactManager.CONTACT_OPERATION_REQUEST.equals(type)) {
            holder.mTvRefused.setVisibility(View.VISIBLE);
            holder.mTvAccept.setEnabled(true);
        } else {
            holder.mTvRefused.setVisibility(View.INVISIBLE);
            holder.mTvAccept.setEnabled(false);
        }

        switch (type) {
            case ContactManager.CONTACT_OPERATION_ACCEPT:
            case ContactManager.CONTACT_OPERATION_ACCEPT_ACTIVE:
                holder.mTvAccept.setText(R.string.ContactMessageAdapter_Added);
                holder.mTvAccept.setBackgroundResource(R.drawable.bg_tv_contact_operation_accepted);
                holder.mTvAccept.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                break;
            case ContactManager.CONTACT_OPERATION_REJECT:
                holder.mTvAccept.setText(R.string.ContactMessageAdapter_Disagree);
                holder.mTvAccept.setBackgroundResource(R.drawable.bg_tv_contact_operation_refused);
                holder.mTvAccept.setTextColor(ContextCompat.getColor(mContext, android.R.color.holo_red_light));
                break;
            case ContactManager.CONTACT_OPERATION_REJECT_ACTIVE:
                holder.mTvAccept.setText(R.string.ContactMessageAdapter_AlreadyRefused);
                holder.mTvAccept.setBackgroundResource(R.drawable.bg_tv_contact_operation_accepted);
                holder.mTvAccept.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                break;
            case ContactManager.CONTACT_OPERATION_REQUEST:
                holder.mTvAccept.setText(R.string.ContactMessageAdapter_Requesting);
                holder.mTvAccept.setBackgroundResource(R.drawable.bg_tv_contact_operation_acceptable);
                holder.mTvAccept.setTextColor(Color.WHITE);
                break;
            case ContactManager.CONTACT_OPERATION_REQUEST_ACTIVE:
                holder.mTvAccept.setText(R.string.ContactMessageAdapter_Verifying);
                holder.mTvAccept.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey));
                holder.mTvAccept.setTextColor(ContextCompat.getColor(mContext, R.color.textSecondaryColorBlack));
                break;
        }
        GlideUtil.loadAvatarFromUrl(mContext, holder.mIvAvatar, contactMessage.getUser().getAvatar());

    }

    @Override
    public int getViewHolderCount() {
        return mContactOperationList == null ? 0 : mContactOperationList.size();
    }

    public void setOnContactRequestListener(OnContactRequestListener onContactRequestListener) {
        mOnContactRequestListener = onContactRequestListener;
        notifyDataSetChanged();
    }

    static final class ContactMessageHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        private OnContactRequestListener mOnContactRequestListener;
        ImageView mIvAvatar;
        TextView mTvName;
        TextView mTvReason;
        TextView mTvAccept;
        TextView mTvRefused;

        ContactMessageHolder(View itemView) {
            super(itemView);
            mIvAvatar = itemView.findViewById(R.id.ContactMessageAdapter_mIvAvatar);
            mTvName = itemView.findViewById(R.id.ContactMessageAdapter_mTvName);
            mTvReason = itemView.findViewById(R.id.ContactMessageAdapter_mTvReason);
            mTvAccept = itemView.findViewById(R.id.ContactMessageAdapter_mTvAccept);
            mTvRefused = itemView.findViewById(R.id.ContactMessageAdapter_mTvRefused);
            setup();
        }

        private void setup() {
            mTvAccept.setOnClickListener(mOnAcceptClickListener);
            mTvRefused.setOnClickListener(mOnRefusedClickListener);
            itemView.setOnClickListener(mOnDetailsClickListener);
        }

        public void setOnContactRequestListener(OnContactRequestListener onContactRequestListener) {
            mOnContactRequestListener = onContactRequestListener;
        }

        private final View.OnClickListener mOnAcceptClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnContactRequestListener != null) {
                    mOnContactRequestListener.onAcceptRequest(getAdapterPosition());
                }
            }
        };

        private final View.OnClickListener mOnRefusedClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnContactRequestListener != null) {
                    mOnContactRequestListener.onRefusedRequest(getAdapterPosition());
                }
            }
        };

        private final View.OnClickListener mOnDetailsClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnContactRequestListener != null) {
                    mOnContactRequestListener.enterDetails(getAdapterPosition());
                }
            }
        };
    }

    public interface OnContactRequestListener {
        void onAcceptRequest(int position);

        void onRefusedRequest(int position);

        void enterDetails(int position);
    }
}
