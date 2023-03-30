package com.example.network.interceptor;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

//设置公共参数
public class AddParameterInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        HttpUrl url = chain.request().url().newBuilder()
                .addQueryParameter("uid","edergt")
                .build();
        Request request = chain.request().newBuilder().url(url).build();
        return chain.proceed(request);
    }
}
