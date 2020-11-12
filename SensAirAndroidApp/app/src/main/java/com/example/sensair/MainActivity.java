package com.example.sensair;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.github.anastr.speedviewlib.SpeedView;
import com.github.anastr.speedviewlib.components.Section;
import com.github.anastr.speedviewlib.components.Style;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{

    protected TextView textViewGauge;
    protected SpeedView gaugeAirQuality;
    protected ImageButton buttonRealTime, buttonHistory, buttonProfile;
    protected Spinner spinner;
    protected static final int REQUEST_ENABLE_BT = 1;
    protected BtHelper btHelper;
    protected BluetoothDevice sensAir = null;
    protected List<String> categories = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btInit();
        uiInit();
        dropDownInit();

    }

    public void btInit()
    {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);


        if (btAdapter == null)
        {
            String msg = "ERROR: Phone does not support bluetooth. Bluetooth connection failed!";
            Toast.makeText(this,msg,Toast.LENGTH_LONG);
            return;
        }
        else if (!btAdapter.isEnabled())
        {
            startActivityForResult(btEnableIntent, REQUEST_ENABLE_BT);
        }
        //TODO HANDLE SUCCESSFUL DEVICE CONNECTION

        btHelper = new BtHelper(this);

        if(btHelper.isConnected())
        {
            String msg = "Connected to the SensAir!";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
        else
        {
            String msg = "Failed to connect to bluetooth: Please pair device in Settings.";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            return;
        }
    }

    public void uiInit()
    {
        gaugeAirQuality = (SpeedView) findViewById(R.id.gaugeAirQuality);
        gaugeInit();

        buttonRealTime = (ImageButton) findViewById(R.id.buttonRealTime);
        buttonHistory = (ImageButton) findViewById(R.id.buttonHistory);
        buttonProfile = (ImageButton) findViewById(R.id.buttonProfile);

        buttonRealTime.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View V)
            {
                Intent intent = new Intent(MainActivity.this, RealTimeActivity.class);
                startActivity(intent);
            }
        });

        buttonHistory.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View V)
            {
                Intent intent = new Intent(MainActivity.this, HistoryActivityListActivity.class);
                startActivity(intent);
            }
        });

        buttonProfile.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

    }

    public void gaugeInit()
    {
        // TODO set to user preference / default
        gaugeAirQuality.speedTo(75);
        gaugeAirQuality.setTrembleDegree(1);
        gaugeAirQuality.makeSections(3, 0, Style.BUTT);
        ArrayList<Section> sections = gaugeAirQuality.getSections();
        sections.get(0).setColor(Color.rgb(250, 67, 67));
        sections.get(1).setColor(Color.rgb(255, 255, 102));
        sections.get(2).setColor(Color.rgb(90, 245, 110));

    }

    public void dropDownInit()
    {
        Spinner spinner = findViewById(R.id.spinner);
        assert spinner != null;
        spinner.setOnItemSelectedListener(this);

        categories.add("Overall Air Quality");
        categories.add("CO2");
        categories.add("TVOC");
        categories.add("Combustible Gas");
        categories.add("Humidity");
        categories.add("Pressure");
        categories.add("Temperature");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(0);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                //TODO Handle CO2 choice and display meter values
                break;
            case 1:
                //TODO Handle TVOC choice and display meter values
                break;
            case 2:
                //TODO Handle MQ2 choice and display meter values
                break;
            case 3:
                //TODO Handle HUMUDITY choice and display meter values
                break;
            case 4:
                //TODO Handle PRESSURE choice and display meter values
                break;
            case 5:
                //TODO Handle TEMPERATURE choice and display meter values
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if(!btHelper.isConnected())
        {
            String msg = "Oops! Lost connection to the SensAir. Please pair device in Settings.";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }

    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (requestCode == REQUEST_ENABLE_BT)
        {
            if (requestCode == RESULT_OK) {
                String msg = "Bluetooth enabled";
                Toast.makeText(this, msg, Toast.LENGTH_LONG);
            }
            else if (requestCode == RESULT_CANCELED)
            {
                String msg = "Bluetooth enable failed";
                Toast.makeText(this, msg, Toast.LENGTH_LONG);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();  // inflates menu designed in /res/menu
        inflater.inflate(R.menu.menu_main_activity,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();          // gets item ID
        if(id == R.id.infoButton)          // if edit button is clicked
        {
            Intent intent = new Intent(MainActivity.this, InfoActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}