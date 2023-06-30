package com.example.learnningproject.customview;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AnimView extends View {

    public static final float RADIUS = 80.0f;
    private Point currentPoint;
    private Paint mPaint;
    private int mColor;
    private Path path;

    public AnimView(Context context) {
        super(context);
        init();
    }

    public AnimView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AnimView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
        path = new Path();
    }

    private void drawCircle(Canvas canvas) {
        float x = currentPoint.getX();
        float y = currentPoint.getX();
        canvas.drawCircle(x,y,RADIUS,mPaint);
        //mPaint.setColor(Color.GREEN);
        //path.moveTo(x,y);
        //path.lineTo(0,y+RADIUS);
        //RectF rectF = new RectF(0,RADIUS/2,RADIUS,RADIUS);
        //path.addArc(rectF,0,360);
        //path.lineTo(0,y);
        //path.addArc(rectF,180,180);
        canvas.drawPath(path,mPaint);
    }

    private void startAnimation() {
        Log.i("VIEW NEW", "WIDTH: "+ getWidth()+",height: "+getHeight());
        Point start = new Point(RADIUS,RADIUS);
        Point end = new Point(getWidth()-RADIUS,getHeight()-RADIUS);
        ValueAnimator valueAnimator = ValueAnimator.ofObject(new PointEvaluator(),start,end);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                currentPoint = (Point) animation.getAnimatedValue();
                invalidate();
            }
        });
        ObjectAnimator objectAnimator = ObjectAnimator.ofObject(this,"color",new AnimEvaluator(),Color.BLUE,Color.RED);
        AnimatorSet set = new AnimatorSet();
        set.play(valueAnimator).with(objectAnimator);
        set.setStartDelay(1000L);
        set.setDuration(4000L);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (currentPoint == null) {
            currentPoint = new Point(RADIUS,RADIUS);
            drawCircle(canvas);
            startAnimation();
        } else {
            drawCircle(canvas);
        }
       // super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b){

    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int mColor) {
        this.mColor = mColor;
        mPaint.setColor(mColor);
        invalidate();
    }
}
