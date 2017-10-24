package com.yzx.chat.view.activity;

import android.Manifest;
import android.app.ActionBar;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toolbar;

import com.yzx.chat.R;
import com.yzx.chat.widget.adapter.ImageDirAdapter;
import com.yzx.chat.widget.adapter.ImageSelectAdapter;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.widget.listener.OnRecyclerViewClickListener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ImageSelectorActivity extends BaseCompatActivity {

    public static final int RESULT_CODE = ImageSelectorActivity.class.hashCode();
    public static final String RESULT = "ImageSelectedList";

    private static final int HORIZONTAL_ITEM_COUNT = 3;

    private Toolbar mToolbar;
    private RecyclerView mRvImageList;
    private RecyclerView mRvImageDirList;
    private Button mBtnConfirm;
    private ImageSelectAdapter mImageSelectAdapter;
    private BottomSheetBehavior mBottomBehavior;
    private ImageDirAdapter mImageDirAdapter;
    private View mMaskView;
    private String mCurrentShowDir;
    private ColorDrawable mMaskColorDrawable;
    private HashMap<String, List<String>> mGroupingMap;
    private ArrayList<String> mCurrentImagePathList;
    private ArrayList<String> mImageDirPath;
    private ArrayList<String> mSelectedList;

    private TextView mTvChooseDir;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_image_selector;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setView();
        setData();

        requestPermissionsInCompatMode(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }


    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.ImageSelectorActivity_mToolbar);
        mBtnConfirm = (Button) findViewById(R.id.ImageSelectorActivity_mBtnConfirm);
        mRvImageList = (RecyclerView) findViewById(R.id.ImageSelectorActivity_mRvImageList);
        mTvChooseDir = (TextView) findViewById(R.id.ImageSelectorActivity_mTvChooseDir);
        mRvImageDirList = (RecyclerView) findViewById(R.id.ImageSelectorActivity_mRvImageDirList);
        mMaskView = findViewById(R.id.ImageSelectorActivity_mMaskView);

        mCurrentImagePathList = new ArrayList<>();
        mImageDirPath = new ArrayList<>();
        mSelectedList = new ArrayList<>();
        mGroupingMap = new HashMap<>();
        mCurrentShowDir = "";
        mBottomBehavior = BottomSheetBehavior.from(mRvImageDirList);
        mImageSelectAdapter = new ImageSelectAdapter(mCurrentImagePathList, HORIZONTAL_ITEM_COUNT);
        mImageDirAdapter = new ImageDirAdapter(mImageDirPath, mGroupingMap);
        mMaskColorDrawable = new ColorDrawable(Color.BLACK);
    }

    private void setView() {
        mToolbar.setTitle("选择图片");
        mToolbar.setTitleTextColor(Color.WHITE);
        setActionBar(mToolbar);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mRvImageList.setLayoutManager(new GridLayoutManager(this, HORIZONTAL_ITEM_COUNT));
        mRvImageList.setAdapter(mImageSelectAdapter);

        mRvImageDirList.setLayoutManager(new LinearLayoutManager(this));
        mRvImageDirList.setAdapter(mImageDirAdapter);
        mRvImageDirList.addOnItemTouchListener(mOnRecyclerViewClickListener);

        mBottomBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mBottomBehavior.setSkipCollapsed(true);
        mBottomBehavior.setBottomSheetCallback(mBottomSheetCallback);

        mMaskColorDrawable.setAlpha(0);
        mMaskView.setBackground(mMaskColorDrawable);

        mTvChooseDir.setOnClickListener(mOnClickListener);

        mBtnConfirm.setOnClickListener(mOnBtnConfirmClickListener);

        mImageSelectAdapter.setOnSelectedChangeListener(mOnSelectedChangeListener);
    }

    private void setData() {
        new LoadImageAsyncTask(ImageSelectorActivity.this).execute();
    }

    private void loadLocalResource(HashMap<String, List<String>> groupingMap) {
        mGroupingMap.putAll(groupingMap);
        loadDirList();
        setAlbumFolder(null);
    }

    private void loadDirList() {
        mImageDirPath.clear();
        for (String dir : mGroupingMap.keySet()) {
            mImageDirPath.add(dir);
        }
        mImageDirAdapter.notifyDataSetChanged();
    }

    private void setAlbumFolder(String folder) {
        if ((mCurrentShowDir != null && mCurrentShowDir.equals(folder)) || (folder == null && mCurrentShowDir == null)) {
            return;
        }
        mCurrentShowDir = folder;
        mCurrentImagePathList.clear();
        mSelectedList.clear();
        mBtnConfirm.setText("确定");
        if (folder == null) {
            for (Map.Entry<String, List<String>> entry : mGroupingMap.entrySet()) {
                mCurrentImagePathList.addAll(entry.getValue());
            }
            mTvChooseDir.setText("所有图片");
        } else if (mGroupingMap.containsKey(folder)) {
            mCurrentImagePathList.addAll(mGroupingMap.get(folder));
            mTvChooseDir.setText(folder.substring(folder.lastIndexOf("/") + 1));
        }
        mImageSelectAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBottomBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                mBottomBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                mBottomBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        }
    };

    private final View.OnClickListener mOnMaskViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mBottomBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    };

    private final View.OnClickListener mOnBtnConfirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mSelectedList.size() != 0) {
                Intent intent = new Intent();
                intent.putStringArrayListExtra(RESULT, mSelectedList);
                setResult(RESULT_CODE, intent);
            }
            finish();
        }
    };

    private final BottomSheetBehavior.BottomSheetCallback mBottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                mMaskColorDrawable.setAlpha(128);
                mMaskView.setOnClickListener(mOnMaskViewClickListener);
                return;
            } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                mMaskColorDrawable.setAlpha(0);
            }
            mMaskView.setOnClickListener(null);
            mMaskView.setClickable(false);
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            mMaskColorDrawable.setAlpha((int) (128 * slideOffset));
        }
    };


    private final OnRecyclerViewClickListener mOnRecyclerViewClickListener = new OnRecyclerViewClickListener() {
        @Override
        public void onItemClick(int position, View itemView) {
            if (position == 0) {
                setAlbumFolder(null);
            } else {
                setAlbumFolder(mImageDirPath.get(position - 1));
            }
            mBottomBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

        @Override
        public void onItemLongClick(int position, View itemView) {

        }
    };


    private final ImageSelectAdapter.OnSelectedChangeListener mOnSelectedChangeListener = new ImageSelectAdapter.OnSelectedChangeListener() {
        @Override
        public void onSelect(int position, boolean isSelect) {
            if (isSelect) {
                mSelectedList.add(mCurrentImagePathList.get(position));
            } else {
                mSelectedList.remove(mCurrentImagePathList.get(position));
            }
            mBtnConfirm.setText(String.format(Locale.getDefault(), "确定(%d/%d)", mSelectedList.size(), mCurrentImagePathList.size()));
        }
    };

    private static class LoadImageAsyncTask extends AsyncTask<Void, Void, HashMap<String, List<String>>> {

        private WeakReference<ImageSelectorActivity> mWReference;
        private ContentResolver mContentResolver;

        LoadImageAsyncTask(ImageSelectorActivity imageSelectorActivity) {
            mContentResolver = imageSelectorActivity.getContentResolver();
            mWReference = new WeakReference<>(imageSelectorActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected HashMap<String, List<String>> doInBackground(Void... params) {
            Cursor cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, MediaStore.Images.Media.DATE_MODIFIED);
            if (cursor == null) {
                return null;
            }
            File parentFile;
            HashMap<String, List<String>> groupingMap = new HashMap<>(32);
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                parentFile = new File(path).getParentFile();
                if (parentFile == null) {
                    continue;
                }
                String parentPath = parentFile.getAbsolutePath();
                if (!groupingMap.containsKey(parentPath)) {
                    groupingMap.put(parentPath, new ArrayList<String>());
                }
                groupingMap.get(parentPath).add(path);
            }
            cursor.close();
            mContentResolver = null;
            return groupingMap;
        }

        @Override
        protected void onPostExecute(HashMap<String, List<String>> stringListHashMap) {
            super.onPostExecute(stringListHashMap);
            ImageSelectorActivity imageSelectorActivity = mWReference.get();
            if (imageSelectorActivity == null || stringListHashMap == null) {
                return;
            }
            imageSelectorActivity.loadLocalResource(stringListHashMap);
        }
    }
}
