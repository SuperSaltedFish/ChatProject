package com.yzx.chat.module.common.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.adapter.DirectoryPathAdapter;
import com.yzx.chat.widget.adapter.FileAndDirectoryAdapter;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.DividerItemDecoration;
import com.yzx.chat.widget.view.SpacesItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class FileSelectorActivity extends BaseCompatActivity {

    public static final int RESULT_CODE = FileSelectorActivity.class.hashCode();
    public static final String INTENT_EXTRA_SELECTED_FILE_PATH = "SelectedFilePath";

    private static final String ROOT_PATH = Environment.getExternalStorageDirectory().getPath();
    private static final int MAX_SELECTED_COUNT = Constants.MAX_ONCE_FILE_SEND_COUNT;
    private static final int MAX_FILE_SEND_SIZE = Constants.MAX_FILE_SEND_SIZE;

    private RecyclerView mRvFileAndDirectory;
    private RecyclerView mRvDirectoryPath;
    private TextView mTvConfirm;
    private LinearLayoutManager mDirectoryPathLayoutManager;
    private FileAndDirectoryAdapter mFileAndDirectoryAdapter;
    private DirectoryPathAdapter mDirectoryPathAdapter;

    private String mCurrentPaht;
    private List<File> mCurrentFileList;
    private List<String> mDirectoryNameList;
    private ArrayList<String> mSelectedFilePathList;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_file_selector;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mRvFileAndDirectory = findViewById(R.id.FileSelectorActivity_mRvFileAndDirectory);
        mRvDirectoryPath = findViewById(R.id.FileSelectorActivity_mRvDirectoryPath);
        mTvConfirm = findViewById(R.id.FileSelectorActivity_mTvConfirm);
        mCurrentFileList = new ArrayList<>();
        mDirectoryNameList = new ArrayList<>();
        mSelectedFilePathList = new ArrayList<>();
        mFileAndDirectoryAdapter = new FileAndDirectoryAdapter(mCurrentFileList, mSelectedFilePathList);
        mDirectoryPathAdapter = new DirectoryPathAdapter(mDirectoryNameList);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mDirectoryPathLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRvDirectoryPath.setLayoutManager(mDirectoryPathLayoutManager);
        mRvDirectoryPath.setHasFixedSize(true);
        mRvDirectoryPath.addItemDecoration(new SpacesItemDecoration((int) AndroidUtil.dip2px(6), SpacesItemDecoration.HORIZONTAL));
        mRvDirectoryPath.addOnItemTouchListener(mOnPathItemClickListener);
        mRvDirectoryPath.setAdapter(mDirectoryPathAdapter);

        mRvFileAndDirectory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRvFileAndDirectory.addItemDecoration(new DividerItemDecoration(1, ContextCompat.getColor(this, R.color.dividerColorBlack), DividerItemDecoration.HORIZONTAL));
        mRvFileAndDirectory.setHasFixedSize(true);
        mRvFileAndDirectory.addOnItemTouchListener(mOnFileOrDirectoryItemClickListener);
        mRvFileAndDirectory.setAdapter(mFileAndDirectoryAdapter);

        mTvConfirm.setOnClickListener(mOnViewClickListener);
        mTvConfirm.setEnabled(false);

        mDirectoryNameList.add(getString(R.string.FileSelectorActivity_Storage));
        mOnPathItemClickListener.onItemClick(0, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDirectoryNameList.size() == 1) {
            super.onBackPressed();
        } else {
            deleteDirectoryNameBehindOf(mDirectoryNameList.get(mDirectoryNameList.size() - 2));
            updateCurrentDirectoryContent();
        }
    }

    private void confirmSelectedResult() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(INTENT_EXTRA_SELECTED_FILE_PATH,mSelectedFilePathList);
        setResult(RESULT_CODE,intent);
        finish();
    }

    private void updateCurrentDirectoryContent() {
        StringBuilder stringBuilder = new StringBuilder(ROOT_PATH.length() + mDirectoryNameList.size() * 10);
        stringBuilder.append(ROOT_PATH);
        for (int i = 1, size = mDirectoryNameList.size(); i < size; i++) {
            stringBuilder.append("/").append(mDirectoryNameList.get(i));
        }
        mCurrentPaht = stringBuilder.toString();
        File[] files = new File(mCurrentPaht).listFiles();
        mCurrentFileList.clear();
        if (files != null) {
            File file;
            for (File file1 : files) {
                file = file1;
                if (!file.isHidden()) {
                    mCurrentFileList.add(file);
                }
            }
        }
        if (mCurrentFileList.size() > 0) {
            Collections.sort(mCurrentFileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1.isDirectory() && o2.isFile())
                        return -1;
                    if (o1.isFile() && o2.isDirectory())
                        return 1;
                    return o1.getName().compareTo(o2.getName());
                }
            });
        }
        mFileAndDirectoryAdapter.notifyDataSetChanged();
    }

    private void addDirectoryName(String directoryName) {
        mDirectoryPathAdapter.notifyItemInserted(mDirectoryNameList.size());
        mDirectoryNameList.add(directoryName);
        mDirectoryPathLayoutManager.scrollToPosition(mDirectoryNameList.size() - 1);
    }

    private void deleteDirectoryNameBehindOf(String directoryName) {
        int deleteCount;
        if (directoryName == null) {
            deleteCount = mDirectoryNameList.size() - 1;
            String first = mDirectoryNameList.get(0);
            mDirectoryNameList.clear();
            mDirectoryNameList.add(first);
        } else {
            deleteCount = 0;
            Iterator<String> it = mDirectoryNameList.iterator();
            boolean startDelete = false;
            while (it.hasNext()) {
                if (startDelete) {
                    it.next();
                    it.remove();
                    deleteCount++;
                    continue;
                }
                if (directoryName.equals(it.next())) {
                    startDelete = true;
                }
            }
        }
        mDirectoryPathAdapter.notifyItemRangeRemovedEx(mDirectoryNameList.size(), deleteCount);
    }

    private void addSelectedFile(String path) {
        mSelectedFilePathList.add(path);
        mTvConfirm.setEnabled(true);
        mTvConfirm.setText(String.format(Locale.getDefault(), "%s(%d/%d)", getString(R.string.ImageSelectorActivity_Send), mSelectedFilePathList.size(), MAX_SELECTED_COUNT));

    }

    private void removeSelectedFile(String path) {
        mSelectedFilePathList.remove(path);
        mTvConfirm.setText(String.format(Locale.getDefault(), "%s(%d/%d)", getString(R.string.ImageSelectorActivity_Send), mSelectedFilePathList.size(), MAX_SELECTED_COUNT));
        mTvConfirm.setEnabled(mSelectedFilePathList.size() > 0);
    }

    private final View.OnClickListener mOnViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.FileSelectorActivity_mTvConfirm:
                    confirmSelectedResult();
                    break;
            }
        }
    };


    private final OnRecyclerViewItemClickListener mOnPathItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {
            if (position == 0) {
                deleteDirectoryNameBehindOf(null);
                updateCurrentDirectoryContent();
            } else if (position != mDirectoryNameList.size() - 1) {
                deleteDirectoryNameBehindOf(mDirectoryNameList.get(position));
                updateCurrentDirectoryContent();
            }
        }
    };

    private final OnRecyclerViewItemClickListener mOnFileOrDirectoryItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {
            File file = mCurrentFileList.get(position);
            if (file.isDirectory()) {
                addDirectoryName(mCurrentFileList.get(position).getName());
                updateCurrentDirectoryContent();
            } else {
                FileAndDirectoryAdapter.FileAndDirectoryHolder holder = (FileAndDirectoryAdapter.FileAndDirectoryHolder) viewHolder;
                if (holder.isSelected()) {
                    holder.setSelected(false);
                    removeSelectedFile(file.getPath());
                } else if (mSelectedFilePathList.size() < MAX_SELECTED_COUNT) {
                    if (file.length() <= MAX_FILE_SEND_SIZE) {
                        holder.setSelected(true);
                        addSelectedFile(file.getPath());
                    } else {
                        showToast(String.format(Locale.getDefault(), getString(R.string.FileSelectorActivity_MaxSelectedFileSize), MAX_FILE_SEND_SIZE / 1024 / 1024));
                    }
                } else {
                    showToast(String.format(Locale.getDefault(), getString(R.string.FileSelectorActivity_MaxSelectedCountHint), MAX_SELECTED_COUNT));
                }
            }
        }
    };
}
