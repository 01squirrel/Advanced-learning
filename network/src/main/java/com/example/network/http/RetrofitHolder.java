package com.example.network.http;

import android.text.TextUtils;
import android.util.Log;

import com.example.network.BaseConfig;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitHolder {
    private static final String TAG = RetrofitHolder.class.getSimpleName();
    private static String BaseUrl = "";
    private static Retrofit instance;

    private RetrofitHolder(){}

    public static Retrofit getInstance(){
        if(TextUtils.isEmpty(BaseUrl)){
            Log.e(TAG, "getInstance:  BaseUrl is initialized" );
            initBaseUrl();
        }
        if(instance == null){
            instance = new Retrofit.Builder()
                    //设置URL
                    .baseUrl(BaseUrl)
                    //设置OKHttpclient
                    .client(OkHttpHolder.getInstance())
                    //设置转换gson
                    .addConverterFactory(GsonConverterFactory.create())
                    // 适配rxjava，目的在于使用观察者模式，分解上层请求的过程，便于我们横加干预（比如请求嵌套）
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return instance;
    }

    public static Retrofit getInstance(int timeout){
        if(TextUtils.isEmpty(BaseUrl)){
            Log.e(TAG, "getInstance:  BaseUrl is initialized" );
        }
        if(instance == null){
            instance = new Retrofit.Builder()
                    .baseUrl(BaseUrl)
                    .client(OkHttpHolder.getInstance(timeout))
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return instance;
    }

    //初始化baseurl
    public static void initBaseUrl(){
        RetrofitHolder.BaseUrl = new BaseConfig().getBASE_URL();
    }
    public static void initBaseUrl(String baseUrl){RetrofitHolder.BaseUrl = baseUrl;}
    //reset
    public static void reset(){
        RetrofitHolder.instance = null;
        OkHttpHolder.reset();
    }

    public static String getBaseUrl(){
        return BaseUrl;
    }
}
