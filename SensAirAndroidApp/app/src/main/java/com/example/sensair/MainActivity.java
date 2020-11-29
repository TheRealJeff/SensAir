package com.example.sensair;


import androidx.annotation.Nullable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.bluetooth.BluetoothAdapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
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



import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.Manifest;

import eu.basicairdata.bluetoothhelper.BluetoothHelper;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,LocationListener
{
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    protected SpeedView gaugeAirQuality;
    protected ImageButton buttonRealTime, buttonHistory, buttonProfile;
    protected static final int REQUEST_ENABLE_BT = 1;
    protected List<String> categories = new ArrayList<>();
    protected BluetoothHelper mBluetooth = new BluetoothHelper();
    private final String DEVICE_NAME = "SensAir";
    private Thread thread;
    Spinner spinner;

    private float co2;
    private float tvoc;
    private float mq2;
    private float humidity;
    private float pressure;
    private float altitude;
    private float temperature;

    DBHelper AirDB ;
    List<AirData> airDataList ;
    static String key;
    LocationManager locationManager;
    String provider;


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


                         /** String[] data = message.split(",");
                         co2 = Float.parseFloat(data[0].substring(0,data[0].length()-1));
                         tvoc = Float.parseFloat(data[1].substring(0,data[1].length()-1));
                         mq2 = Float.parseFloat(data[2].substring(0,data[2].length()-1));
                         humidity = Float.parseFloat(data[3].substring(0,data[3].length()-1));
                         pressure = Float.parseFloat(data[4].substring(0,data[4].length()-1));
                         altitude = Float.parseFloat(data[5].substring(0,data[5].length()-1));
                         temperature = Float.parseFloat(data[6].substring(0,data[6].length()-1));
                         AirData airData = new AirData(null,String.valueOf(co2),String.valueOf(tvoc),
                                 String.valueOf(mq2),String.valueOf(humidity),String.valueOf(pressure),String.valueOf(temperature));
                          */
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

        thread = new Thread() {

            @Override
            public void run() {
                while (!thread.isInterrupted())
                {
                    try
                    {
                        Thread.sleep(100);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getData();
                            switch (spinner.getSelectedItemPosition()) {
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

        AirDB = new DBHelper(getApplicationContext());
        airDataList = AirDB.getAllData();
        Log.i("Location Info", "got data!");


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        checkLocationPermission();
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {

            Log.i("Location Info", "Location achieved!");

        } else {

            Log.i("Location Info", "No location :(");

        }
        if(airDataList.size() == 0){
            Log.i("myapp" , "size is zero");
            Double latt = location.getLatitude();
            Double longe = location.getLongitude();
            Log.i("latlong", latt + " " + longe );
            AirData airData = new AirData("get", "good", "kid", "kid", "kid", "kid", "kid", latt, longe );
            AirDB.insertAirData(airData);
        }
        else{
            AirData temp = airDataList.get(airDataList.size()-1);
            Double latt = location.getLatitude();
            Double longe = location.getLongitude();
            Log.i("latlong", latt + " " + longe );
            AirData airData = new AirData("get", "good", "kid", "kid", "kid", "kid", "kid", latt, longe);
            if(temp.getHour() != airData.getHour()) {
                AirDB.insertAirData(airData);
                Log.i("myapp", "adding to air data");
            }
        }

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
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
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
        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(0);
    }

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


                getData();
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
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationManager.removeUpdates(this);
        }
        //TODO Check if still connected

//            String msg = "Oops! Lost connection to the SensAir. Please pair device in Settings.";
//            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

    }

    protected void onResume()
    {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(provider, 400, 1, this);
        }
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

    @Override
    public void onLocationChanged(Location location) {

        Double lat = location.getLatitude();
        Double lng = location.getLongitude();

        Log.i("Location info: Lat", lat.toString());
        Log.i("Location info: Lng", lng.toString());

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void getLocation(View view) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        onLocationChanged(location);


    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        locationManager.requestLocationUpdates(provider, 400, 1, this);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }



}