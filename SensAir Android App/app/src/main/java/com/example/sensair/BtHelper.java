package com.example.sensair;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class BtHelper
{
    private TextView myLabel;
    private EditText myTextbox;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mSocket;
    private UUID uuid;
    private BluetoothDevice mmDevice;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private Context context;
    private Handler handler = new Handler();

    public BtHelper(Context context)
    {
        this.context = context;
    }

    void findBT()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("SensAir"))
                {
                    mmDevice = device;
                    Toast.makeText(context,"Successfully connected to the SensAir!",Toast.LENGTH_LONG).show();
                    break;
                }
            }
        }
       // TODO Display toast saying device found or nah
    }

    void openBT() throws IOException
    {
        uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mSocket.connect();
        mOutputStream = mSocket.getOutputStream();
        mInputStream = mSocket.getInputStream();

        ReadData read = new ReadData();
        read.start();
    }

    private interface MessageConstants
    {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }


    private class ReadData extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer;
        private int readBufferPosition = 0;

        public ReadData()
        {
            mmInStream = mInputStream;
            mmOutStream = mOutputStream;
        }

        public void run() {
            mmBuffer = new byte[1024];
            final byte delimiter = 67; //This is the ASCII code for 'C'

            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true)
            {
                try
                {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
//                    Message readMsg = handler.obtainMessage(
//                            MessageConstants.MESSAGE_READ, numBytes, -1,
//                            mmBuffer);
//                    readMsg.sendToTarget();
                } catch (IOException e)
                {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }


        }

    }

    void sendData() throws IOException
    {
        String msg = myTextbox.getText().toString();
        msg += "\n";
        mOutputStream.write(msg.getBytes());
        myLabel.setText("Data Sent");
    }

    void closeBT() throws IOException
    {
        mOutputStream.close();
        mInputStream.close();
        mSocket.close();
        myLabel.setText("Bluetooth Closed");
    }

    boolean isConnected()
    {
        if(mmDevice.getName()=="SensAir")
            return true;
        else
            return false;
    }

}
