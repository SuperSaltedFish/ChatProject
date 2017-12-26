package com.yzx.chat.base;

import android.support.v7.util.DiffUtil;

import java.util.List;

/**
 * Created by YZX on 2017年11月28日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public abstract class DiffCalculate<T> extends DiffUtil.Callback {

    public abstract boolean isItemEquals(T oldItem, T newItem);

    public abstract boolean isContentsEquals(T oldItem, T newItem);

    private List<T> mNewData;
    private List<T> mOldData;

    public DiffCalculate(List<T> oldData, List<T> newData) {
        this.mOldData = oldData;
        this.mNewData = newData;
    }

    @Override
    public int getOldListSize() {
        return mOldData == null ? 0 : mOldData.size();
    }

    @Override
    public int getNewListSize() {
        return mNewData == null ? 0 : mNewData.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return isItemEquals(mOldData.get(oldItemPosition), mNewData.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return isContentsEquals(mOldData.get(oldItemPosition), mNewData.get(newItemPosition));
    }
}
