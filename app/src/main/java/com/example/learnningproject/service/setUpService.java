package com.example.learnningproject.service;

import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.Nullable;

/**
 * 创建启动服务-通过继承intentService
 * 大多数启动服务无需同时处理多个请求,最佳选择是利用 IntentService 类实现服务。
 */

public class setUpService extends IntentService {

    public setUpService() {
        super("intentService");
    }

    /**
     * @param name 服务名
     * @deprecated
     */
    public setUpService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            Thread.sleep(5000);//在这里完成相关工作，示例只是sleep 5s
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }
}
