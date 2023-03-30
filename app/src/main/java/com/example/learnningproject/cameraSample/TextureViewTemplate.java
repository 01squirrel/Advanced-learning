package com.example.learnningproject.cameraSample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TextureViewTemplate extends TextureView {
    public TextureViewTemplate(@NonNull Context context) {
        super(context);
    }

    public TextureViewTemplate(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TextureViewTemplate(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, width*3/4);
    }
}
