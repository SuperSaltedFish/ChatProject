package com.yzx.chat.network.framework;


import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;


public class Http {

    private static final String BOUNDARY = "abcdefghijklmnopqrstuvwxyz123456789";

    private final static int CONNECT_TIMEOUT = 20000;
    private final static int READ_TIMEOUT = 20000;

    @NonNull
    public static Result doGet(String remoteUrl, String params) {
        Result result = new Result();
        do {
            HttpURLConnection conn = null;
            try {
                URL url;
                if (!TextUtils.isEmpty(params)) {
                    url = new URL(remoteUrl + "?" + params);
                } else {
                    url = new URL(remoteUrl);
                }
                conn = (HttpURLConnection) url.openConnection();
                conn.setUseCaches(true);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
            } catch (IOException e) {
                if (conn != null) {
                    conn.disconnect();
                }
                result.throwable = e;
                break;
            }

            try {
                int responseCode = conn.getResponseCode();
                result.responseCode = responseCode;
                if (HttpURLConnection.HTTP_OK == responseCode) { //连接成功
                    readDataFromInputStreamByString(conn.getInputStream(), conn.getContentLength(), result);
                } else {

                }
            } catch (IOException e) {
                result.throwable = e;
            }
        } while (false);

        return result;
    }

    @NonNull
    public static Result doPost(String remoteUrl, String params) {
        Result result = new Result();
        do {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(remoteUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "text/plain");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
            } catch (IOException e) {
                if (conn != null) {
                    conn.disconnect();
                }
                result.throwable = e;
                break;
            }

            if (!TextUtils.isEmpty(params)) {
                conn.setRequestProperty("Content-Length", String.valueOf(params.getBytes().length));
                BufferedWriter bufferedWriter = null;
                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream(), Charset.forName("Utf-8"));
                    bufferedWriter = new BufferedWriter(outputStreamWriter);
                    bufferedWriter.write(params);
                    bufferedWriter.flush();
                } catch (IOException e) {
                    conn.disconnect();
                    result.throwable = e;
                    break;
                } finally {
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            try {
                int responseCode = conn.getResponseCode();
                result.responseCode = responseCode;
                if (HttpURLConnection.HTTP_OK == responseCode) { //连接成功
                    readDataFromInputStreamByString(conn.getInputStream(), conn.getContentLength(), result);
                } else {

                }
            } catch (IOException e) {
                result.throwable = e;
            }
        } while (false);

        return result;
    }

    @NonNull
    public static Result doPostByMultiParams(String remoteUrl, Map<HttpParamsType,Map<String, Object>> params) {
        Result result = new Result();
        do {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(remoteUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
            } catch (IOException e) {
                if (conn != null) {
                    conn.disconnect();
                }
                result.throwable = e;
                break;
            }

            if (params != null && params.size() > 0) {
                DataOutputStream outStream;
                try {
                    outStream = new DataOutputStream(conn.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                    conn.disconnect();
                    result.throwable = e;
                    break;
                }

                ParamsType_For:
                for (Map.Entry<HttpParamsType,Map<String, Object>> entry : params.entrySet()) {
                    Map<String, Object> paramsMap = entry.getValue();
                    switch (entry.getKey()) {
                        case PARAMETER_HTTP:
                            String dataItem;
                            for (Map.Entry<String,Object> paramsItem : paramsMap.entrySet()) {
                                dataItem = String.format("--%s\r\nContent-Disposition: form-data; name=\"%s\"\r\nContent-Type: text/plain;charset=UTF-8\nContent-Transfer-Encoding: 8bit\r\n\r\n%s\r\n", BOUNDARY, paramsItem.getKey(), paramsItem.getValue());
                                try {
                                    outStream.writeBytes(dataItem);
                                } catch (IOException e) {
                                    result.throwable = e;
                                    try {
                                        outStream.close();
                                    } catch (IOException e2) {
                                        e2.printStackTrace();
                                    }
                                    conn.disconnect();
                                    break ParamsType_For;
                                }
                            }
                            break;
                        case PARAMETER_UPLOAD:
                            for (Map.Entry<String,Object> paramsItem : paramsMap.entrySet()) {
                                FileInputStream is = null;
                                List<String> value = (List<String>) paramsItem.getValue();
                                if (value == null) {
                                    continue;
                                }
                                try {
                                    for (String path : value) {
                                        File file = new File(path);
                                        if (!file.exists()) {
                                            continue;
                                        }
                                        String head = String.format("--%s\r\nContent-Disposition: form-data; name=\"%s\";filename=\"%s\"\r\nContent-Type: application/octet-stream\r\nContent-Transfer-Encoding: binary\r\n\r\n", BOUNDARY, paramsItem.getKey(), path);
                                        is = new FileInputStream(path);
                                        outStream.writeBytes(head);
                                        byte[] bytes = new byte[1024];
                                        int len;
                                        while ((len = is.read(bytes)) != -1) {
                                            outStream.write(bytes, 0, len);
                                        }
                                        outStream.writeBytes("\r\n");
                                        is.close();
                                    }
                                } catch (IOException e) {
                                    try {
                                        outStream.close();
                                    } catch (IOException e2) {
                                        e2.printStackTrace();
                                    }
                                    conn.disconnect();
                                    result.throwable = e;
                                    break ParamsType_For;
                                } finally {
                                    if (is != null) {
                                        try {
                                            is.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }
                            }
                            break;
                    }
                }
                try {
                    outStream.writeBytes(String.format("--%s--\r\n", BOUNDARY));
                    outStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        outStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                int responseCode = conn.getResponseCode();
                result.responseCode = responseCode;
                if (HttpURLConnection.HTTP_OK == responseCode) { //连接成功
                    readDataFromInputStreamByString(conn.getInputStream(), conn.getContentLength(), result);
                } else {

                }
            } catch (IOException e) {
                result.throwable = e;
            }
        } while (false);

        return result;
    }

    private static void readDataFromInputStreamByString(InputStream in, int dataLength, Result result) throws IOException {
        if (dataLength < 1) {
            dataLength = 16;
        }
        BufferedReader responseReader = null;
        try {
            char[] buff = new char[1024];
            StringBuilder content = new StringBuilder(dataLength);
            responseReader = new BufferedReader(new InputStreamReader(in));
            int readLen;
            while ((readLen = responseReader.read(buff)) != -1) {
                content.append(buff, 0, readLen);
            }
            result.responseContent = content.toString();
        } finally {
            if (responseReader != null) {
                try {
                    responseReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class Result {
        private int responseCode;
        private String responseContent;
        private Throwable throwable;

        public int getResponseCode() {
            return responseCode;
        }

        public String getResponseContent() {
            return responseContent;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }

        public void setResponseContent(String responseContent) {
            this.responseContent = responseContent;
        }

        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
        }
    }

}
