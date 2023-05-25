package com.example.learnningproject.service;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.learnningproject.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//绑定服务实例
public class BinderActivity extends AppCompatActivity {
    LocalService localService;
    boolean mBound = false;
    Messenger mService = null;
    boolean bound;
    Intent localIntent;
    private BluetoothDevice bluetoothDevice;
    boolean mConnected = false;
    private List<BluetoothGattService> gattServices;


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
        localIntent = new Intent(this,LocalService.class);
        mBound = bindService(localIntent,connection, Context.BIND_AUTO_CREATE);
        //绑定服务通过messenger
        bound = bindService(new Intent(this,MessengerService.class),messageConnection,Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBound){
            stopService(localIntent);
        }
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
            if(localService != null) {
                mBound = true;
                if(!localService.initialize()) {
                    Log.e("BLE SERVICE", "Unable to initialize Bluetooth");
                    finish();
                }
                localService.connect("ble service");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            localService = null;
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

    //新增蓝牙连接
    //通过侦听来自服务的事件，活动能够根据与 BLE 设备的当前连接状态更新用户界面。
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(LocalService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
            } else if (LocalService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
            } else if (LocalService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                gattServices = localService.getSupportedGattServices();
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter(){
        final IntentFilter filter = new IntentFilter();
        filter.addAction(LocalService.ACTION_GATT_CONNECTED);
        filter.addAction(LocalService.ACTION_GATT_DISCONNECTED);
        return filter;
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver,makeGattUpdateIntentFilter());
        if(localService != null) {
            final boolean result = localService.connect("ble service");
            Log.d("BLE service", "Connect request result=" + result);
        }
    }

    public void displayGattServices(List<BluetoothGattService> gattServices) {
        String uuid = null;
        ArrayList<HashMap<String,String>> gattServiceData = new ArrayList<>();
        ArrayList<ArrayList<HashMap<String,String>>> gattCharacteristicData = new ArrayList<>();
        ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
        for(BluetoothGattService gattService : gattServices) {
            HashMap<String,String> currentServiceData = new HashMap<>();
            //服务获取uuid
            uuid = gattService.getUuid().toString();
            currentServiceData.put("list_data",uuid);
            gattServiceData.add(currentServiceData);
            gattService.getCharacteristics();
        }
    }
}