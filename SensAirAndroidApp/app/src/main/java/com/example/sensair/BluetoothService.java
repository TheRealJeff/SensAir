package com.example.sensair;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.example.sensair.realtimeplotting.AltitudeDataActivity;
import com.example.sensair.realtimeplotting.CarbonDioxideDataActivity;
import com.example.sensair.realtimeplotting.HumidityDataActivity;
import com.example.sensair.realtimeplotting.PressureDataActivity;
import com.example.sensair.realtimeplotting.SmokeIndexDataActivity;
import com.example.sensair.realtimeplotting.TemperatureDataActivity;
import com.example.sensair.realtimeplotting.VolatileOrganicCompoundsActivity;

import com.example.sensair.history.AirData;
import com.example.sensair.history.DBHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import eu.basicairdata.bluetoothhelper.BluetoothHelper;

public class BluetoothService extends Service
{
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    protected BluetoothHelper mBluetooth = new BluetoothHelper();
    protected BluetoothDevice mBluetoothDevice;
    protected static boolean supportsBluetooth;
    protected Thread thread;
    BluetoothAdapter btAdapter;
    protected String TAG = "BTService";
    GPSService gpsService;
    protected Location location;
    protected AirData airData = null;
    protected DBHelper dbHelper;
    protected boolean firstRun = true;


    private static float co2;
    private static float tvoc;
    private static float mq2;
    private static float humidity;
    private static float pressure;
    private static float altitude;
    private static float temperature;
    private static int overallAirQuality;
    private static double longitude, latitude;

    protected SharedPreferences preferences;
    protected boolean altitudeState;
    protected boolean pressureState;
    protected boolean temperatureState;
    protected boolean humidityState;
    protected boolean criticalAlertsState;
    protected String altitudeMaxThreshold;
    protected String altitudeMinThreshold;
    protected String pressureMaxThreshold;
    protected String pressureMinThreshold;
    protected String temperatureMaxThreshold;
    protected String temperatureMinThreshold;
    protected String humidityMaxThreshold;
    protected String humidityMinThreshold;

    private static final int co2NotificationIDDefault = 1;
    private static final int mq2NotificationIDDefault = 2;
    private static final int tvocNotificationIDDefault = 3;
    private static final int altitudeNotificationIDMax = 4;
    private static final int temperatureNotificationIDMax = 5;
    private static final int pressureNotificationIDMax = 6;
    private static final int co2NotificationIDDanger = 7;
    private static final int mq2NotificationIDDanger = 8;
    private static final int tvocNotificationIDDanger = 9;
    private static final int humidityNotificationIDMax = 10;
    private static final int altitudeNotificationIDMin = 11;
    private static final int temperatureNotificationIDMin = 12;
    private static final int pressureNotificationIDMin = 13;
    private static final int humidityNotificationIDMin = 14;


    @Override
    public void onCreate()
    {
        supportsBluetooth = btInit();
        if (supportsBluetooth)
            connect();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_cloud_queue_black_18dp)
                .setContentIntent(pendingIntent)
                .build();

        dbHelper = new DBHelper(getApplicationContext());

        if(supportsBluetooth)
            startBluetoothThreading();

        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void getData()
    {
        mBluetooth.SendMessage("1");
    }

    public boolean btInit()
    {
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if(btAdapter==null)
            return false;

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        for(BluetoothDevice device : pairedDevices)
        {
            if (device.getName().equals("SensAir"))
            {
                mBluetoothDevice = device;
                return true;
            }
        }
        return false;
    }

