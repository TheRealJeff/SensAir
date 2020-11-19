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

import com.example.sensair.BluetoothService;
import com.example.sensair.R;
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

import java.util.Objects;

public class CarbonMonoxideDataActivity extends AppCompatActivity implements OnChartValueSelectedListener
{

    private LineChart coChart;

    protected Thread thread;
    protected BluetoothService btService;
    protected boolean btIsBound = false;
    protected Typeface tfLight = Typeface.DEFAULT;

    private float co;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carbon_monoxide_data);
        uiInit();
        plottingInit();
        startBluetoothThreading();
    }

    public void uiInit()
    {
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Carbon Monoxide (CO)");
    }

    public void plottingInit()
    {
        coChart = findViewById(R.id.coChart);
        coChart.setOnChartValueSelectedListener(this);

        // enable description text
        coChart.getDescription().setEnabled(true);

        // enable touch gestures
        coChart.setTouchEnabled(true);

        // enable scaling and dragging
        coChart.setDragEnabled(true);
        coChart.setScaleEnabled(true);
        coChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        coChart.setPinchZoom(true);

        // set an alternative background color
        coChart.setBackgroundColor(Color.BLACK);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        coChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l =coChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(tfLight);
        l.setTextColor(Color.WHITE);

        XAxis xl =coChart.getXAxis();
        xl.setTypeface(tfLight);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis =coChart.getAxisLeft();
        leftAxis.setTypeface(tfLight);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(1000f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis =coChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void addEntry()
    {

        LineData data = coChart.getData();

        if (data != null)
        {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null)
            {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), co), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            coChart.notifyDataSetChanged();

            // limit the number of visible entries
            coChart.setVisibleXRangeMaximum(200);
//            coChart.setVisibleYRange(30, YAxis.AxisDependency.LEFT);

            // move to the latest entry
            coChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // chart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet()
    {
        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(0f);
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
                            if(btIsBound)
                            {
                                co = btService.getCo2();
                                addEntry();
                                System.out.println("CO DATA THREAD");
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