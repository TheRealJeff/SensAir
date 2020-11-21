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
import com.github.mikephil.charting.components.LimitLine;
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

public class PressureDataActivity extends AppCompatActivity implements OnChartValueSelectedListener
{
    protected LineChart pressureChart;
    protected Button freeze;
    protected boolean frozen = false;
    protected SpeedView gaugePressure;
    protected float average,n;
    protected TextView textViewAverage;
    protected Typeface tfLight = Typeface.DEFAULT;
    
    protected Thread thread;
    protected BluetoothService btService;
    protected boolean btIsBound = false;

    private float pressure;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pressure_data);
        uiInit();
        plottingInit();
        gaugeInit();
        startBluetoothThreading();
    }

    public void uiInit()
    {
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Air Pressure");
        startBluetoothThreading();

        freeze = findViewById(R.id.pressureFreezeButton);
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
        textViewAverage = findViewById(R.id.pressureAverage);
    }

    public void plottingInit()
    {
        pressureChart = findViewById(R.id.pressureChart);
        pressureChart.setOnChartValueSelectedListener(this);

        // enable touch gestures
        pressureChart.setTouchEnabled(true);

        // enable scaling and dragging
        pressureChart.setDragEnabled(true);
        pressureChart.setScaleEnabled(true);
        pressureChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        pressureChart.setPinchZoom(true);

        // set an alternative background color
        pressureChart.setBackgroundColor(Color.TRANSPARENT);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);

        // add empty data
        pressureChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l =pressureChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(tfLight);
        l.setTextColor(Color.BLACK);

        XAxis xl =pressureChart.getXAxis();
        xl.setTypeface(tfLight);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setTextSize(12f);
        xl.setEnabled(true);

        LimitLine middle = new LimitLine(6.3f,"Dangerous (Below)");
        middle.setLineColor(Color.RED);
        middle.setLineWidth(2f);
        middle.enableDashedLine(10f, 10f, 0f);
        middle.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        middle.setTextSize(11f);
        middle.setTypeface(tfLight);

        LimitLine upper = new LimitLine(250f,"Dangerous (Above)");
        upper.setLineColor(Color.RED);
        upper.setLineWidth(2f);
        upper.enableDashedLine(10f, 10f, 0f);
        upper.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        upper.setTextSize(11f);
        upper.setTypeface(tfLight);

        YAxis leftAxis =pressureChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(middle);
        leftAxis.addLimitLine(upper);
        leftAxis.setTypeface(tfLight);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(300);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(12f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis =pressureChart.getAxisRight();
        rightAxis.setEnabled(false);

        pressureChart.getDescription().setEnabled(false);
        pressureChart.getLegend().setEnabled(false);
    }

    public void gaugeInit()
    {
        Section s1,s2,s3;
        ArrayList<Section> sections = new ArrayList<>();
        ArrayList<Float> ticks = new ArrayList<>();

        gaugePressure = findViewById(R.id.gaugePressure);
        gaugePressure.speedTo(0);
        gaugePressure.setWithTremble(false);
        gaugePressure.setMinMaxSpeed(0,300);
        gaugePressure.setUnit(" KPa");

        s1 = new Section(0f,.021f,Color.parseColor("#EE5C42"),80);
        s2 = new Section(.021f,.83333f,Color.parseColor("#00CD66"),80);
        s3 = new Section(.83333f,1f,Color.parseColor("#EE5C42"),80);
        sections.add(s1);
        sections.add(s2);
        sections.add(s3);
        gaugePressure.clearSections();
        gaugePressure.addSections(sections);

        gaugePressure.setMarksNumber(5);
        ticks.add(1/6f);
        ticks.add(2/6f);
        ticks.add(3/6f);
        ticks.add(4/6f);
        ticks.add(5/6f);

        gaugePressure.setTicks(ticks);
    }

    private void addEntry()
    {

        LineData data = pressureChart.getData();

        if (data != null)
        {

            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null)
            {
                set = createSet();
                data.addDataSet(set);
            }
            data.addEntry(new Entry(set.getEntryCount()/100f, pressure/1000), 0);
            data.notifyDataChanged();
            pressureChart.notifyDataSetChanged();
            pressureChart.setVisibleXRangeMaximum(3);
            pressureChart.moveViewToX(data.getEntryCount());

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
                                    pressure = btService.getPressure();
                                    addEntry();

                                    n++;
                                    average = average + ((pressure - average)) / n;

                                    textViewAverage.setText(String.format("%.0f", average/1000f) + " KPa");
                                    gaugePressure.speedTo(pressure/1000f);
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