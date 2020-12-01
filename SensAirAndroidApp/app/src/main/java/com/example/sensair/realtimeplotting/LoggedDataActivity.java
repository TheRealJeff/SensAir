package com.example.sensair.realtimeplotting;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.example.sensair.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoggedDataActivity extends AppCompatActivity
{
    LogDbHelper logDbHelper = new LogDbHelper(this);
    ListView listView;
    LogDataAdapter logDataAdapter;
    Button clearButton;

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

        ArrayList<LogDataModel> data = logDbHelper.getAllData();
        logDataAdapter = new LogDataAdapter(this,data);

        //TODO only include data for a given day
        for(LogDataModel datas : data)
        {

        }

        listView = findViewById(R.id.logDataList);
        listView.setAdapter(logDataAdapter);

        clearButton = findViewById(R.id.clearLoggedData);
        clearButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                deleteList();
            }
        });
    }

    public void deleteList()
    {
        logDataAdapter.clear();
        logDataAdapter.notifyDataSetChanged();

        ArrayList<LogDataModel> data = logDbHelper.getAllData();
        for (LogDataModel datum : data)
        {
            logDbHelper.deleteItem(datum);
        }
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }
}