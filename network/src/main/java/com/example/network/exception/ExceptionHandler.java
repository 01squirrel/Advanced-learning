package com.example.network.exception;


import com.example.network.R;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.orhanobut.logger.Logger;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;

import javax.net.ssl.SSLException;

import retrofit2.HttpException;

//错误异常处理
public class ExceptionHandler {
    public static final int UNKNOWN_ERROR = 1000;//未知错误
    public static final int PARSE_ERROR = 1001;//解析错误
    public static final int NETWORK_ERROR = 1002;//网络错误
    public static final int HTTP_ERROR = 1003;//协议错误
    public static final int SSL_ERROR = 1004;//证书错误
    public static final int TIMEOUT_ERROR = 1005;//连接超时
    public static final int NO_NET_ERROR = 1006;//无网络错误
    public static final int DATA_ERROR = 1007;//数据异常
    public static final int NET_CONNECT_ERROR = 1008;//网络连接异常

    public static class ResponseThrowable extends Exception{
        private int code;
        private String msg;
        private int msgResId;
        ResponseThrowable(int code,String msg,int msgResId){
            this.code = code;
            this.msg = msg;
            this.msgResId = msgResId;
        }
        ResponseThrowable(Throwable e,int code) {
            super(e);
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public int getMsgResId() {
            return msgResId;
        }

        public void setMsgResId(int msgResId) {
            this.msgResId = msgResId;
        }
    }

    public static class ServerException extends RuntimeException{
        private int code;
        private String msg;
        private int msgResId;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public int getMsgResId() {
            return msgResId;
        }

        public void setMsgResId(int msgResId) {
            this.msgResId = msgResId;
        }
    }


    public static ResponseThrowable handlerException(Throwable e){
        ResponseThrowable et;
        if(e instanceof HttpException){
            et = new ResponseThrowable(e,HTTP_ERROR);
            et.msg = "网络错误";
            et.msgResId = R.string.network_error;
        }else if(e instanceof ServerException){
            ServerException resultException = (ServerException) e;
            et = new ResponseThrowable(resultException, resultException.code);
            et.msg = resultException.getMsg();
            et.msgResId = resultException.getMsgResId();
        }else if(e instanceof JsonParseException || e instanceof JSONException || e instanceof ParseException){
            et = new ResponseThrowable(e,PARSE_ERROR);
            et.msg = "解析错误";
            et.msgResId = R.string.parse_error;
        }else if(e instanceof ConnectException){
            et = new ResponseThrowable(e,NET_CONNECT_ERROR);
            et.msg = "网络连接失败";
            et.msgResId = R.string.network_error;
        }else if(e instanceof SSLException){
            et = new ResponseThrowable(e,SSL_ERROR);
            et.msg = "证书验证失败";
            et.msgResId = R.string.certificate_error;
        }else if(e instanceof ConnectTimeoutException){
            et = new ResponseThrowable(e,TIMEOUT_ERROR);
            et.msg = "连接超时";
            et.msgResId = R.string.connection_timeout;
        }else if(e instanceof UnknownHostException || e instanceof SocketException){
            et = new ResponseThrowable(e,NET_CONNECT_ERROR);
            et.msg = "网络连接异常";
            et.msgResId = R.string.unknown_host;
        }else{
            et = new ResponseThrowable(e,UNKNOWN_ERROR);
            et.msg = "未知错误";
            et.msgResId = R.string.unknown_error;
        }
        Gson gson = new Gson();
        Logger.e(gson.toJson(et));
        return et;
    }
}
