package com.yzx.chat.network.framework;


import android.os.Build;
import android.text.TextUtils;

import com.yzx.chat.util.LogUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;


public class Http {

    private final static int CONNECT_TIMEOUT = 20000;
    private final static int READ_TIMEOUT = 20000;


    public static ResponseParams call(HttpRequest request, HttpConverter converter) {
        ResponseParams responseParams = new ResponseParams();
        HttpURLConnection conn = null;
        try {
            URL url = new URL(request.url());
            conn = (HttpURLConnection) url.openConnection();
            setDefaultAttribute(conn);
            setAttributeFromRequest(conn, request);
            LogUtil.e("开始访问:" + url.toExternalForm());
            writeRequestBody(conn, request, converter);
            readResponseBody(conn, responseParams);
        } catch (Exception e) {
            if (conn != null) {
                conn.disconnect();
            }
            responseParams.throwable = e;
        }
        return responseParams;
    }

    public static ResponseParams callDownload(HttpRequest request, HttpConverter converter, String savePath, DownloadCallback callback, Cancellable cancellable) {
        ResponseParams responseParams = new ResponseParams();
        HttpURLConnection conn = null;
        try {
            URL url = new URL(request.url());
            conn = (HttpURLConnection) url.openConnection();
            setDefaultAttribute(conn);
            setAttributeFromRequest(conn, request);
            LogUtil.e("开始访问:" + url.getPath());
            writeRequestBody(conn, request, converter);
            readResponseBodyToFile(conn, responseParams, getFilePath(request.url(), savePath, conn.getHeaderField("Content-Disposition")), callback, cancellable);
        } catch (Exception e) {
            if (conn != null) {
                conn.disconnect();
            }
            responseParams.throwable = e;
        }
        return responseParams;
    }

    private static void setDefaultAttribute(HttpURLConnection conn) {
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("Content-Type", "text/plain");
       conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setDoInput(true);
    }

    private static void setAttributeFromRequest(HttpURLConnection conn, HttpRequest request) throws ProtocolException {
        conn.setRequestMethod(request.method());
        conn.setDoOutput(request.isBodyWritable());
        for (Map.Entry<String, String> entry : request.headers().entrySet()) {
            conn.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    private static void writeRequestBody(HttpURLConnection conn, HttpRequest request, HttpConverter converter) throws Exception {
        if (request.isBodyWritable()) {
            OutputStream stream = null;
            try {
                stream = conn.getOutputStream();
                request.writeBodyTo(converter, stream);
                stream.flush();
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }
    }

    private static void readResponseBody(HttpURLConnection conn, ResponseParams responseParams) throws IOException {
        responseParams.responseCode = conn.getResponseCode();
        if (HttpURLConnection.HTTP_OK != responseParams.responseCode) {
            return;
        }
        int bodyLength = conn.getContentLength();
        if (bodyLength == 0) {
            return;
        }
        if (bodyLength < 0) {
            bodyLength = 8096;
        }
        InputStream stream = conn.getInputStream();
        byte[] body = new byte[bodyLength];
        byte[] buff = new byte[2048];
        int currentLen = 0;
        int readLen;
        try {
            while ((readLen = stream.read(buff)) != -1) {
                if (currentLen == bodyLength) {
                    bodyLength = (int) (bodyLength * 1.5 + readLen);
                    byte[] newBody = new byte[bodyLength];
                    System.arraycopy(body, 0, newBody, 0, body.length);
                    body = newBody;
                }
                System.arraycopy(buff, 0, body, currentLen, readLen);
                currentLen += readLen;
            }
            if (body.length != currentLen) {
                byte[] newBody = new byte[currentLen];
                System.arraycopy(body, 0, newBody, 0, currentLen);
                body = newBody;
            }
            responseParams.body = body;
        } finally {
            stream.close();
        }
    }

    private static void readResponseBodyToFile(HttpURLConnection conn, ResponseParams responseParams, String savePath, DownloadCallback callback, Cancellable cancellable) throws IOException {
        responseParams.responseCode = conn.getResponseCode();
        if (HttpURLConnection.HTTP_OK != responseParams.responseCode) {
            return;
        }
        long fileSize = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? conn.getContentLengthLong() : conn.getContentLength();
        File file = new File(savePath);
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Failure to create a file(" + savePath + ")");
        }
        FileOutputStream fileStream = null;
        BufferedInputStream in = null;
        try {
            fileStream = new FileOutputStream(file);
            in = new BufferedInputStream(conn.getInputStream());
            byte[] buff = new byte[2048];
            long alreadyDownloadSize = 0;
            int downloadPercent = 0;
            int len;
            while ((len = in.read(buff)) != -1 && !cancellable.isCancel()) {
                fileStream.write(buff, 0, len);
                alreadyDownloadSize += len;
                if (callback != null) {
                    downloadPercent = (int) (100f * alreadyDownloadSize / fileSize);
                    callback.onProcess(downloadPercent);
                }
            }
            if (callback != null && downloadPercent != 100) {
                callback.onProcess(100);
            }
            fileStream.flush();
            responseParams.body = savePath.getBytes();
        } finally {
            if (fileStream != null) {
                fileStream.close();
            }
            if (in != null) {
                in.close();
            }
        }

    }


    private static String getFilePath(String downloadUrl, String savePath, String remoteFileName) {
        File file = new File(savePath);
        if (savePath.lastIndexOf('.') > savePath.lastIndexOf('/')) {
            file = new File(savePath);
        } else {
            String fileName = null;
            if (!TextUtils.isEmpty(remoteFileName)) {
                String[] items = remoteFileName.split(";");
                String keywork = "filename=";
                for (String item : items) {
                    if (!TextUtils.isEmpty(item) && item.contains(keywork) && keywork.length() + 1 < item.length()) {
                        fileName = item.substring(item.lastIndexOf(keywork) + keywork.length() + 1).replace("\"", "");
                        break;
                    }
                }
            }
            if (TextUtils.isEmpty(fileName)) {
                try {
                    downloadUrl = TextUtils.isEmpty(downloadUrl) ? null : URLDecoder.decode(downloadUrl, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (downloadUrl != null && downloadUrl.lastIndexOf('/') + 1 < downloadUrl.length()) {
                    fileName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
                } else {
                    fileName = String.valueOf(System.currentTimeMillis());
                }
            }
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(file.getPath() + File.separator + fileName);
        }
        return file.getPath();
    }


}
