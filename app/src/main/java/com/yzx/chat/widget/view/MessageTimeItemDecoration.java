package com.yzx.chat.widget.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.SparseLongArray;
import android.view.View;

import com.yzx.chat.configure.Constants;
import com.yzx.chat.core.util.LogUtil;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.rong.imlib.model.Message;

/**
 * Created by YZX on 2019年05月05日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class MessageTimeItemDecoration extends RecyclerView.ItemDecoration {

    private List<Message> mMessageList;
    private SparseLongArray mTimeDisplayStateArray;

    private FormatAdapter mFormatAdapter;

    private TextPaint mTextPaint;
    private float mTextHeight;
    private int mDecorationHeight;

    public MessageTimeItemDecoration(@NonNull RecyclerView.Adapter adapter, List<Message> messageList) {
        mMessageList = messageList;
        mTimeDisplayStateArray = new SparseLongArray(32);
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        setTextSize(mTextPaint.getTextSize());
        registerAdapterDataObserver(adapter);
    }


    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        RecyclerView.LayoutManager manager = parent.getLayoutManager();
        if (manager == null || mFormatAdapter == null || mDecorationHeight == 0) {
            return;
        }
        int position = manager.getPosition(view);
        LogUtil.e(position+"  "+view);
        if (position >= mMessageList.size()) {//有footer的时候
            return;
        }
        Message message = mMessageList.get(position);
        if (mTimeDisplayStateArray.get(message.getMessageId(), -1) != -1) {
            outRect.top = mDecorationHeight;
        } else {
            outRect.top = 0;
        }
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        RecyclerView.LayoutManager manager = parent.getLayoutManager();
        if (manager == null || mFormatAdapter == null || mDecorationHeight == 0) {
            return;
        }
        for (int index = 0, childCount = parent.getChildCount(); index < childCount; index++) {
            View v = parent.getChildAt(index);
            int position = manager.getPosition(v);
            if (position >= mMessageList.size()) {//有footer的时候
                return;
            }
            Message message = mMessageList.get(position);
            long time = mTimeDisplayStateArray.get(message.getMessageId(), -1);
            if (time != -1) {
                String strTime = mFormatAdapter.formatTimeToString(time);
                float x = parent.getWidth() / 2f;
                float y = v.getTop() - (mDecorationHeight - mTextHeight) / 2;
                c.drawText(strTime, x, y, mTextPaint);
            }
        }
    }

    private void registerAdapterDataObserver(RecyclerView.Adapter adapter) {
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (mMessageList == null || mMessageList.size() <= positionStart) {
                    return;
                }
                int listSize = mMessageList.size();
                if (itemCount == 1) {
                    Message message = mMessageList.get(positionStart);
                    if (positionStart == listSize - 1) {
                        mTimeDisplayStateArray.put(message.getMessageId(), getMessageTime(message));
                    } else {
                        long latestTime = mTimeDisplayStateArray.get(mTimeDisplayStateArray.keyAt(mTimeDisplayStateArray.size() - 1), 0);
                        if (Math.abs(getMessageTime(message) - latestTime) >= Constants.CHAT_MESSAGE_TIME_DISPLAY_INTERVAL) {
                            mTimeDisplayStateArray.put(message.getMessageId(), getMessageTime(message));
                        }
                    }
                } else {
                    if (positionStart != 0) {
                        positionStart--;
                    }
                    long latestTime;
                    if (positionStart + itemCount == listSize) {//从最末尾插入
                        latestTime = 0;
                    } else { //从中间插入
                        latestTime = getMessageTime(mMessageList.get(positionStart + itemCount));
                    }
                    for (int i = positionStart + itemCount - 1; i >= positionStart; i--) {
                        Message message = mMessageList.get(i);
                        long messageTime = getMessageTime(message);
                        if (Math.abs(latestTime - messageTime) >= Constants.CHAT_MESSAGE_TIME_DISPLAY_INTERVAL) {
                            mTimeDisplayStateArray.append(message.getMessageId(), messageTime);
                            latestTime = messageTime;
                        }
                    }
                }
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                for (int i = toPosition, count = toPosition + itemCount; i < count; i++) {
                    mTimeDisplayStateArray.delete(mMessageList.get(i).getMessageId());
                }
                onItemRangeRemoved(fromPosition, itemCount);
                onItemRangeInserted(toPosition, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if (mMessageList == null || mMessageList.size() == 0) {
                    mTimeDisplayStateArray.clear();
                    return;
                }
                if (mMessageList.size() < positionStart || positionStart == 0) {
                    return;
                }
                int start = positionStart - 1;
                Message message = mMessageList.get(start);
                if (positionStart == mMessageList.size()) {
                    mTimeDisplayStateArray.put(message.getMessageId(), getMessageTime(message));
                } else {
                    Message nextItem = mMessageList.get(start + 1);
                    if (Math.abs(getMessageTime(message) - getMessageTime(nextItem)) >= Constants.CHAT_MESSAGE_TIME_DISPLAY_INTERVAL) {
                        mTimeDisplayStateArray.put(message.getMessageId(), getMessageTime(message));
                    }
                }
            }
        });
    }


    public void setTextSize(float textSize) {
        mTextPaint.setTextSize(textSize);
        Paint.FontMetrics metrics = mTextPaint.getFontMetrics();
        mTextHeight = metrics.bottom - metrics.top;
    }

    public void setTextColor(int textColor) {
        mTextPaint.setColor(textColor);
    }

    public void setDecorationHeight(int decorationHeight) {
        mDecorationHeight = decorationHeight;
    }

    public void setFormatAdapter(FormatAdapter formatAdapter) {
        mFormatAdapter = formatAdapter;
    }

    public interface FormatAdapter {
        String formatTimeToString(long milliseconds);
    }

    public static long getMessageTime(Message message) {
        return message.getMessageDirection() == Message.MessageDirection.SEND ? message.getSentTime() : message.getReceivedTime();
    }
}
