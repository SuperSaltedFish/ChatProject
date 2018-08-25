package com.yzx.chat.widget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.util.AndroidUtil;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Created by YZX on 2018年08月25日.
 * 如果你不给自己设限，世界上便没有限制你发挥的篱笆。
 */
public class FileAndDirectoryAdapter extends BaseRecyclerViewAdapter<FileAndDirectoryAdapter.FileAndDirectoryHolder> {

    private static final double BYTE_SIZE_GB = 1024 * 1024;

    private List<File> mFileList;
    private final String UNIT_TERM;
    private final String SIZE_LABEL;

    public FileAndDirectoryAdapter(List<File> fileList) {
        mFileList = fileList;
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
            holder.mIvFileTypeIcon.setSelected(true);
            String[] fileNames = file.list();
            holder.mTvFileInfo.setText(String.format(Locale.getDefault(), "%d %s", fileNames == null ? 0 : fileNames.length, UNIT_TERM));
        } else {
            holder.mIvFileTypeIcon.setSelected(false);
            holder.mTvFileInfo.setText(String.format(Locale.getDefault(), "%s：%s", SIZE_LABEL, fileSizeFormat(file.length())));
        }
        holder.mTvFileName.setText(file.getName());
    }

    @Override
    public int getViewHolderCount() {
        return mFileList == null ? 0 : mFileList.size();
    }

    final static class FileAndDirectoryHolder extends BaseRecyclerViewAdapter.BaseViewHolder {
        ImageView mIvFileTypeIcon;
        TextView mTvFileName;
        TextView mTvFileInfo;

        FileAndDirectoryHolder(View itemView) {
            super(itemView);
            mIvFileTypeIcon = itemView.findViewById(R.id.FileAndDirectoryAdapter_mIvFileTypeIcon);
            mTvFileName = itemView.findViewById(R.id.FileAndDirectoryAdapter_mTvFileName);
            mTvFileInfo = itemView.findViewById(R.id.FileAndDirectoryAdapter_mTvFileInfo);
        }
    }

    private static String fileSizeFormat(long size) {
        if (size < 1024) {
            return String.format(Locale.getDefault(), "%d KB", size);
        } else if (size < BYTE_SIZE_GB) {
            return String.format(Locale.getDefault(), "%d MB", size / 1024);
        } else {
            return String.format(Locale.getDefault(), "%.02f GB", size / BYTE_SIZE_GB);
        }
    }
}
