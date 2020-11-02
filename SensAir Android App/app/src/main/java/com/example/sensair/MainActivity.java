package com.example.sensair;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{


    protected Button buttonReadValues;
    protected TextView  eco2, tvoc, combustibleGas;
    protected Intent btEnableIntent;
    protected static final int REQUEST_ENABLE_BT = 1;
    ArrayList<String>  btDevices = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btInit();
        uiInit();
    }

    public void btInit()
    {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        if (btAdapter == null)
        {
            String msg = "ERROR: Phone does not support bluetooth. Bluetooth connection failed!";
            Toast.makeText(this,msg,Toast.LENGTH_LONG);
            return;
        }
        else if (!btAdapter.isEnabled())
        {
            startActivityForResult(btEnableIntent, REQUEST_ENABLE_BT);
        }

        btAdapter.startDiscovery();
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        registerReceiver(bluetoothReceiver,intentFilter);

    }

    BroadcastReceiver bluetoothReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btDevices.add(bluetoothDevice.getName());
            }
        }
    };

    public void uiInit()
    {
        tvoc = findViewById(R.id.tvocData);
        eco2 = findViewById(R.id.eco2Data);
        combustibleGas = findViewById(R.id.combustionData);
        buttonReadValues = findViewById(R.id.buttonReadValues);

        buttonReadValues.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                /*
                 * TODO Here we have to find out how to fetch data from dev board.
                 *       Once we have the data, just set text to readings (maybe process data too)
                 */
                System.out.println("BUTTON CLICKED!!!");
                eco2.setText("x ppm");
                tvoc.setText("y (out of n)");
                combustibleGas.setText("z ppm");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (requestCode == REQUEST_ENABLE_BT)
        {
            if (requestCode == RESULT_OK) {
                String msg = "Bluetooth enabled";
                Toast.makeText(this, msg, Toast.LENGTH_LONG);
            }
            else if (requestCode == RESULT_CANCELED)
            {
                String msg = "Bluetooth enable failed";
                Toast.makeText(this, msg, Toast.LENGTH_LONG);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}