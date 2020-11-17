package com.example.sensair;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class HistoryDetailActivity extends AppCompatActivity {
    private static final String TAG = "HistoryDetailActivity";
    private DBHelper airData;
    protected List<AirData> airDataList;
    protected Button delete;
    protected String key;
    protected TextView CO2,TVOC,Combust,Humidity, Pressure, Temperature;


    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.history_activity_detail);

        airData = new DBHelper(this);

        CO2 = findViewById(R.id.co2);
        TVOC = findViewById(R.id.tvoc);
        Combust = findViewById(R.id.gas);
        Humidity = findViewById(R.id.humidity);
        Pressure = findViewById(R.id.pressure);
        Temperature = findViewById(R.id.temp);
        delete = findViewById(R.id.Delete);


        Bundle extras = getIntent().getExtras();
        key = extras.getString("key");
        System.out.println(key);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        airDataList = airData.getAllData();
        for(AirData a: airDataList){
            if(a.getKey() == key){
                System.out.println("Hello I am seeting text views for the detail activity");
                CO2.setText(a.getCO2());
                TVOC.setText(a.getTVOC());
                Combust.setText(a.getGAS());
                Humidity.setText(a.getHUMIDITY());
                Pressure.setText(a.getPRESSURE());
                Temperature.setText(a.getTEMP());
            }
        }

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                airData.deleteItem(key);
                Toast.makeText(getBaseContext(),"Deleted", Toast.LENGTH_LONG).show();
            }
        });


    }
    // onOptionsItemSelected definition. Listens for menu item clicks
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
