package com.example.learnningproject.cameraSample;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.display.DisplayManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraState;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.learnningproject.MainActivity;
import com.example.learnningproject.R;
import com.example.learnningproject.databinding.ActivityCameraXactivityBinding;
import com.example.learnningproject.databinding.CameraUiContainerBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//cameraX 预览，图片分析，图片拍摄，视频拍摄

/**
 * ImageCapture: 配置camerax拍摄用例，可以控制用例操作的不同方面。
 * CameraXConfig: 可以更改在不同场景下的配置。
 * Preview：提供元数据输出，以便使用 Preview.getTargetRotation() 了解目标分辨率的旋转设置。
 * ImageAnalysis：提供元数据输出，以便了解图片缓冲区坐标相对于显示坐标的位置。
 * Exif: 元数据、缓冲区或同时更改两者，从而反映旋转设置。更改的值取决于 HAL 实现。
 */
public class CameraXActivity extends AppCompatActivity implements LifecycleOwner,Observer<CameraState> {
    ActivityCameraXactivityBinding cameraxViewBinding;
    CameraUiContainerBinding cameraUiContainerBinding;
    private File outputDirectory;//共享文件目录
    private List<String> EXTENSION_lIST = new ArrayList<>();

    private int revealId = -1; //显示
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private static final String FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final String PHOTO_EXTENSION = ".jpg";
    private static final double RATIO_4_3_VALUE = 4.0 / 3.0;
    private static final double RATIO_16_9_VALUE = 16.0 / 9.0;

