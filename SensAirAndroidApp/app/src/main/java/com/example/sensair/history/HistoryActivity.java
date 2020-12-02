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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sensair.BluetoothService;
import com.example.sensair.GPSService;
import com.example.sensair.history.HistoryAdapter;
import com.example.sensair.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HistoryActivity  extends AppCompatActivity implements OnMapReadyCallback {

    // Declare Data base helper

    private DBHelper AirBase = new DBHelper(this);
    ListView listView;
    HistoryAdapter dayDataAdapter;
    ArrayList<String> days;
    ArrayList<AirData> airDataList;
    ArrayList<LatLng> latLngs;
    ArrayList<Integer> overallAverages;
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    protected LinearLayoutManager layoutManager;
    protected GPSService gpsService;
    protected BluetoothService btService = new BluetoothService();

    LocationManager locationManager;

    @Override
    protected void onCreate( Bundle savedInstances){

        super.onCreate(savedInstances);

        setContentView(R.layout.activity_historyactivity_list);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        uiInit();

        mMapView = (MapView) findViewById(R.id.mapView_day);
        initGoogleMap(savedInstances);
    }

    public void uiInit()
    {
        airDataList = AirBase.getAllData();

        days = new ArrayList<>();
        latLngs = new ArrayList<>();
        overallAverages = new ArrayList<>();

        String temp = airDataList.get(0).getDATE();
        days.add(temp);
        latLngs.add(new LatLng(airDataList.get(0).getLattidude(),airDataList.get(0).getLongitude()));
        overallAverages.add(Integer.parseInt(airDataList.get(0).getOVERALL()));

        for(AirData airData : airDataList)
        {
            System.out.println("AIR DATA HOUR IN ENTIRE LIST: "+airData.getDATE() + airData.getHOUR());
            if(!airData.getDate().equals(temp))
            {
                days.add(airData.getDate());
                latLngs.add(new LatLng(airData.getLattidude(),airData.getLongitude()));
            }
        }

        for(String day : days)
        {
            int overallSum = 0;
            int count = 0;
            for(AirData airdata : airDataList)
            {
                if(airdata.getDate().equals(day))
                {
                    overallSum+=Integer.parseInt(airdata.getOVERALL());
                    count++;
                }
            }
            int overallQualityAverage = overallSum/count;
            overallAverages.add(overallQualityAverage);
        }

        System.out.println("OVERALL AVERAGES: "+overallAverages);

        dayDataAdapter = new HistoryAdapter(this,days);

        listView = findViewById(R.id.dayList);
        assert listView != null;
        listView.setAdapter(dayDataAdapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                String date = (String) parent.getItemAtPosition(position);
                Intent intent = new Intent(HistoryActivity.this,HistoryDetailActivity.class);
                intent.putExtra("date",date);
                startActivity(intent);
                return false;
            }
        });
    }

    public void deleteDay(String date)
    {
        for(AirData airData : airDataList)
        {
        // TODO allow deletion of days
        }
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
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
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
        float lat = (float)(btService.getLatitude());
        float lon = (float)(btService.getLongitude());
        System.out.println("LAT AND LONGITUDE FROM HISTORY ACTIVITY BEFORE ZOOMING: ");
        final float zoomLevel = 16.0f; //This goes up to 21
        final LatLng latLng = new LatLng(lat,lon);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));


        for(int i = 0; i<latLngs.size();i++)
        {
            int overall = overallAverages.get(i);
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
