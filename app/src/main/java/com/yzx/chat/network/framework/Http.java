package com.yzx.chat.network.framework;


import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.yzx.chat.util.LogUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class Http {

    private final static int CONNECT_TIMEOUT = 30000;
    private final static int READ_TIMEOUT = 30000;

    @NonNull
    public static Result doGet(String remoteUrl, String params) {
        LogUtil.e("开始访问："+remoteUrl);
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
//                conn.setRequestProperty("Content-Type", "text/plain");
                //  conn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
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
        LogUtil.e("开始访问："+remoteUrl);
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
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Charset", "UTF-8");
//               conn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
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

    private static void readDataFromInputStreamByString(InputStream in, int dataLength, Result result) throws IOException {
        if(dataLength<1){
            dataLength=16;
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
