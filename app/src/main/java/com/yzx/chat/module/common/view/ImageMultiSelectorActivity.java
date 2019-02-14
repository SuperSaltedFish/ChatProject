package com.yzx.chat.module.common.view;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.widget.adapter.ImageDirAdapter;
import com.yzx.chat.widget.adapter.LocalMultiImageAdapter;
import com.yzx.chat.widget.listener.ImageAutoLoadScrollListener;
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

    public static final int RESULT_CODE = ImageMultiSelectorActivity.class.hashCode();
    public static final String INTENT_EXTRA_IMAGE_PATH_LIST = "ImagePathList";
    public static final String INTENT_EXTRA_IS_ORIGINAL = "IsOriginal";

    private static final int HORIZONTAL_ITEM_COUNT = 4;
    private static final int MAX_SELECTED_COUNT = Constants.MAX_ONCE_IMAGE_SEND_COUNT;

    private RecyclerView mRvImage;
    private RecyclerView mRvImageDir;
    private Switch mOriginalSwitch;
    private Button mBtnPreview;
    private TextView mTvConfirm;
    private LocalMultiImageAdapter mLocalMultiImageAdapter;
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
    private boolean isOriginal;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_image_multi_selector;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mRvImage = findViewById(R.id.ImageMultiSelectorActivity_mRvImageList);
        mOriginalSwitch = findViewById(R.id.ImageMultiSelectorActivity_mOriginalSwitch);
        mTvChooseDir = findViewById(R.id.ImageMultiSelectorActivity_mTvChooseDir);
        mRvImageDir = findViewById(R.id.ImageMultiSelectorActivity_mRvImageDirList);
        mBtnPreview = findViewById(R.id.ImageMultiSelectorActivity_mBtnPreview);
        mMaskView = findViewById(R.id.ImageMultiSelectorActivity_mMaskView);
        mTvConfirm = findViewById(R.id.ImageMultiSelectorActivity_mTvConfirm);

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
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this,R.color.backgroundColorGrey)));
        mRvImage.setLayoutManager(new GridLayoutManager(this, HORIZONTAL_ITEM_COUNT));
        mRvImage.setHasFixedSize(true);
        mRvImage.setAdapter(mLocalMultiImageAdapter);
        mRvImage.addOnScrollListener(new ImageAutoLoadScrollListener());

        mRvImageDir.setLayoutManager(new LinearLayoutManager(this));
        mRvImageDir.setAdapter(mImageDirAdapter);
        mRvImageDir.addOnItemTouchListener(mOnBottomSheetItemClickListener);
        mRvImageDir.addItemDecoration(new SpacesItemDecoration((int) AndroidHelper.dip2px(12)));

        mBottomBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mBottomBehavior.setSkipCollapsed(true);
        mBottomBehavior.setBottomSheetCallback(mBottomSheetCallback);

        mMaskColorDrawable.setAlpha(0);
        mMaskView.setBackground(mMaskColorDrawable);

        mTvChooseDir.setOnClickListener(mOnChooseDirClickListener);
        mLocalMultiImageAdapter.setOnImageItemChangeListener(mOnImageItemChangeListener);
        mOriginalSwitch.setOnCheckedChangeListener(mOnOriginalSwitchChangeListener);
        mBtnPreview.setOnClickListener(mOnPreviewClickListener);
        mTvConfirm.setOnClickListener(mOnConfirmClickListener);

        setData();

        updateCountText();
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
        mTvConfirm.setText(R.string.ImageSelectorActivity_Send);
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
            mTvConfirm.setText(String.format(Locale.getDefault(), "%s(%d/%d)", getString(R.string.ImageSelectorActivity_Send), mSelectedList.size(), MAX_SELECTED_COUNT));
            mBtnPreview.setText(String.format(Locale.getDefault(), "%s(%d)", getString(R.string.ImageSelectorActivity_Preview), mSelectedList.size()));
            mTvConfirm.setEnabled(true);
            mBtnPreview.setEnabled(true);
        } else {
            mTvConfirm.setText(R.string.ImageSelectorActivity_Send);
            mBtnPreview.setText(R.string.ImageSelectorActivity_Preview);
            mTvConfirm.setEnabled(false);
            mBtnPreview.setEnabled(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImageViewPagerActivity.RESULT_CODE && data != null) {
            ArrayList<String> selectList = data.getStringArrayListExtra(ImageViewPagerActivity.INTENT_EXTRA_IMAGE_SELECTED_LIST);
            isOriginal = data.getBooleanExtra(ImageViewPagerActivity.INTENT_EXTRA_IS_ORIGINAL, isOriginal);
            mOriginalSwitch.setChecked(isOriginal);
            if (selectList != null) {
                mSelectedList.clear();
                mSelectedList.addAll(selectList);
                mLocalMultiImageAdapter.notifyDataSetChanged();
            }
            if (data.getBooleanExtra(ImageViewPagerActivity.INTENT_EXTRA_IS_SENDING, false)) {
                mOnConfirmClickListener.onClick(null);
            }
        }
    }

    private final View.OnClickListener mOnConfirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mSelectedList.size() != 0) {
                Intent intent = new Intent();
                intent.putStringArrayListExtra(INTENT_EXTRA_IMAGE_PATH_LIST, mSelectedList);
                intent.putExtra(INTENT_EXTRA_IS_ORIGINAL, isOriginal);
                setResult(RESULT_CODE, intent);
            }
            finish();
        }
    };

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

    private final CompoundButton.OnCheckedChangeListener mOnOriginalSwitchChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            isOriginal = isChecked;
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
            intent.putExtra(ImageViewPagerActivity.INTENT_EXTRA_MAX_SELECTED_COUNT, MAX_SELECTED_COUNT);
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
        public boolean onItemSelect(int position, boolean isSelect) {
            if (isSelect) {
                if (!mSelectedList.contains(mCurrentImagePathList.get(position))) {
                    if (mSelectedList.size() >= MAX_SELECTED_COUNT) {
                        showToast(String.format(getString(R.string.ImageMultiSelectorActivity_SelectedCountLimitHint), MAX_SELECTED_COUNT));
                        return false;
                    } else {
                        mSelectedList.add(mCurrentImagePathList.get(position));
                    }
                }
            } else {
                mSelectedList.remove(mCurrentImagePathList.get(position));
            }
            updateCountText();
            return true;
        }

        @Override
        public void onItemClick(int position) {
            Intent intent = new Intent(ImageMultiSelectorActivity.this, ImageViewPagerActivity.class);
            intent.putStringArrayListExtra(ImageViewPagerActivity.INTENT_EXTRA_IMAGE_LIST, mCurrentImagePathList);
            intent.putStringArrayListExtra(ImageViewPagerActivity.INTENT_EXTRA_IMAGE_SELECTED_LIST, mSelectedList);
            intent.putExtra(ImageViewPagerActivity.INTENT_EXTRA_CURRENT_POSITION, position);
            intent.putExtra(ImageViewPagerActivity.INTENT_EXTRA_IS_ORIGINAL, isOriginal);
            intent.putExtra(ImageViewPagerActivity.INTENT_EXTRA_MAX_SELECTED_COUNT, MAX_SELECTED_COUNT);
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
