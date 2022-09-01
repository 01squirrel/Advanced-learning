package com.example.network.http;

public class MRequest {
    private static String token;
    public static String getToken(){
        return token == null ? "" : token;
    }
    public static void setToken(String token){
        MRequest.token = token;
    }
}
