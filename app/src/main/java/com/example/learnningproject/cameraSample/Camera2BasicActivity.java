package com.example.learnningproject.cameraSample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;

import com.example.learnningproject.R;
import com.example.learnningproject.databinding.ActivityCamera2BasicBinding;

public class Camera2BasicActivity extends AppCompatActivity {
    private final int FLAGS_FULLSCREEN = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    private final long ANIMATION_FAST_MILLIS = 50L;
    ActivityCamera2BasicBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_camera2_basic);
        setContentView(binding.getRoot());
    }

    @Override
    protected void onResume() {
        super.onResume();
        long IMMERSIVE_FLAG_TIMEOUT = 500L;
        binding.fragmentContainer.postDelayed(()-> binding.fragmentContainer.setSystemUiVisibility(FLAGS_FULLSCREEN), IMMERSIVE_FLAG_TIMEOUT);
    }
}