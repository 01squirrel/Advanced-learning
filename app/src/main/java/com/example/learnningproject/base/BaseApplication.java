package com.example.learnningproject.base;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraXConfig;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.learnningproject.broadcast.ManifestBroadcast;
import com.example.learnningproject.database.AppDataBase;
import com.example.learnningproject.database.dao.WordDao;
import com.example.learnningproject.database.entity.UserEntity;
import com.example.learnningproject.database.entity.Word;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.Unit;
import kotlin.jvm.Volatile;

public class BaseApplication extends Application implements CameraXConfig.Provider {

    private BroadcastReceiver br;
    public BaseApplication() {
        super();
    }
    private static volatile AppDataBase dataBase;
    private static final int NUMBER_OF_THREADS = 4;
    public static ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

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
    public AppDataBase getDbInstance(){
        UserEntity entity = new UserEntity(1,"bob","jojo",18);
        if(dataBase == null){
            synchronized (AppDataBase.class){
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
                                    databaseWriteExecutor.execute(()->{
                                        WordDao wordDao = dataBase.wordDao();
                                        wordDao.deleteAll();
                                        Word word = new Word("Hello");
                                        wordDao.insertWord(word);
                                        word = new Word("World");
                                        wordDao.insertWord(word);
                                    });

                                }
                            })
                            .build();
                }
            }
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

    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
                .setMinimumLoggingLevel(Log.ERROR)
                .setAvailableCamerasLimiter(CameraSelector.DEFAULT_FRONT_CAMERA)
                .build();
    }
}
