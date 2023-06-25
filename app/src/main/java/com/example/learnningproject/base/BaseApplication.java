package com.example.learnningproject.base;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraXConfig;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava2.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava2.RxDataStore;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.learnningproject.broadcast.ManifestBroadcast;
import com.example.learnningproject.database.AppDataBase;
import com.example.learnningproject.database.dao.WordDao;
import com.example.learnningproject.database.entity.UserEntity;
import com.example.learnningproject.database.entity.Word;
import com.example.network.BaseConfig;
import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Flowable;
import io.reactivex.Single;

public class BaseApplication extends Application implements CameraXConfig.Provider {

    RxDataStore<Preferences> rxDataStore = new RxPreferenceDataStoreBuilder(this,"data_store").build();
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
        crashInit();
        BaseConfig config = new BaseConfig();
        config.setBASE_URL("176.16.4.1");
        //从preferences dataStore读取内容
        Preferences.Key<String> netUrl = PreferencesKeys.stringKey("baseUrl");
        Flowable<String> flowable = rxDataStore.data().map(prefs ->prefs.get(netUrl));
        //写入内容
        Single<Preferences> updateResult = rxDataStore.updateDataAsync(prefsIn ->{
            MutablePreferences preferences = prefsIn.toMutablePreferences();
            String current = prefsIn.get(netUrl);
            preferences.set(netUrl,current != null ? "192.168.10.1" : "176.16.4.1");
            return Single.just(preferences);
        });
    }

    private void crashInit() {
        //增加上报进程控制
        String packageName = this.getPackageName();
        String processName = getProcessName(Process.myPid());
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(this);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        strategy.setDeviceID("AP101S")
                .setAppVersion("com.example.learnningproject")
                .setAppReportDelay(15000);
        strategy.setCrashHandleCallback(new CrashReport.CrashHandleCallback(){
            @Override
            public synchronized Map<String, String> onCrashHandleStart(int crashType, String errorType,
                                                                       String errorMessage, String errorStack) {
                LinkedHashMap<String,String> map = new LinkedHashMap<>();
                map.put("crashType",String.valueOf(crashType));
                map.put("error",errorMessage);
                return map;
            }

            @Override
            public synchronized byte[] onCrashHandleStart2GetExtraDatas(int crashType, String errorType,
                                                                        String errorMessage, String errorStack) {
                try {
                    return "Extra data.".getBytes(StandardCharsets.UTF_8);
                } catch (Exception e) {
                    return null;
                }
            }
        });
        CrashReport.initCrashReport(this,strategy);
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
                                    Executors.newSingleThreadExecutor().execute(()-> dataBase.userLibraryDao().addUser(entity));
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
    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号  /proc/pid/cmdline
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
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
