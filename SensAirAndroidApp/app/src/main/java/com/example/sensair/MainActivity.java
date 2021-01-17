package com.example.sensair;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;

import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
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

import com.example.sensair.history.AirData;
import com.example.sensair.history.DBHelper;
import com.example.sensair.history.HistoryActivity;
import com.example.sensair.realtimeplotting.LoggedDataActivity;
import com.github.anastr.speedviewlib.SpeedView;
import com.github.anastr.speedviewlib.components.Section;
import com.github.anastr.speedviewlib.components.Style;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import eu.basicairdata.bluetoothhelper.BluetoothHelper;
import kotlin.jvm.functions.Function2;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,LocationListener
{

    protected SpeedView gaugeAirQuality;
    protected ImageButton buttonRealTime, buttonHistory, buttonProfile;
    protected List<String> categories = new ArrayList<>();
    protected Thread thread;
    protected Spinner spinner;
    protected BluetoothService btService;
    protected GPSService gpsService;
    protected SharedPreferences sharedPreferences;
    protected DBHelper dbHelper = new DBHelper(this);

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    LocationManager locationManager;
    String provider;

    private float co2;
    private float tvoc;
    private float mq2;
    private float humidity;
    private float pressure;
    private float altitude;
    private float temperature;
    private float overallQualityScore;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiInit();
        dropDownInit();
        locationServicesInit();
        setTitle("Live Air Quality");
        btService = new BluetoothService();

        Intent btServiceIntent = new Intent(getApplicationContext(), BluetoothService.class);
        startService(btServiceIntent);

        gpsService = new GPSService();
        Intent gpsServiceIntent = new Intent(getApplicationContext(),GPSService.class);
        startService(gpsServiceIntent);

        if(btService.btInit())
        {
            longToast("Successfully connected to the SensAir Device!");
            startBluetoothThreading();
        }
        else
        {
            longToast("Failed to connect to the SensAir Device. Please check Bluetooth Settings and try again.");
        }

        Date date = Calendar.getInstance().getTime();
        AirData sampleAirData = new AirData(3,453,34,150,47, (float) 101.32,18,date,(double) 34.3160f,(double) 115.1597f);
        dbHelper.insertAirData(sampleAirData);
    }

    public void uiInit()
    {
        gaugeAirQuality = findViewById(R.id.gaugeAirQuality);
        gaugeAirQuality.setWithTremble(true);

        buttonRealTime = findViewById(R.id.buttonRealTime);
        buttonHistory = findViewById(R.id.buttonHistory);
        buttonProfile = findViewById(R.id.buttonProfile);

        buttonRealTime.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View V)
            {
                Intent intent = new Intent(MainActivity.this, RealTimeDataActivity.class);
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
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

    }

    public void dropDownInit()
    {
        spinner = findViewById(R.id.spinner);
        assert spinner != null;
        spinner.setOnItemSelectedListener(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String defaultMetric = sharedPreferences.getString("meter","0");

        categories.add("Overall Air Quality");
        categories.add("Smoke Index");
        categories.add("Carbon Dioxide");
        categories.add("Volatile Organic Compounds");
        categories.add("Humidity");
        categories.add("Pressure");
        categories.add("Temperature");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(Integer.parseInt(defaultMetric));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void locationServicesInit()
    {
        checkLocationPermission();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        if(provider==null)
        {
            Toast.makeText(this, "Location Services Disabled. Visit Settings to Enable them.", Toast.LENGTH_LONG).show();

        }
        else
        {
            Location location = locationManager.getLastKnownLocation(provider);

            if (location != null)
            {

                Log.i("Location Info", "Location achieved!");

            } else
            {

                Log.i("Location Info", "No location :(");

            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            return false;
        }
         else
            return true;
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
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION)
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

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLocationChanged(Location location) {

        Double lat = location.getLatitude();
        Double lng = location.getLongitude();

        Log.i("Location info: Lat", lat.toString());
        Log.i("Location info: Lng", lng.toString());

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
                        co2= btService.getCo2();
                        tvoc = btService.getTvoc();
                        mq2 = btService.getMq2();
                        humidity = btService.getHumidity();
                        pressure = btService.getPressure();
                        altitude = btService.getAltitude();
                        temperature = btService.getTemperature();
                        overallQualityScore = btService.getOverallQuality();

                        switch (spinner.getSelectedItemPosition())
                        {
                            case 0:     // overall quality
                                if(overallQualityScore==3)
                                {
                                    gaugeAirQuality.speedTo(2.5f);
                                    gaugeAirQuality.setUnit("Excellent Air Quality Index!");
                                }
                                else if(overallQualityScore==2)
                                {
                                    gaugeAirQuality.speedTo(1.5f);
                                    gaugeAirQuality.setUnit("Moderate Air Quality Index");
                                }
                                else if(overallQualityScore==1)
                                {
                                    gaugeAirQuality.speedTo(0.5f);
                                    gaugeAirQuality.setUnit("Garbage Air Quality Index");   // Big d approved
                                }
                                break;
                            case 1:     // CO
                                gaugeAirQuality.speedTo(mq2);
                                break;
                            case 2:     // CO
                                gaugeAirQuality.speedTo(co2);
                                break;
                            case 3:     // TVOC
                                gaugeAirQuality.speedTo(tvoc);
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
    protected void onStart()
    {
        super.onStart();
        if(thread!=null&&!thread.isAlive())
        {
            thread.start();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if(thread!=null)
        {
            thread.interrupt();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        btService.disconnect();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        setGaugeMetric(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {

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

    public void setGaugeMetric(int position)
    {
        Section s1,s2,s3,s4,s5;
        List<Section> sections = new ArrayList<>();
        ArrayList<Float> ticks = new ArrayList<>();


        switch (position)           // handles different gauge selections
        {
            case 0:     // overall quality
                gaugeAirQuality.speedTo(0);                     // reset gauge

                gaugeAirQuality.setMinMaxSpeed(0,3);          // rescale gauge for each metric

                s1 = new Section(0f,.33333f,Color.parseColor("#EE5C42"),110);       // create according sections
                s2 = new Section(.33333f,.66666f,Color.parseColor("#FFFF33"),110);
                s3 = new Section(.66666f,1f,Color.parseColor("#00CD66"),110);
                sections.add(s1);
                sections.add(s2);
                sections.add(s3);
                gaugeAirQuality.clearSections();
                gaugeAirQuality.addSections(sections);


                gaugeAirQuality.setSpeedTextColor(Color.TRANSPARENT);

                gaugeAirQuality.setTickNumber(0);
                gaugeAirQuality.setMarksNumber(0);      // set labels

                gaugeAirQuality.speedTo(3);

                break;
            case 1:     // CO
                gaugeAirQuality.speedTo(0);
                gaugeAirQuality.setMinMaxSpeed(0,800);
                gaugeAirQuality.setUnit("");

                s1 = new Section(0f,.5f,Color.parseColor("#00CD66"),110);
                s2 = new Section(.5f,1f,Color.parseColor("#EE5C42"),110);
                sections.add(s1);
                sections.add(s2);
                gaugeAirQuality.clearSections();
                gaugeAirQuality.addSections(sections);

                gaugeAirQuality.setSpeedTextColor(Color.BLACK);

                gaugeAirQuality.setMarksNumber(3);
                ticks.add(0.25f);
                ticks.add(0.5f);
                ticks.add(0.75f);


                gaugeAirQuality.setTicks(ticks);

                gaugeAirQuality.speedTo(mq2);
                break;

            case 2:     // co

                gaugeAirQuality.speedTo(770);
                gaugeAirQuality.setMinMaxSpeed(0,2000);
                gaugeAirQuality.setUnit("Parts-per Million (ppm)");

                s1 = new Section(0f,.4f,Color.parseColor("#00CD66"),110);
                s2 = new Section(.4f,.8f,Color.parseColor("#FFFF33"),110);
                s3 = new Section(.8f,1f,Color.parseColor("#EE5C42"),110);
                sections.add(s1);
                sections.add(s2);
                sections.add(s3);
                gaugeAirQuality.clearSections();
                gaugeAirQuality.addSections(sections);

                gaugeAirQuality.setMarksNumber(9);
                ticks.add(0.2f);
                ticks.add(0.4f);
                ticks.add(0.6f);
                ticks.add(0.8f);
                gaugeAirQuality.setTicks(ticks);

//                gaugeAirQuality.speedTo(mq2);
                break;


            case 3:     // TVOC


                gaugeAirQuality.setMinMaxSpeed(0,4000);
                gaugeAirQuality.setUnit("Parts-per Million (ppm)");

                s1 = new Section(0f,.1f,Color.parseColor("#00CD66"),110);
                s2 = new Section(.1f,.5f,Color.parseColor("#FFFF33"),110);
                s3 = new Section(.5f,1f,Color.parseColor("#EE5C42"),110);
                sections.add(s1);
                sections.add(s2);
                sections.add(s3);
                gaugeAirQuality.clearSections();
                gaugeAirQuality.addSections(sections);

                gaugeAirQuality.setMarksNumber(9);
                ticks.add(0.1f);
                ticks.add(0.3f);
                ticks.add(0.5f);
                ticks.add(0.7f);
                ticks.add(0.9f);
                gaugeAirQuality.setTicks(ticks);

                gaugeAirQuality.speedTo(300);
//                gaugeAirQuality.speedTo(tvoc);
                break;
            case 4:     // Humidity
                gaugeAirQuality.speedTo(0);
                gaugeAirQuality.setMinMaxSpeed(0,100);
                gaugeAirQuality.setUnit("Percent Humidity (%)");

                s1 = new Section(0f,.2f,Color.parseColor("#BFEFFF"),110);
                s2 = new Section(.2f,.4f,Color.parseColor("#B0E2FF"),110);
                s3 = new Section(.4f,.6f,Color.parseColor("#7EC0EE"),110);
                s4 = new Section(.6f,.8f,Color.parseColor("#499DF5"),110);
                s5 = new Section(.8f,1f,Color.parseColor("#0276FD"),110);
                sections.add(s1);
                sections.add(s2);
                sections.add(s3);
                sections.add(s4);
                sections.add(s5);
                gaugeAirQuality.clearSections();
                gaugeAirQuality.addSections(sections);

                gaugeAirQuality.setSpeedTextColor(Color.BLACK);

                gaugeAirQuality.setMarksNumber(9);
                ticks.add(0.1f);
                ticks.add(0.2f);
                ticks.add(0.3f);
                ticks.add(0.4f);
                ticks.add(0.5f);
                ticks.add(0.6f);
                ticks.add(0.7f);
                ticks.add(0.8f);
                ticks.add(0.9f);
                gaugeAirQuality.setTicks(ticks);

                gaugeAirQuality.speedTo(humidity);
                break;
            case 5:     // Pressure
                gaugeAirQuality.speedTo(0);
                gaugeAirQuality.setMinMaxSpeed(0,300);
                gaugeAirQuality.setUnit("Kilopascals (kPa)");

                s1 = new Section(0f,.021f,Color.parseColor("#EE5C42"),110);
                s2 = new Section(.021f,.83333f,Color.parseColor("#00CD66"),110);
                s3 = new Section(.83333f,1f,Color.parseColor("#EE5C42"),110);
                sections.add(s1);
                sections.add(s2);
                sections.add(s3);
                gaugeAirQuality.clearSections();
                gaugeAirQuality.addSections(sections);

                gaugeAirQuality.setSpeedTextColor(Color.BLACK);

                gaugeAirQuality.setMarksNumber(5);
                ticks.add(1/6f);
                ticks.add(2/6f);
                ticks.add(3/6f);
                ticks.add(4/6f);
                ticks.add(5/6f);

                gaugeAirQuality.setTicks(ticks);

                gaugeAirQuality.speedTo(pressure/1000);
                break;
            case 6:     // Temperature
                gaugeAirQuality.speedTo(0);
                gaugeAirQuality.setMinMaxSpeed(-40,40);
                gaugeAirQuality.setUnit(" Celsius (C)");

                s1 = new Section(0f,.5f,Color.parseColor("#74BBFB"),110);
                s2 = new Section(.5f,1f,Color.parseColor("#ff6666"),110);
                sections.add(s1);
                sections.add(s2);
                gaugeAirQuality.clearSections();
                gaugeAirQuality.addSections(sections);

                gaugeAirQuality.setSpeedTextColor(Color.BLACK);

                gaugeAirQuality.setMarksNumber(9);
                ticks.add(0.1f);
                ticks.add(0.2f);
                ticks.add(0.3f);
                ticks.add(0.4f);
                ticks.add(0.5f);
                ticks.add(0.6f);
                ticks.add(0.7f);
                ticks.add(0.8f);
                ticks.add(0.9f);
                gaugeAirQuality.setTicks(ticks);

                gaugeAirQuality.speedTo(temperature);
                break;
        }
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

        setGauge();
    }

    public void setGauge()
    {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String defaultMetric = sharedPreferences.getString("meter","0");

        spinner.setSelection(Integer.parseInt(defaultMetric));
    }


}