    public void startBluetoothThreading()
    {
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
                    if(mBluetooth!=null)
                        getData();
                }
            }
        };
        thread.start();
    }

    public void connect()
    {
        mBluetooth.Connect(mBluetoothDevice);
        mBluetooth.setBluetoothHelperListener(new BluetoothHelper.BluetoothHelperListener() {
            @Override
            public void onBluetoothHelperMessageReceived(BluetoothHelper bluetoothhelper, final String message)
            {
                String[] data = message.split(",");
                if (data.length == 7)
                {
                    co2 = Float.parseFloat(data[0].substring(0, data[0].length() - 1));
                    tvoc = Float.parseFloat(data[1].substring(0, data[1].length() - 1));
                    mq2 = Float.parseFloat(data[2].substring(0, data[2].length() - 1));
                    humidity = Float.parseFloat(data[3].substring(0, data[3].length() - 1));
                    pressure = Float.parseFloat(data[4].substring(0, data[4].length() - 1));
                    altitude = Float.parseFloat(data[5].substring(0, data[5].length() - 1));
                    temperature = Float.parseFloat(data[6].substring(0, data[6].length() - 1));

                    overallAirQuality = 0;

                    if(co2<=1000)
                        overallAirQuality++;
                    if(tvoc<=400)
                        overallAirQuality++;
                    if(mq2<=400)
                        overallAirQuality++;

                    longitude = gpsService.getLongitude();
                    latitude = gpsService.getLatitude();

                    Date date = Calendar.getInstance().getTime();

                    AirData temp = new AirData(overallAirQuality,co2,tvoc,mq2,humidity,pressure,temperature,date,longitude,latitude);

                    if(airData==null&&longitude!=0&&latitude!=0)
                    {
                        airData = temp;
                        dbHelper.insertAirData(airData);
                    }
                    else if (Integer.parseInt(temp.getMinutes()) - Integer.parseInt(airData.getMinutes())  >= 1)
                    {
                        airData = temp;
                        dbHelper.insertAirData(airData);
                    }
                    valueCheck(co2, tvoc, mq2, humidity, pressure, altitude, temperature);
                }
            }

            @Override
            public void onBluetoothHelperConnectionStateChanged(BluetoothHelper bluetoothhelper, boolean isConnected) {
                if (!isConnected)
                {
                    mBluetooth.Connect(mBluetoothDevice);
                }
            }
        });
    }

    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "SensAir Notification Center",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public void valueCheck(float co2, float tvoc, float mq2, float humidity, float pressure, float altitude, float temperature){

        Log.d(TAG, "checking values");

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        altitudeState = preferences.getBoolean("SettingAltitudeState", false);
        pressureState = preferences.getBoolean("SettingPressureState", false);
        temperatureState = preferences.getBoolean("SettingTemperatureState", false);
        humidityState = preferences.getBoolean("SettingHumidityState", false);
        criticalAlertsState = preferences.getBoolean("AirQualityAlerts", false);
        System.out.println(criticalAlertsState);

        if(altitudeState == true) {
            altitudeMaxThreshold = preferences.getString("SettingMaximumAltitudeThreshold", null);
            altitudeMinThreshold = preferences.getString("SettingMinimumAltitudeThreshold", null);
        }
        if(pressureState == true){
            pressureMaxThreshold = preferences.getString("SettingMaximumPressureThreshold", null);
            pressureMinThreshold = preferences.getString("SettingMinimumPressureThreshold", null);
        }
        if(temperatureState == true){
            temperatureMaxThreshold = preferences.getString("SettingMaximumTemperatureThreshold", null);
            temperatureMinThreshold = preferences.getString("SettingMinimumTemperatureThreshold", null);
        }
        if(humidityState == true){
            humidityMaxThreshold = preferences.getString("SettingMaximumHumidityThreshold", null);
            humidityMinThreshold = preferences.getString("SettingMinimumHumidityThreshold", null);
        }
        //TODO makes custom messages with expandable notifications
        if (criticalAlertsState == true) {

            if (co2 >= 1000 && co2 < 2000) {
                Intent intent = new Intent(this, CarbonDioxideDataActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                createNotificationChannel();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_warning)
                        .setContentTitle("Warning! CO2 Levels are Significant")
                        .setContentText("The CO2 levels in your surrounding environment seem to be higher than acceptable levels, please try to protect yourself and stay away from the source of CO2")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("The CO2 levels in your surrounding environment seem to be higher than acceptable levels, please try to protect yourself and stay away from the source of CO2"))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(co2NotificationIDDefault, builder.build());
            } else if (co2 >= 2000) {
                Intent intent = new Intent(this, CarbonDioxideDataActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                createNotificationChannel();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_danger)
                        .setContentTitle("DANGER! CO2 Levels are High")
                        .setContentText("The CO2 levels in your surrounding environment seem to be higher than acceptable levels, please try to protect yourself and stay away from the source of CO2")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("The CO2 levels in your surrounding environment seem to be higher than acceptable levels, please try to protect yourself and stay away from the source of CO2"))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(co2NotificationIDDanger, builder.build());
            }

            if (tvoc >= 400 && tvoc < 2000) {
                Intent intent = new Intent(this, VolatileOrganicCompoundsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                createNotificationChannel();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_warning)
                        .setContentTitle("Warning! TVOC Levels are Significant")
                        .setContentText("TVOC levels in your surrounding environment seem to be higher than acceptable levels, please try to protect yourself and stay away from the source of TVOC")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("TVOC levels in your surrounding environment seem to be higher than acceptable levels, please try to protect yourself and stay away from the source of TVOC"))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(tvocNotificationIDDefault, builder.build());
            } else if (tvoc >= 2000) {
                Intent intent = new Intent(this, VolatileOrganicCompoundsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                createNotificationChannel();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_danger)
                        .setContentTitle("DANGER! TVOC Levels are High")
                        .setContentText("TVOC levels in your surrounding environment are significantly higher than the safety threshold, please move away from the source as soon as possible and contact emergency services if necessary")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("TVOC levels in your surrounding environment are significantly higher than the safety threshold, please move away from the source as soon as possible and contact emergency services if necessary"))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(tvocNotificationIDDanger, builder.build());
            }

            if (mq2 >= 400) {
                Intent intent = new Intent(this, SmokeIndexDataActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                createNotificationChannel();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_warning)
                        .setContentTitle("Warning! Smoke Levels are Significant")
                        .setContentText("Smoke levels are significant, there is a danger of smoke inhalation, please detect the source of smoke and contact emergency services in necessary")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Smoke levels are significant, there is a danger of smoke inhalation, please detect the source of smoke and contact emergency services in necessary"))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(mq2NotificationIDDefault, builder.build());
            }
        }

        if(pressureState == true && pressureMaxThreshold != null) {
            if( pressure >= Integer.parseInt(pressureMaxThreshold)) {
                Intent intent = new Intent(this, PressureDataActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                createNotificationChannel();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_warning)
                        .setContentTitle("Warning! Pressure Threshold Reached")
                        .setContentText("Pressure levels have surpassed the custom maximum level. Please check the real time data page for the exact humidity reading")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Pressure levels have surpassed the custom maximum level. Please check the real time data page for the exact humidity reading"))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(pressureNotificationIDMax, builder.build());
            }
        }

        if(pressureState == true && pressureMinThreshold != null) {
            if( pressure <= Integer.parseInt(pressureMinThreshold)) {
                Intent intent = new Intent(this, PressureDataActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                createNotificationChannel();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_warning)
                        .setContentTitle("Warning! Pressure Threshold Reached")
                        .setContentText("Pressure levels have surpassed the custom minimum level. Please check the real time data page for the exact humidity reading")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Pressure levels have surpassed the custom minimum level. Please check the real time data page for the exact humidity reading"))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(pressureNotificationIDMin, builder.build());
            }
        }

        if (altitudeState == true && altitudeMaxThreshold != null) {
            if (altitude >= Integer.parseInt(altitudeMaxThreshold)){
                Intent intent = new Intent(this, AltitudeDataActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                createNotificationChannel();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_warning)
                        .setContentTitle("Warning! Altitude Threshold Reached")
                        .setContentText("Altitude has surpassed the custom maximum level indicated by you. Please check the real time data page for the exact temperature reading")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Altitude has surpassed the custom maximum level indicated by you. Please check the real time data page for the exact temperature reading"))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(altitudeNotificationIDMax, builder.build());
            }
        }

        if (altitudeState == true && altitudeMinThreshold != null) {
            if (altitude <= Integer.parseInt(altitudeMinThreshold)){
                Intent intent = new Intent(this, AltitudeDataActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                createNotificationChannel();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_warning)
                        .setContentTitle("Warning! Altitude Threshold Reached")
                        .setContentText("Altitude has surpassed the custom minimum level indicated by you. Please check the real time data page for the exact temperature reading")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Altitude has surpassed the custom minimum level indicated by you. Please check the real time data page for the exact temperature reading"))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(altitudeNotificationIDMin, builder.build());
            }
        }

        if (temperatureState == true && temperatureMaxThreshold != null) {
            if (Integer.parseInt(temperatureMaxThreshold) > 0) {
                if (temperature >= Integer.parseInt(temperatureMaxThreshold)) {
                    Intent intent = new Intent(this, TemperatureDataActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                    createNotificationChannel();
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notification_warning)
                            .setContentTitle("Warning! Temperature Threshold Reached")
                            .setContentText("Temperature has surpassed the custom maximum level indicated by you. Please check the real time data page for the exact temperature reading ")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("Temperature has surpassed the custom maximum level indicated by you. Please check the real time data page for the exact temperature reading "))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);

                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                    notificationManagerCompat.notify(temperatureNotificationIDMax, builder.build());
                }
            }
            else if(Integer.parseInt(temperatureMaxThreshold) < 0){
                if (temperature <= Integer.parseInt(temperatureMaxThreshold)) {
                    Intent intent = new Intent(this, TemperatureDataActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                    createNotificationChannel();
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notification_warning)
                            .setContentTitle("Warning! Temperature Threshold Reached")
                            .setContentText("Temperature has surpassed the custom maximum level indicated by you. Please check the real time data page for the exact temperature reading ")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("Temperature has surpassed the custom maximum level indicated by you. Please check the real time data page for the exact temperature reading "))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);

                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                    notificationManagerCompat.notify(temperatureNotificationIDMax, builder.build());
                }
            }
        }

        if (temperatureState == true && temperatureMinThreshold != null) {
            if (Integer.parseInt(temperatureMinThreshold) > 0) {
                if (temperature <= Integer.parseInt(temperatureMinThreshold)) {
                    Intent intent = new Intent(this, TemperatureDataActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                    createNotificationChannel();
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notification_warning)
                            .setContentTitle("Warning! Temperature Threshold Reached")
                            .setContentText("Temperature has surpassed the custom minimum level indicated by you. Please check the real time data page for the exact temperature reading ")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("Temperature has surpassed the custom minimum level indicated by you. Please check the real time data page for the exact temperature reading "))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);

                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                    notificationManagerCompat.notify(temperatureNotificationIDMax, builder.build());
                }
            }
            else if(Integer.parseInt(temperatureMinThreshold) < 0){
                if (temperature >= Integer.parseInt(temperatureMinThreshold)) {
                    Intent intent = new Intent(this, TemperatureDataActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                    createNotificationChannel();
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notification_warning)
                            .setContentTitle("Warning! Temperature Threshold Reached")
                            .setContentText("Temperature has surpassed the custom minimum level indicated by you. Please check the real time data page for the exact temperature reading ")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("Temperature has surpassed the custom minimum level indicated by you. Please check the real time data page for the exact temperature reading "))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);

                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                    notificationManagerCompat.notify(temperatureNotificationIDMin, builder.build());
                }
            }
        }

        if (humidityState == true && humidityMaxThreshold != null){
            if (humidity >= Integer.parseInt(humidityMaxThreshold)){
                Intent intent = new Intent(this, HumidityDataActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                createNotificationChannel();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_warning)
                        .setContentTitle("Warning! Humidity Threshold Reached")
                        .setContentText("Humidity levels have surpassed the custom maximum level indicated by you. Please check the real time data page for the exact humidity reading ")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Humidity levels have surpassed the custom maximum level indicated by you. Please check the real time data page for the exact humidity reading "))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(humidityNotificationIDMax, builder.build());
            }
        }

        if (humidityState == true && humidityMinThreshold != null){
            if (humidity <= Integer.parseInt(humidityMinThreshold)){
                Intent intent = new Intent(this, HumidityDataActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                createNotificationChannel();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_warning)
                        .setContentTitle("Warning! Humidity Threshold Reached")
                        .setContentText("Humidity levels have surpassed the custom minimum level indicated by you. Please check the real time data page for the exact humidity reading ")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Humidity levels have surpassed the custom minimum level indicated by you. Please check the real time data page for the exact humidity reading "))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(humidityNotificationIDMin, builder.build());
            }
        }

    }

    public void disconnect()
    {
        mBluetooth.Disconnect(true);
    }

    public void print(String s)
    {
        System.out.println(s);
    }

    public float getCo2()
    {
        return co2;
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
    public float getOverallQuality() { return overallAirQuality; }
    public double getLongitude() { return longitude; }
    public double getLatitude() { return latitude; }
}