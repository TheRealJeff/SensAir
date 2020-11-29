package com.example.sensair.realtimeplotting;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.example.sensair.R;

import java.util.ArrayList;
import java.util.Objects;

public class LoggedDataActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_data);

        uiInit();
    }

    public void uiInit()
    {
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle("Logged Data");

        ArrayList<LogDataModel> data = new ArrayList<>();    // TODO set data to adapter after pulling from SQL
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }
}