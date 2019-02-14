package com.yzx.chat.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.yzx.chat.R;
import com.yzx.chat.widget.dialog.ErrorDialog;
import com.yzx.chat.widget.dialog.ProgressDialog;
import com.yzx.chat.widget.listener.Cancelable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


/**
 * Created by YZX on 2017年06月12日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public abstract class BaseFragment<P extends BasePresenter> extends Fragment {

    @LayoutRes
    protected abstract int getLayoutID();

    protected abstract void init(View parentView);

    protected abstract void setup(Bundle savedInstanceState);

    protected P mPresenter;

    public Context mContext;
    protected InputMethodManager mInputManager;
    private boolean isOnceVisible;
    protected ProgressDialog mProgressDialog;
    protected ErrorDialog mErrorDialog;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    protected void onFirstVisible() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutID(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initPresenter();
        init(view);
        setup(savedInstanceState);
        if (getUserVisibleHint() && !isOnceVisible) {
            isOnceVisible = true;
            onFirstVisible();
        }
    }


    @CallSuper
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mPresenter != null) {
            mPresenter.detachView();
            mPresenter = null;
        }
        if (mErrorDialog != null) {
            mErrorDialog.dismiss();
        }
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isOnceVisible && isVisibleToUser) {
            isOnceVisible = true;
            onFirstVisible();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isNeedShowMissingPermissionDialog = false;
        boolean result = true;
        ArrayList<String> deniedPermissions = null;
        for (int i = 0, length = permissions.length; i < length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                result = false;
                if (!shouldShowRequestPermissionRationale(permissions[i])) {
                    isNeedShowMissingPermissionDialog = true;
                }
                if (deniedPermissions == null) {
                    deniedPermissions = new ArrayList<>();
                }
                deniedPermissions.add(permissions[i]);
            }
        }
        if (result) {
            onRequestPermissionsResult(requestCode, true, null);
        } else if (isNeedShowMissingPermissionDialog) {
            showMissingPermissionDialog(requestCode, deniedPermissions.toArray(new String[0]));
        } else {
            onRequestPermissionsResult(requestCode, false, deniedPermissions.toArray(new String[0]));
        }
    }

    protected void onRequestPermissionsResult(int requestCode, boolean isSuccess, String[] deniedPermissions) {

    }

    public void requestPermissionsInCompatMode(@NonNull String[] permissions, int requestCode) {
        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }

        int size = permissionList.size();
        if (size != 0) {
            requestPermissions(permissionList.toArray(new String[size]), requestCode);
        } else {
            onRequestPermissionsResult(requestCode, true, null);
        }
    }

    private void showMissingPermissionDialog(final int requestCode, final String[] deniedPermissions) {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.PermissionDialog_Help)
                .setMessage(R.string.PermissionDialog_MissPermissionHint)
                .setNegativeButton(R.string.PermissionDialog_Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onRequestPermissionsResult(requestCode, false, deniedPermissions);
                    }
                })
                .setPositiveButton(R.string.PermissionDialog_Setting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                        onRequestPermissionsResult(requestCode, false, deniedPermissions);
                    }
                })
                .setCancelable(true)
                .show();
    }

    // 启动应用的设置界面
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + mContext.getPackageName()));
        startActivity(intent);
    }


    public void showSoftKeyboard(View focusView) {
        if (mContext != null) {
            if (mInputManager == null) {
                mInputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            }
            focusView.requestFocus();
            mInputManager.showSoftInput(focusView, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void hideSoftKeyboard() {
        if (mContext instanceof Activity) {
            Activity activity = (Activity) mContext;
            if (mInputManager == null) {
                mInputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            }
            if (activity.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                if (activity.getCurrentFocus() != null)
                    mInputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
            mProgressDialog = new ProgressDialog(mContext, getString(R.string.ProgressHint_Default));
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
