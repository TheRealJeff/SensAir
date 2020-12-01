package com.example.sensair;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import kotlin.collections.ArrayDeque;

public class HistoryDetailActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "HistoryDetailActivity";
    private DBHelper airData;
    protected List<AirData> airDataList;
    protected Button delete;
    protected String key;
    protected int day_toread;
    public ArrayList<AirData> airHourlyList = new ArrayList<AirData>();
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    protected LinearLayoutManager daylayoutManager;
    LocationManager locationManager;

    int good_threshold = 50;
    int medium_threhold = 25;
    int bad_threshold = 10;



    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.history_activity_detail);

        Log.i("DayTR", "enter detail activity");

        airData = new DBHelper(this);
        airDataList = airData.getAllData();
        Log.i("DayTR", "about to separte list");
        Bundle extras = getIntent().getExtras();
        day_toread = Integer.valueOf(extras.getString("Day_Sampler"));
        Log.i("DayTR", "logging values for " + day_toread);
        hourListSeparator(airDataList);

        mMapView = (MapView) findViewById(R.id.mapView_hour);
        initGoogleMap(savedInstance);

        final RecyclerView dayrecyclerView = findViewById(R.id.daily_list);
        assert dayrecyclerView != null;
        DailyAdapter dayAdapter= new DailyAdapter(airHourlyList);
        daylayoutManager = new LinearLayoutManager(this);
        dayrecyclerView.setLayoutManager(daylayoutManager);
        dayrecyclerView.setAdapter(dayAdapter);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        delete = (Button) findViewById(R.id.Delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                airData.deleteItem(key);
                Toast.makeText(getBaseContext(),"Deleted", Toast.LENGTH_LONG).show();
            }
        });


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
    public void onMapReady(GoogleMap map) {
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
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15.0f));


        Log.i("Airbase Debug","size " + airDataList.size());
        for(int i = 0; i < airHourlyList.size() ; i++ ){
            Log.i("Marker", "entering loop");
            Double lattitude = Double.valueOf(airDataList.get(i).getLattidude());
            Double longittude = Double.valueOf(airDataList.get(i).getLongitude());
            Log.i("Marker", lattitude + " " +longittude);
           // int Overall = Integer.valueOf(airHourlyList.get(i).getOVERALL());
           /* if(Overall >= good_threshold){
                map.addMarker((new MarkerOptions().position(new LatLng(lattitude, longittude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));

            }
            if(Overall >= medium_threhold && Overall < good_threshold){
                map.addMarker((new MarkerOptions().position(new LatLng(lattitude, longittude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))));

            }
            if(Overall <= bad_threshold){

            } */
            map.addMarker((new MarkerOptions().position(new LatLng(lattitude, longittude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))));
        }
        Log.i("Airbase Debug", "Exit loop");
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
    protected void hourListSeparator(List<AirData> airSets){
        int hour_hold = 0;
        Log.i("DayTR", "enter hour separator");


        for(int i = 0; i < airSets.size() ; i++){
            Log.i("DayTR", "enter hour separator");
            if(airSets.get(i).getDay() == day_toread ) {
                Log.i("DayTR", "found matching day");
                if (hour_hold != airSets.get(i).getHour()) {
                    Log.i("DayTR", "adding to hour list");
                    hour_hold = airSets.get(i).getHour();
                    airHourlyList.add(airSets.get(i));
                }
            }
        }
    }

}
