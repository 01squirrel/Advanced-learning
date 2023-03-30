package com.example.learnningproject.util;

import android.graphics.Matrix;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

public class ExifUtils {
    public static String TAG = "ExifUtils";

    /** Transforms rotation and mirroring information into one of the [ExifInterface] constants */
    public static int computeExifOrientation(int rotationDegrees,boolean mirrored){
        int orientation = ExifInterface.ORIENTATION_UNDEFINED;
        if(rotationDegrees == 0 && !mirrored){
            orientation = ExifInterface.ORIENTATION_NORMAL;
        }else if(rotationDegrees == 0){
            orientation = ExifInterface.ORIENTATION_FLIP_HORIZONTAL;
        }else if(rotationDegrees == 90 && !mirrored){
            orientation = ExifInterface.ORIENTATION_ROTATE_90;
        }else if(rotationDegrees == 90){
            orientation = ExifInterface.ORIENTATION_TRANSPOSE;
        }else if(rotationDegrees == 180 && !mirrored){
            orientation = ExifInterface.ORIENTATION_ROTATE_180;
        }else if(rotationDegrees == 180){
            orientation = ExifInterface.ORIENTATION_FLIP_VERTICAL;
        }else if(rotationDegrees == 270 && !mirrored){
            orientation = ExifInterface.ORIENTATION_TRANSVERSE;
        }else if (rotationDegrees == 270){
            orientation = ExifInterface.ORIENTATION_ROTATE_270;
        }
        return orientation;
    }

    /**
     * Helper function used to convert an EXIF orientation enum into a transformation matrix
     * that can be applied to a bitmap.
     *
     * @return matrix - Transformation required to properly display [Bitmap]
     */
public static Matrix decodeExifOrientation(int exifOrientation){
    Matrix matrix = new Matrix();

    // Apply transformation corresponding to declared EXIF orientation
    switch (exifOrientation){
        case ExifInterface.ORIENTATION_NORMAL:
        case ExifInterface.ORIENTATION_UNDEFINED:
            break;
        case ExifInterface.ORIENTATION_ROTATE_90:
            matrix.postRotate(90f);
            break;
        case ExifInterface.ORIENTATION_ROTATE_180:
            matrix.postRotate(180f);
            break;
        case ExifInterface.ORIENTATION_ROTATE_270:
            matrix.postRotate(270f);
            break;
        case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
            matrix.postScale(-1f,1f);
            break;
        case ExifInterface.ORIENTATION_FLIP_VERTICAL:
            matrix.postScale(1f,-1f);
            break;
        case ExifInterface.ORIENTATION_TRANSPOSE:
            matrix.postScale(-1f,1f);
            matrix.postRotate(270f);
            break;
        case ExifInterface.ORIENTATION_TRANSVERSE:
            matrix.postScale(1f,-1f);
            matrix.postRotate(90f);
            break;
        default:
            Log.e(TAG, "Invalid orientation: "+exifOrientation);
    }
    return matrix;
}
}
