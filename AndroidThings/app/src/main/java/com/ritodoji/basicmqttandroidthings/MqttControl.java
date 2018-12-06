package com.ritodoji.basicmqttandroidthings;


import android.os.Handler;
import android.util.Log;



import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.android.things.pio.Gpio;

import java.io.IOException;

public class MqttControl implements MqttCallback {
    private static final String username = "oillbaqv";
    private static final String password = "K5n201l1eD79";
    private static final String serveruri = "tcp://m15.cloudmqtt.com:19635";
    private static final String clientId = "raspberry_ghentuong";
    private static final String toPic = "command_to_raspberry";
    private static final String TAG = MqttControl.class.getSimpleName();
    private static final  int QOs = 1;
    private MqttClient client ;


    private static final String topic_raspberry_door = "raspberry_door is_open";


    //Dong co dong mo cua
    private static final boolean FULL_STEP_MOTOR_SEQUENCE[][] = new boolean[][] {
            { false, false, false, true },
            { false, false, true, false },
            { false, true, false, false },
            { true, false, false, false },
    };
    private int step_count = 0;
    private Gpio MORTOR0;
    private Gpio MORTOR1;
    private Gpio MORTOR2;
    private Gpio MORTOR3;
    private boolean quay_tien = true;
    public int quay_token = 0;

    //Kiem soat trang thai cua
    public boolean doorisopen = true;
    public boolean command_mocua = true;

    private Handler mHandler = new Handler();


    public  MqttControl(Gpio MORTOR0, Gpio MORTOR1, Gpio MORTOR2, Gpio MORTOR3) throws MqttException {
        this.MORTOR0 = MORTOR0;
        this.MORTOR1 = MORTOR1;
        this.MORTOR2 = MORTOR2;
        this.MORTOR3 = MORTOR3;
        subcribeToTopic();
    }


    public void subcribeToTopic() throws MqttException {
        client = new MqttClient(serveruri,clientId,new MemoryPersistence());
        client.connect(connectOptionchoice());
        client.subscribe(toPic);
        client.setCallback(this);
        Log.d(TAG,"Raspberry connected");

    }

    private static MqttConnectOptions connectOptionchoice(){
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setUserName(username);
        connectOptions.setPassword(password.toCharArray());
        connectOptions.setCleanSession(false);
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setKeepAliveInterval(30);
        return connectOptions;
    }

    public void close() throws MqttException {
        client.disconnect();
        client.close();
        Log.d(TAG,"Raspberry disconnected");
    }

    public void sendmessage(String data, String topic) throws MqttException {
        if(!client.isConnected()){
            subcribeToTopic();
        }
        MqttMessage message = new MqttMessage(data.getBytes());
        message.setQos(QOs);
        client.publish(topic,message);
    }

    @Override
    public void connectionLost(Throwable cause) {
        try {
            Log.d(TAG,"Reconnecting ~ ................(-______-)#......");
            client.connect(MqttControl.connectOptionchoice());
            Log.d(TAG,"Connected success ! ");

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws IOException, MqttException {
        String payload = new String(message.getPayload());
        Log.d(TAG,"message : " + payload);
        //
        if(!(quay_token > 0)){
            if(payload.equals("open") && !doorisopen){
                command_mocua = true;
                dieukhien_dongco(true);
                Log.d(TAG,"quay true");
                return;
            }
            if(payload.equals("close") && doorisopen){
                command_mocua = false;
                dieukhien_dongco(false);
                Log.d(TAG,"quay false");
                return;
            }
            if(payload.equals("request")){
                command_mocua = doorisopen;
                updatetrangthai();
                Log.d(TAG,"gui trang thai");
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }


    public void dieukhien_dongco(boolean huongquay){


        step_count = 0;
        if(huongquay){
            quay_tien = true;
            quay_token = 500;
        }
        else{
            quay_tien = false;
            quay_token = 600;
        }

        mHandler.post(action_quay);
    }



    //Dieu khien dong co
    private Runnable action_quay = new Runnable() {
        @Override
        public void run() {


            try{
                if (quay_token > 0) {
                    if(quay_tien){
                        MORTOR0.setValue(FULL_STEP_MOTOR_SEQUENCE[step_count][0]);
                        MORTOR1.setValue(FULL_STEP_MOTOR_SEQUENCE[step_count][1]);
                        MORTOR2.setValue(FULL_STEP_MOTOR_SEQUENCE[step_count][2]);
                        MORTOR3.setValue(FULL_STEP_MOTOR_SEQUENCE[step_count][3]);
                    }
                    else{
                        MORTOR0.setValue(FULL_STEP_MOTOR_SEQUENCE[step_count][3]);
                        MORTOR1.setValue(FULL_STEP_MOTOR_SEQUENCE[step_count][2]);
                        MORTOR2.setValue(FULL_STEP_MOTOR_SEQUENCE[step_count][1]);
                        MORTOR3.setValue(FULL_STEP_MOTOR_SEQUENCE[step_count][0]);
                    }
                    step_count = (step_count + 1) % 4;
                    quay_token = quay_token - 1;
                    mHandler.postDelayed(action_quay,4);
                }else{
                    step_count = 0;
                    MORTOR0.setValue(false);
                    MORTOR1.setValue(false);
                    MORTOR2.setValue(false);
                    MORTOR3.setValue(false);
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    };
    private Runnable reset_mortor = new Runnable() {
        @Override
        public void run() {
            quay_token = 0;
        }
    };


    public void updatetrangthai() {
        try {
            String data_update = String.valueOf(doorisopen);
            sendmessage(data_update, topic_raspberry_door);
            Log.i(TAG, "Data MQTT was updated.");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}

