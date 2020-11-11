package com.example.sensair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.github.anastr.speedviewlib.SpeedView;
import com.github.anastr.speedviewlib.components.Section;
import com.github.anastr.speedviewlib.components.Style;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.basicairdata.bluetoothhelper.BluetoothHelper;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{

    protected SpeedView gaugeAirQuality;
    protected ImageButton buttonRealTime, buttonHistory, buttonProfile;
    protected static final int REQUEST_ENABLE_BT = 1;
    protected List<String> categories = new ArrayList<>();
    protected BluetoothHelper mBluetooth = new BluetoothHelper();
    private String DEVICE_NAME = "SensAir";

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

//        btInit();
        uiInit();
        dropDownInit();


        mBluetooth.Connect(DEVICE_NAME);
        mBluetooth.setBluetoothHelperListener(new BluetoothHelper.BluetoothHelperListener() {
            @Override
            public void onBluetoothHelperMessageReceived(BluetoothHelper bluetoothhelper, final String message)
            {
                 runOnUiThread(new Runnable()
                 {
                     @Override
                     public void run()
                     {
                         // Update here your User Interface
                         System.out.println(message);
                         String[] data = message.split(",");
                         co2 = Float.parseFloat(data[0].substring(0,data[0].length()-1));
                         tvoc = Float.parseFloat(data[1].substring(0,data[1].length()-1));
                         mq2 = Float.parseFloat(data[2].substring(0,data[2].length()-1));
                         humidity = Float.parseFloat(data[3].substring(0,data[3].length()-1));
                         pressure = Float.parseFloat(data[4].substring(0,data[4].length()-1));
                         altitude = Float.parseFloat(data[5].substring(0,data[5].length()-1));
                         temperature = Float.parseFloat(data[6].substring(0,data[6].length()-1));

                         System.out.println("\n\n-------------------DATA REQUEST-------------------");
                         System.out.println("CO2: "+(co2));
                         System.out.println("TVOC: "+tvoc);
                         System.out.println("MQ2: "+mq2);
                         System.out.println("Humidity: "+humidity);
                         System.out.println("Pressure: "+pressure);
                         System.out.println("Altitude: "+altitude);
                         System.out.println("Temperature: "+temperature);
                     }
                 });
            }

            @Override
            public void onBluetoothHelperConnectionStateChanged(BluetoothHelper bluetoothhelper, boolean isConnected) {
                if (isConnected)
                {
                    System.out.println("Connected");
                    mBluetooth.SendMessage("1");
                    System.out.println("Sending Message");
                }
                else
                {
                    System.out.println("Disconnected");
                    mBluetooth.Connect(DEVICE_NAME);
                }
            }
        });

    }

    public void getData()
    {
        mBluetooth.SendMessage("1");
    }

    public void btInit()
    {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);


        if (btAdapter == null)
        {
            String msg = "ERROR: Phone does not support bluetooth. Bluetooth connection failed!";
            Toast.makeText(this, msg, Toast.LENGTH_LONG);
            return;
        } else if (!btAdapter.isEnabled())
        {
            startActivityForResult(btEnableIntent, REQUEST_ENABLE_BT);
        }
        //TODO HANDLE SUCCESSFUL DEVICE CONNECTION

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
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

    }

    public void gaugeInit()
    {
        // TODO set to user preference / default
//        gaugeAirQuality.speedTo(75);
        gaugeAirQuality.setTrembleDegree((float)0.5);
//        gaugeAirQuality.makeSections(3, 0, Style.BUTT);
//        ArrayList<Section> sections = gaugeAirQuality.getSections();
//        sections.get(0).setColor(Color.rgb(250, 67, 67));
//        sections.get(1).setColor(Color.rgb(255, 255, 102));
//        sections.get(2).setColor(Color.rgb(90, 245, 110));

    }

    public void dropDownInit()
    {
        Spinner spinner = findViewById(R.id.spinner);
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
        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(0);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:     // overall quality
                //TODO Handle Overall choice and display meter values

                break;
            case 1:     // CO2          TODO for all: adjust sections so that danger zones are properly reflected
                gaugeAirQuality.speedTo(0);
                gaugeAirQuality.setMinMaxSpeed(0,2000);
                gaugeAirQuality.setUnit(" PPM");
                gaugeAirQuality.speedTo(co2);
                break;
            case 2:     // TVOC
                getData();
                gaugeAirQuality.speedTo(0);
                gaugeAirQuality.setMinMaxSpeed(0,600);
                gaugeAirQuality.setUnit(" PPB");
                gaugeAirQuality.speedTo(tvoc);
                break;
            case 3:     // MQ2
                getData();
                gaugeAirQuality.speedTo(0);
                gaugeAirQuality.setMinMaxSpeed(0,600);
                gaugeAirQuality.setUnit(" PPB");
                gaugeAirQuality.speedTo(mq2);
                break;
            case 4:     // Humidity
                getData();
                gaugeAirQuality.speedTo(0);
                gaugeAirQuality.setMinMaxSpeed(0,100);
                gaugeAirQuality.setUnit(" %");
                gaugeAirQuality.speedTo(humidity);
                break;
            case 5:     // Pressure
                getData();
                gaugeAirQuality.speedTo(0);
                gaugeAirQuality.setMinMaxSpeed(0,150);
                gaugeAirQuality.setUnit(" KPa");
                gaugeAirQuality.speedTo(pressure/1000);
                break;
            case 6:     // Temperature
                getData();
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

    @Override
    protected void onPause()
    {
        super.onPause();
        //TODO Check if still connected

//            String msg = "Oops! Lost connection to the SensAir. Please pair device in Settings.";
//            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

    }

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