package com.yzx.chat.core.net.framework;

import android.text.TextUtils;
import android.util.Pair;

import com.yzx.chat.core.net.framework.Executor.HttpRequest;
import com.yzx.chat.core.net.framework.annotation.FileListPart;
import com.yzx.chat.core.net.framework.annotation.FilePart;
import com.yzx.chat.core.net.framework.annotation.GET;
import com.yzx.chat.core.net.framework.annotation.Headers;
import com.yzx.chat.core.net.framework.annotation.HttpApi;
import com.yzx.chat.core.net.framework.annotation.Multipart;
import com.yzx.chat.core.net.framework.annotation.POST;
import com.yzx.chat.core.net.framework.annotation.Param;
import com.yzx.chat.core.net.framework.annotation.Part;


import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * Created by YZX on 2018年07月17日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class AnnotationParser {

    @NonNull
    public static RequestParams parse(Method method, Object[] args) {
        RequestParams params = new RequestParams();
        parseMethodAnnotation(method.getAnnotations(), params);
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0, count = annotations.length; i < count; i++) {
            parseParamsAnnotation(annotations[i], args[i], params);
        }
        return params;
    }

    private static void parseMethodAnnotation(Annotation[] annotations, RequestParams params) {
        if (annotations == null || annotations.length == 0 || params == null) {
            return;
        }
        for (Annotation annotation : annotations) {
            if (annotation instanceof GET) {
                params.method = HttpRequest.METHOD_GET;
                params.relativeUrl = ((GET) annotation).value();
            } else if (annotation instanceof POST) {
                params.method = HttpRequest.METHOD_POST;
                params.relativeUrl = ((POST) annotation).value();
            } else if (annotation instanceof HttpApi) {
                params.method = ((HttpApi) annotation).method();
                params.relativeUrl = ((HttpApi) annotation).url();
            } else if (annotation instanceof Multipart) {
                params.isMultipart = true;
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
                    params.headers.put(headerName, headerValue);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void parseParamsAnnotation(Annotation[] annotations, Object param, RequestParams params) {
        if (annotations == null || annotations.length == 0 || params == null) {
            return;
        }
        if (params.isMultipart) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof Part) {
                    Part part = (Part) annotation;
                    Map<String, Object> paramsMap = params.paramsPartMap.get(part.part());
                    if (paramsMap == null) {
                        paramsMap = new LinkedHashMap<>();
                        params.paramsPartMap.put(part.part(), paramsMap);
                    }
                    paramsMap.put(part.paramName(), param);
                } else if (annotation instanceof FilePart) {
                    if (param instanceof File) {
                        FilePart filePart = (FilePart) annotation;
                        List<Pair<String, File>> fileList = params.filePartMap.get(filePart.part());
                        if (fileList == null) {
                            fileList = new ArrayList<>();
                            params.filePartMap.put(filePart.part(), fileList);
                        }
                        fileList.add(new Pair<>(filePart.fileName(), (File) param));
                    } else {
                        throw new RuntimeException("@FilePart supports only types 'File'.");
                    }
                } else if (annotation instanceof FileListPart) {
                    if (param instanceof List) {
                        List paths = (List) param;
                        if (paths.size() == 0) {
                            continue;
                        }
                        if (paths.get(0) instanceof File) {
                            FileListPart fileListPart = (FileListPart) annotation;
                            List<Pair<String, File>> fileList = params.filePartMap.get(fileListPart.value());
                            if (fileList == null) {
                                fileList = new ArrayList<>();
                                params.filePartMap.put(fileListPart.value(), fileList);
                            }
                            for (File f : (List<File>) param) {
                                fileList.add(new Pair<>(f.getName(), (File) param));
                            }
                        } else {
                            throw new RuntimeException("@FileListPart supports only types 'List<File>'.");
                        }
                    } else {
                        throw new RuntimeException("@FilePart supports only types 'List<File>'.");
                    }
                } else if (annotation instanceof Param) {
                    throw new RuntimeException("@Param cannot be used with @Multipart.");
                } else {
                    throw new RuntimeException("Unknown annotations：" + annotation.toString());
                }
            }
        } else {
            for (Annotation annotation : annotations) {
                if (annotation instanceof Param) {
                    params.params.put(((Param) annotation).value(), param);
                } else if ((annotation instanceof Part) || (annotation instanceof FilePart)) {
                    throw new RuntimeException("@Part or @FilePart must be used with @Multipart.");
                } else {
                    throw new RuntimeException("Unknown annotations：" + annotation.toString());
                }
            }
        }
    }
}
