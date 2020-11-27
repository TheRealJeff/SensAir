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
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.SubscriptSpan;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
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

public class CarbonDioxideDataActivity extends AppCompatActivity implements OnChartValueSelectedListener
{
    protected LineChart co2Chart;
    protected ImageButton imageButtonFreeze,imageButtonSave;
    protected boolean frozen = false;
    protected SpeedView gaugeCo2;
    protected float average,n;
    protected TextView textViewAverage;
    protected Typeface tfLight = Typeface.DEFAULT;

    protected BtThread thread;
    protected BluetoothService btService = new BluetoothService();

    protected static float selected;

    private float co2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carbon_dioxide_data);
        uiInit();
        plottingInit();
        gaugeInit();
        startBluetoothThreading();
    }

    public void uiInit()
    {
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Carbon Dioxide (CO2)");

        textViewAverage = findViewById(R.id.co2Average);

        imageButtonFreeze = findViewById(R.id.co2FreezeButton);
        imageButtonFreeze.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(frozen)
                {
                    frozen = false;
                    imageButtonFreeze.setImageResource(R.drawable.ic_pause_black_18dp);
                    co2Chart.clear();
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
                    Toast.makeText(CarbonDioxideDataActivity.this,"No Value Selected: Select a data point on graph first",Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(CarbonDioxideDataActivity.this, String.format("%.0f", selected) + " ppm CO2 saved!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void plottingInit()
    {
        co2Chart = findViewById(R.id.co2Chart);
        co2Chart.setOnChartValueSelectedListener(this);

        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        mv.setChartView(co2Chart); // For bounds control
        co2Chart.setMarker(mv); // Set the marker to the chart

        // enable touch gestures
        co2Chart.setTouchEnabled(true);

        // enable scaling and dragging
        co2Chart.setDragEnabled(true);
        co2Chart.setScaleEnabled(true);
        co2Chart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        co2Chart.setPinchZoom(true);

        // set an alternative background color
        co2Chart.setBackgroundColor(Color.TRANSPARENT);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);

        // add empty data
        co2Chart.setData(data);

        // get the legend (only possible after setting data)
        Legend l =co2Chart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(tfLight);
        l.setTextColor(Color.BLACK);

        XAxis xl =co2Chart.getXAxis();
        xl.setTypeface(tfLight);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setTextSize(12f);
        xl.setEnabled(true);

        LimitLine middle = new LimitLine(800f,"Moderate");
        middle.setLineColor(Color.YELLOW);
        middle.setLineWidth(2f);
        middle.enableDashedLine(10f, 10f, 0f);
        middle.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        middle.setTextSize(11f);
        middle.setTypeface(tfLight);

        LimitLine upper = new LimitLine(1600f,"Dangerous");
        upper.setLineColor(Color.RED);
        upper.setLineWidth(2f);
        upper.enableDashedLine(10f, 10f, 0f);
        upper.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        upper.setTextSize(11f);
        upper.setTypeface(tfLight);

        YAxis leftAxis =co2Chart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(upper);
        leftAxis.addLimitLine(middle);
        leftAxis.setTypeface(tfLight);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(2500f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(12f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis =co2Chart.getAxisRight();
        rightAxis.setEnabled(false);

        co2Chart.getDescription().setEnabled(false);
        co2Chart.getLegend().setEnabled(false);
    }

    public void gaugeInit()
    {
        gaugeCo2 = findViewById(R.id.gaugeCo2);
        gaugeCo2.setMinMaxSpeed(0,2000);
        gaugeCo2.setWithTremble(false);
        gaugeCo2.setUnit(" ppm");

        Section s1,s2,s3;
        ArrayList<Section> sections = new ArrayList<>();
        ArrayList<Float> ticks = new ArrayList<>();

        s1 = new Section(0f,.4f,Color.parseColor("#00CD66"),80);
        s2 = new Section(.4f,.8f,Color.parseColor("#FFFF33"),80);
        s3 = new Section(.8f,1f,Color.parseColor("#EE5C42"),80);
        sections.add(s1);
        sections.add(s2);
        sections.add(s3);
        gaugeCo2.clearSections();
        gaugeCo2.addSections(sections);

        gaugeCo2.setMarksNumber(9);
        ticks.add(0.2f);
        ticks.add(0.4f);
        ticks.add(0.6f);
        ticks.add(0.8f);
        gaugeCo2.setTicks(ticks);
    }

    private void addEntry()
    {

        LineData data = co2Chart.getData();

        if (data != null)
        {

            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null)
            {
                set = createSet();
                data.addDataSet(set);
            }
            data.addEntry(new Entry(set.getEntryCount()/100f, co2), 0);
            data.notifyDataChanged();
            co2Chart.notifyDataSetChanged();
            co2Chart.setVisibleXRangeMaximum(6);
            co2Chart.moveViewToX(data.getEntryCount());

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
                            co2 = btService.getCo2();
                            addEntry();

                            n++;
                            average = average + ((co2 - average)) / n;

                            textViewAverage.setText(String.format("%.0f", average) + " ppm");
                            gaugeCo2.speedTo(co2);
                        }
                    }
                });
            }
        }
    }
}