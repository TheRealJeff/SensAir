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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

public class TemperatureDataActivity extends AppCompatActivity implements OnChartValueSelectedListener
{
    protected LineChart temperatureChart;
    protected ImageButton imageButtonFreeze,imageButtonSave;
    protected Button freeze;
    protected boolean frozen = false;
    protected SpeedView gaugeTemperature;
    protected float average,n;
    protected TextView textViewAverage;
    protected Typeface tfLight = Typeface.DEFAULT;

    protected LogDbHelper logDbHelper = new LogDbHelper(this);

    protected BtThread thread;
    protected BluetoothService btService = new BluetoothService();

    private float temperature,selected;

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

        imageButtonFreeze = findViewById(R.id.temperatureFreezeButton);
        imageButtonFreeze.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(frozen)
                {
                    frozen = false;
                    imageButtonFreeze.setImageResource(R.drawable.ic_pause_black_18dp);
                    temperatureChart.clear();
                    plottingInit();
                    thread = new BtThread();
                    thread.start();
                }
                else if(!frozen)
                {
                    frozen = true;
                    imageButtonFreeze.setImageResource(R.drawable.ic_play_arrow_black_18dp);
                    thread.interrupt();
                }
            }
        });

        imageButtonSave = findViewById(R.id.logButton);
        imageButtonSave.setOnClickListener(new  View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if(selected==0)
                    Toast.makeText(TemperatureDataActivity.this, "No Value Selected!", Toast.LENGTH_SHORT).show();
                else
                {
                    logDbHelper.insertLogData(new LogDataModel("-1", "Temperature", String.format("%.0f", selected), "C"));
                    Toast.makeText(TemperatureDataActivity.this, String.format("%.0f", selected) + " Degrees C saved!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        textViewAverage = findViewById(R.id.temperatureAverage);
    }

    public void plottingInit()
    {
        temperatureChart = findViewById(R.id.temperatureChart);
        temperatureChart.setOnChartValueSelectedListener(this);

        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        mv.setChartView(temperatureChart); // For bounds control
        temperatureChart.setMarker(mv); // Set the marker to the chart

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

        LimitLine middle = new LimitLine(-50f,"Dangerous (Below)");
        middle.setLineColor(Color.RED);
        middle.setLineWidth(2f);
        middle.enableDashedLine(10f, 10f, 0f);
        middle.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        middle.setTextSize(11f);
        middle.setTypeface(tfLight);

        LimitLine upper = new LimitLine(50f,"Dangerous (Above)");
        upper.setLineColor(Color.RED);
        upper.setLineWidth(2f);
        upper.enableDashedLine(10f, 10f, 0f);
        upper.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        upper.setTextSize(11f);
        upper.setTypeface(tfLight);

        YAxis leftAxis = temperatureChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(middle);
        leftAxis.addLimitLine(upper);
        leftAxis.setTypeface(tfLight);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(60f);
        leftAxis.setAxisMinimum(-60f);
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
            temperatureChart.setVisibleXRangeMaximum(6);
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
        thread = new BtThread();
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
        if(thread!=null&&!thread.isAlive())
        {
            thread.start();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if(thread!=null)
        {
            thread.interrupt();
        }
    }


    @Override
    public void onValueSelected(Entry e, Highlight h)
    {
        Log.i("Entry selected", e.toString());
        selected = e.getY();
    }

    @Override
    public void onNothingSelected()
    {
        Log.i("Nothing selected", "Nothing selected.");
    }

    public class BtThread extends Thread
    {
        public void run()
        {
            while (!thread.isInterrupted())
            {
                try
                {
                    Thread.sleep(10);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(!frozen)
                        {
                            temperature = btService.getTemperature();
                            addEntry();

                            n++;
                            average = average + ((temperature - average)) / n;

                            textViewAverage.setText(String.format("%.0f", average) + " C");
                            gaugeTemperature.speedTo(temperature);
                        }
                    }
                });
            }
        }
    }
}