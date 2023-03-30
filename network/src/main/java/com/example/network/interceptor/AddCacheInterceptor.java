package com.example.network.interceptor;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AddCacheInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
        Response response = chain.proceed(request);
        try {
            response.newBuilder().header("cache-control", "public,max-age=0")
                    .removeHeader("Retrofit")// 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
        return response;
    }
}
