package com.yzx.chat.base;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.DialogFragment;
import cn.swiftpass.standardwallet.R;
import cn.swiftpass.standardwallet.core.listener.Cancelable;
import cn.swiftpass.standardwallet.widget.dialog.ErrorDialog;
import cn.swiftpass.standardwallet.widget.dialog.ProgressDialog;

/**
 * Created by 叶智星 on 2018年09月20日.
 * 每一个不曾起舞的日子，都是对生命的辜负。
 */
public abstract class BaseDialogFragment<P extends BasePresenter> extends DialogFragment {
    @LayoutRes
    protected abstract int getLayoutID();

    protected abstract void init(View parentView);

    protected abstract void setup(Bundle savedInstanceState);

    protected P mPresenter;

    public Context mContext;

    protected ProgressDialog mProgressDialog;
    protected ErrorDialog mErrorDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogFragmentStyle);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setWindowsGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            container = new FrameLayout(mContext);
        }
        return inflater.inflate(getLayoutID(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initPresenter();
        init(view);
        setup(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mPresenter != null) {
            mPresenter.detachView();
            mPresenter = null;
        }
        if (mErrorDialog != null) {
            mErrorDialog.setOnDismissListener(null);
            mErrorDialog.dismiss();
            mErrorDialog = null;
        }
        if (mProgressDialog != null) {
            mProgressDialog.setOnCancelListener(null);
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }


    @Override
    public void onPause() {
        super.onPause();
        setWindowAnimations(R.style.BottomDialogFragmentDismissAnimStyle);
    }

    protected void setWindowLayout(int width, int height) {
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(width, height);
            }
        }
    }

    protected void setWindowAnimations(@StyleRes int res) {
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setWindowAnimations(res);
            }
        }
    }

    protected void setEnableWindowDimBehind(boolean isEnableDimBehind) {
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                if (isEnableDimBehind) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                }
            }
        }
    }

    protected void setWindowsBackgroundDrawable(Drawable drawable) {
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(drawable);
            }
        }
    }

    protected void setWindowsGravity(int gravity) {
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setGravity(gravity);
            }
        }
    }


    public void showToast(String content) {
        showToast(content, Toast.LENGTH_SHORT);
    }

    public void showLongToast(String content) {
        showToast(content, Toast.LENGTH_LONG);
    }

    public void showToast(String content, int duration) {
        Toast.makeText(mContext, content, duration).show();
    }


    public void setEnableLoading(boolean isEnable) {
        setEnableLoading(isEnable, null);
    }

    public void setEnableLoading(boolean isEnable, final Cancelable cancelable) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext, getString(R.string.Hint_Loading));
        }
        if (isEnable) {
            if (cancelable != null) {
                mProgressDialog.setCancelable(true);
                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        cancelable.cancel();
                    }
                });
            } else {
                mProgressDialog.setCancelable(false);
                mProgressDialog.setOnCancelListener(null);
            }
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        } else {
            mProgressDialog.setOnCancelListener(null);
            mProgressDialog.dismiss();
        }
    }

    public void showErrorDialog(String error) {
        showErrorDialog(error, null);
    }

    public void showErrorDialog(String error, DialogInterface.OnDismissListener listener) {
        if (mErrorDialog == null) {
            mErrorDialog = new ErrorDialog(mContext);
        }
        mErrorDialog.setOnDismissListener(listener);
        mErrorDialog.setContentText(error);
        if (!mErrorDialog.isShowing()) {
            mErrorDialog.show();
        }
    }

    public boolean isAttachedToPresenter() {
        return mPresenter != null;
    }

    @SuppressWarnings("unchecked")
    private void initPresenter() {
        if (this instanceof BaseView) {
            BaseView view = (BaseView) this;
            mPresenter = (P) view.getPresenter();
            if (mPresenter == null) {
                return;
            }
            Class aClass = this.getClass();
            while (aClass != null) {
                Type type = aClass.getGenericSuperclass();
                if (type instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    Type genericType = parameterizedType.getActualTypeArguments()[0];
                    Class<?>[] interfaces = mPresenter.getClass().getInterfaces();
                    for (Class c : interfaces) {
                        if (c == genericType) {
                            mPresenter.attachView(view);
                            return;
                        }
                    }
                } else {
                    aClass = aClass.getSuperclass();
                }
            }
            mPresenter = null;
        }
    }
}
