package com.yzx.chat.widget.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private OnAcceptContactRequestListener mOnAcceptContactRequestListener;

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
        holder.mTvName.setText(contactMessage.getUser().getNickname());
        String reason = contactMessage.getReason();
        if(TextUtils.isEmpty(reason)){
            holder.mTvReason.setText(R.string.ContactOperationAdapter_DefaultReason);
        }else {
            holder.mTvReason.setText(contactMessage.getReason());
        }
        String type = contactMessage.getType();
        if (ContactManager.CONTACT_OPERATION_REQUEST.equals(type)) {
            holder.mBtnState.setEnabled(true);
            holder.setAcceptContactRequestListener(mOnAcceptContactRequestListener);
        } else {
            holder.mBtnState.setEnabled(false);
            holder.setAcceptContactRequestListener(null);
        }
        switch (type) {
            case ContactManager.CONTACT_OPERATION_ACCEPT:
            case ContactManager.CONTACT_OPERATION_ACCEPT_ACTIVE:
                holder.mBtnState.setText(R.string.ContactMessageAdapter_Added);
                break;
            case ContactManager.CONTACT_OPERATION_REFUSED:
                holder.mBtnState.setText(R.string.ContactMessageAdapter_Disagree);
                break;
            case ContactManager.CONTACT_OPERATION_REFUSED_ACTIVE:
                holder.mBtnState.setText(R.string.ContactMessageAdapter_Refused);
                break;
            case ContactManager.CONTACT_OPERATION_REQUEST:
                holder.mBtnState.setText(R.string.ContactMessageAdapter_Requesting);
                break;
            case ContactManager.CONTACT_OPERATION_REQUEST_ACTIVE:
                holder.mBtnState.setText(R.string.ContactMessageAdapter_Verifying);
                break;
        }
        GlideUtil.loadAvatarFromUrl(mContext, holder.mIvAvatar, contactMessage.getUser().getAvatar());

    }

    @Override
    public int getViewHolderCount() {
        return mContactOperationList == null ? 0 : mContactOperationList.size();
    }

    public void setOnAcceptContactRequestListener(OnAcceptContactRequestListener onAcceptContactRequestListener) {
        mOnAcceptContactRequestListener = onAcceptContactRequestListener;
    }

    static final class ContactMessageHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        private OnAcceptContactRequestListener mAcceptContactRequestListener;
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
            setup();
        }

        private void setup() {
            mBtnState.setOnClickListener(mOnAcceptClickListener);
        }

        public void setAcceptContactRequestListener(OnAcceptContactRequestListener acceptContactRequestListener) {
            mAcceptContactRequestListener = acceptContactRequestListener;
        }

        private final View.OnClickListener mOnAcceptClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAcceptContactRequestListener != null) {
                    mAcceptContactRequestListener.onAcceptContactRequest(getAdapterPosition());
                }
            }
        };
    }

    public interface OnAcceptContactRequestListener {
        void onAcceptContactRequest(int position);
    }
}
