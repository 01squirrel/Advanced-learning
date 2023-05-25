package com.example.learnningproject.util;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;

import androidx.lifecycle.LiveData;

public class OrientationLiveData extends LiveData<Integer> {
    private static final String TAG = OrientationLiveData.class.getSimpleName();
    private final CameraCharacteristics characteristics;

    public OrientationLiveData(Context context, CameraCharacteristics characteristics) {
        this.characteristics = characteristics;
    }

    private final OrientationEventListener listener = new OrientationEventListener(null) {
        @Override
        public void onOrientationChanged(int i) {
            Log.i(TAG, "onOrientationChanged: device orientation is ---------"+i);
            int orientation = Surface.ROTATION_0;
            if(i <= 135 && i > 45){
                orientation = Surface.ROTATION_90;
            }else if (i <= 225){
                orientation = Surface.ROTATION_180;
            }else if(i <= 315){
                orientation = Surface.ROTATION_270;
            }
            int relative = computeRelativeRotation(characteristics,orientation);
            if(getValue() == null){
                Log.i(TAG, "onOrientationChanged: liveData getValue null");
            }
            if(relative != getValue()){
                postValue(relative);
            }
        }
    };
    @Override
    protected void onActive() {
        super.onActive();
        listener.enable();
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        listener.disable();
    }

    public int computeRelativeRotation(CameraCharacteristics characteristics,int surfaceRotation) {
        //获得相机与设备方向的夹角
        int sensorOrientationDegrees = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        Log.i(TAG, "computeRelativeRotation: sensor orientation is ----------"+sensorOrientationDegrees);
        int deviceOrientationDegrees = 0;
        if (surfaceRotation == Surface.ROTATION_90) {
            deviceOrientationDegrees = 90;
        } else if (surfaceRotation == Surface.ROTATION_180) {
            deviceOrientationDegrees = 180;
        } else if (surfaceRotation == Surface.ROTATION_270) {
            deviceOrientationDegrees = 270;
        }
        int sign;
        if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT){
            sign = 1;
        } else {
            sign = -1;
        }
        return ( 360 + (sensorOrientationDegrees - deviceOrientationDegrees * sign)) % 360;
    }
}
