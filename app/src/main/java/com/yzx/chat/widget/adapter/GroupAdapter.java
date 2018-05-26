package com.yzx.chat.widget.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.widget.view.NineGridAvatarView;

import java.util.List;

/**
 * Created by YZX on 2018年03月09日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupAdapter extends BaseRecyclerViewAdapter<GroupAdapter.GroupHolder> {

    private List<GroupBean> mGroupList;


    public GroupAdapter(List<GroupBean> groupList) {
        mGroupList = groupList;
    }

    @Override
    public GroupHolder getViewHolder(ViewGroup parent, int viewType) {
        return new GroupHolder(LayoutInflater.from(mContext).inflate(R.layout.item_group, parent, false));

    }

    @Override
    public void bindDataToViewHolder(GroupHolder holder, int position) {
        GroupBean group = mGroupList.get(position);
        holder.mTvGroupName.setText(group.getNameAndMemberNumber());
        String avatarUrl = group.getAvatarUrlFromMember();
        Object[] avatarList;
        if (TextUtils.isEmpty(avatarUrl)) {
            avatarList = new Object[]{R.drawable.ic_avatar_default};
        } else {
            avatarList = avatarUrl.split(",");
        }
        holder.mIvAvatar.setImageUrlList(avatarList);
    }

    @Override
    public int getViewHolderCount() {
        return mGroupList == null ? 0 : mGroupList.size();
    }


    static class GroupHolder extends BaseRecyclerViewAdapter.BaseViewHolder {
        TextView mTvGroupName;
        NineGridAvatarView mIvAvatar;

        GroupHolder(View itemView) {
            super(itemView);
            mTvGroupName = itemView.findViewById(R.id.GroupAdapter_mTvGroupName);
            mIvAvatar = itemView.findViewById(R.id.GroupAdapter_mIvAvatar);
        }
    }

}
