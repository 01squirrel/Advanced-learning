package com.example.network.http;

import com.example.network.interceptor.NetWorkInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpHolder {
    private static  int TIMEOUT_READ = 15;
    private static  int TIMEOUT_CONNECTION = 15;
    private static  int TIMEOUT_WRITE = 15;

    private static OkHttpClient instance;
    private OkHttpHolder(){

    }

    public static int getTimeoutRead() {
        return TIMEOUT_READ;
    }

    public static void setTimeoutRead(int timeoutRead) {
        TIMEOUT_READ = timeoutRead;
    }

    public static int getTimeoutConnection() {
        return TIMEOUT_CONNECTION;
    }

    public static void setTimeoutConnection(int timeoutConnection) {
        TIMEOUT_CONNECTION = timeoutConnection;
    }

    public static int getTimeoutWrite() {
        return TIMEOUT_WRITE;
    }

    public static void setTimeoutWrite(int timeoutWrite) {
        TIMEOUT_WRITE = timeoutWrite;
    }

    //获取实例 OKHttp
    public static OkHttpClient getInstance(){
        if (instance == null){
            instance = new OkHttpClient.Builder()
                    //设置拦截器
                    .addInterceptor(new NetWorkInterceptor())
                    //设置超时
                    .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
                    //设置网络通道建立超时时间
                    .connectTimeout(TIMEOUT_CONNECTION,TimeUnit.SECONDS)
                    //设置数据写入超时
                    .writeTimeout(TIMEOUT_WRITE,TimeUnit.SECONDS)
                    //设置连接数
                    .connectionPool(new ConnectionPool(8,TIMEOUT_CONNECTION,TimeUnit.SECONDS))
                    //设置请求打印
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build();
        }
        return instance;
    }

    //使用自定义的超时时间，三者一致
    public static OkHttpClient getInstance(int timeout){
        if(instance == null){
            instance = new OkHttpClient.Builder()
                    .addInterceptor(new NetWorkInterceptor())
                    //timeout
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .connectTimeout(timeout,TimeUnit.SECONDS)
                    .writeTimeout(timeout,TimeUnit.SECONDS)
                    //set max connected number,time
                    .connectionPool(new ConnectionPool(8,timeout,TimeUnit.SECONDS))
                    //request print log
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build();
        }
        return instance;
    }
//重置
    public static void reset() {
        OkHttpHolder.instance = null;
        //OkHttpHolder.instanceSSL = null;
    }
}
