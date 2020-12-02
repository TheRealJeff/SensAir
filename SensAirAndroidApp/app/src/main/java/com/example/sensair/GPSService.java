package com.example.sensair;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;


public class GPSService extends Service
{

    private static final String TAG = "GPS SERVICE";
    private static LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private static Location initialLocation;
    private static double longitude, latitude;

    private static class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            mLastLocation = new Location(provider);
            if (mLastLocation != null)
            {
                longitude = mLastLocation.getLongitude();
                latitude = mLastLocation.getLatitude();
            }
        }

        @Override
        public void onLocationChanged(Location location)
        {
            this.mLastLocation = location;
            if (mLastLocation != null)
            {
                longitude = mLastLocation.getLongitude();
                latitude = mLastLocation.getLatitude();
            }
        }

        @Override
        public void onProviderDisabled(String provider)
        {
        }

        @Override
        public void onProviderEnabled(String provider)
        {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        initializeLocationManager();
        try
        {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex)
        {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex)
        {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try
        {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex)
        {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex)
        {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        initialLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        longitude = initialLocation.getLongitude();
        latitude = initialLocation.getLatitude();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public static double getLongitude()
    {
        return longitude;
    }

    public static double getLatitude()
    {
        return latitude;
    }


}

//public class GPSService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener
//{
//    private LocationRequest mLocationRequest;
//    private GoogleApiClient mGoogleApiClient;
//    private static final String LOGSERVICE = "GPS SERVICE";
//    private static double latitude,longitude;
//
//    @Override
//    public void onCreate()
//    {
//        buildGoogleApiClient();
//        System.out.println("ONCREATEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId)
//    {
//        System.out.println("ON START COMMANDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
//        if (!mGoogleApiClient.isConnected())
//            mGoogleApiClient.connect();
//        return START_STICKY;
//    }
//
//
//    @Override
//    public void onConnected(Bundle bundle)
//    {
//        Log.i(LOGSERVICE, "onConnected" + bundle);
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//        {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if (l != null) {
//            Log.i(LOGSERVICE, "lat " + l.getLatitude());
//            Log.i(LOGSERVICE, "lng " + l.getLongitude());
//            latitude = l.getLatitude();
//            longitude = l.getLatitude();
//        }
//
//        startLocationUpdate();
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        Log.i(LOGSERVICE, "onConnectionSuspended " + i);
//
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
////        Log.i(LOGSERVICE, "lat " + location.getLatitude());
////        Log.i(LOGSERVICE, "lng " + location.getLongitude());
//
//        latitude = location.getLatitude();
//        longitude = location.getLatitude();
//        LatLng mLocation = (new LatLng(location.getLatitude(), location.getLongitude()));
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Log.i(LOGSERVICE, "onDestroy - Estou sendo destruido ");
//
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//        Log.i(LOGSERVICE, "onConnectionFailed ");
//
//    }
//
//    private void initLocationRequest() {
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(5000);
//        mLocationRequest.setFastestInterval(2000);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//    }
//
//    private void startLocationUpdate() {
//        initLocationRequest();
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//    }
//
//    private void stopLocationUpdate() {
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//
//    }
//
//    protected synchronized void buildGoogleApiClient() {
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addOnConnectionFailedListener(this)
//                .addConnectionCallbacks(this)
//                .addApi(LocationServices.API)
//                .build();
//    }
//
//    public double getLatitude()
//    {
//        return latitude;
//    }
//
//    public double getLongitude()
//    {
//        return longitude;
//    }
//}
