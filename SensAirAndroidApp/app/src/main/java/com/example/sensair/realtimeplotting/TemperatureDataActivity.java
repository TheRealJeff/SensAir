package com.example.sensair.realtimeplotting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.sensair.BluetoothService;
import com.example.sensair.R;
import com.github.anastr.speedviewlib.SpeedView;
import com.github.anastr.speedviewlib.components.Section;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Objects;

public class TemperatureDataActivity extends AppCompatActivity implements OnChartValueSelectedListener
{
    protected LineChart temperatureChart;
    protected Button freeze;
    protected boolean frozen = false;
    protected SpeedView gaugeTemperature;
    protected float average,n;
    protected TextView textViewAverage;
    protected Typeface tfLight = Typeface.DEFAULT;

    protected Thread thread;
    protected BluetoothService btService;
    protected boolean btIsBound = false;

    private float temperature;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_data);
        uiInit();
        plottingInit();
        gaugeInit();
        startBluetoothThreading();
    }

    public void uiInit()
    {
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Temperature");

        freeze = findViewById(R.id.temperatureFreezeButton);
        freeze.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (frozen)
                {
                    freeze.setText("Freeze");
                    frozen = false;
                } else if (!frozen)
                {
                    freeze.setText("Continue");
                    frozen = true;
                }
            }
        });
        textViewAverage = findViewById(R.id.temperatureAverage);
    }

    public void plottingInit()
    {
        temperatureChart = findViewById(R.id.temperatureChart);
        temperatureChart.setOnChartValueSelectedListener(this);

        // enable touch gestures
        temperatureChart.setTouchEnabled(true);

        // enable scaling and dragging
        temperatureChart.setDragEnabled(true);
        temperatureChart.setScaleEnabled(true);
        temperatureChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        temperatureChart.setPinchZoom(true);

        // set an alternative background color
        temperatureChart.setBackgroundColor(Color.TRANSPARENT);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);

        // add empty data
        temperatureChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l =temperatureChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(tfLight);
        l.setTextColor(Color.BLACK);

        XAxis xl =temperatureChart.getXAxis();
        xl.setTypeface(tfLight);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setTextSize(12f);
        xl.setEnabled(true);

        YAxis leftAxis =temperatureChart.getAxisLeft();
        leftAxis.setTypeface(tfLight);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(40f);
        leftAxis.setAxisMinimum(-40f);
        leftAxis.setTextSize(12f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis =temperatureChart.getAxisRight();
        rightAxis.setEnabled(false);

        temperatureChart.getDescription().setEnabled(false);
        temperatureChart.getLegend().setEnabled(false);
    }

    public void gaugeInit()
    {
        gaugeTemperature = findViewById(R.id.gaugeTemperature);

        Section s1,s2;
        ArrayList<Section> sections = new ArrayList<>();
        ArrayList<Float> ticks = new ArrayList<>();
        
        gaugeTemperature.speedTo(0);
        gaugeTemperature.setMinMaxSpeed(-40,40);
        gaugeTemperature.setWithTremble(false);
        gaugeTemperature.setUnit(" C");

        s1 = new Section(0f,.5f,Color.parseColor("#74BBFB"),80);
        s2 = new Section(.5f,1f,Color.parseColor("#ff6666"),80);
        sections.add(s1);
        sections.add(s2);
        gaugeTemperature.clearSections();
        gaugeTemperature.addSections(sections);

        gaugeTemperature.setMarksNumber(9);
        ticks.add(0.1f);
        ticks.add(0.2f);
        ticks.add(0.3f);
        ticks.add(0.4f);
        ticks.add(0.5f);
        ticks.add(0.6f);
        ticks.add(0.7f);
        ticks.add(0.8f);
        ticks.add(0.9f);
        gaugeTemperature.setTicks(ticks);
    }

    private void addEntry()
    {

        LineData data = temperatureChart.getData();

        if (data != null)
        {

            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null)
            {
                set = createSet();
                data.addDataSet(set);
            }
            data.addEntry(new Entry(set.getEntryCount()/100f, temperature), 0);
            data.notifyDataChanged();
            temperatureChart.notifyDataSetChanged();
            temperatureChart.setVisibleXRangeMaximum(3);
            temperatureChart.moveViewToX(data.getEntryCount());

        }
    }

    private LineDataSet createSet()
    {
        LineDataSet set = new LineDataSet(null,"");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(3f);
        set.setDrawCircles(false);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    public void startBluetoothThreading()
    {
        thread =new Thread()
        {

            @Override
            public void run () {
                while (!thread.isInterrupted())
                {
                    try
                    {
                        Thread.sleep(10);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(!frozen)
                            {
                                if (btIsBound)
                                {
                                    temperature = btService.getTemperature();
                                    addEntry();

                                    n++;
                                    average = average + ((temperature - average)) / n;

                                    textViewAverage.setText(String.format("%.0f", average) + " C");
                                    gaugeTemperature.speedTo(temperature);
                                }
                            }
                        }
                    });
                }
            }
        };
        thread.start();
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, connection, Context.BIND_ADJUST_WITH_ACTIVITY | Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        super.onStop();
        unbindService(connection);
        btIsBound = false;

        if(thread!=null)
        {
            thread.interrupt();
        }
    }


    private final ServiceConnection connection = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            btService = binder.getService();
            btIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            btIsBound = false;
        }
    };

    @Override
    public void onValueSelected(Entry e, Highlight h)
    {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected()
    {
        Log.i("Nothing selected", "Nothing selected.");
    }
}