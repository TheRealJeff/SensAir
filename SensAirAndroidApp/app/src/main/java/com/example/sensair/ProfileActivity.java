package com.example.sensair;

import android.content.SharedPreferences;
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
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;

public class ProfileActivity extends AppCompatActivity {

    protected SharedPreferences preferences;
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

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //preferences.getAll();

        boolean altitudeState = preferences.getBoolean("SettingAltitudeState", false);
        Log.d(TAG, String.valueOf(altitudeState));
        String altitudeThreshold = preferences.getString("SettingAltitudeThreshold", null);
        Log.d(TAG, altitudeThreshold);

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        protected EditTextPreference editTextPreferenceAltitude;
        protected EditTextPreference editTextPreferencePressure;
        protected EditTextPreference editTextPreferenceTemperature;
        protected SwitchPreference switchPreferenceAltitude;

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