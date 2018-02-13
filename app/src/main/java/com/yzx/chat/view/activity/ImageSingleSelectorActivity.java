package com.yzx.chat.view.activity;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.adapter.ImageDirAdapter;
import com.yzx.chat.widget.adapter.LocalSingleImageAdapter;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.SpacesItemDecoration;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by YZX on 2018年02月12日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ImageSingleSelectorActivity extends BaseCompatActivity {

    public static final int RESULT_CODE = 1;
    public static final String RESULT = "ImageSelectedList";

    private static final int HORIZONTAL_ITEM_COUNT = 4;

    private RecyclerView mRvImage;
    private RecyclerView mRvImageDir;
    private LocalSingleImageAdapter mLocalSingleImageAdapter;
    private BottomSheetBehavior mBottomBehavior;
    private ImageDirAdapter mImageDirAdapter;
    private View mMaskView;
    private String mCurrentShowDir;
    private ColorDrawable mMaskColorDrawable;
    private HashMap<String, List<String>> mGroupingMap;
    private ArrayList<String> mCurrentImagePathList;
    private ArrayList<String> mImageDirPath;

    private TextView mTvChooseDir;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_image_single_selector;
    }

    @Override
    protected void init() {
        mRvImage = findViewById(R.id.ImageSingleSelectorActivity_mRvImageList);
        mTvChooseDir = findViewById(R.id.ImageSingleSelectorActivity_mTvChooseDir);
        mRvImageDir = findViewById(R.id.ImageSingleSelectorActivity_mRvImageDirList);
        mMaskView = findViewById(R.id.ImageSingleSelectorActivity_mMaskView);

        mCurrentImagePathList = new ArrayList<>(128);
        mImageDirPath = new ArrayList<>();
        mGroupingMap = new HashMap<>();
        mCurrentShowDir = "";
        mBottomBehavior = BottomSheetBehavior.from(mRvImageDir);
        mLocalSingleImageAdapter = new LocalSingleImageAdapter(mCurrentImagePathList, HORIZONTAL_ITEM_COUNT);
        mImageDirAdapter = new ImageDirAdapter(mImageDirPath, mGroupingMap);
        mMaskColorDrawable = new ColorDrawable(Color.BLACK);
    }

    @Override
    protected void setup() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mRvImage.setLayoutManager(new GridLayoutManager(this, HORIZONTAL_ITEM_COUNT));
        mRvImage.setHasFixedSize(true);
        mRvImage.setAdapter(mLocalSingleImageAdapter);

        mRvImageDir.setLayoutManager(new LinearLayoutManager(this));
        mRvImageDir.setHasFixedSize(true);
        mRvImageDir.setAdapter(mImageDirAdapter);
        mRvImageDir.addOnItemTouchListener(mOnBottomSheetItemClickListener);
        mRvImageDir.addItemDecoration(new SpacesItemDecoration((int) AndroidUtil.dip2px(12)));

        mBottomBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mBottomBehavior.setSkipCollapsed(true);
        mBottomBehavior.setBottomSheetCallback(mBottomSheetCallback);

        mMaskColorDrawable.setAlpha(0);
        mMaskView.setBackground(mMaskColorDrawable);

        mTvChooseDir.setOnClickListener(mOnChooseDirClickListener);

        setData();
    }

    private void setData() {
        new LoadImageAsyncTask(ImageSingleSelectorActivity.this).execute();
    }

    private void loadLocalResource(HashMap<String, List<String>> groupingMap) {
        mGroupingMap.putAll(groupingMap);
        loadDirList();
        setAlbumFolder(null);
    }

    private void loadDirList() {
        mImageDirPath.clear();
        mImageDirPath.addAll(mGroupingMap.keySet());
        mImageDirAdapter.notifyDataSetChanged();
    }

    private void setAlbumFolder(String folder) {
        if ((mCurrentShowDir != null && mCurrentShowDir.equals(folder)) || (folder == null && mCurrentShowDir == null)) {
            return;
        }
        mCurrentShowDir = folder;
        mCurrentImagePathList.clear();
        if (folder == null) {
            for (Map.Entry<String, List<String>> entry : mGroupingMap.entrySet()) {
                mCurrentImagePathList.addAll(entry.getValue());
            }
            mTvChooseDir.setText(R.string.ImageSelectorActivity_AllImage);
        } else if (mGroupingMap.containsKey(folder)) {
            mCurrentImagePathList.addAll(mGroupingMap.get(folder));
            mTvChooseDir.setText(folder.substring(folder.lastIndexOf("/") + 1));
        }
        mLocalSingleImageAdapter.notifyDataSetChanged();
    }

    private final View.OnClickListener mOnChooseDirClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBottomBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                mBottomBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                mBottomBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        }
    };

    private final OnRecyclerViewItemClickListener mOnBottomSheetItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {
            mImageDirAdapter.setSelectedPosition(position);
            if (position == 0) {
                setAlbumFolder(null);
            } else {
                setAlbumFolder(mImageDirPath.get(position - 1));
            }
            mBottomBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
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

    private final View.OnClickListener mOnMaskViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mBottomBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    };


    private static class LoadImageAsyncTask extends AsyncTask<Void, Void, HashMap<String, List<String>>> {

        private WeakReference<ImageSingleSelectorActivity> mWReference;
        private ContentResolver mContentResolver;

        LoadImageAsyncTask(ImageSingleSelectorActivity imageSingleSelectorActivity) {
            mContentResolver = imageSingleSelectorActivity.getContentResolver();
            mWReference = new WeakReference<>(imageSingleSelectorActivity);
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
            ImageSingleSelectorActivity imageSingleSelectorActivity = mWReference.get();
            if (imageSingleSelectorActivity == null || stringListHashMap == null) {
                return;
            }
            imageSingleSelectorActivity.loadLocalResource(stringListHashMap);
        }
    }
}
