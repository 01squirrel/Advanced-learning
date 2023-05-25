package com.example.learnningproject.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.service.controls.Control;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.learnningproject.R;

/**
 * 以下是对 Messenger 使用方式的摘要：
 * 使用此方式创建的服务可与远程进程通信。
 * 1.服务实现一个 Handler，由其接收来自客户端的每个调用的回调。
 * 2.服务使用 Handler 来创建 Messenger 对象（该对象是对 Handler 的引用）。
 * 3.Messenger 创建一个 IBinder，服务通过 onBind() 将其返回给客户端。
 * 4.客户端使用 IBinder 将 Messenger（它引用服务的 Handler）实例化，然后再用其将 Message 对象发送给服务。
 * 5.服务在其 Handler 中（具体而言，是在 handleMessage() 方法中）接收每个 Message
 */
public class MessengerService extends Service {

    static final int MSG_SAY_HELLO = 1;
    Messenger messenger;
    NotificationManager manager;
    //创建handler接收客户端消息
    static class IncomingHandler extends Handler {
        private final Context clientContext;
//        @Deprecated
//        IncomingHandler(Context context){
//            this.clientContext = context.getApplicationContext();
//        }

        public IncomingHandler(@NonNull Looper looper, Context clientContext) {
            super(looper);
            this.clientContext = clientContext.getApplicationContext();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_SAY_HELLO) {
                Toast.makeText(clientContext, "hello!", Toast.LENGTH_SHORT).show();
            } else {
                super.handleMessage(msg);
            }
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        messenger = new Messenger(new IncomingHandler(Looper.myLooper(),this));
        return messenger.getBinder();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onCreate() {
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotice();
    }

    @Override
    public void onDestroy() {
        manager.cancel(R.string.app_name);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void showNotice(){
        CharSequence charSequence = getText(R.string.app_name);
        PendingIntent content = PendingIntent.getActivity(this,0,new Intent(this, Control.class),PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_background)  // the status icon
                .setTicker(charSequence)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.bind_service))  // the label of the entry
                .setContentText(charSequence)  // the contents of the entry
                .setContentIntent(content)  // The intent to send when the entry is clicked
                .build();
        manager.notify(R.string.app_name,notification);
    }
}
