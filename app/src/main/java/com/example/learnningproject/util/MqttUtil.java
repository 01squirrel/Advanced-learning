package com.example.learnningproject.util;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import java.util.logging.Logger;

public class MqttUtil {
    private final Context context;
    private final String serverURI;//tcp://broker.hivemq.com:1883
    private final String clientID;

    private final MqttAndroidClient mqttAndroidClient;

    public MqttUtil(Context context, String serverURI, String clientID) {
        this.context = context;
        this.serverURI = serverURI;
        this.clientID = clientID;
        mqttAndroidClient = new MqttAndroidClient(context,serverURI,clientID);
    }

    public void connect(String username, String password, IMqttActionListener cbConnect, MqttCallback cbClient) {
        mqttAndroidClient.setCallback(cbClient);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        try {
            IMqttToken token = mqttAndroidClient.connect(options,null,cbConnect);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i("MQTT MSG","mqtt connect success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public void subscribe(String topic,int qos,IMqttActionListener cbSubscribe) {
        try {
            mqttAndroidClient.subscribe(topic, qos,null,cbSubscribe);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public void unsubscribe(String topic,IMqttActionListener cbUnsubscribe) {
        try {
            mqttAndroidClient.unsubscribe(topic,null,cbUnsubscribe);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public void publish(String topic,String msg,int qos,boolean retained,IMqttActionListener cbPublish){
        try {
            MqttMessage message = new MqttMessage(msg.getBytes());
            message.setQos(qos);
            message.setRetained(retained);
            mqttAndroidClient.publish(topic,message,null,cbPublish);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnected(IMqttActionListener cbDisconnect) {
        try {
            mqttAndroidClient.disconnect(null,cbDisconnect);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}
