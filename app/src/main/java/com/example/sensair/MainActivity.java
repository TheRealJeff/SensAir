package com.example.sensair;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{

    protected Button buttonReadValues;
    protected TextView  eco2, tvoc, combustibleGas;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    public void init()
    {
        buttonReadValues = findViewById(R.id.buttonReadValues);
        tvoc = findViewById(R.id.tvocData);
        eco2 = findViewById(R.id.eco2Data);
        combustibleGas = findViewById(R.id.combustionData);

        buttonReadValues.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                eco2.setText("x ppm");
                tvoc.setText("y (out of n)");
                combustibleGas.setText("z ppm");
            }
        });
    }
}