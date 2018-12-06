package com.ritodoji.basicmqttandroidthings;

import android.os.Handler;
import android.app.Activity;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.ViewConfiguration;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


import java.io.IOException;


public class MainActivity extends Activity {

    MqttControl mqttControl;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static String pin_lightsensor = "BCM26";
    private static String pin_DCN0 = "BCM5";
    private static String pin_DCN1 = "BCM6";
    private static String pin_DCN2 = "BCM12";
    private static String pin_DCN3 = "BCM16";


    //Sensor kiem tra cua
    private Gpio lightsensor;
    private boolean doorisopen = false;

    //Dong co dong mo cua
    private static final boolean FULL_STEP_MOTOR_SEQUENCE[][] = new boolean[][] {
            { false, false, false, true },
            { false, false, true, false },
            { false, true, false, false },
            { true, false, false, false },
    };

    private Gpio MORTOR0;
    private Gpio MORTOR1;
    private Gpio MORTOR2;
    private Gpio MORTOR3;
    private boolean quay_token = false;


    //Kiem soat trang thai cua

    private Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "Start onCreate.");

        try {

            //Define mortor
            MORTOR0 = PeripheralManager.getInstance().openGpio(pin_DCN0);
            MORTOR0.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            MORTOR1 = PeripheralManager.getInstance().openGpio(pin_DCN1);
            MORTOR1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            MORTOR2 = PeripheralManager.getInstance().openGpio(pin_DCN2);
            MORTOR2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            MORTOR3 = PeripheralManager.getInstance().openGpio(pin_DCN3);
            MORTOR3.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);


            //Define light sensor
            lightsensor = PeripheralManager.getInstance().openGpio(pin_lightsensor);
            lightsensor.setDirection(Gpio.DIRECTION_IN);
            lightsensor.setEdgeTriggerType(Gpio.EDGE_BOTH);
            lightsensor.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {

                    alwaylightsensor();
                    return true;
                }
            });



        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Define Gpio successed");

        //Define MQTT connect
        try {
            mqttControl = new MqttControl(MORTOR0, MORTOR1, MORTOR2, MORTOR3);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Define MQTT successed");

        alwaylightsensor(); // Cap nhat trang thai hien tai len data khivua khoi dong




    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mqttControl.close();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        try{
            lightsensor.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////




    private void alwaylightsensor(){
        try {
            doorisopen = !lightsensor.getValue();
            mqttControl.doorisopen = doorisopen;
            if(!mqttControl.command_mocua && !doorisopen){
                mqttControl.quay_token = 0;
            }
            mqttControl.updatetrangthai();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


