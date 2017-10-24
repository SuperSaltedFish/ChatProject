package com.yzx.chat.widget.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.test.ChatTestData;
import com.yzx.chat.util.GlideUtil;

import java.util.List;


/**
 * Created by YZX on 2017年06月03日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ChatAdapter extends BaseRecyclerViewAdapter<ChatAdapter.ItemView> {
    private List<ChatTestData.ChatBean> mChatList;

    public ChatAdapter(List<ChatTestData.ChatBean> chatList) {
        mChatList = chatList;
    }

    @Override
    public ItemView getViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case ChatTestData.ChatBean.CHAT_SEND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_chat_send, parent, false);
                break;
            case ChatTestData.ChatBean.CHAT_RECEIVE:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_chat_receive, parent, false);
                break;
        }
        return new ItemView(view);
    }

    @Override
    public void bindDataToViewHolder(ItemView holder, int position) {
        ChatTestData.ChatBean chatBean = mChatList.get(position);
        int contentType = chatBean.getContentType();
        String content = chatBean.getContent();
        holder.reset();
        switch (contentType) {
            case ChatTestData.ChatBean.TYPE_TEXT:
                holder.mTvTextContent.setText(content);
                holder.mTvTextContent.setVisibility(View.VISIBLE);
                break;
            case ChatTestData.ChatBean.TYPE_IMAGE:
                holder.mIvImageContent.setVisibility(View.VISIBLE);
                GlideUtil.loadFromUrl(mContext, holder.mIvImageContent, content);
                break;
            case ChatTestData.ChatBean.TYPE_MOVIE:

                break;
        }
        if(chatBean.getChatType()==ChatTestData.ChatBean.CHAT_RECEIVE) {
            GlideUtil.loadFromUrl(mContext, holder.mIvHeadImage, R.drawable.temp_head_image);
        }
    }


    @Override
    public int getItemCount() {
        if (mChatList == null)
            return 0;
        return mChatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mChatList.get(position).getChatType();
    }

    final static class ItemView extends RecyclerView.ViewHolder {
        ImageView mIvHeadImage;
        TextView mTvTextContent;
        ImageView mIvImageContent;

        ItemView(View itemView) {
            super(itemView);
            initView();
        }

        private void initView() {
            mIvHeadImage = (ImageView) itemView.findViewById(R.id.ChatAdapter_mIvHeadImage);
            mTvTextContent = (TextView) itemView.findViewById(R.id.ChatAdapter_mTvTextContent);
            mIvImageContent = (ImageView) itemView.findViewById(R.id.ChatAdapter_mIvImageContent);
        }

        void reset() {
            GlideUtil.clear(itemView.getContext(),mIvImageContent);
            mTvTextContent.setVisibility(View.GONE);
            mIvImageContent.setVisibility(View.GONE);
            mTvTextContent.setText("");
            mIvImageContent.setImageBitmap(null);

        }


    }
}
