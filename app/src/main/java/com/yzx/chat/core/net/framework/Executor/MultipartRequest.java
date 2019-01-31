package com.yzx.chat.core.net.framework.Executor;

import android.support.annotation.Nullable;
import android.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by YZX on 2019年01月30日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class MultipartRequest extends HttpRequest {

    private static final String BOUNDARY = UUID.randomUUID().toString();

    private List<Pair<String, Object>> mPartList;

    public MultipartRequest(String url) {
        super(url, METHOD_POST);
        mPartList = Collections.synchronizedList(new ArrayList<Pair<String, Object>>());
        putHeader("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
    }

    public void addPart(String partName, String contentType, byte[] content) {
        mPartList.add(new Pair<String, Object>(partName, new BytePart(contentType, content)));
    }

    public void addPart(String partName, File file, @Nullable String fileName) {
        if (!file.exists() || !file.isFile()) {
            return;
        }
        if (fileName == null || "".equals(fileName)) {
            fileName = file.getName();
        }
        mPartList.add(new Pair<String, Object>(fileName, new FilePart(fileName, file)));
    }


    @Override
    public void writeBodyTo(OutputStream outputStream) throws IOException {
        if (!hasBody()) {
            return;
        }
        for (Pair<String, Object> pair : mPartList) {
            String partName = pair.first;
            Object partEntity = pair.second;
            if (partEntity instanceof BytePart) {
                BytePart bytePart = (BytePart) partEntity;
                outputStream.write(stringToByteArray(getMultipartHead(partName, bytePart.contentType)));
                outputStream.write(bytePart.content);
                outputStream.write(stringToByteArray("\r\n"));
            } else if (partEntity instanceof FilePart) {
                FilePart filePart = (FilePart) partEntity;
                convertFileTo(partName, filePart.fileName, filePart.file, outputStream);
            }
        }
        outputStream.write(stringToByteArray(getMultipartFoot()));
    }

    @Override
    public boolean hasBody() {
        return mPartList.size() > 0;
    }

    private static void convertFileTo(String partName, String fileName, File file, OutputStream outputStream) throws IOException {
        outputStream.write(stringToByteArray(getFileMultipartHead(partName, fileName)));
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] bytes = new byte[2048];
        int len;
        while ((len = fileInputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, len);
        }
        outputStream.write(stringToByteArray("\r\n"));
        fileInputStream.close();
    }

    private static String getMultipartHead(String partName, String contentType) {
        return String.format("--%s\r\nContent-Disposition: form-data; name=\"%s\"\r\nContent-Type: %s;charset=UTF-8\nContent-Transfer-Encoding: 8bit\r\n\r\n", BOUNDARY, partName, contentType);
    }

    private static String getFileMultipartHead(String partName, String fileName) {
        return String.format("--%s\r\nContent-Disposition: form-data; name=\"%s\";filename=\"%s\"\r\nContent-Type: application/octet-stream\r\nContent-Transfer-Encoding: binary\r\n\r\n", BOUNDARY, partName, fileName);
    }

    private static String getMultipartFoot() {
        return String.format("--%s--\r\n", BOUNDARY);
    }

    private static byte[] stringToByteArray(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private static class BytePart {
        String contentType;
        byte[] content;

        BytePart(String contentType, byte[] content) {
            this.contentType = contentType;
            this.content = content;
        }
    }

    private static class FilePart {
        String fileName;
        File file;

        FilePart(String fileName, File file) {
            this.fileName = fileName;
            this.file = file;
        }
    }

}
