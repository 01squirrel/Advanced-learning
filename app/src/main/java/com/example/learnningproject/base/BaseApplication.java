package com.example.learnningproject.base;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.learnningproject.broadcast.ManifestBroadcast;
import com.example.learnningproject.database.AppDataBase;
import com.example.learnningproject.database.entity.UserEntity;

import java.util.concurrent.Executors;

import kotlin.Unit;

public class BaseApplication extends Application {

    private BroadcastReceiver br;
    public BaseApplication() {
        super();
    }
    private AppDataBase dataBase;

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
    public AppDataBase getDb(){
        UserEntity entity = new UserEntity(1,"bob","jojo",18);
        if(dataBase == null){
            dataBase = Room.databaseBuilder(getApplicationContext(),AppDataBase.class,"learn_database")
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            //预填充数据库,示例
                            Executors.newSingleThreadExecutor().execute(()->{
                                dataBase.userLibraryDao().addUser(entity);
                            });
                        }
                    })
                    .build();
        }
        return dataBase;
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
