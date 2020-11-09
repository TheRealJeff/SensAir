package com.example.sensair;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BtHelper extends AppCompatActivity
{
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private Handler handler; //get info from Bluetooth Services
    protected Context context;
    protected BluetoothDevice device;
    protected final UUID MY_UUID =  UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected mBluetoothService mmBluetoothService;

    public BtHelper(Context context)
    {
        this.context = context;
    }

    //test comment

    //Define Messages when transmitting between the service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }

    private class  ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;
        //        private final InputStream mInStream;
        //private final OutputStream mOutStream; Use this to send data to target device
        private byte[] mBuffer; //mBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mDevice = device;
            BluetoothSocket tmp = null;
            mmBluetoothService = new mBluetoothService(socket);

            //Get the input and output stream, using temp because member streams are final
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);

            } catch (IOException e) {
                Log.e(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!       Error occurred when creating socket.", e);
            }
            mSocket = tmp;
        }

        public void run() {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            btAdapter.cancelDiscovery();

            try {
                mSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }
            mmBluetoothService = new mBluetoothService(mSocket);
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    public boolean isConnected()
    {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices)
        {
            if (device.getName().equals("SensAir"))
            {
                setDevice(device);
                return true;
            }
        }
        return false;
    }

    public void setDevice(BluetoothDevice device)
    {
        this.device = device;
    }
}