    private LocalBroadcastManager localBroadcastManager;
    private final long IMMERSIVE_FLAG_TIMEOUT = 500L;
    private Preview preview;//预览示例
    private ImageCapture imageCapture;//照片用例
    private ImageAnalysis imageAnalysis;//照片分析用例
    private Camera camera;//相机
    private ProcessCameraProvider cameraProvider;//相机进程提供者
    private WindowManager windowManager;
    private DisplayManager displayManager = (DisplayManager) this.getSystemService(DISPLAY_SERVICE);
    private ExecutorService cameraExecutor;//用于执行相机操作
    private BroadcastReceiver volumeReceiver;//监听音量键
    private DisplayManager.DisplayListener displayListener;//监听设备方向变化

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraxViewBinding =  ActivityCameraXactivityBinding.inflate(getLayoutInflater());
        setContentView(cameraxViewBinding.getRoot());
        initViewAndData();
    }

    private void initViewAndData() {
        EXTENSION_lIST.add("JPG");
        cameraExecutor = Executors.newSingleThreadExecutor();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        volumeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getIntExtra(MainActivity.KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)
                        == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    cameraUiContainerBinding.cameraCaptureButton.setOnClickListener(v -> {
                        v.performClick();
                        v.invalidate();
                        v.setPressed(true);
                        v.postDelayed(()->{
                            v.invalidate();
                            v.setPressed(false);
                        },50L);
                    });
                }
            }
        };
        // Set up the intent filter that will receive events from our main activity
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.KEY_EVENT_ACTION);
        localBroadcastManager.registerReceiver(volumeReceiver,filter);
        //每当设别方向发生改变时更新
        displayListener = new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int displayId) {

            }

            @Override
            public void onDisplayRemoved(int displayId) {

            }

            @Override
            public void onDisplayChanged(int displayId) {
                if (displayId == revealId) {
                    int rotation = cameraxViewBinding.getRoot().getDisplay().getRotation();
                    Log.d("CAMERA_ORIENTATION", "Rotation changed: "+ rotation );
                    imageCapture.setTargetRotation(rotation);
                    imageAnalysis.setTargetRotation(rotation);
                }
            }
        };
        displayManager.registerDisplayListener(displayListener,null);
        //初始化windowManager以检索显示指标
        windowManager = getWindowManager();
        outputDirectory = getOutputDirectory();
        cameraxViewBinding.pvView.post(()->{
            revealId = cameraxViewBinding.pvView.getDisplay().getDisplayId();
            updateCameraUI();
            //启动相机及其用例
            setUpCamera();
        });

    }

    private void setUpCamera() {
        //创建用于绑定相机生命周期
       ListenableFuture<ProcessCameraProvider> cameraProviderFuture =  ProcessCameraProvider.getInstance(this);
       cameraProviderFuture.addListener(() ->{
           try {
               cameraProvider = cameraProviderFuture.get();
               if (hasBackCamera()) {
                   lensFacing = CameraSelector.LENS_FACING_BACK;
               } else if (hasFrontCamera()) {
                   lensFacing = CameraSelector.LENS_FACING_FRONT;
               } else {
                   throw new IllegalStateException("Back and front camera are unavailable");
               }
               //启用或禁用相机之间的切换
               updateCameraSwitchButton();
               bindCameraUseCases();
           } catch (ExecutionException | InterruptedException e) {
               e.printStackTrace();
           }
       }, ContextCompat.getMainExecutor(this));
    }

    private void updateCameraUI() {
        cameraUiContainerBinding.getRoot().removeAllViews();
        cameraUiContainerBinding = CameraUiContainerBinding.inflate(getLayoutInflater(),cameraxViewBinding.getRoot(),true);
        //加载最新的图片
        new Thread(()->{
           File[] files = outputDirectory.listFiles();
           List<File> remains = new ArrayList<>();
           if(files!= null) {
               for(File file:files) {
                   if (!EXTENSION_lIST.contains(file.getName().split("\\.")[1].toUpperCase(Locale.ROOT))) {
                       remains.remove(file);
                   }
               }
               setThumbnail(Uri.fromFile(remains.get(0)));
           }
        }).start();
        //拍照监听
        cameraUiContainerBinding.cameraCaptureButton.setOnClickListener(view -> {
            //存放照片的文件
            File photoFile = createFile(outputDirectory,FILENAME,PHOTO_EXTENSION);
            //设置照片元数据
            ImageCapture.Metadata metadata = new ImageCapture.Metadata();
            metadata.setReversedHorizontal(lensFacing == CameraSelector.LENS_FACING_FRONT);
            //保存新捕获图像的选项。
            ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions
                    .Builder(photoFile)
                    .setMetadata(metadata)
                    .build();
            imageCapture.takePicture(options, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    Uri output = outputFileResults.getSavedUri();
                    if(output == null) output = Uri.fromFile(photoFile);
                    Log.d("TAG", "Photo capture succeeded:"+ output);
                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        setThumbnail(output);
                    }
                    //发送隐式广播通知
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        sendBroadcast(new Intent(android.hardware.Camera.ACTION_NEW_PICTURE));
                    }
                    //如果是内部存储图片
                    File file  = new File(String.valueOf(output));
                    String extension = file.getName().split("\\.")[1];
                    String mimeTypeMap = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                    MediaScannerConnection connection = new MediaScannerConnection(CameraXActivity.this, new MediaScannerConnection.MediaScannerConnectionClient() {
                        @Override
                        public void onMediaScannerConnected() {
                            Log.i("TAG", "media scanner connected ");
                        }

                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.d("TAG", "Image capture scanned into media store: "+uri);
                        }
                    });
                    connection.scanFile(file.getAbsolutePath(),mimeTypeMap);
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Log.e("TAG", "Photo capture failed: "+ exception.getMessage(), exception);
                }
            });
            //模拟拍照闪烁效果,api>23
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                cameraxViewBinding.getRoot().postDelayed(() ->{
                    cameraxViewBinding.getRoot().setForeground(new ColorDrawable(Color.WHITE));
                    cameraxViewBinding.getRoot().postDelayed(() ->{
                        cameraxViewBinding.getRoot().setForeground(null);
                    },50L);
                },500L);
            }
        });

        //切换摄像头方向
        cameraUiContainerBinding.cameraSwitchButton.setOnClickListener(view ->{
            view.setEnabled(false);
            if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                lensFacing = CameraSelector.LENS_FACING_FRONT;
            } else {
                lensFacing = CameraSelector.LENS_FACING_BACK;
            }
            //重新绑定相机用例
            bindCameraUseCases();
        });
    }

    private void bindCameraUseCases() {
        //获取屏幕预览宽高比
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        Log.d("METRICS", "Screen metrics: "+metrics.widthPixels + "X" + metrics.heightPixels);
        int screenAspect = aspectRatio(metrics.widthPixels,metrics.heightPixels);
        Log.d("SCREEN RATIO", "Preview aspect ratio: " + screenAspect);
        final int[] rotation = new int[1];
        OrientationEventListener listener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int i) {
                if(i >= 45 && i <= 135){
                    rotation[0] = Surface.ROTATION_270;
                }else if(i >= 135 && i < 225 ){
                    rotation[0] = Surface.ROTATION_180;
                }else if(i >= 225 && i < 315){
                    rotation[0] = Surface.ROTATION_90;
                }else  rotation[0] = Surface.ROTATION_0;
            }
        };
        listener.enable();
        //屏幕自然方向的旋转
        //int rotation = cameraxViewBinding.pvView.getDisplay().getRotation();
        //相机设置要求
        CameraSelector selector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
        //屏幕预览用例
        preview = new Preview.Builder()
                .setTargetAspectRatio(screenAspect)
                .setTargetRotation(rotation[0])
                .build();
        //拍照用例
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(screenAspect)
                .setTargetRotation(rotation[0])
                .build();
        //图片分析用例
        imageAnalysis = new ImageAnalysis.Builder()
                .setTargetAspectRatio(screenAspect)
                .setTargetRotation(rotation[0])
                .build();
        imageAnalysis.setAnalyzer(cameraExecutor, new AnalyzerListener());
        cameraProvider.unbindAll();//重新绑定之前先取消绑定
        if (camera != null) {
            //取消观察前一个相机实例
            camera.getCameraInfo().getCameraState().removeObserver(this);
        }
        try {
            camera = cameraProvider.bindToLifecycle(this,selector,preview,imageCapture,imageAnalysis);
            preview.setSurfaceProvider(cameraxViewBinding.pvView.getSurfaceProvider());
            observeCameraState(camera.getCameraInfo());
        } catch (Exception exc) {
            Log.e("TAG", "Use case binding failed", exc);
        }
    }

    private File getOutputDirectory() {
            Application application = getApplication();
            File mediaFile = new File(this.getExternalMediaDirs()[0],"CameraX_Image");
            if (mediaFile.mkdirs()) {
                return mediaFile;
            } else {
                return application.getFilesDir();
            }
        }
    private File createFile(File folder,String extension,String format) {
        return new File(folder,SimpleDateFormat.getDateInstance().format(System.currentTimeMillis())+extension);
    }

    private void setThumbnail(Uri uri) {
        cameraUiContainerBinding.photoViewButton.post(() ->{
            int padding = (int)(getResources().getDimension(R.dimen.stroke_small));
            cameraUiContainerBinding.photoViewButton.setPadding(padding,padding,padding,padding);
            Glide.with(cameraUiContainerBinding.photoViewButton)
                    .load(uri).
                    apply(RequestOptions.circleCropTransform())
                    .into(cameraUiContainerBinding.photoViewButton);
        });
    }

    private int aspectRatio(int width, int height) {
        double preRatio = (double)Math.max(width,height) / Math.min(width,height);
        if (Math.abs(preRatio - RATIO_4_3_VALUE) <= Math.abs(preRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3;
        } else {
            return AspectRatio.RATIO_16_9;
        }
    }

    private void observeCameraState(CameraInfo info){
        info.getCameraState().observe(this, this);
    }

    private boolean hasBackCamera() {
        boolean hasBack = false;
        try {
            hasBack = cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
        return hasBack;
    }

    private boolean hasFrontCamera() {
        boolean hasFront = false;
        try {
            hasFront = cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA);
        }  catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
        return hasFront;
    }

    private void updateCameraSwitchButton() {
        cameraUiContainerBinding.cameraSwitchButton.setEnabled(hasBackCamera() && hasFrontCamera());
    }
    @Override
    protected void onResume() {
        super.onResume();
        cameraxViewBinding.pvView.postDelayed(this::hideSystemUI, IMMERSIVE_FLAG_TIMEOUT);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        bindCameraUseCases();
        updateCameraSwitchButton();
    }

    private void hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
        WindowInsetsControllerCompat controllerCompat = new WindowInsetsControllerCompat(getWindow(),cameraxViewBinding.getRoot());
        controllerCompat.hide(WindowInsetsCompat.Type.systemBars());
        controllerCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    @Override
    public void onChanged(CameraState cameraState) {
            switch (cameraState.getType()) {
                case PENDING_OPEN: // Ask the user to close other camera apps
                    Toast.makeText(this,
                            "CameraState: Pending Open",
                            Toast.LENGTH_SHORT).show();
                case OPENING:
                    // Show the Camera UI
                    Toast.makeText(CameraXActivity.this,
                            "CameraState: Opening",
                            Toast.LENGTH_SHORT).show();
                case OPEN:
                    // Setup Camera resources and begin processing
                    Toast.makeText(CameraXActivity.this,
                            "CameraState: Open",
                            Toast.LENGTH_SHORT).show();
                case CLOSING:
                    // Close camera UI
                    Toast.makeText(CameraXActivity.this,
                            "CameraState: Closing",
                            Toast.LENGTH_SHORT).show();
                case CLOSED:
                    // Free camera resources
                    Toast.makeText(CameraXActivity.this,
                            "CameraState: Closed",
                            Toast.LENGTH_SHORT).show();
            }
            if (cameraState.getError() != null) {
                switch (cameraState.getError().getCode()) {
                    case CameraState.ERROR_STREAM_CONFIG:
                        //打开错误
                        // Make sure to setup the use cases properly
                        Toast.makeText(CameraXActivity.this,
                                "Stream config error",
                                Toast.LENGTH_SHORT).show();
                    case CameraState.ERROR_CAMERA_IN_USE:
                        // Close the camera or ask user to close another camera app that's using the
                        // camera
                        Toast.makeText(CameraXActivity.this,
                                "Camera in use",
                                Toast.LENGTH_SHORT).show();
                    case CameraState.ERROR_OTHER_RECOVERABLE_ERROR:
                        Toast.makeText(CameraXActivity.this,
                                "Other recoverable error",
                                Toast.LENGTH_SHORT).show();
                    case CameraState.ERROR_MAX_CAMERAS_IN_USE:
                        // Close another open camera in the app, or ask the user to close another
                        // camera app that's using the camera
                        Toast.makeText(CameraXActivity.this,
                                "Max cameras in use",
                                Toast.LENGTH_SHORT).show();
                    case CameraState.ERROR_CAMERA_DISABLED:
                        // Ask the user to enable the device's cameras
                        Toast.makeText(CameraXActivity.this,
                                "Camera disabled",
                                Toast.LENGTH_SHORT).show();
                    case CameraState.ERROR_CAMERA_FATAL_ERROR:
                        // Ask the user to reboot the device to restore camera function
                        Toast.makeText(CameraXActivity.this,
                                "Fatal error",
                                Toast.LENGTH_SHORT).show();
                    case CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED:
                        // Ask the user to disable the "Do Not Disturb" mode, then reopen the camera
                        Toast.makeText(CameraXActivity.this,
                                "Do not disturb mode enabled",
                                Toast.LENGTH_SHORT).show();
                }
            }
    }

    private static class AnalyzerListener implements ImageAnalysis.Analyzer {

        @Override
        public void analyze(@NonNull ImageProxy image) {
          ByteBuffer buffer = image.getPlanes()[0].getBuffer();
          buffer.rewind();
          byte[] bytes = new byte[buffer.remaining()];
          buffer.get(bytes);
           // 将数据转换为范围为0-255的像素值数组
          List<Integer> pixels = new ArrayList<>();
          for ( byte data : bytes) {
             pixels.add((int)data & 0xFF);
          }
          //计算图像的平均亮度
            int sum = 0;
            for (int pixel : pixels) {
                sum += pixel;
            }
            Log.i("IMAGE LIGHT", "image average light : "+ sum/pixels.size());
            image.close();
        }
    }

}