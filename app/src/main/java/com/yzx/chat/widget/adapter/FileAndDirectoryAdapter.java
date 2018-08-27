package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.FileUtil;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Created by YZX on 2018年08月25日.
 * 如果你不给自己设限，世界上便没有限制你发挥的篱笆。
 */
public class FileAndDirectoryAdapter extends BaseRecyclerViewAdapter<FileAndDirectoryAdapter.FileAndDirectoryHolder> {

    private List<File> mFileList;
    private List<String> mSelectedFilePathList;
    private final String UNIT_TERM;
    private final String SIZE_LABEL;

    public FileAndDirectoryAdapter(List<File> fileList, List<String> selectedFilePathList) {
        mFileList = fileList;
        mSelectedFilePathList = selectedFilePathList;
        UNIT_TERM = AndroidUtil.getString(R.string.Unit_Term);
        SIZE_LABEL = AndroidUtil.getString(R.string.FileAndDirectoryAdapter_FileSize);
    }

    @Override
    public FileAndDirectoryHolder getViewHolder(ViewGroup parent, int viewType) {
        return new FileAndDirectoryHolder(LayoutInflater.from(mContext).inflate(R.layout.item_file_and_directory, parent, false));
    }

    @Override
    public void bindDataToViewHolder(FileAndDirectoryHolder holder, int position) {
        File file = mFileList.get(position);
        if (file.isDirectory()) {
            holder.mCbSelected.setVisibility(View.GONE);
            holder.mIvFileTypeIcon.setSelected(true);
            String[] fileNames = file.list();
            holder.mTvFileInfo.setText(String.format(Locale.getDefault(), "%d %s", fileNames == null ? 0 : fileNames.length, UNIT_TERM));
        } else {
            holder.mCbSelected.setVisibility(View.VISIBLE);
            holder.mCbSelected.setChecked(mSelectedFilePathList.contains(file.getPath()));
            holder.mIvFileTypeIcon.setSelected(false);
            holder.mTvFileInfo.setText(String.format(Locale.getDefault(), "%s：%s", SIZE_LABEL, FileUtil.fileSizeFormat(file.length())));
        }
        holder.mTvFileName.setText(file.getName());
    }

    @Override
    public int getViewHolderCount() {
        return mFileList == null ? 0 : mFileList.size();
    }

    public final static class FileAndDirectoryHolder extends BaseRecyclerViewAdapter.BaseViewHolder {
        ImageView mIvFileTypeIcon;
        TextView mTvFileName;
        TextView mTvFileInfo;
        CheckBox mCbSelected;

        FileAndDirectoryHolder(View itemView) {
            super(itemView);
            mIvFileTypeIcon = itemView.findViewById(R.id.FileAndDirectoryAdapter_mIvFileTypeIcon);
            mTvFileName = itemView.findViewById(R.id.FileAndDirectoryAdapter_mTvFileName);
            mTvFileInfo = itemView.findViewById(R.id.FileAndDirectoryAdapter_mTvFileInfo);
            mCbSelected = itemView.findViewById(R.id.FileAndDirectoryAdapter_mCbSelected);
        }

        public void setSelected(boolean isSelect) {
            mCbSelected.setChecked(isSelect);
        }

        public boolean isSelected() {
            return mCbSelected.isChecked();
        }
    }

}
