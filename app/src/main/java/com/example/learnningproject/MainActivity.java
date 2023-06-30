package com.example.learnningproject;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowMetrics;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.example.learnningproject.contract.ExampleContract;
import com.example.learnningproject.database.entity.Song;
import com.example.learnningproject.database.entity.Word;
import com.example.learnningproject.presenter.ExamplePresenter;
import com.example.learnningproject.ui.main.MainFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ExampleContract.View {

    ActivityResultLauncher<String[]> launcher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                for(Map.Entry<String,Boolean> entry : result.entrySet()) {
                    if(!entry.getValue()) {
                        Log.i("TAG-PERMISSION:", entry.getKey()+" not granted");
                    }
                }
            });
    ExamplePresenter presenter;
    List<String> permissions;
    public static String KEY_EVENT_ACTION = "key_event_action";
    public static String KEY_EVENT_EXTRA = "key_event_extra";

    @Override
    public void showLoadingProgress() {
    }

    @Override
    public void dismissLoadingProgress() {

    }

    @Override
    public void onLoginSuccess(boolean isLogin) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//不加载像素，不占用内存；
        BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background, options);

        options.inSampleSize = calculateInSampleSize(options, 100, 100);//缩放
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = false;
        BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background, options);
    }

    public enum WindowSizeClass {COMPACT, MEDIUM, EXPANDED}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissions = Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        for(String permission : permissions) {
           if (ActivityCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_GRANTED) {
               permissions.remove(permission);
           }
        }
        if (permissions.size() > 0) {
            launcher.launch((String[]) permissions.toArray());
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in,//enter
                            R.anim.fade_out,//exit
                            R.anim.fade_in,//popEnter
                            R.anim.slide_out//popExit
                    )
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
        presenter = new ExamplePresenter(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            computeWindowSizeClasses();
            SystemClock.sleep(1000);
        }
        presenter.login("uuid0001");
        //获取电量信息
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = this.registerReceiver(null, filter);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        int chargeFlag = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargeFlag == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargeFlag == BatteryManager.BATTERY_PLUGGED_AC;
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int battery = level * 100 / scale;
        Log.i("BATTERY STATUS", "is charging " + isCharging
                + ",charge form status: USB--" + usbCharge + ",AC--" + acCharge
                + ",the battery is " + battery);
        //从其他应用接收数据
        Intent receiveIntent = getIntent();
        String action = receiveIntent.getAction();
        String type = receiveIntent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String text = receiveIntent.getStringExtra(Intent.EXTRA_TEXT);
                if(text != null) {//do something with the data
                    Log.i("tag","received message == "+text);
                }
            } else if (type.startsWith("image/")) {
                Uri imageUri = receiveIntent.getParcelableExtra(Intent.EXTRA_STREAM);
                if(imageUri != null) {
                    Log.i("tag","received message == "+imageUri);
                }
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                ArrayList<Uri> uris = receiveIntent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (uris.size() > 0) {
                    Log.i("tag","received message == "+uris);
                }
            }
        }
        //content://user_dictionary/word
        //gson使用
        Gson gson = new Gson();
        gson.fromJson("",new TypeToken<List<Word>>(){}.getType());
        //fastjson使用,示例
        JSON.toJSONString(new Word("word")); //序列化为JSON字符串
        JSON.toJSONString(new Word("hello"), SerializerFeature.BeanToArray);//序列化为json数组
        JSON.parseObject("jsonData",Word.class);//反序列化为对象
        JSON.parseObject("jsonData", Feature.SupportArrayToBean);
        JSON.parse("jsondata");//解析json数据
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void computeWindowSizeClasses() {
        WindowMetrics metrics = getWindowManager().getCurrentWindowMetrics();
        float widthDp = metrics.getBounds().width() / getResources().getDisplayMetrics().density;
        WindowSizeClass sizeClass;
        if (widthDp < 600f) {
            sizeClass = WindowSizeClass.COMPACT;
        } else if (widthDp < 840f) {
            sizeClass = WindowSizeClass.MEDIUM;
        } else {
            sizeClass = WindowSizeClass.EXPANDED;
        }
        float heightDp = metrics.getBounds().height() / getResources().getDisplayMetrics().density;
        if (heightDp < 480f) {
            sizeClass = WindowSizeClass.COMPACT;
        } else if (heightDp < 900f) {
            sizeClass = WindowSizeClass.MEDIUM;
        } else {
            sizeClass = WindowSizeClass.EXPANDED;
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int sampleSize = 1;
        int width = options.outWidth;
        int height = options.outHeight;
        if (width > reqWidth || height > reqHeight) {
            int halfWidth = width / 2;
            int halfHeight = height / 2;
            while ((halfWidth / sampleSize) > reqWidth && (halfHeight / sampleSize) > reqHeight) {
                sampleSize *= 2;
            }
        }
        return sampleSize;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Intent intent = new Intent(KEY_EVENT_ACTION);
            intent.putExtra(KEY_EVENT_EXTRA,keyCode);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    long startTime = 0L;
    @Override
    public void onBackPressed() {
        //startTime = System.currentTimeMillis();
        if(System.currentTimeMillis() - startTime > 1000) {
            Toast.makeText(this,"再次滑动退出", Toast.LENGTH_SHORT).show();
            startTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }
    public native String stringGet();
    static {
        System.loadLibrary("learnningproject");
    }
}