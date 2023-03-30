package com.example.network;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;

import okhttp3.HttpUrl;

public class NetWorkUtil {
    int NET_CNNT_OK = 1; // NetworkAvailable
    int NET_CNNT_TIMEOUT = 2; // no NetworkAvailable
    int NET_NOT_PREPARE = 3; // Net no ready
    int NET_ERROR = 4; //net error

    //check network is available or not
    public Boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return !(null == info || !info.isAvailable());
    }

    //get local ip address
    public String getIpAddress() {
        String address = "";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                Enumeration<InetAddress> inetAddresses = interfaces.nextElement().getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        address = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException exception) {
            exception.printStackTrace();
        }
        return address;
    }

    //ping network
    public Boolean pingNetwork() {
        boolean result = false;
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL("http://www.baidu.com").openConnection();
            // TIMEOUT
            int TIMEOUT = 3000;
            urlConnection.setConnectTimeout(TIMEOUT);
            urlConnection.connect();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != urlConnection) {
                urlConnection.disconnect();
            }
        }
        return result;
    }

    //is wifi on
    public Boolean isWifiEnabled(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context,"missing read phone state permission",Toast.LENGTH_SHORT).show();
        }
        return info != null && info.isConnected() || telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS;
    }
}
