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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


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
            boolean isMultiParams = false;
            for (Annotation annotation : annotations) {
                if (annotation instanceof HttpApi) {
                    httpRequest = new HttpRequestImpl();
                    parseMethodAnnotation((HttpApi) annotation, httpRequest);
                    parseParamsAnnotation(method.getParameterAnnotations(), args, httpRequest);
                } else if (annotation instanceof MultiParams) {
                    isMultiParams = true;
                }
            }
            if (httpRequest == null) {
                throw new RuntimeException("Method \"" + method.getName() + "\" must explicitly declare \"@HttpApi\" annotation");
            }
            if (method.getReturnType() != Call.class) {
                throw new RuntimeException("The return value type of the \"" + method.getName() + "\" method must be " + CallImpl.class);
            }
            httpRequest.enableMultiParams(isMultiParams);
            Type genericReturnType = method.getGenericReturnType();
            if (genericReturnType instanceof ParameterizedType) {
                Type type = ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
                if (type instanceof WildcardType) {
                    throw new RuntimeException("The \"" + method.getName() + "\" method must explicitly declare the generic parameters of the returned value");
                }
                return new CallImpl(httpRequest, mDefaultHttpDataFormatAdapter, type);
            } else {
                throw new RuntimeException("The return value of \"" + method.getName() + "\" must explicitly declare generic parameters");
            }
        }


        private void parseMethodAnnotation(HttpApi annotation, HttpRequestImpl httpRequest) {
            httpRequest.setRequestMethod(annotation.RequestMethod());
            httpRequest.setUrl(mBaseUrl + annotation.Path());
        }

        @SuppressWarnings("unchecked")
        private void parseParamsAnnotation(Annotation[][] annotations, Object[] params, HttpRequestImpl httpRequest) {
            Map<HttpParamsType, List<Pair<String, Object>>> paramsTypeMap = new EnumMap<>(HttpParamsType.class);
            for (int i = 0, size = annotations.length; i < size; i++) {
                for (int j = 0, length = annotations[i].length; j < length; j++) {
                    if (annotations[i][j] instanceof HttpParam) {
                        List<Pair<String, Object>> paramsList = paramsTypeMap.get(HttpParamsType.PARAMETER_HTTP);
                        if (paramsList == null) {
                            paramsList = new ArrayList<>();
                            paramsTypeMap.put(HttpParamsType.PARAMETER_HTTP, paramsList);
                        }
                        HttpParam httpParam = (HttpParam) annotations[i][j];
                        paramsList.add(new Pair<>(httpParam.value(), params[i]));
                    } else if (annotations[i][j] instanceof UploadPath) {
                        List<Pair<String, Object>> paramsList = paramsTypeMap.get(HttpParamsType.PARAMETER_UPLOAD);
                        if (paramsList == null) {
                            paramsList = new ArrayList<>();
                            paramsTypeMap.put(HttpParamsType.PARAMETER_UPLOAD, paramsList);
                        }
                        UploadPath uploadPath = (UploadPath) annotations[i][j];
                        String paramsName = uploadPath.value();
                        if (TextUtils.isEmpty(paramsName)) {
                            continue;
                        }
                        List<String> pathList = null;
                        for (Pair<String, Object> pair : paramsList) {
                            if (paramsName.equals(pair.key)) {
                                pathList = (List<String>) pair.value;
                                break;
                            }
                        }
                        if (pathList == null) {
                            pathList = new ArrayList<>();
                            paramsList.add(new Pair<String, Object>(paramsName, pathList));
                        }

                        if (params[i] instanceof List) {
                            try {
                                pathList.addAll((List<String>) params[i]);
                            } catch (ClassCastException e) {
                                throw new RuntimeException(UploadPath.class.getSimpleName() + " only support String and List<String> type as a parameter");
                            }
                        } else if (params[i] instanceof String) {
                            String path = (String) params[i];
                            if (!TextUtils.isEmpty(path)) {
                                pathList.add((String) params[i]);
                            }
                        } else {
                            throw new RuntimeException(UploadPath.class.getSimpleName() + " does not support the " + params[i].getClass() + " type as a parameter");
                        }
                    }
                }
            }
            httpRequest.setParams(paramsTypeMap);
        }
    }

}
