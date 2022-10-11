package com.example.learnningproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowMetrics;

import com.example.learnningproject.ui.main.MainFragment;

public class MainActivity extends AppCompatActivity {

    public enum WindowSizeClass{COMPACT,MEDIUM,EXPANDED}

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
        computeWindowSizeClasses();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void computeWindowSizeClasses(){
        WindowMetrics metrics = getWindowManager().getCurrentWindowMetrics();
        float widthDp = metrics.getBounds().width() / getResources().getDisplayMetrics().density;
        WindowSizeClass sizeClass;
        if(widthDp < 600f){
            sizeClass = WindowSizeClass.COMPACT;
        }else if(widthDp < 840f){
            sizeClass = WindowSizeClass.MEDIUM;
        }else {
            sizeClass = WindowSizeClass.EXPANDED;
        }
        float heightDp = metrics.getBounds().height() / getResources().getDisplayMetrics().density;
        if(heightDp < 480f){
            sizeClass = WindowSizeClass.COMPACT;
        }else if(heightDp < 900f){
            sizeClass = WindowSizeClass.MEDIUM;
        }else {
            sizeClass = WindowSizeClass.EXPANDED;
        }
    }
}