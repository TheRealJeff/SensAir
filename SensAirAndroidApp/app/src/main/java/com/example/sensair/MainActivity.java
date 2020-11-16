package com.example.sensair;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import com.github.anastr.speedviewlib.SpeedView;
import com.github.anastr.speedviewlib.components.Section;
import com.github.anastr.speedviewlib.components.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import eu.basicairdata.bluetoothhelper.BluetoothHelper;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{

    protected SpeedView gaugeAirQuality;
    protected ImageButton buttonRealTime, buttonHistory, buttonProfile;
    protected List<String> categories = new ArrayList<>();
    protected Thread thread;
    protected Spinner spinner;
    protected BluetoothService btService;
    protected boolean btIsBound = false;

    private float co2;
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
        setContentView(R.layout.activity_main);

        uiInit();
        dropDownInit();

        if(checkBluetoothConnection())
        {
            longToast("Successfully connected to the SensAir Device!");
            startBluetoothThreading();
        }
        else
        {
            longToast("Failed to connect to the SensAir Device. Please check Bluetooth Settings and try again.");
        }
    }

    public void uiInit()
    {
        gaugeAirQuality = (SpeedView) findViewById(R.id.gaugeAirQuality);
        gaugeInit();

        buttonRealTime = (ImageButton) findViewById(R.id.buttonRealTime);
        buttonHistory = (ImageButton) findViewById(R.id.buttonHistory);
        buttonProfile = (ImageButton) findViewById(R.id.buttonProfile);

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
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

    }

    public void gaugeInit()
    {
        // TODO set to user preference / default
        gaugeAirQuality.setWithTremble(false);
    }

    public void dropDownInit()
    {
        spinner = findViewById(R.id.spinner);
        assert spinner != null;
        spinner.setOnItemSelectedListener(this);

        categories.add("Overall Air Quality");
        categories.add("CO2");
        categories.add("TVOC");
        categories.add("Combustible Gas");
        categories.add("Humidity");
        categories.add("Pressure");
        categories.add("Temperature");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(0);
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
                            co2 = btService.getCo2();
                            tvoc = btService.getTvoc();
                            mq2 = btService.getMq2();
                            humidity = btService.getHumidity();
                            pressure = btService.getPressure();
                            altitude = btService.getAltitude();
                            temperature = btService.getTemperature();
                        }

                        switch (spinner.getSelectedItemPosition())
                        {
                            case 0:     // overall quality
                                //TODO Handle Overall choice and display meter values
                                break;
                            case 1:     // CO2          TODO for all: adjust sections so that danger zones are properly reflected
                                gaugeAirQuality.speedTo(co2);
                                break;
                            case 2:     // TVOC
                                gaugeAirQuality.speedTo(tvoc);
                                break;
                            case 3:     // MQ2
                                gaugeAirQuality.speedTo(mq2);
                                break;
                            case 4:     // Humidity
                                gaugeAirQuality.speedTo(humidity);
                                break;
                            case 5:     // Pressure
                                gaugeAirQuality.speedTo(pressure / 1000);
                                break;
                            case 6:     // Temperature
                                gaugeAirQuality.speedTo(temperature);
                                break;
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
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
//        Section section_1;
//        Section section_2;
//        Section section_3;

        switch (position)
        {
            case 0:     // overall quality
                //TODO Handle Overall choice and display meter values
//                sections = gaugeAirQuality.getSections();
//                sections.removeAll(sections);
//                gaugeAirQuality.makeSections(3, 0, Style.BUTT);
//                ArrayList<Section> sections = gaugeAirQuality.getSections();
//
//                section_1 = sections.get(0);
//                section_2 = sections.get(1);
//                section_3 = sections.get(2);
//
//                section_1.setColor(Color.rgb(250, 67, 67));
//                section_2.setColor(Color.rgb(255, 255, 102));
//                section_3.setColor(Color.rgb(90, 245, 110));
                break;
            case 1:     // CO2          TODO for all: adjust sections so that danger zones are properly reflected
//                sections = gaugeAirQuality.getSections();
//                sections.removeAll(sections);
//                gaugeAirQuality.makeSections(3, 0, Style.BUTT);
//                sections = gaugeAirQuality.getSections();
//                section_1 = sections.get(0);
//                section_2 = sections.get(1);
//                section_3 = sections.get(2);
//
                gaugeAirQuality.speedTo(0);
                gaugeAirQuality.setMinMaxSpeed(0,2500);
//
//                section_1.setColor(Color.rgb(90, 245, 110));
//                section_2.setColor(Color.rgb(255, 255, 102));
//                section_3.setColor(Color.rgb(250, 67, 67));
//                    section_1.setStartOffset(0);
//                    section_1.get((float)(1000/2500));
//                    section_2.setStartEndOffset((float).4004,(float).8);
//                    section_3.setStartEndOffset((float).8004,1);

                gaugeAirQuality.setUnit(" ppm");
                gaugeAirQuality.speedTo(co2);
                break;
            case 2:     // TVOC
                gaugeAirQuality.speedTo(0);
                gaugeAirQuality.setMinMaxSpeed(0,600);
                gaugeAirQuality.setUnit(" ppb");
                gaugeAirQuality.speedTo(tvoc);
                break;
            case 3:     // MQ2
                gaugeAirQuality.speedTo(0);
                gaugeAirQuality.setMinMaxSpeed(0,600);
                gaugeAirQuality.setUnit(" ppb");
                gaugeAirQuality.speedTo(mq2);
                break;
            case 4:     // Humidity
                gaugeAirQuality.speedTo(0);
                gaugeAirQuality.setMinMaxSpeed(0,100);
                gaugeAirQuality.setUnit(" %");
                gaugeAirQuality.speedTo(humidity);
                break;
            case 5:     // Pressure
                gaugeAirQuality.speedTo(0);
                gaugeAirQuality.setMinMaxSpeed(0,150);
                gaugeAirQuality.setUnit(" KPa");
                gaugeAirQuality.speedTo(pressure/1000);
                break;
            case 6:     // Temperature
                gaugeAirQuality.speedTo(0);
                gaugeAirQuality.setMinMaxSpeed(0,40);
                gaugeAirQuality.setUnit(" Celsius");
                gaugeAirQuality.speedTo(temperature);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {

    }

    protected void onResume()
    {
        super.onResume();
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        boolean isPaired = false;
        if(btAdapter!=null)
        {
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices)
            {
                if (device.getName().equals("SensAir"))
                    isPaired = true;
            }
            if (!isPaired)
            {
                longToast("Oops! Looks like the SensAir device was disconnected. Please reconnect in settings.");
            }
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
        int id = item.getItemId();
        if(id == R.id.infoButton)
        {
            Intent intent = new Intent(MainActivity.this, InfoActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
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

    public void print(String s)
    {
        System.out.println(s);
    }


    public void longToast(String toast_message)
    {
        Toast.makeText(this,toast_message,Toast.LENGTH_LONG).show();
    }

    public void shortToast(String toast_message)
    {
        Toast.makeText(this,toast_message,Toast.LENGTH_SHORT).show();
    }
}