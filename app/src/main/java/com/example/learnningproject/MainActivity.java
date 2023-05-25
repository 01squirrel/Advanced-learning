package com.example.learnningproject;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.WindowMetrics;

import com.example.learnningproject.contract.ExampleContract;
import com.example.learnningproject.presenter.ExamplePresenter;
import com.example.learnningproject.ui.main.MainFragment;

public class MainActivity extends AppCompatActivity implements ExampleContract.View {

    ActivityResultLauncher<String[]> launcher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {

            });
    ExamplePresenter presenter;
    String[] permissions;

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
        permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        launcher.launch(permissions);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
        presenter = new ExamplePresenter(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            computeWindowSizeClasses();
            SystemClock.sleep(1000);
        }
        presenter.login("uuid0001");
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
}