package com.yzx.chat.view.activity;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.widget.adapter.ImageDirAdapter;
import com.yzx.chat.widget.adapter.LocalMultiImageAdapter;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.SpacesItemDecoration;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ImageMultiSelectorActivity extends BaseCompatActivity {

    public static final int RESULT_CODE = 1000;
    public static final String INTENT_EXTRA_IMAGE_PATH_LIST = "ImagePathList";
    public static final String INTENT_EXTRA_IS_ORIGINAL = "IsOriginal";

    private static final int HORIZONTAL_ITEM_COUNT = 4;

    private RecyclerView mRvImage;
    private RecyclerView mRvImageDir;
    private RadioButton mRBtnOriginal;
    private Button mBtnPreview;
    private LocalMultiImageAdapter mLocalMultiImageAdapter;
    private BottomSheetBehavior mBottomBehavior;
    private ImageDirAdapter mImageDirAdapter;
    private View mMaskView;
    private MenuItem mSendMenuItem;
    private String mCurrentShowDir;
    private ColorDrawable mMaskColorDrawable;
    private HashMap<String, List<String>> mGroupingMap;
    private ArrayList<String> mCurrentImagePathList;
    private ArrayList<String> mImageDirPath;
    private ArrayList<String> mSelectedList;

    private TextView mTvChooseDir;
    private boolean isOriginal;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_image_multi_selector;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mRvImage = findViewById(R.id.ImageMultiSelectorActivity_mRvImageList);
        mRBtnOriginal = findViewById(R.id.ImageMultiSelectorActivity_mRBtnOriginal);
        mTvChooseDir = findViewById(R.id.ImageMultiSelectorActivity_mTvChooseDir);
        mRvImageDir = findViewById(R.id.ImageMultiSelectorActivity_mRvImageDirList);
        mBtnPreview = findViewById(R.id.ImageMultiSelectorActivity_mBtnPreview);
        mMaskView = findViewById(R.id.ImageMultiSelectorActivity_mMaskView);

        mCurrentImagePathList = new ArrayList<>(128);
        mImageDirPath = new ArrayList<>();
        mSelectedList = new ArrayList<>();
        mGroupingMap = new HashMap<>();
        mCurrentShowDir = "";
        mBottomBehavior = BottomSheetBehavior.from(mRvImageDir);
        mLocalMultiImageAdapter = new LocalMultiImageAdapter(mCurrentImagePathList, mSelectedList, HORIZONTAL_ITEM_COUNT);
        mImageDirAdapter = new ImageDirAdapter(mImageDirPath, mGroupingMap);
        mMaskColorDrawable = new ColorDrawable(Color.BLACK);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mRvImage.setLayoutManager(new GridLayoutManager(this, HORIZONTAL_ITEM_COUNT));
        mRvImage.setHasFixedSize(true);
        mRvImage.setAdapter(mLocalMultiImageAdapter);

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

        mLocalMultiImageAdapter.setOnImageItemChangeListener(mOnImageItemChangeListener);

        mRBtnOriginal.setOnClickListener(mOnOriginalClickListener);

        mBtnPreview.setOnClickListener(mOnPreviewClickListener);

        setData();
    }


    private void setData() {
        new LoadImageAsyncTask(ImageMultiSelectorActivity.this).execute();
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
        mSelectedList.clear();
        mSendMenuItem.setTitle(R.string.ImageSelectorActivity_Send);
        if (folder == null) {
            for (Map.Entry<String, List<String>> entry : mGroupingMap.entrySet()) {
                mCurrentImagePathList.addAll(entry.getValue());
            }
            mTvChooseDir.setText(R.string.ImageSelectorActivity_AllImage);
        } else if (mGroupingMap.containsKey(folder)) {
            mCurrentImagePathList.addAll(mGroupingMap.get(folder));
            mTvChooseDir.setText(folder.substring(folder.lastIndexOf("/") + 1));
        }
        mLocalMultiImageAdapter.notifyDataSetChanged();
    }

    private void updateCountText() {
        int selectedCount = mSelectedList.size();
        if (selectedCount > 0) {
            mSendMenuItem.setTitle(String.format(Locale.getDefault(), "%s(%d/%d)", getString(R.string.ImageSelectorActivity_Send), mSelectedList.size(), mCurrentImagePathList.size()));
            mBtnPreview.setText(String.format(Locale.getDefault(), "%s(%d)", getString(R.string.ImageSelectorActivity_Preview), mSelectedList.size()));
            mSendMenuItem.setEnabled(true);
            mBtnPreview.setEnabled(true);
        } else {
            mSendMenuItem.setTitle(R.string.ImageSelectorActivity_Send);
            mBtnPreview.setText(R.string.ImageSelectorActivity_Preview);
            mSendMenuItem.setEnabled(false);
            mBtnPreview.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_multi_selector, menu);
        mSendMenuItem = menu.findItem(R.id.ImageMultiSelectorMenu_Send);
        updateCountText();
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ImageMultiSelectorMenu_Send) {
            if (mSelectedList.size() != 0) {
                Intent intent = new Intent();
                intent.putStringArrayListExtra(INTENT_EXTRA_IMAGE_PATH_LIST, mSelectedList);
                intent.putExtra(INTENT_EXTRA_IS_ORIGINAL, isOriginal);
                setResult(RESULT_CODE, intent);
            }
            finish();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImageViewPagerActivity.RESULT_CODE && data != null) {
            ArrayList<String> selectList = data.getStringArrayListExtra(ImageViewPagerActivity.INTENT_EXTRA_IMAGE_SELECTED_LIST);
            isOriginal = data.getBooleanExtra(ImageViewPagerActivity.INTENT_EXTRA_IS_ORIGINAL, isOriginal);
            mRBtnOriginal.setChecked(isOriginal);
            if (selectList != null) {
                mSelectedList.clear();
                mSelectedList.addAll(selectList);
                mLocalMultiImageAdapter.notifyDataSetChanged();
            }
        }
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

    private final View.OnClickListener mOnMaskViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mBottomBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    };

    private final View.OnClickListener mOnOriginalClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isOriginal = !isOriginal;
            mRBtnOriginal.setChecked(isOriginal);
        }
    };

    private final View.OnClickListener mOnPreviewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ImageMultiSelectorActivity.this, ImageViewPagerActivity.class);
            intent.putStringArrayListExtra(ImageViewPagerActivity.INTENT_EXTRA_IMAGE_LIST, mSelectedList);
            intent.putStringArrayListExtra(ImageViewPagerActivity.INTENT_EXTRA_IMAGE_SELECTED_LIST, mSelectedList);
            intent.putExtra(ImageViewPagerActivity.INTENT_EXTRA_CURRENT_POSITION, 0);
            intent.putExtra(ImageViewPagerActivity.INTENT_EXTRA_IS_ORIGINAL, isOriginal);
            startActivityForResult(intent, 0);
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


    private final LocalMultiImageAdapter.OnImageItemChangeListener mOnImageItemChangeListener = new LocalMultiImageAdapter.OnImageItemChangeListener() {
        @Override
        public void onItemSelect(int position, boolean isSelect) {
            if (isSelect) {
                if (!mSelectedList.contains(mCurrentImagePathList.get(position))) {
                    mSelectedList.add(mCurrentImagePathList.get(position));
                }
            } else {
                mSelectedList.remove(mCurrentImagePathList.get(position));
            }
            updateCountText();
        }

        @Override
        public void onItemClick(int position) {
            Intent intent = new Intent(ImageMultiSelectorActivity.this, ImageViewPagerActivity.class);
            intent.putStringArrayListExtra(ImageViewPagerActivity.INTENT_EXTRA_IMAGE_LIST, mCurrentImagePathList);
            intent.putStringArrayListExtra(ImageViewPagerActivity.INTENT_EXTRA_IMAGE_SELECTED_LIST, mSelectedList);
            intent.putExtra(ImageViewPagerActivity.INTENT_EXTRA_CURRENT_POSITION, position);
            intent.putExtra(ImageViewPagerActivity.INTENT_EXTRA_IS_ORIGINAL, isOriginal);
            startActivityForResult(intent, 0);
        }
    };

    private static class LoadImageAsyncTask extends AsyncTask<Void, Void, HashMap<String, List<String>>> {

        private WeakReference<ImageMultiSelectorActivity> mWReference;
        private ContentResolver mContentResolver;

        LoadImageAsyncTask(ImageMultiSelectorActivity imageMultiSelectorActivity) {
            mContentResolver = imageMultiSelectorActivity.getContentResolver();
            mWReference = new WeakReference<>(imageMultiSelectorActivity);
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
            ImageMultiSelectorActivity imageMultiSelectorActivity = mWReference.get();
            if (imageMultiSelectorActivity == null || stringListHashMap == null) {
                return;
            }
            imageMultiSelectorActivity.loadLocalResource(stringListHashMap);
        }
    }
}
