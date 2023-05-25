package com.example.learnningproject.cameraSample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

//1.相机预览帧，创建surfaceView 2.初始化
//ImageReader：常用来拍照或接收 YUV 数据。
//MediaRecorder：常用来录制视频。
//MediaCodec：常用来录制视频。
//SurfaceHolder：常用来显示预览画面。
//SurfaceTexture：常用来显示预览画面。
public class SurfaceViewTemplate extends SurfaceView implements SurfaceHolder.Callback,Runnable {

    private SurfaceHolder surfaceHolder;
    //绘图的canvas
    private Canvas canvas;
    //子线程标志位
    private boolean isDrawing;
    //起始点
    private final int x = 0;
    private final int y = 0;
    private Paint paint;
    private Path path;

    public SurfaceViewTemplate(Context context) {
        super(context);
        initView();
    }

    public SurfaceViewTemplate(Context context, AttributeSet attrs) {
        super(context, attrs,0);
        initView();
    }

    public SurfaceViewTemplate(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setAntiAlias(true);
        path = new Path();
        //设置路径起始点
        path.moveTo(0,0);
        initView();
    }

    //初始化view
    private void initView(){
        surfaceHolder = getHolder();
        //注册回调方法
        surfaceHolder.addCallback(this);
        //设置部分参数
        setFocusable(true);
        setKeepScreenOn(true);
        setFocusableInTouchMode(true);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        isDrawing = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        isDrawing = false;
    }


    @Override
    public void run() {
    //子线程
        while (isDrawing){
            long start = System.currentTimeMillis();
            startDraw();
            long end = System.currentTimeMillis();
            if(end - start < 100){
                try {
                    Thread.sleep(100-(end-start));
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
//         示例 1
//        if(isDrawing){
//            startDraw();
//
//            x+=1;
//            y = (int)(100 * Math.sin(2 * x * Math.PI / 180) + 400);
//            path.lineTo(x,y);
//        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int X = (int) event.getX();
        int Y = (int) event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(X,Y);
                break;
            case MotionEvent.ACTION_DOWN:
                path.moveTo(X,Y);
                break;
        }
        return true;
    }

    private void startDraw(){
        try{
            //1.获取canvas对象
            canvas = surfaceHolder.lockCanvas();
            //2.子线程绘制
            //绘制背景
            canvas.drawColor(Color.GRAY);
            //3.绘制路径
            canvas.drawPath(path,paint);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //   3.释放canvas对象并提交画布
            if(canvas != null){
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
