package com.example.learnningproject.api;

import com.example.network.http.RetrofitHolder;

/**
 * API模块统一管理
 */
public class ApiHolder {
    private static ModuleApi moduleApi;
    public static ModuleApi getModuleApi(){
        if(moduleApi == null){
            moduleApi = RetrofitHolder.getInstance().create(ModuleApi.class);
        }
        return moduleApi;
    }
}
