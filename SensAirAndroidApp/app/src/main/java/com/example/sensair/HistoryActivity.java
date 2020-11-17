package com.example.sensair;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class HistoryActivity  extends AppCompatActivity {

    // Declare Data base helper

    private DBHelper AirBase = new DBHelper(this);



    @Override
    protected void onCreate( Bundle savedInstances){

        super.onCreate(savedInstances);

        setContentView(R.layout.activity_historyactivity_list);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.history_toolbar);
        //setSupportActionBar(toolbar);
        //toolbar.setTitle(getTitle());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO implement search function
            }
        });

        final RecyclerView recyclerView = findViewById(R.id.historyactivity_list);
        assert recyclerView != null;
        final List<AirData> airDataList = AirBase.getAllData();

        HistoryAdapter airAdapter= new HistoryAdapter(airDataList);
        recyclerView.setAdapter(airAdapter);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}
