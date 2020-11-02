package com.example.sensair;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import static androidx.core.app.ActivityCompat.startActivityForResult;

public class BtHelper
{
   static final UUID mUUID = UUID.fromString("902c3dac-8cb1-4f58-8d66-ef9b13ed4095");
   private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter myBTAdapter;
    private BluetoothSocket btSocket = null;
    private int highHR = 0;
    private int lowHR = 0;
    protected BluetoothDevice hc05;
    protected Context btContext;
    protected boolean firstOn;
    private boolean streamOn;

    public BtHelper(Context context, String mac)
    {
        myBTAdapter = BluetoothAdapter.getDefaultAdapter();
        streamOn = false;
        btContext = context;
        setHc05(mac);
    }

    public void btEnable(Activity activity){
        if(!myBTAdapter.isEnabled()){
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(activity, enableBT, REQUEST_ENABLE_BT, null);
        }
    }

    public void setHc05(String mac)
    {
        hc05 = myBTAdapter.getRemoteDevice(mac);
    }

    public void estConnect()
    {
        new BTconnection().execute();
    }

    public BluetoothDevice getHC05()
    {
        return hc05;
    }

    public ArrayList<BluetoothDevice> deviceList()
    {
        return new ArrayList<>(myBTAdapter.getBondedDevices());

    }

    private class BTconnection extends AsyncTask<Void, Void, Void>
    {
        boolean isConnected = true;

        @Override
        protected void onPreExecute() {
            // TODO can display here when connecting to device
        }

        @Override
        protected Void doInBackground(Void... devices)
        {

            try {
                if (btSocket == null || !streamOn) {
                    //disconnectnConfirm();
                    hc05 = getHC05();
                    btSocket = hc05.createInsecureRfcommSocketToServiceRecord(mUUID);
                    btSocket.connect();
                }
            } catch (IOException e) {
                isConnected = false;
            }
            if (!isConnected)
            {
                    // TODO notify that device is off
            }
            return null;
        }

        @Override
        protected void onPostExecute (Void result){
            super.onPostExecute(result);

            if(!isConnected){
                Toast.makeText(btContext,"Connection to BlueTooth Failed.",Toast.LENGTH_LONG);
                if(btSocket != null)
                {
                    try
                    {
                        btSocket = null;
                        hc05 = getHC05();
                        btSocket = hc05.createInsecureRfcommSocketToServiceRecord(mUUID);
                        btSocket.connect();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    };

                }
            } else {
                Toast.makeText(btContext,"Successfully connected to SensAir device!",Toast.LENGTH_LONG);
                streamOn = true;
            }
        }
    }

    private class ContentAsync extends AsyncTask<Void,Void,Void>
    {

        public ContentAsync()
        {
            // TODO content synchronization
        }
        @Override
        protected Void doInBackground(Void... voids)
        {
            return null;
        }
    }

    public void content(TextView textview, boolean active)
    {
        InputStream inputStream = null;
        String data = "";
        System.out.println("content: BTSOCKET" + btSocket);
        if(btSocket != null){
            if(btSocket.isConnected()) {
                try {
                    inputStream = btSocket.getInputStream();
                    inputStream.skip(inputStream.available());

                    for (int i = 0; i < 3; i++)
                    {
                        byte b = (byte) inputStream.read();
                        data += (char) b;
                    }

                    // TODO Handle reading of bytes

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        refresh(textview, (btContext));
    }

    private void refresh(final TextView textview, final Context context)
    {
        final Handler handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
            }
        };

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                // TODO Synchronize content. Run threads to send data

            }
        };
        handler.postDelayed(runnable, 4000);
    }
}

