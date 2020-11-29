package com.example.sensair;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;

public class ProfileActivity extends AppCompatActivity {

    protected SharedPreferences preferences;
    protected String TAG = "profileActivity";
    //public static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_profile);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

//        NavUtils.navigateUpFromSameTask(this);



    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //preferences.getAll();

        boolean altitudeState = preferences.getBoolean("SettingAltitudeState", false);
        Log.d(TAG, String.valueOf(altitudeState));
        String altitudeThreshold = preferences.getString("SettingAltitudeThreshold", null);
        Log.d(TAG, altitudeThreshold);


        Intent intent = new Intent(this, MainActivity.class);
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
        notificationManagerCompat.notify(1, builder.build());




         */


    }

    /*
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

     */



    public static class SettingsFragment extends PreferenceFragmentCompat {

        protected EditTextPreference editTextPreferenceAltitude;
        protected EditTextPreference editTextPreferencePressure;
        protected EditTextPreference editTextPreferenceTemperature;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            editTextPreferenceAltitude = findPreference(getString(R.string.altitude_setting_threshold));
            editTextPreferenceAltitude.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            });

            editTextPreferencePressure = findPreference(getString(R.string.pressure_setting_threshold));
            editTextPreferencePressure.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            });

            editTextPreferenceTemperature = findPreference(getString(R.string.temperature_setting_threshold));
            editTextPreferenceTemperature.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                }
            });



        }

    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}