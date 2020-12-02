package com.example.sensair.history;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sensair.BluetoothService;
import com.example.sensair.R;
import com.example.sensair.realtimeplotting.LoggedDataActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import kotlin.collections.ArrayDeque;

public class HistoryDetailActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "HistoryDetailActivity";
    private DBHelper airData;
    private BluetoothService btService = new BluetoothService();
    protected ArrayList<AirData> airDataList;
    protected ArrayList<AirData> dailyData;
    ArrayList<AirData> hourlyAverages;
    protected HistoryDetailAdapter hourlyDataAdapter;
    ArrayList<LatLng> latLngs;
    protected ListView listView;
    protected Button delete,log;
    protected String key,date;
    protected int day_toread;
    public ArrayList<AirData> airHourlyList = new ArrayList<AirData>();
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    protected LinearLayoutManager daylayoutManager;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.history_activity_detail);

        Intent intent = getIntent();
        date = intent.getStringExtra("date");

        uiInit();

        mMapView = (MapView) findViewById(R.id.mapView_hour);
        initGoogleMap(savedInstance);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void uiInit()
    {
        setTitle("Hourly Averages");

        airData = new DBHelper(this);
        airDataList = airData.getAllData();

        dailyData = new ArrayList<>();
        hourlyAverages = new ArrayList<>();
        latLngs = new ArrayList<>();

        for(AirData airData : airDataList)
        {
            System.out.println("AIR DATA HOUR IN ENTIRE LIST: "+airData.getDATE() + airData.getHOUR());
            if(airData.getDATE().equals(date))
            {
                dailyData.add(airData);
            }
        }

        String tempDate = null;
        String hour = dailyData.get(0).getHOUR();

        ArrayList<String> hours = new ArrayList<>();
        hours.add(hour);
        for(AirData datum : dailyData)
        {
            if(!datum.getHOUR().equals(hour))
            {
                hour = datum.getHOUR();
                hours.add(hour);
            }
        }



        for(String h : hours)
        {
            float co2Avg = 0,vocAvg = 0,smokeAvg = 0,humidityAvg = 0,pressureAvg = 0,tempAvg = 0;
            float count = 0;
            double longitude = 0,latitude =0;
            for (AirData airdata : dailyData)
            {
                if(airdata.getHOUR().equals(h))
                {
                    co2Avg += Float.parseFloat(airdata.getCO2());
                    vocAvg += Float.parseFloat(airdata.getTVOC());
                    smokeAvg += Float.parseFloat(airdata.getGAS());
                    humidityAvg += Float.parseFloat(airdata.getHUMIDITY());
                    pressureAvg += Float.parseFloat(airdata.getPRESSURE());
                    tempAvg += Float.parseFloat(airdata.getTEMP());
                    tempDate = airdata.getDATE();

                    System.out.println("CO2 RUNNING AVERAGE: "+co2Avg);
                    longitude = airdata.getLongitude();
                    latitude = airdata.getLattidude();



                    count++;
                }
            }
            co2Avg /= count;
            vocAvg /= count;
            smokeAvg /= count;
            humidityAvg /= count;
            pressureAvg /= count;
            tempAvg /= count;

            System.out.println("CO2 AVERAGE FINAL CALCULATION: " +co2Avg + " for HOUR: "+h);

            AirData temp = new AirData(tempDate, h, "3", String.format("%.0f", co2Avg), String.format("%.0f", vocAvg),
                    String.format("%.0f", smokeAvg), String.format("%.0f", humidityAvg), String.format("%.0f", pressureAvg / 1000),
                    String.format("%.0f", tempAvg), latitude, longitude);

            latLngs.add(new LatLng(latitude,longitude));
            hourlyAverages.add(temp);
        }


        hourlyDataAdapter = new HistoryDetailAdapter(this,hourlyAverages);
        listView = findViewById(R.id.hourList);
        assert listView != null;
        listView.setAdapter(hourlyDataAdapter);

        log = findViewById(R.id.logActivityButton);
        log.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(HistoryDetailActivity.this, LoggedDataActivity.class);
                intent.putExtra("date",date);
                startActivity(intent);
            }
        });
    }
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }
    private void initGoogleMap(Bundle savedInstanceState){
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
    }
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled(true);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
        float lat = (float)(btService.getLatitude());
        float lon = (float)(btService.getLongitude());
        final float zoomLevel = 18f; //This goes up to 21
        final LatLng latLng = new LatLng(lat,lon);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));


        Log.i("Airbase Debug","size " + airDataList.size());
        for(int i = 0; i<latLngs.size();i++)
        {
            AirData data = dailyData.get(i);
            int overall = Integer.parseInt(data.getOVERALL());
            if(overall==3)
                map.addMarker((new MarkerOptions().position(latLngs.get(i)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));
            else if(overall==2)
                map.addMarker((new MarkerOptions().position(latLngs.get(i)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))));
            else if(overall==1)
                map.addMarker((new MarkerOptions().position(latLngs.get(i)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))));
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                LatLng LL = latLngs.get(position);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LL, zoomLevel));
            }
        });
    }
    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    // onOptionsItemSelected definition. Listens for menu item clicks
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
//    protected void hourListSeparator(List<AirData> airSets){
//        int hour_hold = 0;
//        Log.i("DayTR", "enter hour separator");
//
//
//        for(int i = 0; i < airSets.size() ; i++){
//            Log.i("DayTR", "enter hour separator");
//            if(airSets.get(i).getDay() == day_toread ) {
//                Log.i("DayTR", "found matching day");
//                if (hour_hold != airSets.get(i).getHour()) {
//                    Log.i("DayTR", "adding to hour list");
//                    hour_hold = airSets.get(i).getHour();
//                    airHourlyList.add(airSets.get(i));
//                }
//            }
//        }
//    }

}
