package com.example.sensair;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.github.anastr.speedviewlib.SpeedView;
import com.github.anastr.speedviewlib.components.Section;
import com.github.anastr.speedviewlib.components.Style;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{


    protected TextView textViewGauge;
    protected SpeedView gaugeAirQuality;
    protected Button buttonRealTime, buttonHistory, buttonProfile;
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
        registerReceiver(bluetoothReceiver,intentFilter);

        ArrayList<BluetoothDevice>  pairedDevices = new ArrayList<>();
        for(BluetoothDevice device : pairedDevices)
        {
            if(device.getName() == "SensAir")
            {
                String msg = "Connected to the SensAir!";
                Toast.makeText(this, msg, Toast.LENGTH_LONG);
            }
            else
            {
                String msg = "Failed to connect to bluetooth: Please pair device in Settings.";
                Toast.makeText(this, msg, Toast.LENGTH_LONG);
            }
        }

    }



    public void uiInit()
    {
        gaugeAirQuality = (SpeedView) findViewById(R.id.gaugeAirQuality);
        gaugeInit();

        buttonRealTime = (Button) findViewById(R.id.buttonRealTime);
        buttonHistory = (Button) findViewById(R.id.buttonHistory);
        buttonProfile = (Button) findViewById(R.id.buttonProfile);

        buttonRealTime.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View V)
            {
                Intent intent = new Intent(MainActivity.this, RealTimeActivity.class);
                startActivity(intent);
            }
        });

        buttonHistory.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View V)
            {
                Intent intent = new Intent(MainActivity.this, HistoryActivityListActivity.class);
                startActivity(intent);
            }
        });

        buttonProfile.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

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

    public void gaugeInit()
    {
               // TODO set to user preference / default
        gaugeAirQuality.speedTo(75);
        gaugeAirQuality.setTrembleDegree(1);
        gaugeAirQuality.makeSections(3,0,Style.BUTT);
        ArrayList<Section> sections = gaugeAirQuality.getSections();
        sections.get(0).setColor(Color.rgb(250,67,67));
        sections.get(1).setColor(Color.rgb(255,255,102));
        sections.get(2).setColor(Color.rgb(90,245,110));

        textViewGauge = (TextView) findViewById(R.id.gaugeAnalysis);
        float current_speed = gaugeAirQuality.getSpeed();
        if(current_speed<33.33)
        {
            textViewGauge.setText("Poor. Evacuate.");
            textViewGauge.setTextColor(Color.rgb(250,67,67));
        }
        else if(current_speed<66.66)
        {
            textViewGauge.setText("Moderate.");
            textViewGauge.setTextColor(Color.rgb(255,255,102));
        }
        else if(current_speed<100)
        {
            textViewGauge.setText("Excellent!");
            textViewGauge.setTextColor(Color.rgb(16,196,10));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();  // inflates menu designed in /res/menu
        inflater.inflate(R.menu.menu_main_activity,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();          // gets item ID
        if(id == R.id.infoButton)          // if edit button is clicked
        {
            Intent intent = new Intent(MainActivity.this, InfoActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}