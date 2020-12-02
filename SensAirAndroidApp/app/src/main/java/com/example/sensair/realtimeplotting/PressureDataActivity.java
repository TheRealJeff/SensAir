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

public class PressureDataActivity extends AppCompatActivity implements OnChartValueSelectedListener
{
    protected LineChart pressureChart;
    protected ImageButton imageButtonFreeze,imageButtonSave;
    protected boolean frozen = false;
    protected SpeedView gaugePressure;
    protected float average,n;
    protected TextView textViewAverage;
    protected Typeface tfLight = Typeface.DEFAULT;
<<<<<<< HEAD
=======
    protected LogDbHelper logDbHelper = new LogDbHelper(this);
>>>>>>> d091fd0a4a2f69cc49a76e5bc66cb57a487f3f8a
    
    protected BtThread thread;
    protected BluetoothService btService = new BluetoothService();

    private float pressure,selected;

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

        imageButtonFreeze = findViewById(R.id.pressureFreezeButton);
        imageButtonFreeze.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(frozen)
                {
                    frozen = false;
                    imageButtonFreeze.setImageResource(R.drawable.ic_pause_black_18dp);
                    pressureChart.clear();
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
<<<<<<< HEAD
                // TODO save to database
                if(selected==0)
                {
                    Toast.makeText(PressureDataActivity.this,"No Value Selected: Select a data point on graph first",Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(PressureDataActivity.this, String.format("%.0f", selected) + " ppm CO saved!", Toast.LENGTH_SHORT).show();
=======
                if(selected==0)
                    Toast.makeText(PressureDataActivity.this, "No Value Selected!", Toast.LENGTH_SHORT).show();
                else
                {
                    logDbHelper.insertLogData(new LogDataModel("-1", "Pressure", String.format("%.0f", selected), "KPa"));
                    Toast.makeText(PressureDataActivity.this, String.format("%.0f", selected) + " KPA saved!", Toast.LENGTH_SHORT).show();
>>>>>>> d091fd0a4a2f69cc49a76e5bc66cb57a487f3f8a
                }
            }
        });
        textViewAverage = findViewById(R.id.pressureAverage);
    }

    public void plottingInit()
    {
        pressureChart = findViewById(R.id.pressureChart);
        pressureChart.setOnChartValueSelectedListener(this);

        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        mv.setChartView(pressureChart); // For bounds control
        pressureChart.setMarker(mv); // Set the marker to the chart

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
            pressureChart.setVisibleXRangeMaximum(6);
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
                while (!this.isInterrupted())
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
                            pressure = btService.getPressure();
                            addEntry();

                            n++;
                            average = average + ((pressure - average)) / n;

                            textViewAverage.setText(String.format("%.0f", average/1000f) + " KPa");
                            gaugePressure.speedTo(pressure/1000f);
                        }
                    }
                });
            };
        }
    }
}