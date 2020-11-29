package com.example.sensair;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity  extends AppCompatActivity implements OnMapReadyCallback {

    // Declare Data base helper

    private DBHelper AirBase = new DBHelper(this);
    public List<AirData> airDailyList = new ArrayList<AirData>();
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    protected LinearLayoutManager layoutManager;

    LocationManager locationManager;

    int good_threshold = 50;
    int medium_threhold = 25;
    int bad_threshold = 10;

    @Override
    protected void onCreate( Bundle savedInstances){

        super.onCreate(savedInstances);

        setContentView(R.layout.activity_historyactivity_list);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        final RecyclerView recyclerView = findViewById(R.id.historyactivity_list);
        assert recyclerView != null;
        final List<AirData> airDataList = AirBase.getAllData();
        Log.i("List", "OG List size" + AirBase.getAllData().size());

        dayListSeparator(airDataList);
        Log.i("List", "List Separator Called");

        mMapView = (MapView) findViewById(R.id.mapView_day);
        initGoogleMap(savedInstances);

        HistoryAdapter airAdapter= new HistoryAdapter(airDailyList);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(airAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

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

        //Log.i("Airbase Debug","size " + airDailyList.size());
        for(int i = 0; i < airDailyList.size() ; i++ ){
            Log.i("Marker", "entering loop");
            Double lattitude = Double.valueOf(airDailyList.get(i).getLattidude());
            Double longittude = Double.valueOf(airDailyList.get(i).getLongitude());
            Log.i("Marker", lattitude + " " + longittude);
            //int Overall = Integer.valueOf(airDailyList.get(i).getOVERALL());
           /* if(Overall >= good_threshold){
                map.addMarker((new MarkerOptions().position(new LatLng(lattitude, longittude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));

            }
            if(Overall >= medium_threhold && Overall < good_threshold){
                map.addMarker((new MarkerOptions().position(new LatLng(lattitude, longittude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))));

            }
            if(Overall <= bad_threshold){

            } */
            map.addMarker((new MarkerOptions().position(new LatLng(lattitude, longittude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))));
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    protected void dayListSeparator(List<AirData> airSets){
        int day_hold = 0;
        for(int i = 0; i < airSets.size() ; i++){
            Log.i("Daily Seprator", "Day" + airSets.get(i).getDay());
            Log.i("Daily Seprator", "day_hold" + day_hold);
            if(day_hold != airSets.get(i).getDay()){
                day_hold = airSets.get(i).getDay();
                Log.i("separator" , "Separator Size =" + airSets.get(i).toString());
                airDailyList.add(airSets.get(i));
            }
        }
    }

}
