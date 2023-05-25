package com.example.learnningproject.cameraSample.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Surface;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceView surfaceView;
    SurfaceHolder holder;
    Camera mCamera;
    List<Camera.Size> sizes;
    private int degrees;
    int previewWidth,previewHeight;
    public CameraSurfaceView(Context context) {
        super(context);
        surfaceView = new SurfaceView(context);
        surfaceView.setZOrderOnTop(true);
        surfaceView.setZOrderMediaOverlay(true);
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.setFormat(PixelFormat.TRANSPARENT);
    }

    public void setCamera(Activity activity,Camera camera,int cameraId) {
        if(mCamera == camera) return;
        stopPreview();
        mCamera = camera;
        mCamera.setFaceDetectionListener(new MYFaceDetectListener());
        if(mCamera != null) {
            sizes = mCamera.getParameters().getSupportedPreviewSizes();
            for(int i = 0; i < sizes.size(); i++){
                if(sizes.get(i).width == 640 && sizes.get(i).height == 480 ) {
                    previewWidth = 640;
                    previewHeight = 480;
                    break;
                }
                if(sizes.get(i).width == 800 && sizes.get(i).height == 480) {
                    previewWidth = 800;
                    previewHeight = 480;
                    break;
                }
                if(sizes.get(i).width == 1280 && sizes.get(i).height == 720) {
                    previewWidth = 1280;
                    previewHeight = 720;
                    break;
                }
                previewWidth = sizes.get(i).width;
                previewHeight = sizes.get(i).height;
            }
            degrees = setCameraDisplayRotation(activity,cameraId,camera);
            requestLayout();
            try{
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            mCamera.startPreview();
        }
    }

    private void stopPreview() {
        if(mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        try{
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
            startFaceDetection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (holder.getSurface() == null) return;
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPreviewSize(previewWidth,previewHeight);
        requestLayout();
        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(degrees);
            mCamera.startPreview();
            if(parameters.getMaxNumDetectedFaces() > 0) {
                mCamera.startFaceDetection();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int setCameraDisplayRotation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId,cameraInfo);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0; //设备的旋转角度
        switch (rotation) {
            case Surface.ROTATION_0:
                break;
            case Surface.ROTATION_90: degrees = 90;break;
            case Surface.ROTATION_180: degrees = 180;break;
            case Surface.ROTATION_270: degrees = 270;break;
        }
        int result;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;// compensate the mirror
        } else {
            result = (360 + cameraInfo.orientation - degrees) % 360;
        }
        camera.setDisplayOrientation(result);
        return result;
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        if(mCamera != null) mCamera.stopPreview();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = resolveSize(getSuggestedMinimumWidth(),widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(),heightMeasureSpec);

    }

    @Override
    protected void onLayout(boolean changed,int l, int t, int r, int b){

    }
    public void startFaceDetection(){
        // Try starting Face Detection
        Camera.Parameters params = mCamera.getParameters();

        // start face detection only *after* preview has started
        if (params.getMaxNumDetectedFaces() > 0){
            // camera supports face detection, so can start it:
            mCamera.startFaceDetection();
        }
    }
    static class MYFaceDetectListener implements Camera.FaceDetectionListener {

        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces.length > 0){
                Log.d("FaceDetection", "face detected: "+ faces.length +
                        " Face 1 Location X: " + faces[0].rect.centerX() +
                        "Y: " + faces[0].rect.centerY() );
            }

        }
    }
}
