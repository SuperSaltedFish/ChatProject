package com.yzx.chat.core.net.framework;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


class HttpRequestImpl implements HttpRequest {

    String baseUrl;
    String relativeUrl;
    String method;
    LinkedHashMap<String, String> headers;
    LinkedHashMap<String, Object> params;
    LinkedHashMap<String, Object> multipart;

    boolean isFormUrlEncoded;
    boolean isMultipart;

    HttpRequestImpl() {
        headers = new LinkedHashMap<>();
        params = new LinkedHashMap<>();
        multipart = new LinkedHashMap<>();
    }

    @Override
    public String url() {
        if (isFormUrlEncoded && isBodyWriteInURL()) {
            return baseUrl + relativeUrl + "?" + getBodyOfFormUrlEncoded();
        } else {
            return baseUrl + relativeUrl;
        }
    }

    @Override
    public String method() {
        return method.toUpperCase(Locale.US);
    }

    @Override
    public Map<String, String> headers() {
        if (isFormUrlEncoded) {
            headers.put("Content-Type", "application/x-www-form-urlencoded");
        } else if (isMultipart) {
            headers.put("Content-Type", "multipart/form-data; boundary=" + MULTIPART_BOUNDARY);
        }
        return headers;
    }

    @Override
    public boolean isMultipart() {
        return isMultipart;
    }

    @Override
    public boolean isFormUrlEncoded() {
        return isFormUrlEncoded;
    }

    @Override
    public boolean isBodyWritable() {
        return !isBodyWriteInURL();
    }

    @Override
    public void writeBodyTo(HttpConverter converter, OutputStream outputStream) throws Exception {
        if (isFormUrlEncoded) {
            outputStream.write(stringToByteArray(getBodyOfFormUrlEncoded()));
        } else if (isMultipart) {
            if (multipart.size() > 0) {
                for (Map.Entry<String, Object> entry : multipart.entrySet()) {
                    String partName = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof File) {
                        convertFileTo(partName, (File) value, outputStream);
                        continue;
                    } else if (value instanceof List) {
                        List list = (List) value;
                        if (list.size() > 0 && list.get(0) instanceof File) {
                            for (File file : (List<File>) list) {
                                convertFileTo(partName, file, outputStream);
                            }
                            continue;
                        }
                    }
                    convertPartBodyTo(partName, value, converter, outputStream);
                }
                outputStream.write(stringToByteArray(getMultipartFoot()));
            }
        } else {
            convertBodyTo(converter, outputStream);
        }
    }

    private boolean isBodyWriteInURL() {
        return TextUtils.equals(method(), METHOD_GET) || TextUtils.equals(method(), METHOD_DELETE);
    }

    private String getBodyOfFormUrlEncoded() {
        StringBuilder builder = new StringBuilder(24 * params.size());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            builder.append(entry.getKey()).append('=').append(entry.getValue()).append('&');
        }
        return builder.substring(0, builder.length() - 2);
    }

    private void convertBodyTo(HttpConverter converter, OutputStream outputStream) throws IOException {
        byte[] convertBody = converter.convertRequest(params);
        if (convertBody != null) {
            outputStream.write(convertBody);
        }
    }

    private static void convertPartBodyTo(String partName, Object body, HttpConverter converter, OutputStream outputStream) throws IOException {
        byte[] convertBody = converter.convertMultipartRequest(partName, body);
        if (convertBody != null) {
            outputStream.write(stringToByteArray(getMultipartHead(partName)));
            outputStream.write(convertBody);
            outputStream.write(stringToByteArray("\r\n"));
        }
    }

    private static void convertFileTo(String partName, File file, OutputStream outputStream) throws IOException {
        outputStream.write(stringToByteArray(getFileMultipartHead(partName, file.getName())));
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] bytes = new byte[2048];
        int len;
        while ((len = fileInputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, len);
        }
        outputStream.write(stringToByteArray("\r\n"));
        fileInputStream.close();
    }

    private static String getMultipartHead(String partName) {
        return String.format("--%s\r\nContent-Disposition: form-data; name=\"%s\"\r\nContent-Type: text/plain;charset=UTF-8\nContent-Transfer-Encoding: 8bit\r\n\r\n", MULTIPART_BOUNDARY, partName);
    }

    private static String getFileMultipartHead(String partName, String fileName) {
        return String.format("--%s\r\nContent-Disposition: form-data; name=\"%s\";filename=\"%s\"\r\nContent-Type: application/octet-stream\r\nContent-Transfer-Encoding: binary\r\n\r\n", MULTIPART_BOUNDARY, partName, fileName);
    }

    private static String getMultipartFoot() {
        return String.format("--%s--\r\n", MULTIPART_BOUNDARY);
    }

    private static byte[] stringToByteArray(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }


}

