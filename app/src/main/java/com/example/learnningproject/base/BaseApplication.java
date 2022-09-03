package com.example.learnningproject.base;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.example.learnningproject.broadcast.ManifestBroadcast;

public class BaseApplication extends Application {

    private BroadcastReceiver br;
    public BaseApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /* 只要注册上下文有效，上下文注册的接收器就会接收广播。
         * 例如，如果您在 Activity 上下文中注册，只要 Activity 没有被销毁，您就会收到广播。
         * 如果您在应用上下文中注册，只要应用在运行，您就会收到广播。
         */
        br = new ManifestBroadcast();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(br,filter);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(br);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
