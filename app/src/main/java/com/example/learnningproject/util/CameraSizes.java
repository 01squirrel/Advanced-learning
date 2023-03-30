package com.example.learnningproject.util;
import android.graphics.Point;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import kotlin.collections.CollectionsKt;
import kotlin.comparisons.ComparisonsKt;

/** Helper class used to pre-compute shortest and longest sides of a [Size] */
public class CameraSizes {
    private int width,height;

    public CameraSizes(int width, int height) {
        this.width = width;
        this.height = height;
    }
    private final Size size = new Size(width,height);

    int longer = Math.max(size.getWidth(),size.getHeight());
    int shorter = Math.min(size.getWidth(),size.getHeight());

    /** Standard High Definition size for pictures and video */
    static CameraSizes SIZE_1080P = new CameraSizes(1920,1080);

    /** Returns a [SmartSize] object for the given [Display] */
    public CameraSizes getDisplayCameraSize(Display display){
        Point point = new Point();
        display.getRealSize(point);
        return new CameraSizes(point.x,point.y);
    }

    //Returns the largest available PREVIEW size. For more information
    public <T> Size getPreviewOutputSize(Display display, CameraCharacteristics characteristics, Class<T> targetClass, @Nullable Integer format){
        // Find which is smaller: screen or 1080p
        CameraSizes screenSize = getDisplayCameraSize(display);
        boolean hdScreen = screenSize.longer >= SIZE_1080P.longer || screenSize.shorter >= SIZE_1080P.shorter;
        CameraSizes maxSize = hdScreen ? SIZE_1080P : screenSize;
        // If image format is provided, use it to determine supported sizes; else use target class
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] allSizes = format == null ? map.getOutputSizes(targetClass) : map.getOutputSizes(format);
        List<Size> validSize;
        // Get available sizes and sort them by area from largest to smallest
        Arrays.sort(allSizes,new compareSize());
//        for(Size size1 : allSizes){
//            CollectionsKt.sortedWith(ComparisonsKt.compareBy());
//       }
        return allSizes[0];
    }
    @NonNull
    @Override
    public String toString() {
        return "SmartSize("+longer+"X"+shorter+")";
    }


    public class compareSize implements Comparator<Size>{

        @Override
        public int compare(Size size, Size t1) {
            int a = size.getWidth() * size.getHeight();
            int b = t1.getHeight() * t1.getWidth();
            return Integer.compare(a, b);
        }

        @Override
        public Comparator<Size> reversed() {
            return Comparator.super.reversed();
        }
    }
}
