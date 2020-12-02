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

public class VolatileOrganicCompoundsActivity extends AppCompatActivity implements OnChartValueSelectedListener
{
    protected LineChart vocChart;
    protected ImageButton imageButtonFreeze,imageButtonSave;
    protected boolean frozen = false;
    protected SpeedView gaugeVoc;
    protected float average,n;
    protected TextView textViewAverage;
    protected Typeface tfLight = Typeface.DEFAULT;
    protected LogDbHelper logDbHelper = new LogDbHelper(this);

    protected BtThread thread;
    protected BluetoothService btService = new BluetoothService();

    private float tvoc,selected;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volatile_organic_compounds);
        uiInit();
        plottingInit();
        gaugeInit();
        startBluetoothThreading();
    }

    public void uiInit()
    {
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Volatile Organic Compounds");

        imageButtonFreeze = findViewById(R.id.vocFreezeButton);
        imageButtonFreeze.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(frozen)
                {
                    frozen = false;
                    imageButtonFreeze.setImageResource(R.drawable.ic_pause_black_18dp);
                    vocChart.clear();
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
        imageButtonSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (selected == 0)
                    Toast.makeText(VolatileOrganicCompoundsActivity.this, "No Value Selected!", Toast.LENGTH_SHORT).show();
                else
                {
                    logDbHelper.insertLogData(new LogDataModel("-1", "VOC", String.format("%.0f", selected), "ppm"));
                    Toast.makeText(VolatileOrganicCompoundsActivity.this, String.format("%.0f", selected) + " ppm VOC saved!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        textViewAverage = findViewById(R.id.vocAverage);
    }

    public void plottingInit()
    {
        vocChart = findViewById(R.id.vocChart);
        vocChart.setOnChartValueSelectedListener(this);

        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        mv.setChartView(vocChart); // For bounds control
        vocChart.setMarker(mv); // Set the marker to the chart

        // enable touch gestures
        vocChart.setTouchEnabled(true);

        // enable scaling and dragging
        vocChart.setDragEnabled(true);
        vocChart.setScaleEnabled(true);
        vocChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        vocChart.setPinchZoom(true);

        // set an alternative background color
        vocChart.setBackgroundColor(Color.TRANSPARENT);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);

        // add empty data
        vocChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l =vocChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(tfLight);
        l.setTextColor(Color.BLACK);

        XAxis xl =vocChart.getXAxis();
        xl.setTypeface(tfLight);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setTextSize(12f);
        xl.setEnabled(true);

        LimitLine middle = new LimitLine(400f,"Moderate");
        middle.setLineColor(Color.YELLOW);
        middle.setLineWidth(2f);
        middle.enableDashedLine(10f, 10f, 0f);
        middle.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        middle.setTextSize(11f);
        middle.setTypeface(tfLight);

        LimitLine upper = new LimitLine(2000f,"Dangerous");
        upper.setLineColor(Color.RED);
        upper.setLineWidth(2f);
        upper.enableDashedLine(10f, 10f, 0f);
        upper.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        upper.setTextSize(11f);
        upper.setTypeface(tfLight);

        YAxis leftAxis =vocChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(upper);
        leftAxis.addLimitLine(middle);
        leftAxis.setTypeface(tfLight);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(2200);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(12f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis =vocChart.getAxisRight();
        rightAxis.setEnabled(false);

        vocChart.getDescription().setEnabled(false);
        vocChart.getLegend().setEnabled(false);
    }

    public void gaugeInit()
    {
        Section s1,s2,s3;
        ArrayList<Section> sections = new ArrayList<>();
        ArrayList<Float> ticks = new ArrayList<>();

        gaugeVoc = findViewById(R.id.gaugeVoc);
        gaugeVoc.speedTo(0);
        gaugeVoc.setWithTremble(false);
        gaugeVoc.setMinMaxSpeed(0,4000);
        gaugeVoc.setUnit("ppb");

        s1 = new Section(0f,.1f,Color.parseColor("#00CD66"),80);
        s2 = new Section(.1f,.5f,Color.parseColor("#FFFF33"),80);
        s3 = new Section(.5f,1f,Color.parseColor("#EE5C42"),80);
        sections.add(s1);
        sections.add(s2);
        sections.add(s3);
        gaugeVoc.clearSections();
        gaugeVoc.addSections(sections);

        gaugeVoc.setMarksNumber(9);
        ticks.add(0.1f);
        ticks.add(0.3f);
        ticks.add(0.5f);
        ticks.add(0.7f);
        ticks.add(0.9f);
        gaugeVoc.setTicks(ticks);
    }

    private void addEntry()
    {

        LineData data = vocChart.getData();

        if (data != null)
        {

            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null)
            {
                set = createSet();
                data.addDataSet(set);
            }
            data.addEntry(new Entry(set.getEntryCount()/100f, tvoc), 0);
            data.notifyDataChanged();
            vocChart.notifyDataSetChanged();
            vocChart.setVisibleXRangeMaximum(6);
            vocChart.moveViewToX(data.getEntryCount());

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
                            tvoc = btService.getHumidity();
                            addEntry();

                            n++;
                            average = average + ((tvoc - average)) / n;

                            textViewAverage.setText(String.format("%.0f", average) + " ppb");
                            gaugeVoc.speedTo(tvoc);
                        }
                    }
                });
            }
        }
    }
}