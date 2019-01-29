package com.yzx.chat.core.net.framework;


import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;


public class ApiProxy {

    private HashMap<Class, Object> mProxyInstanceMap;
    private ApiProxyHandler mApiProxyHandler;
    private final HttpConverter mDefaultHttpConverter;
    private final String mBaseUrl;

    public ApiProxy(String baseUrl, @NonNull HttpConverter httpConverter) {
        if (TextUtils.isEmpty(baseUrl)) {
            throw new RuntimeException("The baseUrl can't be empty.");
        }
        mBaseUrl = baseUrl;
        mDefaultHttpConverter = httpConverter;
        mProxyInstanceMap = new HashMap<>();
        mApiProxyHandler = new ApiProxyHandler();
    }

    public synchronized Object getProxyInstance(Class<?> interfaceClass) {
        Object instance = mProxyInstanceMap.get(interfaceClass);
        if (instance == null) {
            instance = Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, mApiProxyHandler);
            mProxyInstanceMap.put(interfaceClass, instance);
        }

        return instance;
    }

    public String getBaseUrl() {
        return mBaseUrl;
    }

    private class ApiProxyHandler implements InvocationHandler {

        @SuppressWarnings("unchecked")
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            HttpRequestImpl httpRequest = AnnotationParser.parse(method, args);
            httpRequest.baseUrl = mBaseUrl;

            Class<?> returnType = method.getReturnType();
            if (returnType == Call.class) {
                Type genericReturnType = method.getGenericReturnType();
                if (genericReturnType instanceof ParameterizedType) {
                    Type type = ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
                    if (type instanceof WildcardType) {
                        throw new RuntimeException("The \"" + method.getName() + "\" method must explicitly declare the generic parameters of the returned value");
                    }
                    return new CallImpl(httpRequest,type,mDefaultHttpConverter);
                } else {
                    throw new RuntimeException("The return value of \"" + method.getName() + "\" must explicitly declare generic parameters");
                }
            } else if (returnType == DownloadCall.class) {
                return new DownloadCallImpl(httpRequest,mDefaultHttpConverter);
            } else {
                throw new RuntimeException("The return value type of the \"" + method.getName() + "\" method must be " + Call.class + " or" + DownloadCall.class);
            }

        }

    }

}
