package com.example.sensair.realtimeplotting;

import androidx.appcompat.app.AppCompatActivity;

<<<<<<< HEAD
import android.os.Bundle;
import com.example.sensair.R;

import java.util.ArrayList;
=======
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.example.sensair.R;
import com.example.sensair.history.AirData;
import com.example.sensair.history.DBHelper;

import java.util.ArrayList;
import java.util.List;
>>>>>>> d091fd0a4a2f69cc49a76e5bc66cb57a487f3f8a
import java.util.Objects;

public class LoggedDataActivity extends AppCompatActivity
{
<<<<<<< HEAD
=======
    LogDbHelper logDbHelper = new LogDbHelper(this);
    ArrayList<LogDataModel> dataList;
    ListView listView;
    String date;
    LogDataAdapter logDataAdapter;
    Button clearButton;
    DBHelper dbHelper;

>>>>>>> d091fd0a4a2f69cc49a76e5bc66cb57a487f3f8a
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_data);

<<<<<<< HEAD
=======
        Intent intent = getIntent();
        date = intent.getStringExtra("date");

>>>>>>> d091fd0a4a2f69cc49a76e5bc66cb57a487f3f8a
        uiInit();
    }

    public void uiInit()
    {
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle("Logged Data");

<<<<<<< HEAD
        ArrayList<LogDataModel> data = new ArrayList<>();    // TODO set data to adapter after pulling from SQL
=======
        ArrayList<LogDataModel> data = logDbHelper.getAllData();

        dataList = new ArrayList<>();

        //TODO only include data for a given day
        for(LogDataModel datum : data)
        {
            if(datum.getDATE().equals(date))
                dataList.add(datum);
        }

        logDataAdapter = new LogDataAdapter(this,dataList);

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
>>>>>>> d091fd0a4a2f69cc49a76e5bc66cb57a487f3f8a
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }
}