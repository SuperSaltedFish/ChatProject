package com.yzx.chat.module.contact.view;

import android.graphics.Color;
import android.os.Bundle;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.util.AndroidHelper;
import com.yzx.chat.widget.view.SegmentedControlView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

public class NotificationMessageActivity extends BaseCompatActivity {

    private SegmentedControlView mSegmentedControlView;
    private ContactOperationFragment mContactOperationFragment;
    private SystemMessageFragment mSystemMessageFragment;
    private FragmentManager mFragmentManager;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_notification_message;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mSegmentedControlView = findViewById(R.id.NotificationMessageActivity_mSegmentedControlView);
        mContactOperationFragment = new ContactOperationFragment();
        mSystemMessageFragment = new SystemMessageFragment();
        mFragmentManager = getSupportFragmentManager();
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        int paddingLeftRight = (int) AndroidHelper.dip2px(10);
        int paddingTopBottom = (int) AndroidHelper.dip2px(5);
        mSegmentedControlView
                .setItems(getResources().getStringArray(R.array.NotificationMessageActivity_Subtitle))
                .setColors(Color.WHITE, ContextCompat.getColor(this, R.color.colorAccent))
                .setTextSize(AndroidHelper.sp2px(14))
                .setItemPadding(paddingLeftRight, paddingTopBottom, paddingLeftRight, paddingTopBottom)
                .setRadius(AndroidHelper.dip2px(4))
                .setDefaultSelectedPosition(0)
                .setOnSelectionChangedListener(mOnSelectedChangedListener)
                .update();

        mFragmentManager.beginTransaction()
                .add(R.id.NotificationMessageActivity_mFlContent, mContactOperationFragment)
                .add(R.id.NotificationMessageActivity_mFlContent, mSystemMessageFragment)
                .hide(mSystemMessageFragment)
                .commit();
    }

    private final SegmentedControlView.OnSelectedChangedListener mOnSelectedChangedListener = new SegmentedControlView.OnSelectedChangedListener() {
        @Override
        public void onSelected(int position, String text) {
            switch (position) {
                case 0:
                    mFragmentManager.beginTransaction()
                            .show(mContactOperationFragment)
                            .hide(mSystemMessageFragment)
                            .commit();
                    break;
                case 1:
                    mFragmentManager.beginTransaction()
                            .show(mSystemMessageFragment)
                            .hide(mContactOperationFragment)
                            .commit();
                    break;
            }
        }
    };
}
