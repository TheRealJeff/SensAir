package com.example.sensair;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class mBluetoothService extends AppCompatActivity {
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private Handler handler; //get info from Bluetooth Services

    //Define Messages when transmitting between the service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }

    private class  ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        //private final OutputStream mOutStream; Use this to send data to target device
        private byte[] mBuffer; //mBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket){
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //Get the input and output stream, using temp because member streams are final
            try{
                tmpIn = socket.getInputStream();
            }catch (IOException e){
                Log.e(TAG, "Error occurred when creating input stream", e);
            }

            mInStream = tmpIn;
        }

        public void run() {
            mBuffer = new byte[1024];
            int numBytes;

            //Listen Until Execption
            while (true) {
                try {
                    // Read from InputStream
                    numBytes = mInStream.read(mBuffer);
                    //Send the obtained bytes to the UI activity.
                    Message readMsg = handler.obtainMessage(MessageConstants.MESSAGE_READ, numBytes, -1, mBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }
        public void cancel(){
            try{
                mSocket.close();
            } catch (IOException e){
                Log.e(TAG, "Could not close the connect socket",e);
            }
        }
    }
}
