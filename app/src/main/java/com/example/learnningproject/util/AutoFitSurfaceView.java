package com.example.learnningproject.util;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

public class AutoFitSurfaceView extends SurfaceView {
    private float aspectRatio = 0f;
    private final String TAG = AutoFitSurfaceView.class.getSimpleName();
    public AutoFitSurfaceView(Context context) {
        super(context);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (aspectRatio == 0f) {
            setMeasuredDimension(width, height);
        } else {
            int newWidth,newHeight;
            float actualRatio = aspectRatio;
            if (width < height) {
                actualRatio = 1 / aspectRatio;
            }
            if (width < height * actualRatio) {
                newHeight = height;
                newWidth = Math.round(height * actualRatio);
            } else {
                newWidth = width;
                newHeight = Math.round(width * actualRatio);
            }
            setMeasuredDimension(newWidth,newHeight);
        }
    }
    public void setAspectRatio(int width,int height){
        if(width < 0 || height < 0){
            Log.e(TAG, "setAspectRatio: Size cannot be negative" );
        }else{
            aspectRatio = (float) width / height;
            getHolder().setFixedSize(width,height);
            requestLayout();
        }
    }
}
