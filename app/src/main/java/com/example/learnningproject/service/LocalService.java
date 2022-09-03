package com.example.learnningproject.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.Random;

//绑定服务，通过扩展binder类

/**
 * 以下为设置方式：
 *
 * 1.在您的服务中，创建可执行以下某种操作的 Binder 实例：
 *      包含客户端可调用的公共方法。
 *      返回当前的 Service 实例，该实例中包含客户端可调用的公共方法。
 *      返回由服务承载的其他类的实例，其中包含客户端可调用的公共方法。
 * 2.从 onBind() 回调方法返回此 Binder 实例。
 * 3.在客户端中，从 onServiceConnected() 回调方法接收 Binder，并使用提供的方法调用绑定服务。
 */
public class LocalService extends Service {

    /**
     * LocalBinder 为客户端提供 getService() 方法，用于检索 LocalService 的当前实例
     */
    public class LocalBinder extends Binder{
        LocalService getService(){
            return LocalService.this;
        }
    }

    private final IBinder binder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final Random num = new Random();

    public int getRandomNumber(){
        return num.nextInt(100);
    }
}
