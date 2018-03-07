package com.yzx.chat.base;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.yzx.chat.R;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年06月12日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public abstract class BaseCompatActivity<P extends BasePresenter> extends AppCompatActivity {

    protected P mPresenter;
    protected InputMethodManager mInputManager;

    @LayoutRes
    protected abstract int getLayoutID();

    protected abstract void init(Bundle savedInstanceState);

    protected abstract void setup(Bundle savedInstanceState);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layoutID = getLayoutID();
        if (layoutID != 0) {
            setContentView(layoutID);
        }
        initPresenter();
        Toolbar toolbar = findViewById(R.id.Default_mToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            setTitle(null);
        }
        init(savedInstanceState);
        setup(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.detachView();
            mPresenter = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    protected void showToast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    protected void showSoftKeyboard(View focusView) {
        if (mInputManager == null) {
            mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        focusView.requestFocus();
        mInputManager.showSoftInput(focusView, InputMethodManager.SHOW_IMPLICIT);
    }

    protected void hideSoftKeyboard() {
        if (mInputManager == null) {
            mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                mInputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
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
            onRequestPermissionsResult(requestCode, true);
        } else if (isNeedShowMissingPermissionDialog) {
            showMissingPermissionDialog(requestCode);
        } else {
            onRequestPermissionsResult(requestCode, false);
        }
    }

    protected void onRequestPermissionsResult(int requestCode, boolean isSuccess) {

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
            onRequestPermissionsResult(requestCode, true);
        }
    }

    private void showMissingPermissionDialog(final int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.PermissionDialog_Help);
        builder.setMessage(R.string.PermissionDialog_MissPermissionHint);
        builder.setNegativeButton(R.string.PermissionDialog_Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onRequestPermissionsResult(requestCode, false);
            }
        });
        builder.setPositiveButton(R.string.PermissionDialog_Setting, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings();
                onRequestPermissionsResult(requestCode, false);
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
