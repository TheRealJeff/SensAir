package com.example.sensair;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.sensair.ui.historyFragments.InfoFragment;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowHomeEnabled(true);  // shows back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // enables back button

        setContentView(R.layout.info_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, InfoFragment.newInstance())
                    .commitNow();
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}