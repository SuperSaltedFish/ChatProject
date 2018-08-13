package com.yzx.chat.base;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.yzx.chat.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年06月12日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public abstract class BaseCompatActivity<P extends BasePresenter> extends AppCompatActivity {

    public static final int SYSTEM_UI_MODE_NONE = 0;
    public static final int SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS = 1;
    public static final int SYSTEM_UI_MODE_TRANSPARENT_LIGHT_BAR_STATUS = 2;
    public static final int SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS_AND_NAVIGATION = 3;
    public static final int SYSTEM_UI_MODE_FULLSCREEN = 4;
    public static final int SYSTEM_UI_MODE_LIGHT_BAR = 5;

    @IntDef({SYSTEM_UI_MODE_NONE
            , SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS
            , SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS_AND_NAVIGATION
            , SYSTEM_UI_MODE_FULLSCREEN})
    @Retention(RetentionPolicy.SOURCE)
    private @interface SystemUiMode {
    }


    protected P mPresenter;
    protected InputMethodManager mInputManager;
    private Toast mToast;
    private TextView mTvToast;

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isNeedShowMissingPermissionDialog = false;
        boolean result = true;
        ArrayList<String> deniedPermissions = null;
        for (int i = 0, length = permissions.length; i < length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                result = false;
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
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
            showMissingPermissionDialog(requestCode,deniedPermissions.toArray(new String[deniedPermissions.size()]));
        } else {
            onRequestPermissionsResult(requestCode, false, deniedPermissions.toArray(new String[deniedPermissions.size()]));
        }
    }

    protected void onRequestPermissionsResult(int requestCode, boolean isSuccess, String[] deniedPermissions) {

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
            onRequestPermissionsResult(requestCode, true,null);
        }
    }

    private void showMissingPermissionDialog(final int requestCode,final String[] deniedPermissions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.PermissionDialog_Help);
        builder.setMessage(R.string.PermissionDialog_MissPermissionHint);
        builder.setNegativeButton(R.string.PermissionDialog_Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onRequestPermissionsResult(requestCode, false,deniedPermissions);
            }
        });
        builder.setPositiveButton(R.string.PermissionDialog_Setting, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings();
                onRequestPermissionsResult(requestCode, false,deniedPermissions);
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

        }
    }

    public void setSystemUiMode(int mode) {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        switch (mode) {
            case SYSTEM_UI_MODE_NONE:
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                TypedValue value = new TypedValue();
                getTheme().resolveAttribute(R.attr.colorPrimary, value, true);
                window.setStatusBarColor(value.data);
                break;
            case SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS:
            case SYSTEM_UI_MODE_TRANSPARENT_LIGHT_BAR_STATUS:
                if (mode == SYSTEM_UI_MODE_TRANSPARENT_LIGHT_BAR_STATUS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                } else {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                }
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
                break;
            case SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS_AND_NAVIGATION:
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setNavigationBarColor(Color.TRANSPARENT);
                window.setStatusBarColor(Color.TRANSPARENT);
                break;
            case SYSTEM_UI_MODE_FULLSCREEN:
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                break;
            case SYSTEM_UI_MODE_LIGHT_BAR:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
                break;
        }
    }

    public void setBrightness(float paramFloat) {
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.screenBrightness = paramFloat;
        window.setAttributes(params);
    }

    public void showToast(String content) {
        showToast(content, Toast.LENGTH_SHORT);
    }

    public void showLongToast(String content) {
        showToast(content, Toast.LENGTH_LONG);
    }

    public void showToast(String content, int duration) {
        if (mToast == null) {
            mToast = new Toast(this);
            View toastView = getLayoutInflater().inflate(R.layout.view_toast_default, null);
            mTvToast = toastView.findViewById(R.id.BaseCompatActivity_mTvToast);
            mToast.setView(toastView);
        }
        mToast.setDuration(duration);
        mTvToast.setText(content);
        mToast.show();
    }


    public void showSoftKeyboard(View focusView) {
        if (mInputManager == null) {
            mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        focusView.requestFocus();
        mInputManager.showSoftInput(focusView, InputMethodManager.SHOW_IMPLICIT);
    }

    public void hideSoftKeyboard() {
        if (mInputManager == null) {
            mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                mInputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
