package com.yzx.chat.core.net.framework.Executor;

import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by YZX on 2019年01月30日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class MultipartRequest extends HttpRequest {

    private Map<String, Object> mPartsMap;

    public MultipartRequest(String url) {
        super(url, METHOD_POST);
        mPartsMap = Collections.synchronizedMap(new LinkedHashMap<String, Object>());
    }

    public void addPart(String contentType, byte[] content) {
        mPartsMap.put(contentType, content);
    }

    public void addPart(File file, @Nullable String fileName) {
        if (!file.exists() || !file.isFile()) {
            return;
        }
        if (fileName == null || "".equals(fileName)) {
            fileName = file.getName();
        }
        mPartsMap.put(fileName, file);
    }

    @Override
    public void writeBodyTo(OutputStream outputStream) throws IOException {
        if (!hasBody()) {
            return;
        }
        for (Map.Entry<String, Object> entry : mPartsMap.entrySet()) {
            String partName = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof File) {
                convertFileTo(partName, (File) value, outputStream);
            } else if (value instanceof byte[]) {
                outputStream.write(stringToByteArray(getMultipartHead(partName)));
                outputStream.write((byte[]) value);
                outputStream.write(stringToByteArray("\r\n"));
            }
        }
        outputStream.write(stringToByteArray(getMultipartFoot()));
    }

    @Override
    public boolean hasBody() {
        return mPartsMap.size() > 0;
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
