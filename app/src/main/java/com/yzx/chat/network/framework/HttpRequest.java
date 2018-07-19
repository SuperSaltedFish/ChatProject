package com.yzx.chat.network.framework;


import java.io.OutputStream;
import java.util.Map;

public interface HttpRequest {

    String MULTIPART_BOUNDARY = "abcdefghijklmnopqrstuvwxyz123456789";
    String METHOD_GET = "GET";
    String METHOD_POST = "POST";
    String METHOD_PUT = "PUT";
    String METHOD_DELETE = "DELETE";


    String url();

    String method();

    Map<String, String> headers();

    boolean isMultipart();

    boolean isFormUrlEncoded();

    boolean isBodyWritable();

    void writeBodyTo(HttpConverter converter, OutputStream outputStream) throws Exception;

}
