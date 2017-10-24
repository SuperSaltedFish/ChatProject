package com.yzx.chat.test;//package com.yzx.chat.service;
//
//import android.app.Service;
//import android.content.Intent;
//import android.os.Binder;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Message;
//import android.support.annotation.Nullable;
//
//import com.yzx.chat.network.framework.Call;
//import com.yzx.chat.network.framework.HttpResponse;
//import com.yzx.chat.network.framework.NetworkExecutor;
//
//import java.lang.ref.WeakReference;
//
//
//public class NetWorkService extends Service {
//
//    public final static int HTTP_REQUEST = 0x01;
//    public final static int HTTP_CALLBACK = 0x10;
//
//    private NetworkExecutor mNetworkExecutor;
//    private NetWorkServiceHandler mServiceHandler;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        mNetworkExecutor = NetworkExecutor.getInstance();
//        mServiceHandler = new NetWorkServiceHandler(this);
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return START_STICKY;
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return new NetworkBinder(mServiceHandler);
//    }
//
//    @Override
//    public boolean onUnbind(Intent intent) {
//        return super.onUnbind(intent);
//    }
//
//    public void submit(Call<?> call) {
//        if (call.isCallbackRunOnMainThread()) {
//            mNetworkExecutor.submit(call, mServiceHandler);
//        } else {
//            mNetworkExecutor.submit(call, null);
//        }
//    }
//
//    private static class NetWorkServiceHandler extends Handler {
//        private WeakReference<NetWorkService> mServiceWeakReference;
//
//        public NetWorkServiceHandler(NetWorkService service) {
//            mServiceWeakReference = new WeakReference<>(service);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            NetWorkService service = mServiceWeakReference.get();
//            if (service != null && msg.obj != null) {
//                switch (msg.what) {
//                    case HTTP_REQUEST:
//                        service.submit((Call<?>) msg.obj);
//                        break;
//                    case HTTP_CALLBACK:
//                        CallResult callResult = (CallResult) msg.obj;
//                        Call call = callResult.mCall;
//                        if(!call.isCancel()) {
//                            call.complete(callResult.mResponse);
//                        }
//                        break;
//                }
//            }
//        }
//    }
//
//    public static class NetworkBinder extends Binder {
//
//        private NetWorkServiceHandler mServiceHandler;
//
//        public NetworkBinder(NetWorkServiceHandler handler) {
//            mServiceHandler = handler;
//        }
//
//        public void submitHttpRequest(Call<?> call) {
//            Message message = mServiceHandler.obtainMessage(HTTP_REQUEST, call);
//            message.sendToTarget();
//        }
//    }
//
//    public static class CallResult {
//        Call<?> mCall;
//        HttpResponse mResponse;
//
//        public CallResult(Call<?> call, HttpResponse response) {
//            mCall = call;
//            mResponse = response;
//        }
//    }
//
//}
