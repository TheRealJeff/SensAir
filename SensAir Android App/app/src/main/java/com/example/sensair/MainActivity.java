package com.example.sensair;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{

    protected Button buttonReadValues;
    protected TextView  eco2, tvoc, combustibleGas;
    protected mBluetoothService btService;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    public void init()
    {

        tvoc = findViewById(R.id.tvocData);
        eco2 = findViewById(R.id.eco2Data);
        combustibleGas = findViewById(R.id.combustionData);

        buttonReadValues.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                /*
                    * TODO Here we have to find out how to fetch data from dev board.
                    *       Once we have the data, just set text to readings (maybe process data too)
                 */
                eco2.setText("x ppm");
                tvoc.setText("y (out of n)");
                combustibleGas.setText("z ppm");
            }
        });
    }
}