package com.yzx.chat.network.framework;


import android.support.annotation.NonNull;
import android.text.TextUtils;

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

    private final static int CONNECT_TIMEOUT = 10000;
    private final static int READ_TIMEOUT = 10000;

    @NonNull
    public static Result doGet(String remoteUrl, String params) {
        Result result = new Result();
        do {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(remoteUrl + "?" + params);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(true);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/raw");
                //  conn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
            } catch (IOException e) {
                e.printStackTrace();
                if (conn != null) {
                    conn.disconnect();
                }
                result.errorMessage = e.toString();
                break;
            }

            try {
                int responseCode = conn.getResponseCode();
                if (HttpURLConnection.HTTP_OK == responseCode) { //连接成功
                    readDataFromInputStreamByString(conn.getInputStream(), conn.getContentLength(), result);
                } else {
                    result.errorMessage = "HTTP ResponseCode " + responseCode;
                }
            } catch (IOException e) {
                e.printStackTrace();
                result.errorMessage = e.toString();
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
                conn.setRequestProperty("Content-Type", "application/raw");
                conn.setRequestProperty("Charset", "UTF-8");
//              conn.setRequestProperty("Accept-Encoding", "gzip");
//               conn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
            } catch (IOException e) {
                e.printStackTrace();
                if (conn != null) {
                    conn.disconnect();
                }
                result.errorMessage = e.toString();
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
                    e.printStackTrace();
                    conn.disconnect();
                    result.errorMessage = e.toString();
                    break;
                } finally {
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            result.errorMessage = e.toString();
                        }
                    }
                }
            }
            try {
                int responseCode = conn.getResponseCode();
                if (HttpURLConnection.HTTP_OK == responseCode) { //连接成功
                    readDataFromInputStreamByString(conn.getInputStream(), conn.getContentLength(), result);
                } else {
                    result.errorMessage = "HTTP ResponseCode " + responseCode;
                }
            } catch (IOException e) {
                e.printStackTrace();
                result.errorMessage = e.toString();
            }
        } while (false);

        return result;
    }

    private static void readDataFromInputStreamByString(InputStream in, int dataLength, Result result) {
        BufferedReader responseReader = null;
        try {
            char[] buff = new char[1024];
            StringBuilder content = new StringBuilder(dataLength);
            responseReader = new BufferedReader(new InputStreamReader(in));
            int readLen;
            while ((readLen = responseReader.read(buff)) != -1) {
                content.append(buff, 0, readLen);
            }
            result.response = content.toString();
            result.isSuccess = true;
        } catch (IOException e) {
            e.printStackTrace();
            result.errorMessage = e.getMessage();
        } finally {
            if (responseReader != null) {
                try {
                    responseReader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static class Result {
        public String response;
        public String errorMessage;
        public boolean isSuccess;
    }

}
