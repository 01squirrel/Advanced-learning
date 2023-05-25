package com.example.learnningproject.cameraSample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.internal.Camera2CamcorderProfileProvider;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraProvider;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.ViewPort;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;

import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.OrientationEventListener;
import android.view.Surface;

import com.example.learnningproject.R;

import java.util.List;

//cameraX 预览，图片分析，图片拍摄，视频拍摄

/**
 * ImageCapture: 配置camerax拍摄用例，可以控制用例操作的不同方面。
 * CameraXConfig: 可以更改在不同场景下的配置。
 * Preview：提供元数据输出，以便使用 Preview.getTargetRotation() 了解目标分辨率的旋转设置。
 * ImageAnalysis：提供元数据输出，以便了解图片缓冲区坐标相对于显示坐标的位置。
 * Exif: 元数据、缓冲区或同时更改两者，从而反映旋转设置。更改的值取决于 HAL 实现。
 */
public class CameraXActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_xactivity);
        //可以设置闪光灯模式、目标宽高比等等
        ImageCapture capture = new ImageCapture.Builder()
                .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                .build();
        //旋转设置
        OrientationEventListener listener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int i) {
                int rotation;
                if(i >= 45 && i <= 135){
                    rotation = Surface.ROTATION_270;
                }else if(i >= 135 && i < 225 ){
                    rotation = Surface.ROTATION_180;
                }else if(i >= 225 && i < 315){
                    rotation = Surface.ROTATION_90;
                }else  rotation = Surface.ROTATION_0;
                capture.setTargetRotation(rotation);
            }
        };
        listener.enable();
        //设置viewport,裁剪矩形
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //ViewPort viewPort = new ViewPort.Builder(new Rational(1280,960), .getRotation()).build();
            ImageAnalysis analysis = new ImageAnalysis.Builder().build();
            Preview preview = new Preview.Builder().build();
            UseCaseGroup group = new UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(analysis)
                    .addUseCase(capture)
                    .build();
            ProcessCameraProvider provider;

        }
    }
}