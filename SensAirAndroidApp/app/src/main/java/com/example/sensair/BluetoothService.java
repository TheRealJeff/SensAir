package com.example.sensair;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.Set;
import eu.basicairdata.bluetoothhelper.BluetoothHelper;

public class BluetoothService extends Service
{
    protected final IBinder binder = new LocalBinder();

    protected BluetoothHelper mBluetooth = new BluetoothHelper();
    protected BluetoothDevice mBluetoothDevice;
    protected final String DEVICE_NAME = "SensAir";
    protected Thread thread;
    BluetoothAdapter btAdapter;

    private float co2;
    private float tvoc;
    private float mq2;
    private float humidity;
    private float pressure;
    private float altitude;
    private float temperature;

    public class LocalBinder extends Binder
    {
        public BluetoothService getService()
        {
            return BluetoothService.this;
        }
    }

    @Override
    public void onCreate()
    {
        btInit();
        connect();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    public void getData()
    {
        mBluetooth.SendMessage("1");
    }

    public void btInit()
    {
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        boolean isPaired = false;
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        for(BluetoothDevice device : pairedDevices)
        {
            if(device.getName().equals("SensAir"))
            {
                mBluetoothDevice = device;
                isPaired = true;
            }
        }
        if(isPaired)
        {
            startBluetoothThreading();
        }
    }

    public void startBluetoothThreading()
    {
        thread = new Thread() {

            @Override
            public void run() {
                while (!thread.isInterrupted())
                {
                    try
                    {
                        Thread.sleep(10);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    if(mBluetooth!=null)
                        getData();
                }
            }
        };
        thread.start();
    }

    public void connect()
    {
        mBluetooth.Connect(mBluetoothDevice);
        mBluetooth.setBluetoothHelperListener(new BluetoothHelper.BluetoothHelperListener() {
            @Override
            public void onBluetoothHelperMessageReceived(BluetoothHelper bluetoothhelper, final String message)
            {
                String[] data = message.split(",");
                if (data.length == 7)
                {
                    co2= Float.parseFloat(data[0].substring(0, data[0].length() - 1));
                    tvoc = Float.parseFloat(data[1].substring(0, data[1].length() - 1));
                    mq2 = Float.parseFloat(data[2].substring(0, data[2].length() - 1));
                    humidity = Float.parseFloat(data[3].substring(0, data[3].length() - 1));
                    pressure = Float.parseFloat(data[4].substring(0, data[4].length() - 1));
                    altitude = Float.parseFloat(data[5].substring(0, data[5].length() - 1));
                    temperature = Float.parseFloat(data[6].substring(0, data[6].length() - 1));
                }
            }

            @Override
            public void onBluetoothHelperConnectionStateChanged(BluetoothHelper bluetoothhelper, boolean isConnected) {
                if (isConnected)
                {
                    System.out.println("Connected");
                }
                else
                {
                    System.out.println("Disconnected");
                    mBluetooth.Connect(mBluetoothDevice);
                }
            }
        });
    }

    public void disconnect()
    {
        mBluetooth.Disconnect(true);
    }

    public void print(String s)
    {
        System.out.println(s);
    }

    public float getCo2()
    {
        return co2;
    }
    public float getTvoc()
    {
        return tvoc;
    }
    public float getMq2()
    {
        return mq2;
    }
    public float getHumidity()
    {
        return humidity;
    }
    public float getPressure()
    {
        return pressure;
    }
    public float getAltitude()
    {
        return altitude;
    }
    public float getTemperature()
    {
        return temperature;
    }
    public float getOverallQuality()
    {
        float co_weight = 0;
        float tvoc_weight = 0;
        float mq2_weight = 0;

        if(co2<1000) co_weight=1;
        if(tvoc<250) tvoc_weight=1;
        if(mq2<240) mq2_weight = 1;

        float score = (co_weight+tvoc_weight+mq2_weight)/3;
        score*=100f;
        return score;
    }
}
