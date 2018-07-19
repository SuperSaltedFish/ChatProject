package com.yzx.chat.network.framework;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by YZX on 2018年07月17日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class AnnotationParser {

    @NonNull
    public static HttpRequestImpl parse(Method method, Object[] args) {
        HttpRequestImpl httpRequest = new HttpRequestImpl();
        parseMethodAnnotation(method.getAnnotations(), httpRequest);
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0, count = annotations == null ? 0 : annotations.length; i < count; i++) {
            parseParamsAnnotation(annotations[i], args[i], httpRequest);
        }
        return httpRequest;
    }

    private static void parseMethodAnnotation(Annotation[] annotations, HttpRequestImpl httpRequest) {
        if (annotations == null || annotations.length == 0 || httpRequest == null) {
            return;
        }
        for (Annotation annotation : annotations) {
            if (annotation instanceof GET) {
                httpRequest.method = HttpRequest.METHOD_GET;
                httpRequest.relativeUrl = ((GET) annotation).value();
            } else if (annotation instanceof POST) {
                httpRequest.method = HttpRequest.METHOD_POST;
                httpRequest.relativeUrl = ((POST) annotation).value();
            } else if (annotation instanceof PUT) {
                httpRequest.method = HttpRequest.METHOD_PUT;
                httpRequest.relativeUrl = ((PUT) annotation).value();
            } else if (annotation instanceof DELETE) {
                httpRequest.method = HttpRequest.METHOD_DELETE;
                httpRequest.relativeUrl = ((DELETE) annotation).value();
            } else if (annotation instanceof HttpApi) {
                httpRequest.method = ((HttpApi) annotation).method();
                httpRequest.relativeUrl = ((HttpApi) annotation).url();
            } else if (annotation instanceof FormUrlEncoded) {
                httpRequest.isFormUrlEncoded = true;
            } else if (annotation instanceof Headers) {
                String[] headers = ((Headers) annotation).value();
                String headerName, headerValue;
                for (String header : headers) {
                    int colon = header.indexOf(':');
                    if (colon == -1 || colon == 0 || colon == header.length() - 1) {
                        throw new RuntimeException("@Headers value must be in the form \"Name: Value\". Found:" + header);
                    }
                    headerName = header.substring(0, colon).trim();
                    headerValue = header.substring(colon + 1).trim();
                    if (TextUtils.isEmpty(headerName) || TextUtils.isEmpty(headerValue)) {
                        throw new RuntimeException("@Headers value must be in the form \"Name: Value\". Found:" + header);
                    }
                    httpRequest.headers.put(headerName, headerValue);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void parseParamsAnnotation(Annotation[] annotations, Object param, HttpRequestImpl httpRequest) {
        if (annotations == null || annotations.length == 0 || httpRequest == null) {
            return;
        }
        for (Annotation annotation : annotations) {
            if (annotation instanceof Param) {
                httpRequest.params.put(((Param) annotation).value(), param);
            } else if (annotation instanceof UploadPart) {
                if (httpRequest.isFormUrlEncoded) {
                    throw new RuntimeException("@Url cannot be used with @UploadPart.");
                }
                String partName = ((UploadPart) annotation).value();
                Map<String, Object> uploadParts = httpRequest.multipart;
                if (param instanceof String) {
                    uploadParts.put(partName, new File((String) param));
                } else if (param instanceof List) {
                    List paths = (List) param;
                    if (paths.size() == 0) {
                        continue;
                    }
                    if (paths.get(0) instanceof String) {
                        List<File> files = new ArrayList<>(paths.size());
                        for (Object path : paths) {
                            files.add(new File((String) path));
                        }
                        uploadParts.put(partName, files);
                    } else {
                        throw new RuntimeException("When the value of @UploadPart is List type, the parameter type of List must be String.");
                    }
                }
                httpRequest.isMultipart = true;
            } else if (annotation instanceof Part) {
                if (httpRequest.isFormUrlEncoded) {
                    throw new RuntimeException("@Url cannot be used with @Part.");
                }
                httpRequest.multipart.put(((Part) annotation).value(), param);
                httpRequest.isMultipart = true;
            }
        }
        if (httpRequest.params.size() != 0 && httpRequest.isMultipart) {
            throw new RuntimeException("@Param can not be used with the @Part and @UploadPart.");
        }
    }
}
