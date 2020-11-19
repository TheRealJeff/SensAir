package com.example.sensair;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.sensair.realtimeplotting.AltitudeDataActivity;
import com.example.sensair.realtimeplotting.CarbonDioxideDataActivity;
import com.example.sensair.realtimeplotting.CarbonMonoxideDataActivity;
import com.example.sensair.realtimeplotting.HumidityDataActivity;
import com.example.sensair.realtimeplotting.PressureDataActivity;
import com.example.sensair.realtimeplotting.TemperatureDataActivity;
import com.example.sensair.realtimeplotting.VolatileOrganicCompoundsActivity;

import java.util.ArrayList;
import java.util.Objects;

public class RealTimeDataActivity extends AppCompatActivity
{

    protected Thread thread;
    protected BluetoothService btService;
    protected boolean btIsBound = false;
    protected ArrayAdapter listAdapter;
    protected ListView listView;
    final protected ArrayList<String> metrics = new ArrayList<String>();



    private final String CO2="Carbon Dioxide (CO2)";
    private final  String TVOC="Volatile Organic Compounds (VOC)";
    private final String MQ2="Carbon Monoxide (CO)";
    private final String HUMIDITY="Humidity";
    private final String PRESSURE="Pressure";
    private final String ALTITUDE="Altitude";
    private final String TEMPERATURE="Temperature";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_data);

        uiInit();
        startBluetoothThreading();
    }

    public void uiInit()
    {
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Choose an Air Quality Metric");

        ArrayList<String> metrics = new ArrayList<String>();
        metrics.add(CO2);
        metrics.add(MQ2);
        metrics.add(TVOC);
        metrics.add(HUMIDITY);
        metrics.add(PRESSURE);
        metrics.add(TEMPERATURE);
        metrics.add(ALTITUDE);

        listAdapter = new ArrayAdapter<String>(this, R.layout.item_metric,R.id.airQualityMetric,metrics);
        listView = findViewById(R.id.airQualityMetricList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                final String metric = (String) parent.getItemAtPosition(position);
                Intent intent;
                switch (metric)
                {
                    case CO2:
                        intent = new Intent(RealTimeDataActivity.this, CarbonDioxideDataActivity.class);
                        startActivity(intent);
                        break;
                    case MQ2:
                        intent = new Intent(RealTimeDataActivity.this, CarbonMonoxideDataActivity.class);
                        startActivity(intent);
                        break;
                    case TVOC:
                        intent = new Intent(RealTimeDataActivity.this, VolatileOrganicCompoundsActivity.class);
                        startActivity(intent);
                        break;
                    case HUMIDITY:
                        intent = new Intent(RealTimeDataActivity.this, HumidityDataActivity.class);
                        startActivity(intent);
                        break;
                    case PRESSURE:
                        intent = new Intent(RealTimeDataActivity.this,PressureDataActivity.class);
                        startActivity(intent);
                        break;
                    case TEMPERATURE:
                        intent = new Intent(RealTimeDataActivity.this, TemperatureDataActivity.class);
                        startActivity(intent);
                        break;
                    case ALTITUDE:
                        intent = new Intent(RealTimeDataActivity.this, AltitudeDataActivity.class);
                        startActivity(intent);
                    default:
                        throw new IllegalStateException("Unexpected value: " + metric);
                }
            }
        });

        listView.setAdapter(listAdapter);
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
                                System.out.println("REAL TIME THREAD");
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