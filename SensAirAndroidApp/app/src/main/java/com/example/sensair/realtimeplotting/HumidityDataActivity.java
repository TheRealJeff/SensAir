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

public class HumidityDataActivity extends AppCompatActivity implements OnChartValueSelectedListener
{
    protected LineChart humidityChart;
    protected ImageButton imageButtonFreeze,imageButtonSave;
    protected boolean frozen = false;
    protected SpeedView gaugeHumidity;
    protected float average,n;
    protected TextView textViewAverage;
    protected Typeface tfLight = Typeface.DEFAULT;

    protected BtThread thread;
    protected BluetoothService btService = new BluetoothService();

    private float humidity,selected;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_humidity_data);
        uiInit();
        plottingInit();
        gaugeInit();
        startBluetoothThreading();
    }

    public void uiInit()
    {
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Humidity");

        imageButtonFreeze = findViewById(R.id.humidityFreezeButton);
        imageButtonFreeze.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(frozen)
                {
                    frozen = false;
                    imageButtonFreeze.setImageResource(R.drawable.ic_pause_black_18dp);
                    humidityChart.clear();
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
                // TODO save to database
                if(selected==0)
                {
                    Toast.makeText(HumidityDataActivity.this,"No Value Selected: Select a data point on graph first",Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(HumidityDataActivity.this, String.format("%.0f", selected) + " % Humidity saved!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        textViewAverage = findViewById(R.id.humidityAverage);
    }

    public void plottingInit()
    {
        humidityChart = findViewById(R.id.humidityChart);
        humidityChart.setOnChartValueSelectedListener(this);

        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        mv.setChartView(humidityChart); // For bounds control
        humidityChart.setMarker(mv); // Set the marker to the chart

        // enable touch gestures
        humidityChart.setTouchEnabled(true);

        // enable scaling and dragging
        humidityChart.setDragEnabled(true);
        humidityChart.setScaleEnabled(true);
        humidityChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        humidityChart.setPinchZoom(true);

        // set an alternative background color
        humidityChart.setBackgroundColor(Color.TRANSPARENT);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);

        // add empty data
        humidityChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l =humidityChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(tfLight);
        l.setTextColor(Color.BLACK);

        XAxis xl =humidityChart.getXAxis();
        xl.setTypeface(tfLight);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setTextSize(12f);
        xl.setEnabled(true);

        YAxis leftAxis =humidityChart.getAxisLeft();
        leftAxis.setTypeface(tfLight);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(100);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(12f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis =humidityChart.getAxisRight();
        rightAxis.setEnabled(false);

        humidityChart.getDescription().setEnabled(false);
        humidityChart.getLegend().setEnabled(false);
    }

    public void gaugeInit()
    {
        gaugeHumidity = (SpeedView) findViewById(R.id.gaugeHumidity);
        gaugeHumidity.setMinMaxSpeed(0,100);
        gaugeHumidity.setWithTremble(false);
        gaugeHumidity.setUnit(" %");

        Section s1,s2,s3,s4,s5;
        ArrayList<Section> sections = new ArrayList<>();
        ArrayList<Float> ticks = new ArrayList<>();

        s1 = new Section(0f,.2f,Color.parseColor("#BFEFFF"),80);
        s2 = new Section(.2f,.4f,Color.parseColor("#B0E2FF"),80);
        s3 = new Section(.4f,.6f,Color.parseColor("#7EC0EE"),80);
        s4 = new Section(.6f,.8f,Color.parseColor("#499DF5"),80);
        s5 = new Section(.8f,1f,Color.parseColor("#0276FD"),80);
        sections.add(s1);
        sections.add(s2);
        sections.add(s3);
        sections.add(s4);
        sections.add(s5);
        gaugeHumidity.clearSections();
        gaugeHumidity.addSections(sections);

        gaugeHumidity.setMarksNumber(9);
        ticks.add(0.1f);
        ticks.add(0.2f);
        ticks.add(0.3f);
        ticks.add(0.4f);
        ticks.add(0.5f);
        ticks.add(0.6f);
        ticks.add(0.7f);
        ticks.add(0.8f);
        ticks.add(0.9f);
        gaugeHumidity.setTicks(ticks);
    }

    private void addEntry()
    {

        LineData data = humidityChart.getData();

        if (data != null)
        {

            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null)
            {
                set = createSet();
                data.addDataSet(set);
            }
            data.addEntry(new Entry(set.getEntryCount()/100f, humidity), 0);
            data.notifyDataChanged();
            humidityChart.notifyDataSetChanged();
            humidityChart.setVisibleXRangeMaximum(6);
            humidityChart.moveViewToX(data.getEntryCount());

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
        if(humidityChart.getData()!=null)
        {
            selected = e.getY();
        }
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
                            humidity = btService.getHumidity();
                            addEntry();

                            n++;
                            average = average + ((humidity - average)) / n;

                            textViewAverage.setText(String.format("%.0f", average) + " %");
                            gaugeHumidity.speedTo(humidity);
                        }
                    }
                });
            }
        }
    }
}