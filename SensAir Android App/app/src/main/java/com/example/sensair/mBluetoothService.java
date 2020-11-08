package com.example.sensair;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class mBluetoothService
{
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private Handler handler; // handler that gets info from Bluetooth service
    private BluetoothSocket socket;

    public mBluetoothService(BluetoothSocket socket)
    {
        this.socket = socket;
    }

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket)
        {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            String value = "";

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try
            {
                tmpIn = socket.getInputStream();
                tmpIn.skip(tmpIn.available());
                // TODO start with displaying on terminal. Eventually make a switch out of this
                while(true)
                {
                    byte b = (byte) tmpIn.read();

                    if (((char) b) == 'C')
                    {
                        System.out.println("!!!!!!!!!!!         ITS BEEN DONE\n\n\t\tCO2 Read: "+Integer.parseInt(value));
                        break;
                    }
                    if (((char) b) == 't')
                    {
                        System.out.println("!!!!!!!!!!!         ITS BEEN DONE\n\n\t\tTVOC Read: "+Integer.parseInt(value));
                        break;
                    }
                    if (((char) b) == 'M')
                    {
                        System.out.println("!!!!!!!!!!!         ITS BEEN DONE\n\n\t\tMQ2 Read: "+Integer.parseInt(value));
                        break;
                    }
                    if (((char) b) == 'H')
                    {
                        System.out.println("!!!!!!!!!!!         ITS BEEN DONE\n\n\t\tHumidity Read: "+Integer.parseInt(value));
                        break;
                    }
                    if (((char) b) == 'A')
                    {
                        System.out.println("!!!!!!!!!!!         ITS BEEN DONE\n\n\t\tAltitude Read: "+Integer.parseInt(value));
                        break;
                    }
                    if (((char) b) == 'T')
                    {
                        System.out.println("!!!!!!!!!!!         ITS BEEN DONE\n\n\t\tTemp Read: "+Integer.parseInt(value));
                        break;
                    }
                    else
                    {
                        value+=(char) b;
                    }
                }

            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = handler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}