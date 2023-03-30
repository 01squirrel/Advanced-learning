package com.example.learnningproject.cameraSample;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.internal.CameraDeviceStateCallbacks;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.example.learnningproject.R;
import com.example.learnningproject.databinding.ActivityCamera2Binding;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;


public class Camera2Activity extends AppCompatActivity {

    private final String TAG = "Camera2Activity";
    private final ArrayList<String> permissions = new ArrayList<>();
    private CameraManager manager;
    private String[] cameras;
    private final CameraCharacteristics.Key<Integer> level = CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL;
    private final int cameraId = -1;
    private String frontId;
    private CameraCharacteristics frontCharacter;
    private String backId;
    private CameraCharacteristics backCharacter;
    private CameraCharacteristics characteristics;
    private CameraDevice device;
    private HandlerThread thread;
    private Handler handler;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final int MSG_OPEN_CAMERA = 1;
    private final int MSG_CLOSE_CAMERA = 2;
    private final int MSG_CREATE_SESSION = 3;
    private final int MSG_CLOSE_SESSION = 4;
    private final int MSG_SET_PREVIEW_SIZE = 5;
    private final int MSG_CREATE_REQUEST_BUILDER = 6;
    private final int MSG_START_PREVIEW = 7;
    private final int MSG_STOP_PREVIEW = 8;
    private final int MSG_SET_IMAGE_SIZE = 9;
    private final int MSG_CAPTURE_IMAGE = 10;
    private final int MAX_PREVIEW_WIDTH = 1440;
    private final int MAX_PREVIEW_HEIGHT = 1080;
    private final int MAX_IMAGE_WIDTH = 4032;
    private final int MAX_IMAGE_HEIGHT = 3024;
    private SurfaceTexture previewSurface;
    private Surface surface;//显示预览画面的
    private Surface previewDataSurface;//接收预览帧数据的
    private Surface imageSurface;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder builder;
    private ImageReader previewImageReader;
    private ImageReader imageReader;//接收已捕获照片的
    private final MediaActionSound sound = new MediaActionSound();
    private final BlockingDeque<CaptureResult> captureResults = new LinkedBlockingDeque<>();
    private final Executor saveImageExecutor = Executors.newSingleThreadExecutor();
    ActivityCamera2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_camera2);
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //manager = (CameraManager)this.getSystemService(CameraManager.class);
        //1.获取cameramanager实例,CameraManager 是一个负责查询和建立相机连接的系统服务，可以说 CameraManager 是 Camera2 使用流程的起点
        manager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        try {
            //2.获取相机id列表
            cameras = manager.getCameraIdList();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        for (String id : cameras) {
            try {
                //3.根据ID获取CameraCharacteristics, 是相机信息的提供者
                CameraCharacteristics cameraCharacter = manager.getCameraCharacteristics(id);
               int level = cameraCharacter.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
               if (isLevelSupported(level,cameraCharacter)){
                    if (cameraCharacter.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                        frontId = id;
                        frontCharacter = cameraCharacter;
                    } else if (cameraCharacter.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                        backId = id;
                        backCharacter = cameraCharacter;
                    }
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "onCreate: 111111111111");
        //2.2配置预览尺寸
        binding.tvSurface.setSurfaceTextureListener(new viewListener());
        startThread();
    }

    //检查权限
    public void checkPermission() {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                permissions.remove(permission);
            }
        }
        if (!permissions.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int REQUEST_PERMISSION_CODE = 1;
                requestPermissions(permissions.toArray(new String[0]), REQUEST_PERMISSION_CODE);
            }
        }
    }

    public boolean isLevelSupported(int requiredLevel,CameraCharacteristics characteristics) {
        int[] levels = {
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY,
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED,
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL,
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3
        };
//        int deviceLevel = characteristics.get(level);
//        if(deviceLevel == requiredLevel){
//            return true;
//        }
        for (int level : levels) {
            if (requiredLevel == level) {
                return true;
            }
        }
        return false;
    }

    //监听相机状态
    class callBack extends CameraDevice.StateCallback {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            device = cameraDevice;
            runOnUiThread(() -> Toast.makeText(Camera2Activity.this, "相机已开启", Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Log.e("CAMERA_STATUS", "onDisconnected: device error");
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            device = null;
        }
    }

    private void startThread() {
        thread = new HandlerThread("cameraThread");
        thread.start();
        handler = new Handler(thread.getLooper()) {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_OPEN_CAMERA:
                        openCameraMessage cameraMessage = (openCameraMessage) msg.obj;
                        String id = cameraMessage.cameraId;
                        CameraDevice.StateCallback callback = cameraMessage.callback;
                        checkPermission();
                        try {
                            manager.openCamera(id, callback, handler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                    case MSG_CLOSE_CAMERA:
                        Log.d("CAMERA_CLOSE", "handleMessage: CLOSE ");
                        device.close();
                        break;
                    case MSG_SET_PREVIEW_SIZE:
                        characteristics = frontCharacter == null? backCharacter : frontCharacter;
                        if(characteristics != null){
                            //2.3创建surface
                            int width = msg.arg1;
                            int height = msg.arg2;
                            Size previewSize = getOptimizationSize(characteristics,SurfaceTexture.class,width,height);
                            previewSurface.setDefaultBufferSize(previewSize.getWidth(),previewSize.getHeight());
                            surface = new Surface(previewSurface);
                            //2.6创建imageReader接收预览帧数据
                            int format = ImageFormat.YUV_420_888;
                            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                            if(map.isOutputSupportedFor(format)){
                                previewImageReader = ImageReader.newInstance(previewSize.getWidth(),previewSize.getHeight(),format,3);
                                previewImageReader.setOnImageAvailableListener(new ImageListener(),handler);
                                previewDataSurface = previewImageReader.getSurface();

                            }
                        }
                        break;
                    case MSG_CREATE_SESSION:
                        Log.i(TAG, "handleMessage: camera capture session created");
                        CameraCaptureSession.StateCallback sessionCallback = new sessionListener();
                        List<Surface> outputs = new ArrayList<>();
                        outputs.add(surface);
                        if(previewDataSurface != null)
                        outputs.add(previewDataSurface);
                        if(imageSurface != null)
                            outputs.add(imageSurface);
                        try {
                           //创建相机捕获会话
                            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                OutputConfiguration outputConfiguration = new OutputConfiguration(surface);
                                List<OutputConfiguration> outputConfigurations = new ArrayList<>();
                                outputConfigurations.add(outputConfiguration);
                                outputConfigurations.add(new OutputConfiguration(previewDataSurface));
                                outputConfigurations.add(new OutputConfiguration(imageSurface));
                                SessionConfiguration configuration = new SessionConfiguration(SessionConfiguration.SESSION_HIGH_SPEED,outputConfigurations, (Executor) thread,sessionCallback);
                                device.createCaptureSession(configuration);
                            }else{
                                device.createCaptureSession(outputs,sessionCallback,mainHandler);
                            }
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                    case MSG_CREATE_REQUEST_BUILDER:
                        /**
                        * TEMPLATE_PREVIEW：适用于配置预览的模板。
                        * TEMPLATE_RECORD：适用于视频录制的模板。
                        * TEMPLATE_STILL_CAPTURE：适用于拍照的模板。
                        * TEMPLATE_VIDEO_SNAPSHOT：适用于在录制视频过程中支持拍照的模板。
                        * TEMPLATE_MANUAL：适用于希望自己手动配置大部分参数的模板。
                        */
                        Log.i(TAG, "handleMessage: capture request create");
                        try {
                           builder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                    case MSG_START_PREVIEW:
                        if(device != null && captureSession != null){
                            builder.addTarget(surface);
                            if(previewDataSurface != null)
                                builder.addTarget(previewDataSurface);
                            CaptureRequest request = builder.build();
                            try {
                                captureSession.setRepeatingRequest(request, new RepeatingCaptureCallback(),mainHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case MSG_STOP_PREVIEW:
                        try {
                            closePreview();
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                    case MSG_SET_IMAGE_SIZE:
                        Log.i(TAG, "handleMessage: set image size");
                        if(characteristics != null && builder != null){
                            Size size = getOptimizationSize(characteristics,ImageReader.class, msg.arg1,msg.arg2);
                            imageReader = ImageReader.newInstance(size.getWidth(),size.getHeight(),ImageFormat.JPEG,5);
                            imageReader.setOnImageAvailableListener(new SavaImageAvailableListener(),handler);
                            imageSurface = imageReader.getSurface();
                            //设置图片缩略图大小
                            Size[] thumbnailSize = characteristics.get(CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES);
                            Size thumbnail = getThumbnailSize(thumbnailSize,msg.arg1,msg.arg2);
                            builder.set(CaptureRequest.JPEG_THUMBNAIL_SIZE,thumbnail);
                        }
                        break;
                    case MSG_CAPTURE_IMAGE:
                        if(captureSession != null && builder != null && imageSurface != null && characteristics != null){
                            //根据设备方向配置图像方向
                            int ori = new DeviceOrientationListener(Camera2Activity.this).orientation;
                            int jpgOri = getJPGOrientation(characteristics,ori);
                            builder.set(CaptureRequest.JPEG_ORIENTATION,jpgOri);
                            //配置信息
                            Location location = getLocation();
                            builder.set(CaptureRequest.JPEG_GPS_LOCATION,location);
                            builder.set(CaptureRequest.JPEG_QUALITY,(byte)100);
                            builder.addTarget(imageSurface);
                            //创建请求
                            CaptureRequest request = builder.build();
                            try {
                                captureSession.capture(request,new CaptureImageListener(),mainHandler);
                                //拍摄多张照片
                                //captureSession.captureBurst(requests,new CaptureImageListener(),mainHandler);
                                //连续拍摄
                               // captureSession.setRepeatingRequest(request,new CaptureImageListener(),mainHandler);
                                //停止连拍
                                //调用 CameraCaptueSession.stopRepeating() 方法停止重复模式的 Capture，但是这会导致预览也停止。
                                //调用 CameraCaptueSession.setRepeatingRequest() 方法并且使用预览的 CaptureRequest 对象，停止输出照片。
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + msg.what);
                }
            }
        };
    }
    //4.开启相机
    public void openCamera(String id){
        if(id != null){
            openCameraMessage message = new openCameraMessage(id, new callBack());
            handler.obtainMessage(MSG_OPEN_CAMERA,message).sendToTarget();
        }else{
            Log.e("OPEN_CAMERA", "openCamera: cameraId is null");
        }
    }

    //5.关闭相机
    @Override
    protected void onPause() {
        super.onPause();
        handler.sendEmptyMessage(MSG_CLOSE_CAMERA);
        previewImageReader.close();
        imageReader.close();

    }

    private static class openCameraMessage {
        String cameraId;
        CameraDeviceStateCallbacks callbacks;
        CameraDevice.StateCallback callback;

        public openCameraMessage(String cameraId, CameraDevice.StateCallback callback) {
            this.cameraId = cameraId;
            this.callback = callback;
        }
    }
    //第二大步配置，2.1设置符合要求的尺寸给相机
    public <T> Size getOptimizationSize(CameraCharacteristics characteristics, Class<T> klass, int width, int height){
        StreamConfigurationMap sizeMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] sizes = sizeMap.getOutputSizes(klass);
        return getThumbnailSize(sizes,width,height);
    }
    //获取缩略图大小
    public Size getThumbnailSize(Size[] sizes,int maxWidth,int maxHeight){
        float ratio = (float) maxWidth / maxHeight;
        if(sizes != null){
            for(Size s : sizes){
                if((float) s.getWidth() / s.getHeight() == ratio && s.getWidth() <= maxWidth && s.getHeight() <= maxHeight){
                    return s;
                }
            }
        }
        return null;
    }
    //获取缩略图
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Bitmap getThumbnailImage(String path) throws IOException {
        Bitmap image;
        ExifInterface exifInterface = new ExifInterface(path);
        int orientationFlag = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
        float orientation = 0.0f;
        switch (orientationFlag){
            case ExifInterface.ORIENTATION_NORMAL:orientation = 0.0f;break;
            case ExifInterface.ORIENTATION_ROTATE_90:orientation = 90.0f;break;
            case ExifInterface.ORIENTATION_ROTATE_180:orientation = 180.0f;break;
            case ExifInterface.ORIENTATION_ROTATE_270:orientation = 270.0f;break;
        }
        if(exifInterface.hasThumbnail()){
            image = exifInterface.getThumbnailBitmap();
        }else{
            //Decode 原图获取缩略图
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 16;
            image = BitmapFactory.decodeFile(path,options);
        }
        if(orientation != 0.0f && image != null){
            Matrix matrix = new Matrix();
            matrix.setRotate(orientation);
            image = Bitmap.createBitmap(image,0,0,image.getWidth(),image.getHeight(),matrix,true);
        }
        return image;
    }
    //监听surfaceview的状态
    private class viewListener implements TextureView.SurfaceTextureListener{

        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
            Log.i(TAG, "onSurfaceTextureUpdated: 33333333333333");
            previewSurface = surfaceTexture;
        }
    }
    //监听capture状态
    private class RepeatingCaptureCallback extends CameraCaptureSession.CaptureCallback {

        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            Log.e(TAG, "onCaptureFailed: "+failure);
        }
    }
    //2.4 创建 CameraCaptureSession
    private void createSession(){
        handler.sendEmptyMessage(MSG_CREATE_SESSION);
    }
    private class sessionListener extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            captureSession = cameraCaptureSession;
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
            captureSession = cameraCaptureSession;
        }
    }
    //设置图像数据监听
    private static class ImageListener implements ImageReader.OnImageAvailableListener {

        @Override
        public void onImageAvailable(ImageReader imageReader) {
           Image image = imageReader.acquireNextImage();
           image.getPlanes();
           image.close();
        }
    }
    //2.7设置拍照信息捕获监听
    //拍摄单张照片流程：1.定义回调接口 2.创建imageReader 3.创建captureRequest
    private class CaptureImageListener extends CameraCaptureSession.CaptureCallback{
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            handler.post(()->{
                //添加快门音效
                sound.play(MediaActionSound.SHUTTER_CLICK);
            });
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            try {
                captureResults.put(result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    //2.7设置监听获取image对象
    private class SavaImageAvailableListener implements ImageReader.OnImageAvailableListener {
        private final DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        private final String cameraDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/Camera";
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Image image = imageReader.acquireNextImage();
            CaptureResult result = null;
            try {
                 result = captureResults.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //plane[0] - Y  plane[1] - U  plane[2]-V
            ByteBuffer buffer =  image.getPlanes()[0].getBuffer();
            int size = buffer.remaining();
            byte[] bytes = new byte[size];
            buffer.get(bytes);
            int width = image.getWidth();
            int height = image.getHeight();
            CaptureResult finalResult = result;
            saveImageExecutor.execute(()->{
                long date = System.currentTimeMillis();
                String title = "Image_"+format.format(date);
                String displayName = title+".jpg";
                String path = cameraDir+ File.separator+displayName;
                if(finalResult != null){
                    int orientation = finalResult.get(CaptureResult.JPEG_ORIENTATION);
                    Location location = finalResult.get(CaptureResult.JPEG_GPS_LOCATION);
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
                    //保存图片文件
                    try {
                        FileWriter writer = new FileWriter(path);
                        writer.write(String.valueOf(buffer));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        try {
                            Bitmap bmp = getThumbnailImage(path);
                            if(bmp != null){
                                runOnUiThread(()->{
                                    binding.thumbnailView.setImageBitmap(bmp);
                                    binding.thumbnailView.setScaleX(0.8f);
                                    binding.thumbnailView.setScaleY(0.8f);
                                    binding.thumbnailView.animate().setDuration(50).scaleX(1.0f).scaleY(1.0f).start();
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }
    private static class DeviceOrientationListener extends OrientationEventListener{

        int orientation = 0;
        public DeviceOrientationListener(Context context, int rate) {
            super(context, rate);
        }

        public DeviceOrientationListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int i) {
            this.orientation = i;
        }
    }
    //设置预览大小
    public void setPreviewSize(int maxWidth,int maxHeight){
        handler.obtainMessage(MSG_SET_PREVIEW_SIZE,maxWidth,maxHeight).sendToTarget();
    }
    //2.5创建捕获请求
    public void createCaptureRequest(){
        handler.sendEmptyMessage(MSG_CREATE_REQUEST_BUILDER);
    }
    //2.5开启预览
    public void startPreview(){
        handler.sendEmptyMessage(MSG_START_PREVIEW );
    }
    //关闭预览
    public void closePreview() throws CameraAccessException {
        captureSession.stopRepeating();
    }
    //设置image reader照片大小
    public void setImageSize(int maxWidth,int maxHeight){
        handler.obtainMessage(MSG_SET_IMAGE_SIZE,maxWidth,maxHeight).sendToTarget();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public Location getLocation(){
      LocationManager manager =  getSystemService(LocationManager.class);
      if(manager != null && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
          return manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
      }
      return null;
    }
    //拍照
    public void captureImage(){
        handler.sendEmptyMessage(MSG_CAPTURE_IMAGE);
    }
    //图像方向
    public int getDisplayRotation(CameraCharacteristics characteristics){
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        //摄像头传感器方向
        int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        int previewRotation;
        if(characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT){
            previewRotation = (360 - (degrees + sensorOrientation) % 360) % 360;
        }else{
            previewRotation = (sensorOrientation - degrees + 360) % 360;
        }
        return previewRotation;
    }
    //矫正图像方向
    public int getJPGOrientation(CameraCharacteristics characteristics,int orientation){
        int deviceOrientation = orientation;
        if(deviceOrientation == OrientationEventListener.ORIENTATION_UNKNOWN){
            return 0;
        }
        int sensor = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        // round device orientation to multiple of 90
        deviceOrientation = (deviceOrientation+45)/90 * 90;
        if(characteristics.get(CameraCharacteristics.LENS_FACING )== CameraCharacteristics.LENS_FACING_FRONT){
            deviceOrientation = - deviceOrientation;
        }
        return (deviceOrientation + sensor + 360) % 360;
    }
    //切换摄像头方向
    public void switchCamera(){
        String oldId = device.getId();
        String newId = Objects.equals(oldId, frontId) ? backId : frontId;
        if(newId != null){
            handler.sendEmptyMessage(MSG_CLOSE_CAMERA);
            openCamera(newId);
            createCaptureRequest();
            setPreviewSize(MAX_PREVIEW_WIDTH,MAX_PREVIEW_HEIGHT);
            setImageSize(MAX_IMAGE_WIDTH,MAX_IMAGE_HEIGHT);
            createSession();
            startPreview();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: 2222");
        String id = frontId == null ? backId : frontId;
        setPreviewSize(MAX_PREVIEW_WIDTH,MAX_PREVIEW_HEIGHT);
        openCamera(id);
        createSession();
        createCaptureRequest();
        startPreview();
        setImageSize(MAX_IMAGE_WIDTH,MAX_IMAGE_HEIGHT);
    }
}