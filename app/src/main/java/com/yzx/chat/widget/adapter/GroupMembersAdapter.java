package com.yzx.chat.widget.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.core.entity.GroupMemberEntity;
import com.yzx.chat.util.GlideUtil;

import java.util.List;


/**
 * Created by YZX on 2018年02月18日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class GroupMembersAdapter extends BaseRecyclerViewAdapter<GroupMembersAdapter.GroupMembersHolder> {

    private final int mMaxVisibilityCount;

    private List<GroupMemberEntity> mGroupMembers;

    public GroupMembersAdapter(List<GroupMemberEntity> groupMembers, int maxVisibilityCount) {
        mGroupMembers = groupMembers;
        mMaxVisibilityCount = maxVisibilityCount;
    }

    @Override
    public GroupMembersHolder getViewHolder(ViewGroup parent, int viewType) {
        return new GroupMembersHolder(LayoutInflater.from(mContext).inflate(R.layout.item_group_member, parent, false));
    }

    @Override
    public void bindDataToViewHolder(GroupMembersHolder holder, int position) {
        GroupMemberEntity groupMember = mGroupMembers.get(position);
        holder.mTvMemberName.setText(groupMember.getNicknameInGroup());
        GlideUtil.loadAvatarFromUrl(mContext, holder.mIvAvatar, groupMember.getUserProfile().getAvatar());
    }

    @Override
    public int getViewHolderCount() {
        if (mGroupMembers == null) {
            return 0;
        }
        int size = mGroupMembers.size();
        return size > mMaxVisibilityCount ? mMaxVisibilityCount : size;
    }

    final static class GroupMembersHolder extends BaseRecyclerViewAdapter.BaseViewHolder {

        ImageView mIvAvatar;
        TextView mTvMemberName;

        GroupMembersHolder(View itemView) {
            super(itemView);
            mIvAvatar = itemView.findViewById(R.id.GroupMembersAdapter_mIvAvatar);
            mTvMemberName = itemView.findViewById(R.id.GroupMembersAdapter_mTvMemberName);
        }


    }
}
