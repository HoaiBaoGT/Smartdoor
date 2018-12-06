package com.example.tranhoaibao.smartphone_project;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttException;

public class MainActivity extends AppCompatActivity {


    MqttControl mqttControl;
    private static final String topic_smartphone = "command_to_raspberry";


    public TextView text_door;
    private Button button_open;
    private Button button_close;
    private Button button_request;

    private int thoigiancho = 2000; //ms



    private Handler mHandeler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text_door = (TextView) findViewById(R.id.textview_door);
        button_open = (Button) findViewById(R.id.button_open);
        button_close = (Button) findViewById(R.id.button_close);
        button_request = (Button) findViewById(R.id.button_request);

        button_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mHandeler.post(tat_button);
                mHandeler.postDelayed(bat_button,thoigiancho);

                try {
                    mqttControl.sendmessage("open",topic_smartphone);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandeler.post(tat_button);
                mHandeler.postDelayed(bat_button,thoigiancho);
                try {
                    mqttControl.sendmessage("close",topic_smartphone);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        button_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandeler.post(tat_button);
                mHandeler.postDelayed(bat_button,thoigiancho);
                try {
                    mqttControl.sendmessage("request",topic_smartphone);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });





        try {
            mqttControl = new MqttControl(text_door);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            mqttControl.close();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    private Runnable tat_button = new Runnable() {
        @Override
        public void run() {
            button_open.setEnabled(false);
            button_close.setEnabled(false);
            button_request.setEnabled(false);
        }
    };
    private Runnable bat_button = new Runnable() {
        @Override
        public void run() {
            button_open.setEnabled(true);
            button_close.setEnabled(true);
            button_request.setEnabled(true);
        }
    };
}