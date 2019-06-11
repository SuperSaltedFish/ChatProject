package com.yzx.chat.module;

import android.os.Bundle;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.tool.DirectoryHelper;
import com.yzx.chat.widget.view.RecodeView;


public class TestActivity extends BaseCompatActivity {

    RecodeView mCameraView;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mCameraView = findViewById(R.id.mCameraView);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mCameraView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCameraView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void onClick1(View v) {
        if (mCameraView.isSelected()) {
            mCameraView.stopRecode();
        } else {
            mCameraView.startRecode(DirectoryHelper.getVideoPath() + "/ddd.mp4");
        }
        mCameraView.setSelected(!mCameraView.isSelected());
    }


    public int searchInsert(int[] nums, int target) {
        if (nums == null || nums.length == 0) {
            return -1;
        }
        int length = nums.length;
        boolean isAsc = nums[0] <= nums[length - 1];
        if (isAsc) {
            if (nums[0] > target) {
                return 0;
            }
            if (nums[length - 1] < target) {
                return length;
            }
        } else {
            if (nums[0] < target) {
                return 0;
            }
            if (nums[length - 1] > target) {
                return length;
            }
        }
        return search(nums, 0, length - 1, target, isAsc);
    }

    private int search(int[] nums, int start, int end, int target, boolean isAsc) {
        if (isAsc) {
            if (start > end) {
                return start;
            }
            int index = (start + end) / 2;
            if (nums[index] > target) {
                return search(nums, start, index - 1, target, isAsc);
            } else if (nums[index] < target) {
                return search(nums, index + 1, end, target, isAsc);
            } else return index;
        } else {
            if (start > end) {
                return end;
            }
            int index = (start + end) / 2;
            if (nums[index] > target) {
                return search(nums, index + 1, end, target, isAsc);
            } else if (nums[index] < target) {
                return search(nums, start, index - 1, target, isAsc);
            } else return index;
        }
    }
}




