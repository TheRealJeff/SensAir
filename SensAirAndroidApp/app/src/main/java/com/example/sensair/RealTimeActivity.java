package com.example.sensair;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class RealTimeActivity extends AppCompatActivity
{
    protected final String DEVICE_NAME = "SensAir";
    protected Thread thread;
    protected BluetoothService btService;
    protected boolean btIsBound = false;
    public static Handler UIHandler;

    private float co;
    private float tvoc;
    private float mq2;
    private float humidity;
    private float pressure;
    private float altitude;
    private float temperature;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_real_time);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_hazardous_gasses,R.id.navigation_ambient_air_quality)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if(checkBluetoothConnection())
        {
            startBluetoothThreading();
        }
        else
        {
            longToast("Failed to connect to the SensAir Device. Please check Bluetooth Settings and try again.");
        }
    }

    static
    {
        UIHandler = new Handler(Looper.getMainLooper());
    }
    public static void runOnUI(Runnable runnable){
        UIHandler.post(runnable);
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
                                co = btService.getCo2();
                                tvoc = btService.getTvoc();
                                mq2 = btService.getMq2();
                                humidity = btService.getHumidity();
                                pressure = btService.getPressure();
                                altitude = btService.getAltitude();
                                temperature = btService.getTemperature();
                            }
                        }
                    });
                }
            }
        };
        thread.start();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        unbindService(connection);
        btIsBound = false;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, connection, Context.BIND_ADJUST_WITH_ACTIVITY | Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection connection = new ServiceConnection() {

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

    @Override
    public boolean onSupportNavigateUp()
    {
        super.onBackPressed();
        return true;
    }

    public void longToast(String toast_message)
    {
        Toast.makeText(this,toast_message,Toast.LENGTH_LONG).show();
    }

    public Boolean checkBluetoothConnection()
    {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        if (btAdapter == null)
        {
            return false;
        }

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        for(BluetoothDevice device : pairedDevices)
        {
            if(device.getName().equals("SensAir"))
                return true;
        }
        return false;
    }

    public void print(String message)
    {
        System.out.println(message);
    }

    public float getco()
    {
        return co;
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
}