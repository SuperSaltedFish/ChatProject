package com.yzx.chat.widget.view;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.widget.listener.OnOnlySingleClickListener;

import java.util.Formatter;
import java.util.Locale;

import androidx.annotation.NonNull;

/**
 * Created by YZX on 2018年05月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class MediaControllerPopupWindow extends PopupWindow {

    private static final int DEFAULT_TIMEOUT = 5000;

    private Context mContext;
    private Handler mHandler;
    private View mRootView;
    private ImageView mIvClose;
    private ImageView mIvPlayControl;
    private TextView mTvCurrentTime;
    private TextView mTvTime;
    private SeekBar mSbProgress;

    private View mAnchor;
    private MediaController.MediaPlayerControl mPlayer;
    private View.OnClickListener mOnCloseClickListener;

    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    private boolean isShow;
    private boolean isDragging;


    public MediaControllerPopupWindow(@NonNull Context context) {
        super(context);
        mContext = context;
        mHandler = new Handler();
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        initControllerView();
        initPopupWindow();
    }


    private void initControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = inflate.inflate(R.layout.view_media_controller, null, false);
        mIvClose = mRootView.findViewById(R.id.MediaController_mIvClose);
        mIvPlayControl = mRootView.findViewById(R.id.MediaController_mIvPlayControl);
        mTvCurrentTime = mRootView.findViewById(R.id.MediaController_mTvCurrentTime);
        mTvTime = mRootView.findViewById(R.id.MediaController_mTvTime);
        mSbProgress = mRootView.findViewById(R.id.MediaController_mSbProgress);
        mSbProgress.setFocusable(false);
        mSbProgress.setMax(1000);
        mRootView.setOnClickListener(mOnControllerViewClickListener);
        mIvClose.setOnClickListener(mOnControllerViewClickListener);
        mIvPlayControl.setOnClickListener(mOnControllerViewClickListener);
        mSbProgress.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        setContentView(mRootView);
        reset();
    }

    private void initPopupWindow() {
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        setBackgroundDrawable(null);
        setClippingEnabled(false);//允许弹出窗口超出屏幕范围
    }

    private void reset() {
        mIvPlayControl.setSelected(false);
        mTvCurrentTime.setText(stringForTime(0));
        mTvTime.setText(stringForTime(0));
        mSbProgress.setProgress(0);
        mHandler.removeCallbacksAndMessages(null);
    }

    private String stringForTime(int second) {

        int seconds = second % 60;
        int minutes = (second / 60) % 60;
        int hours = second / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private void doPauseResume() {
        if (mPlayer == null) {
            return;
        }
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        show();
    }

    private void setProgressAndState() {
        if (mPlayer == null || isDragging) {
            return;
        }
        int currentSecond = mPlayer.getCurrentPosition()/1000;
        int durationSecond = mPlayer.getDuration()/1000;
        if(durationSecond==0){
            durationSecond = 1;
        }
        if(mSbProgress.getMax()!=durationSecond){
            mSbProgress.setMax(durationSecond);
        }
        mSbProgress.setProgress(currentSecond);
        mTvTime.setText(stringForTime(durationSecond));
        mTvCurrentTime.setText(stringForTime(currentSecond));
        mIvPlayControl.setSelected(mPlayer.isPlaying());
    }

    public void show() {
        show(DEFAULT_TIMEOUT);
    }

    public void show(int timeout) {
        if (mAnchor == null) {
            return;
        }
        if (!isShow) {
            showAtLocation(mAnchor, Gravity.NO_GRAVITY, 0, 0);
            setProgressAndState();
        }
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(mHideRunnable, timeout);
        mHandler.post(mUpdateProgressRunnable);
        isShow = true;
    }

    public void hide() {
        if (!isShow) {
            return;
        }
        dismiss();
        isShow = false;
        mHandler.removeCallbacksAndMessages(null);
    }

    public void setMediaPlayer(MediaController.MediaPlayerControl mediaPlayer) {
        mPlayer = mediaPlayer;
    }

    public void setAnchorView(View anchor) {
        if (mAnchor != null) {
            mAnchor.setOnTouchListener(null);
        }
        mAnchor = anchor;
        if (mAnchor != null) {
            mAnchor.setOnTouchListener(mOnAnchorTouchListener);
        }
    }

    public void setOnCloseClickListener(View.OnClickListener onCloseClickListener) {
        mOnCloseClickListener = onCloseClickListener;
    }

    private final View.OnClickListener mOnControllerViewClickListener = new OnOnlySingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.MediaController_mIvClose:
                    if(mOnCloseClickListener!=null){
                        mOnCloseClickListener.onClick(v);
                    }
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

    private final View.OnTouchListener mOnAnchorTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            show();
            return false;
        }
    };

    private final SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser || mPlayer == null) {
                return;
            }
            mPlayer.seekTo(progress*1000);
            mTvCurrentTime.setText(stringForTime( progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (mPlayer != null) {
                mPlayer.pause();
            }
            isDragging = true;
            show(3600000);
            mHandler.removeCallbacks(mUpdateProgressRunnable);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mPlayer != null) {
                mPlayer.start();
            }
            isDragging = false;
            show();

        }
    };

    private final Runnable mUpdateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            setProgressAndState();
            if (!isDragging && isShow && mPlayer != null && mPlayer.isPlaying()) {
                mHandler.postDelayed(mUpdateProgressRunnable, 1000);
            }
        }
    };

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

}
