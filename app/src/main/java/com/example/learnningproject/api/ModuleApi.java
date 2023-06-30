package com.example.learnningproject.api;

import com.example.network.http.BaseBean;

import io.reactivex.rxjava3.core.Flowable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 示例，创建API请求模块
 */
public interface ModuleApi {
    @GET("index/login")
    Flowable<BaseBean<Object>> login(@Query("uid") String id);
}
