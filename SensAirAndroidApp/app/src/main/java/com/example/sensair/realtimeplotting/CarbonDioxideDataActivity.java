package com.example.sensair.realtimeplotting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.sensair.BluetoothService;
import com.example.sensair.R;

import java.util.ArrayList;
import java.util.Objects;

public class CarbonDioxideDataActivity extends AppCompatActivity
{

    protected Thread thread;
    protected BluetoothService btService;
    protected boolean btIsBound = false;

    private float co2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carbon_dioxide_data);
        uiInit();
        startBluetoothThreading();
    }

    public void uiInit()
    {
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Carbon Dioxide (CO)");
    }


    public void startBluetoothThreading()
    {
        thread =new Thread()
        {

            @Override
            public void run () {
                while (!thread.isInterrupted())
                {
                    try
                    {
                        Thread.sleep(100);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(btIsBound)
                            {
                                co2 = btService.getCo2();
                                System.out.println("CO2 DATA THREAD");
                            }
                        }
                    });
                }
            }
        };
        thread.start();
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, connection, Context.BIND_ADJUST_WITH_ACTIVITY | Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        super.onStop();
        unbindService(connection);
        btIsBound = false;
    }


    private final ServiceConnection connection = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            btService = binder.getService();
            btIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            btIsBound = false;
        }
    };

}