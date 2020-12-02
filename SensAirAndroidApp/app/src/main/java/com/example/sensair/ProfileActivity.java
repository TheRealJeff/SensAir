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

    protected String TAG = "profileActivity";


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




    }


    public static class SettingsFragment extends PreferenceFragmentCompat {

        protected String TAG = "profileActivity";

        protected EditTextPreference editTextPreferenceAltitudeMax;
        protected EditTextPreference editTextPreferenceAltitudeMin;
        protected EditTextPreference editTextPreferencePressureMax;
        protected EditTextPreference editTextPreferencePressureMin;
        protected EditTextPreference editTextPreferenceTemperatureMax;
        protected EditTextPreference editTextPreferenceTemperatureMin;
        protected EditTextPreference editTextPreferenceHumidityMax;
        protected EditTextPreference editTextPreferenceHumidityMin;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            editTextPreferenceAltitudeMax = findPreference(getString(R.string.altitude_max_setting_threshold));
            editTextPreferenceAltitudeMax.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    Log.d(TAG, "inside setOnBindEditTextListener");
                }
            });

            editTextPreferenceAltitudeMin = findPreference(getString(R.string.altitude_min_setting_threshold));
            editTextPreferenceAltitudeMin.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            });

            editTextPreferencePressureMax = findPreference(getString(R.string.pressure_max_setting_threshold));
            editTextPreferencePressureMax.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            });

            editTextPreferencePressureMin = findPreference(getString(R.string.pressure_min_setting_threshold));
            editTextPreferencePressureMin.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            });

            editTextPreferenceTemperatureMax = findPreference(getString(R.string.temperature_max_setting_threshold));
            editTextPreferenceTemperatureMax.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                }
            });

            editTextPreferenceTemperatureMin = findPreference(getString(R.string.temperature_min_setting_threshold));
            editTextPreferenceTemperatureMin.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                }
            });

            editTextPreferenceHumidityMax = findPreference(getString(R.string.humidity_max_setting_threshold));
            editTextPreferenceHumidityMax.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER );
                }
            });

            editTextPreferenceHumidityMin = findPreference(getString(R.string.humidity_min_setting_threshold));
            editTextPreferenceHumidityMin.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER );
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