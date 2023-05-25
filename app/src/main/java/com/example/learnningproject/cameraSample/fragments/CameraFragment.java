package com.example.learnningproject.cameraSample.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.drawable.ColorDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.DngCreator;
import android.hardware.camera2.TotalCaptureResult;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.learnningproject.R;
import com.example.learnningproject.databinding.FragmentCameraBinding;
import com.example.learnningproject.util.CameraSizes;
import com.example.learnningproject.util.ExifUtils;
import com.example.learnningproject.util.OrientationLiveData;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraFragment extends Fragment {

    private final String TAG = CameraFragment.class.getSimpleName();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public CameraFragment() {
    }

    FragmentCameraBinding binding;
    NavController controller = Navigation.findNavController(requireActivity(), R.id.fragment_container);
    CameraManager manager = (CameraManager) requireContext().getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
    ImageReader imageReader;
    HandlerThread cameraThread, imageReaderThread;
    Handler cameraHandler, imageReaderHandler;
    CameraDevice camera;
    CameraCaptureSession session;
    CameraCharacteristics characteristics;
    private final CameraFragmentArgs args = new com.example.learnningproject.cameraSample.fragments.CameraFragmentArgs();
    private final int IMAGE_BUFFER_SIZE = 3;
    private OrientationLiveData relativeOrientation;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CameraFragment.
     */

    public static CameraFragment newInstance(String param1, String param2) {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        try {
            characteristics = manager.getCameraCharacteristics(args.getCameraId());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        cameraThread = new HandlerThread("CameraThread");
        imageReaderThread  = new HandlerThread("imageReaderThread");
        cameraThread.start();
        imageReaderThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
        imageReaderHandler = new Handler(imageReaderThread.getLooper());
        // Used to rotate the output media to match device orientation
        relativeOrientation = new OrientationLiveData(requireContext(),characteristics);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.setContentView(requireActivity(), R.layout.fragment_camera);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.captureButton.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setTranslationX(-insets.getSystemWindowInsetRight());
            v.setTranslationY(-insets.getSystemWindowInsetBottom());
            insets.consumeSystemWindowInsets();
            return insets;
        });
        binding.viewFinder.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                // Selects appropriate preview size and configures view finder
                try {
                    Size previewSize = CameraSizes.class.newInstance()
                            .getPreviewOutputSize(binding.viewFinder.getDisplay(), characteristics, surfaceHolder.getClass(), args.getPixelFormat());
                    Log.d(TAG, "View finder size: {" + binding.viewFinder.getWidth() + "} x {" + binding.viewFinder.getHeight() + "}");
                    Log.d(TAG, "Selected preview size:" + previewSize);
                    binding.viewFinder.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
                    view.post(new InitCameraThread());
                } catch (IllegalAccessException | java.lang.InstantiationException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                Log.i("CAMERA FRAGMENT", "surfaceChanged: " + i);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                Log.i("TAG", "----surfaceDestroyed");
            }
        });

    }

    public class InitCameraThread implements Runnable {

        @Override
        public void run() {
            try {
                // Open the selected camera
                openCamera(manager,args.getCameraId(), cameraHandler);
                // Initialize an image reader which will be used to capture still photos
                Size[] sizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(args.getPixelFormat());
                Arrays.sort(sizes, (size, t1) -> {
                    int a = size.getWidth() * size.getHeight();
                    int b = t1.getHeight() * t1.getWidth();
                    return Integer.compare(b, a);
                });
                Size maxSize = sizes[0];
                imageReader = ImageReader.newInstance(maxSize.getWidth(), maxSize.getHeight(), args.getPixelFormat(), IMAGE_BUFFER_SIZE);
                // Creates list of Surfaces where the camera will output frames
                List<Surface> targets = new ArrayList<>();
                targets.add(binding.viewFinder.getHolder().getSurface());
                targets.add(imageReader.getSurface());
                // Start a capture session using our open camera and list of Surfaces where frames will go
                createCaptureSession(camera,targets,cameraHandler);
                //CaptureRequest request = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).build();
                CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                builder.set(CaptureRequest.JPEG_QUALITY,(byte)100);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    Location location = getLocation();
                    builder.set(CaptureRequest.JPEG_GPS_LOCATION,location);
                }
                builder.addTarget(binding.viewFinder.getHolder().getSurface());
                CaptureRequest request = builder.build();
                // This will keep sending the capture request as frequently as possible until the
                // session is torn down or session.stopRepeating() is called
                session.setRepeatingRequest(request,null, cameraHandler);
                binding.captureButton.setOnClickListener(view -> {
                    // Disable click listener to prevent multiple requests simultaneously in flight
                    view.setEnabled(false);
                    CombinedCaptureResult captureResult = takePhoto();
                    Log.i(TAG, "run: take photo result: "+captureResult);
                    File output = saveResult(captureResult);
                    Log.d(TAG, "Image saved: "+output.getAbsolutePath());
                    // If the result is a JPEG file, update EXIF metadata with orientation info
                    String suffix = output.getName().substring(output.getName().indexOf("."));
                    if(suffix.equals("jpg")){
                        try {
                            ExifInterface exif = new ExifInterface(output.getAbsolutePath());
                            exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(captureResult.orientation));
                            exif.saveAttributes();
                            Log.d(TAG, "EXIF metadata saved: "+output.getAbsolutePath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    // Display the photo taken to user
                    requireActivity().runOnUiThread(()->{
                        controller.navigate(CameraFragmentDirections
                                .actionCameraToJpegViewer(output.getAbsolutePath())
                                .setOrientation(captureResult.orientation)
                                .setDepth(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && captureResult.format == ImageFormat.DEPTH_JPEG)
                        );
                    });
                    view.setEnabled(true);
                });
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void openCamera(CameraManager manager, String cameraId, Handler handler) throws CameraAccessException {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "openCamera: no camera permission");
            return;
        }
        manager.openCamera(cameraId, new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice cameraDevice) {
                camera = cameraDevice;
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                Log.w(TAG, "Camera "+ cameraId +"has been disconnected");
                requireActivity().finish();
            }

            @Override
            public void onError(@NonNull CameraDevice cameraDevice, int i) {
                String error = "";
                if(i == ERROR_CAMERA_DEVICE){
                    error = "Fatal (device)";
                }else if(i == ERROR_CAMERA_DISABLED){
                    error = "Device policy";
                }else if (i == ERROR_CAMERA_IN_USE){
                    error = "Camera in use";
                }else if (i == ERROR_CAMERA_SERVICE){
                    error = "Fatal (service)";
                }else if(i == ERROR_MAX_CAMERAS_IN_USE){
                    error = "Maximum cameras in use";
                }
                RuntimeException exception = new RuntimeException("Camera + " + cameraId +"error: ("+i+")"+error+")");
                Log.e(TAG, exception.getMessage(), exception);
            }
        }, handler);
    }
    public void createCaptureSession(CameraDevice device,List<Surface> surfaces,Handler handler){
        try {
            device.createCaptureSession(surfaces,new CameraCaptureSession.StateCallback(){

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    session = cameraCaptureSession;
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    RuntimeException exc = new RuntimeException("Camera {"+device.getId()+"} session configuration failed");
                    Log.e(TAG, exc.getMessage(), exc);
                }
            },handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private Location getLocation(){
        LocationManager manager = requireContext().getSystemService(LocationManager.class);
        if(manager != null && ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }
        return null;
    }
    private static class CombinedCaptureResult implements Closeable{
        private final Image image;
        private final CaptureResult metadata;
        private final int orientation;
        private final int format;

        public CombinedCaptureResult(Image image, CaptureResult metadata, int orientation, int format) {
            this.image = image;
            this.metadata = metadata;
            this.orientation = orientation;
            this.format = format;
        }

        @Override
        public void close() {
            image.close();
        }
    }

    public CombinedCaptureResult takePhoto(){
        final CombinedCaptureResult[] combinedCaptureResult = new CombinedCaptureResult[1];
        // Start a new image queue
        ArrayBlockingQueue<Image> imageQueue = new ArrayBlockingQueue<>(IMAGE_BUFFER_SIZE);
        imageReader.setOnImageAvailableListener(imageReader -> {
            Image image = imageReader.acquireNextImage();
            Log.d(TAG, "Image available in queue: {"+image.getTimestamp()+"}");
            imageQueue.add(image);
        },imageReaderHandler);
        try {
            CaptureRequest.Builder builder = session.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.addTarget(imageReader.getSurface());
            session.capture(builder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                    binding.viewFinder.post(new animationTask());
                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    long resultTimestamp = result.get(CaptureResult.SENSOR_TIMESTAMP);
                    Log.d(TAG, "Capture result received: "+resultTimestamp);
                    // Set a timeout in case image captured is dropped from the pipeline
                        try {
                            Image image = imageQueue.take();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                                    image.getFormat() != ImageFormat.DEPTH_JPEG &&
                                    image.getTimestamp() != resultTimestamp)
                                Log.d(TAG, "onCaptureCompleted: matching image dequeued:"+image.getTimestamp());
                            imageReader.setOnImageAvailableListener(null, null);
                            while (imageQueue.size() > 0){
                                imageQueue.take().close();
                            }
                            int rotation = relativeOrientation.getValue() == null ? 0 : relativeOrientation.getValue();
                            boolean mirrored = characteristics.get(CameraCharacteristics.LENS_FACING) ==
                                    CameraCharacteristics.LENS_FACING_FRONT;
                            int exifOrientation = ExifUtils.computeExifOrientation(rotation,mirrored);
                            combinedCaptureResult[0] = new CombinedCaptureResult(image, result, exifOrientation, imageReader.getImageFormat());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                }
            },cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return combinedCaptureResult[0];
    }
    /** Performs recording animation of flashing screen */
    public class animationTask implements Runnable {
        @Override
        public void run() {
            View overlay = binding.overlay;
            ColorDrawable drawable = new ColorDrawable(Color.argb(150,255,255,255));
            overlay.setBackground(drawable);
            long ANIMATION_FAST_MILLIS = 50L;
            overlay.postDelayed(() -> {
                overlay.setBackground(null);
            }, ANIMATION_FAST_MILLIS);
        }
    }

    public File saveResult(CombinedCaptureResult result) {
        File file = null;
        switch (result.format){
            case ImageFormat.JPEG:
            case ImageFormat.DEPTH_JPEG:
                ByteBuffer buffer = result.image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                try {
                    file = createFile(requireContext(),"jpg");
                    try (FileOutputStream outputStream = new FileOutputStream(file)) {
                        outputStream.write(bytes);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    RuntimeException exception = new RuntimeException("Unable to write JPEG image to file");
                    Log.e(TAG, exception.getMessage(),exception );
                }
                break;
            case ImageFormat.RAW_SENSOR:
                try (DngCreator creator = new DngCreator(characteristics, result.metadata)) {
                    file = createFile(requireContext(), "dng");
                    try {
                        FileOutputStream outputStream = new FileOutputStream(file);
                        creator.writeImage(outputStream, result.image);
                    } catch (IOException e) {
                        e.printStackTrace();
                        RuntimeException exception = new RuntimeException("Unable to write JPEG image to file");
                        Log.e(TAG, exception.getMessage(), exception);
                    }
                }
                break;
            default:
                RuntimeException runtimeException = new RuntimeException("Unknown image format: "+result.image.getFormat());
                Log.i(TAG, runtimeException.getMessage(), runtimeException);
        }
        return file;
    }
    /**
     * Create a [File] named a using formatted timestamp with the current date and time.
     *
     * @return [File] created.
     */
    private File createFile(Context context,String extension){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.CHINA);
        return new File(context.getFilesDir(),"IMAGE_"+sdf.format(new Date())+"."+extension);
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            camera.close();
        } catch (Throwable exc) {
            Log.e(TAG, "Error closing camera", exc);
        }
    }

    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();
        cameraThread.quitSafely();
        imageReaderThread.quitSafely();
    }
}