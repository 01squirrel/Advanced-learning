package com.example.learnningproject.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;


/**
 * 若要求服务执行多线程（而非通过工作队列处理启动请求），则可通过扩展 Service 类来处理每个 Intent。
 * 对于每个启动请求，其均使用工作线程来执行作业，且每次仅处理一个请求。
 */
public class StartService extends Service {
    private serviceHandler handler;

    //  receive messages from the thread
    private final class serviceHandler extends Handler {
        public serviceHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }
            //使用startId停止服务，这样我们就不会在处理另一个作业的过程中停止服务
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        //启动运行服务的线程。
        HandlerThread thread = new HandlerThread("serviceStart", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        //Looper 是为关联的线程运行消息循环的对象。
        Looper serviceLooper = thread.getLooper();
        handler = new serviceHandler(serviceLooper);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        //对于每个启动请求，发送一条消息来启动一个作业并传递
        //start ID，这样我们就知道在完成任务时要停止哪个请求
        Message msg = handler.obtainMessage();
        msg.arg1 = startId;
        handler.sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
