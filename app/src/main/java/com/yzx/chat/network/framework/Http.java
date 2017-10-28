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

    private final static int CONNECT_TIMEOUT = 10000;
    private final static int READ_TIMEOUT = 10000;

    @NonNull
    public static Result doGet(String remoteUrl, String params) {
        Result result = new Result();
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        if(remoteUrl.contains("getSecretKey")){
//            result.isSuccess=true;
//            result.response = "{ \"status\": 200, \"data\": { \"secretKey\": \"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCgXeud2ZEneOpJ1gy67F4YmI7E\n" +
//                    "IZtZ927zuU29v1J5OyG8QqI09UDo7izh78qMMRV36pIGM0hbt4lKKsw3oB1migKf\n" +
//                    "wG2vPvzhkcj+hdwtBB7XkCPBTHpsKkiTG4yyqQs2gZcFLn/K2m9DuqvgZ+XgQf98\n" +
//                    "inykJaFkaltJCEwkawIDAQAB\n\" }, \"message\": \"asd\" }";
//        }


        do {
            HttpURLConnection conn = null;
            try {
                URL url;
                if(!TextUtils.isEmpty(params)){
                     url = new URL(remoteUrl + "?" + params);
                }else {
                     url = new URL(remoteUrl);
                }
                conn = (HttpURLConnection) url.openConnection();
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
                e.printStackTrace();
                result.throwable = e;
            }
        } while (false);

        return result;
    }

    @NonNull
    public static Result doPost(String remoteUrl, String params) {
        Result result = new Result();
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        if(remoteUrl.contains("obtainSMSCode")){
//            result.isSuccess=true;
//            result.response = "fN2CLN8S+6NMFD2P7T4W3apX9dHYfbKWoPZ8zp6GVC1ahhOxZ5GLLOUYPt6Fkh//8Mh9ec5FMrpd\n" +
//                    "8YPZGP9ZQVRTTXoCYEt/QK5F26Q69ix4BtlZDvl7t+4c8w8y9/UDwcqTiHiqy1nxEwe2LcwPY/K8\n" +
//                    "a7uoyZdT4nZK4mVReVJnikSs7dTgAzn+xWCBLPuFU07KvyW25z8O0XDXAObuwLv+UeeAjBzwjsnc\n" +
//                    "I+FiyXzmuvBh6oOS4G9XA635u3M2xxtaaJEsDuVzyJySsgziwmF0lYKT0x98uqsZ1V6vv7ixXkuG\n" +
//                    "z5ydy4p6qjO8OTou78r5VFwRTuHn8mD5YGielw==\n";
//        }else if(remoteUrl.contains("login")){
//            result.isSuccess=true;
//        }

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
                e.printStackTrace();
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
                    e.printStackTrace();
                    conn.disconnect();
                    result.throwable = e;
                    break;
                } finally {
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            result.throwable = e;
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
                e.printStackTrace();
                result.throwable = e;
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
            result.responseContent = content.toString();
        } catch (IOException e) {
            e.printStackTrace();
            result.throwable= e;
        } finally {
            if (responseReader != null) {
                try {
                    responseReader.close();
                } catch (IOException e) {
                    result.throwable=e;
                }
            }
        }
    }

    public static class Result {
        public int responseCode;
        public String responseContent;
        public Throwable throwable;
    }

}
