package com.yzx.chat.test;//package com.yzx.chat.base;
//
//import android.content.ComponentName;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.support.annotation.CallSuper;
//import android.support.v4.app.Fragment;
//
//import com.yzx.chat.network.framework.Call;
//import com.yzx.chat.service.NetWorkService;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by YZX on 2017年10月18日.
// * 其实你找不到错误不代表错误不存在，同样你看不到技术比你牛的人并不代表世界上没有技术比你牛的人。
// */
//
//public abstract class HttpActivity extends BaseCompatActivity {
//
//    public final static int FRAGMENT_REQUEST_SUBMIT_HTTP = 0x10000000;
//
//    private NetWorkService.NetworkBinder mNetworkBinder;
//    private List<Call<?>> mTempSaveCallList;
//    private boolean isAlreadyBindNetworkService;
//
//    @CallSuper
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mTempSaveCallList = new ArrayList<>();
//        bindService(new Intent(this, NetWorkService.class), mNetworkServiceConnection, BIND_AUTO_CREATE);
//    }
//
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (mNetworkBinder != null) {
//            unbindService(mNetworkServiceConnection);
//        }
//    }
//
//    public void submitHttpRequest(Call<?> call) {
//        if (!isAlreadyBindNetworkService) {
//            isAlreadyBindNetworkService = bindService(new Intent(this, NetWorkService.class), mNetworkServiceConnection, BIND_AUTO_CREATE);
//            mTempSaveCallList.add(call);
//        } else if (mNetworkBinder == null) {
//            mTempSaveCallList.add(call);
//        } else {
//            mNetworkBinder.submitHttpRequest(call);
//        }
//    }
//
//    private final ServiceConnection mNetworkServiceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            mNetworkBinder = (NetWorkService.NetworkBinder) service;
//            for (int i = 0, size = mTempSaveCallList.size(); i < size; i++) {
//                Call<?> call = mTempSaveCallList.get(i);
//                mNetworkBinder.submitHttpRequest(call);
//            }
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            mNetworkBinder = null;
//            isAlreadyBindNetworkService = false;
//        }
//    };
//
//    @Override
//    public void onFragmentRequest(Fragment fragment, int requestCode, Object arg) {
//        super.onFragmentRequest(fragment, requestCode, arg);
//        switch (requestCode) {
//            case FRAGMENT_REQUEST_SUBMIT_HTTP:
//                submitHttpRequest((Call<?>) arg);
//                break;
//        }
//    }
//}
