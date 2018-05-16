package com.yzx.chat.widget.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.yzx.chat.R;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by YZX on 2018年05月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class MediaControllerView extends FrameLayout {

    private static final int DEFAULT_TIMEOUT = 3000;

    private Context mContext;
    private Handler mHandler;
    private View mRootView;
    private ImageView mIvClose;
    private ImageView mIvPlayControl;
    private TextView mTvCurrentTime;
    private TextView mTvTime;
    private SeekBar mSbProgress;

    private PopupWindow mPopupWindow;
    private View mAnchor;
    private MediaController.MediaPlayerControl mPlayer;

    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    private boolean isShow;
    private boolean isDragging;


    public MediaControllerView(@NonNull Context context) {
        this(context, null);
    }

    public MediaControllerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaControllerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mHandler = new Handler();
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        initControllerView();
        initPopupWindow();
        //   setFocusable(true);
        //   setFocusableInTouchMode(true);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        //  requestFocus();
    }

    private void initControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = inflate.inflate(R.layout.view_media_controller, this, false);
        mIvClose = mRootView.findViewById(R.id.MediaController_mIvClose);
        mIvPlayControl = mRootView.findViewById(R.id.MediaController_mIvPlayControl);
        mTvCurrentTime = mRootView.findViewById(R.id.MediaController_mTvCurrentTime);
        mTvTime = mRootView.findViewById(R.id.MediaController_mTvTime);
        mSbProgress = mRootView.findViewById(R.id.MediaController_mSbProgress);
        mSbProgress.setFocusable(false);
        addView(mRootView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mRootView.setOnClickListener(mOnControllerViewClickListener);
        mIvClose.setOnClickListener(mOnControllerViewClickListener);
        mIvPlayControl.setOnClickListener(mOnControllerViewClickListener);
        mSbProgress.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

        reset();
    }

    private void initPopupWindow() {
        mPopupWindow = new PopupWindow(this, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

    }

    private void reset() {
        if (mRootView != null) {
            mIvPlayControl.setSelected(false);
            mTvCurrentTime.setText(stringForTime(0));
            mTvTime.setText(stringForTime(0));
            mSbProgress.setProgress(0);
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    public void setMediaPlayer(MediaController.MediaPlayerControl mediaPlayer) {
        mPlayer = mediaPlayer;
    }

    public void setAnchorView(View anchor) {
        mAnchor = anchor;
    }

    public void show() {
        show(DEFAULT_TIMEOUT);
    }

    public void show(int timeout) {
        if (!isShow) {
            mPopupWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY, 0, 0);
            setProgress();
        }
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hide();
            }
        }, timeout);
        isShow = true;

    }

    public void hide() {
        if (!isShow) {
            return;
        }
        mPopupWindow.dismiss();
        isShow = false;
    }

    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        mIvPlayControl.setSelected(mPlayer.isPlaying());
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayer == null || isDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (duration > 0) {
            long pos = 1000L * position / duration;
            mSbProgress.setProgress((int) pos);
        }
        int percent = mPlayer.getBufferPercentage();
        mSbProgress.setSecondaryProgress(percent * 10);

        mTvTime.setText(stringForTime(duration));
        mTvCurrentTime.setText(stringForTime(position));
        return position;
    }


    private final View.OnClickListener mOnControllerViewClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.MediaController_mIvClose:
                    break;
                case R.id.MediaController_mIvPlayControl:
                    doPauseResume();
                    break;
                case R.id.MediaController_mLlRootView:
                    hide();
                    break;
            }
        }
    };

    private final SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                return;
            }
            long duration = mPlayer.getDuration();
            long newPosition = (duration * progress) / 1000L;
            mPlayer.seekTo((int) newPosition);
            mTvCurrentTime.setText(stringForTime((int) newPosition));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isDragging = true;
            show(3600000);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isDragging = false;
            show(DEFAULT_TIMEOUT);
        }
    };

}
