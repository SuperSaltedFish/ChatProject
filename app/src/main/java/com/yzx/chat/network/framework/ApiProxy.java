package com.yzx.chat.network.framework;


import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ApiProxy {

    private HashMap<Class, Object> mProxyInstanceMap;
    private ApiProxyHandler mApiProxyHandler;
    private final HttpDataFormatAdapter mDefaultHttpDataFormatAdapter;
    private final String mBaseUrl;

    public ApiProxy(String baseUrl, @NonNull HttpDataFormatAdapter defaultAdapter) {
        mBaseUrl = baseUrl;
        mDefaultHttpDataFormatAdapter = defaultAdapter;
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
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Annotation[] annotations = method.getAnnotations();
            HttpRequestImpl httpRequest = null;
            for (Annotation annotation : annotations) {
                if (annotation instanceof HttpApi) {
                    httpRequest = new HttpRequestImpl();
                    parseMethodAnnotation((HttpApi) annotation, httpRequest);
                    parseParamsAnnotation(method.getParameterAnnotations(), args, httpRequest);
                    break;
                }
            }
            if (httpRequest == null) {
                throw new RuntimeException("Method \"" + method.getName() + "\" must explicitly declare \"@HttpApi\" annotation");
            }
            if (method.getReturnType() != Call.class) {
                throw new RuntimeException("The return value type of the \"" + method.getName() + "\" method must be " + CallImpl.class);
            }
            Type genericReturnType = method.getGenericReturnType();
            if (genericReturnType instanceof ParameterizedType) {
                Type type = ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
                if (type instanceof WildcardType) {
                    throw new RuntimeException("The \"" + method.getName() + "\" method must explicitly declare the generic parameters of the returned value");
                }
                CallImpl call = new CallImpl(httpRequest, mDefaultHttpDataFormatAdapter, type);
                return call;
            } else {
                throw new RuntimeException("The return value of \"" + method.getName() + "\" must explicitly declare generic parameters");
            }
        }


        private void parseMethodAnnotation(HttpApi annotation, HttpRequestImpl httpRequest) {
            httpRequest.setRequestMethod(annotation.RequestMethod());
            httpRequest.setUrl(mBaseUrl + annotation.Path());
        }

        private void parseParamsAnnotation(Annotation[][] annotations, Object[] params, HttpRequestImpl httpRequest) {
            HashMap<String, Object> paramsMap = new HashMap<>();
            HashMap<String, List<String>> uploadMap = null;
            for (int i = 0, size = annotations.length; i < size; i++) {
                for (int j = 0, length = annotations[i].length; j < length; j++) {
                    if (annotations[i][j] instanceof HttpParam) {
                        HttpParam httpParam = (HttpParam) annotations[i][j];
                        paramsMap.put(httpParam.value(), params[i]);
                    } else if (annotations[i][j] instanceof UploadPath) {
                        UploadPath uploadPath = (UploadPath) annotations[i][j];
                        if (uploadMap == null) {
                            uploadMap = new HashMap<>();
                            httpRequest.setUploadMap(uploadMap);
                        }
                        List<String> uploadList;
                        if (params[i] instanceof List) {
                            uploadList = (List<String>) params[i];
                            if (uploadList.size() > 0) {
                                uploadMap.put(uploadPath.value(), uploadList);
                            }
                        } else if (params[i] instanceof String) {
                            String path = (String) params[i];
                            if (!TextUtils.isEmpty(path)) {
                                uploadList = new ArrayList<>(1);
                                uploadList.add(path);
                                uploadMap.put(uploadPath.value(), uploadList);
                            }
                        } else {
                            throw new RuntimeException(UploadPath.class.getSimpleName() + " does not support the " + params[i].getClass() + " type as a parameter");
                        }
                    }
                }
            }
            httpRequest.setParams(paramsMap);
        }
    }

}
