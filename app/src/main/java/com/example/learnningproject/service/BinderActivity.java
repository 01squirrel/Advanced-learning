package com.example.learnningproject.service;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.Toast;

import com.example.learnningproject.R;

//绑定服务
public class BinderActivity extends AppCompatActivity {
    LocalService localService;
    boolean mBound = false;
    Messenger mService = null;
    boolean bound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binder);
        Intent intent = new Intent(this, StartService.class);
        startService(intent);//启动服务
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this,LocalService.class);
        bindService(intent,connection, Context.BIND_AUTO_CREATE);
        //绑定服务通过messenger
        bindService(new Intent(this,MessengerService.class),messageConnection,Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        mBound = false;
        if (bound) {
            unbindService(messageConnection);
            bound = false;
        }
    }

    public void onBindService(View v){
        if(mBound){
            int num = localService.getRandomNumber();
            Toast.makeText(this, "number: " + num, Toast.LENGTH_SHORT).show();
        }
    }

    public void onBindMessage(View view){
        if(!bound) return;
        Message message = Message.obtain(null,MessengerService.MSG_SAY_HELLO,0,0);
        try {
            mService.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //通过扩展binder类获取,我们通过ServiceConnection接口来取得建立连接与连接意外丢失的回调
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LocalService.LocalBinder binder = (LocalService.LocalBinder) iBinder;
            localService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    //通过使用messenger绑定服务
    private final ServiceConnection messageConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = new Messenger(iBinder);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            bound = false;
        }
    };
}