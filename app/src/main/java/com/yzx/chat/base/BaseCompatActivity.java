package com.yzx.chat.base;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.yzx.chat.R;
import com.yzx.chat.widget.listener.onFragmentRequestListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年06月12日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public abstract class BaseCompatActivity<P extends BasePresenter>
        extends AppCompatActivity
        implements onFragmentRequestListener {

    protected P mPresenter;

    @LayoutRes
    protected abstract int getLayoutID();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layout_id = getLayoutID();
        if (layout_id != 0) {
            setContentView(getLayoutID());
        }
        initPresenter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.detachView();
            mPresenter = null;
        }
    }

    @CallSuper
    @Override
    public void onFragmentRequest(Fragment fragment, int requestCode, Object arg) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isNeedShowMissingPermissionDialog = false;
        boolean result = true;
        for (int i = 0, length = permissions.length; i < length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                result = false;
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    isNeedShowMissingPermissionDialog = true;
                    break;
                }
            }
        }
        if (result) {
            onRequestPermissionsSuccess(requestCode);
        } else if (isNeedShowMissingPermissionDialog) {
            showMissingPermissionDialog();
        }
    }

    public void onRequestPermissionsSuccess(int requestCode) {

    }

    public void requestPermissionsInCompatMode(@NonNull String[] permissions, int requestCode) {
        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }

        int size = permissionList.size();
        if (size != 0) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[size]), requestCode);
        } else {
            onRequestPermissionsSuccess(requestCode);
        }
    }

    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.Help);
        builder.setMessage(R.string.miss_Permission_hint);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings();
            }
        });
        builder.setCancelable(true);

        builder.show();
    }

    // 启动应用的设置界面
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    @SuppressWarnings("unchecked")
    private void initPresenter() {
        Type type = this.getClass().getGenericSuperclass();
        if (this instanceof BaseView) {
            BaseView view = (BaseView) this;
            mPresenter = (P) view.getPresenter();
            if (mPresenter != null && type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type genericType = parameterizedType.getActualTypeArguments()[0];
                Class<?>[] interfaces = mPresenter.getClass().getInterfaces();
                for (Class c : interfaces) {
                    if (c == genericType) {
                        mPresenter.attachView(view);
                        return;
                    }
                }
                mPresenter = null;
            }
        }
    }
}
