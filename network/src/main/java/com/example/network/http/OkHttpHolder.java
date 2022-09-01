package com.example.network.http;

import com.example.network.interceptor.NetWorkInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpHolder {
    private static final int TIMEOUT_READ = 15;
    private static final int TIMEOUT_CONNECTION = 15;

    private static OkHttpClient instance;
    private OkHttpHolder(){

    }
    //获取实例 OKHttp
    public static OkHttpClient getInstance(){
        if (instance == null){
            instance = new OkHttpClient.Builder()
                    //设置拦截器
                    .addInterceptor(new NetWorkInterceptor())
                    //设置超时
                    .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
                    .connectTimeout(TIMEOUT_CONNECTION,TimeUnit.SECONDS)
                    //设置连接数
                    .connectionPool(new ConnectionPool(8,TIMEOUT_CONNECTION,TimeUnit.SECONDS))
                    //设置请求打印
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build();
        }
        return instance;
    }

    public static OkHttpClient getInstance(int timeout){
        if(instance == null){
            instance = new OkHttpClient.Builder()
                    .addInterceptor(new NetWorkInterceptor())
                    //timeout
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .connectTimeout(timeout,TimeUnit.SECONDS)
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
