package com.example.network.interceptor;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.example.network.http.MRequest;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class NetWorkInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String token  = MRequest.getToken();
        if(TextUtils.isEmpty(token)){
            return chain.proceed(chain.request());
        }else {
            Request oldRequest = chain.request();
            Request request = oldRequest.newBuilder()
                    .header("Authorization",token)
                    .method(oldRequest.method(), oldRequest.body())
                    .build();
            return chain.proceed(request);
        }
    }
}
