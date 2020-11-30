package com.example.sensair;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.example.sensair.realtimeplotting.AltitudeDataActivity;
import com.example.sensair.realtimeplotting.CarbonDioxideDataActivity;
import com.example.sensair.realtimeplotting.CarbonMonoxideDataActivity;
import com.example.sensair.realtimeplotting.PressureDataActivity;
import com.example.sensair.realtimeplotting.TemperatureDataActivity;
import com.example.sensair.realtimeplotting.VolatileOrganicCompoundsActivity;

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


    private static float co2;
    private static float tvoc;
    private static float mq2;
    private static float humidity;
    private static float pressure;
    private static float altitude;
    private static float temperature;

    protected SharedPreferences preferences;
    protected boolean altitudeState;
    protected boolean pressureState;
    protected boolean temperatureState;
    protected boolean criticalAlertsState;
    protected String altitudeThreshold;
    protected String pressureThreshold;
    protected String temperatureThreshold;

    private static final int co2NotificationIDDefault = 1;
    private static final int mq2NotificationIDDefault = 2;
    private static final int tvocNotificationIDDefault = 3;
    private static final int altitudeNotificationIDDefault = 4;
    private static final int temperatureNotificationIDDefault = 5;
    private static final int pressureNotificationIDDefault = 6;
    private static final int co2NotificationIDDanger = 7;
    private static final int mq2NotificationIDDanger = 8;
    private static final int tvocNotificationIDDanger = 9;

    public class LocalBinder extends Binder
    {
        public BluetoothService getService()
        {
            return BluetoothService.this;
        }
    }


    @Override
    public void onCreate()
    {
        supportsBluetooth = btInit();
        if(supportsBluetooth)
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
                        Thread.sleep(1);
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
                    print("Reading Data");

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
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
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
        criticalAlertsState = preferences.getBoolean("AirQualityAlerts", false);

        if(altitudeState == true) {
            altitudeThreshold = preferences.getString("SettingAltitudeThreshold", null);
        }
        if(pressureState == true){
            pressureThreshold = preferences.getString("SettingPressureThreshold", null);
        }
        if(temperatureState == true){
            temperatureThreshold = preferences.getString("StringTemperatureThreshold", null);
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
                        .setContentText("Hello")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("This is a larger text sequence"))
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
                        .setContentTitle("Warning! CO2 Levels are Significant")
                        .setContentText("Hello")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("This is a larger text sequence"))
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
                        .setContentText("Hello")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("This is a larger text sequence"))
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
                        .setContentTitle("Warning! TVOC Levels are Significant")
                        .setContentText("Hello")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("This is a larger text sequence"))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(tvocNotificationIDDanger, builder.build());
            }

            if (mq2 >= 50 && mq2 < 100) {
                Intent intent = new Intent(this, CarbonMonoxideDataActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                createNotificationChannel();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_warning)
                        .setContentTitle("Warning! CO Levels are Significant")
                        .setContentText("Hello")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("This is a larger text sequence"))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(mq2NotificationIDDefault, builder.build());
            } else if (mq2 >= 100) {
                Intent intent = new Intent(this, CarbonMonoxideDataActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                createNotificationChannel();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_danger)
                        .setContentTitle("Warning! CO Levels are Significant")
                        .setContentText("Hello")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("This is a larger text sequence"))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(mq2NotificationIDDanger, builder.build());
            }
        }

        if(pressureState == true) {
            if( pressure >= Integer.parseInt(pressureThreshold)) {
                Intent intent = new Intent(this, PressureDataActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                createNotificationChannel();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_warning)
                        .setContentTitle("Warning! Pressure Threshold Reached")
                        .setContentText("Hello")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("This is a larger text sequence"))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(pressureNotificationIDDefault, builder.build());
            }
        }

        if (altitudeState == true) {
            if (altitude >= Integer.parseInt(altitudeThreshold)){
                Intent intent = new Intent(this, AltitudeDataActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                createNotificationChannel();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_warning)
                        .setContentTitle("Warning! Altitude Threshold Reached")
                        .setContentText("Hello")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("This is a larger text sequence"))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(altitudeNotificationIDDefault, builder.build());
            }
        }

        if (temperatureState == true) {
            if (Integer.parseInt(temperatureThreshold) > 0) {
                if (temperature >= Integer.parseInt(temperatureThreshold)) {
                    Intent intent = new Intent(this, TemperatureDataActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                    createNotificationChannel();
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notification_warning)
                            .setContentTitle("Warning! Temperature Threshold Reached")
                            .setContentText("Hello")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("This is a larger text sequence"))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);

                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                    notificationManagerCompat.notify(temperatureNotificationIDDefault, builder.build());
                }
            }
            else if(Integer.parseInt(temperatureThreshold) < 0){
                if (temperature <= Integer.parseInt(temperatureThreshold)) {
                    Intent intent = new Intent(this, TemperatureDataActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                    createNotificationChannel();
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notification_warning)
                            .setContentTitle("Warning! Temperature Threshold Reached")
                            .setContentText("Hello")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("This is a larger text sequence"))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);

                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                    notificationManagerCompat.notify(temperatureNotificationIDDefault, builder.build());
                }
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
    public float getOverallQuality()
    {
        float co_weight = 0;
        float tvoc_weight = 0;
        float mq2_weight = 0;

        if(co2<1000) co_weight=1;
        if(tvoc<250) tvoc_weight=1;
        if(mq2<240) mq2_weight = 1;

        float score = (co_weight+tvoc_weight+mq2_weight)/3;
        score*=100f;
        return score;
    }
}