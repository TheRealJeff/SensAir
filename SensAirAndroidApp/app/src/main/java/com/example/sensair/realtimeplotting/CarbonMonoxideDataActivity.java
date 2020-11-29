package com.example.sensair.realtimeplotting;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class CarbonMonoxideDataActivity extends AppCompatActivity implements OnChartValueSelectedListener
{
    protected LineChart coChart;
    protected ImageButton imageButtonFreeze,imageButtonSave;
    protected boolean frozen = false;
    protected SpeedView gaugeCo;
    protected float average,n;
    protected TextView textViewAverage;
    protected Typeface tfLight = Typeface.DEFAULT;
    protected LogDbHelper logDbHelper = new LogDbHelper(this);

    protected BtThread thread;
    protected BluetoothService btService = new BluetoothService();

    private float co,selected;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carbon_monoxide_data);
        uiInit();
        plottingInit();
        gaugeInit();
        startBluetoothThreading();
    }

    public void uiInit()
    {
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Carbon Monoxide (CO)");

        textViewAverage = findViewById(R.id.coAverage);

        imageButtonFreeze = findViewById(R.id.coFreezeButton);
        imageButtonFreeze.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(frozen)
                {
                    frozen = false;
                    imageButtonFreeze.setImageResource(R.drawable.ic_pause_black_18dp);
                    coChart.clear();
                    plottingInit();
                    thread.interrupt();
                }
                else if(!frozen)
                {
                    frozen = true;
                    imageButtonFreeze.setImageResource(R.drawable.ic_play_arrow_black_18dp);
                    thread = new BtThread();
                    thread.start();
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
                LogDataModel logDataModel = new LogDataModel("0","MQ2",Float.toHexString(co));
                logDbHelper.insertLogData(logDataModel);
                Toast.makeText(CarbonMonoxideDataActivity.this, String.format("%.0f", selected) + " ppm CO saved!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void plottingInit()
    {
        coChart = findViewById(R.id.coChart);
        coChart.setOnChartValueSelectedListener(this);

        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        mv.setChartView(coChart); // For bounds control
        coChart.setMarker(mv); // Set the marker to the chart

        // enable touch gestures
        coChart.setTouchEnabled(true);

        // enable scaling and dragging
        coChart.setDragEnabled(true);
        coChart.setScaleEnabled(true);
        coChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        coChart.setPinchZoom(true);

        // set an alternative background color
        coChart.setBackgroundColor(Color.TRANSPARENT);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);

        // add empty data
        coChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l =coChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(tfLight);
        l.setTextColor(Color.BLACK);

        XAxis xl =coChart.getXAxis();
        xl.setTypeface(tfLight);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setTextSize(12f);
        xl.setEnabled(true);

        LimitLine middle = new LimitLine(60f,"Moderate");
        middle.setLineColor(Color.YELLOW);
        middle.setLineWidth(2f);
        middle.enableDashedLine(10f, 10f, 0f);
        middle.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        middle.setTextSize(11f);
        middle.setTypeface(tfLight);

        LimitLine upper = new LimitLine(120f,"Dangerous");
        upper.setLineColor(Color.RED);
        upper.setLineWidth(2f);
        upper.enableDashedLine(10f, 10f, 0f);
        upper.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        upper.setTextSize(11f);
        upper.setTypeface(tfLight);

        YAxis leftAxis =coChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(upper);
        leftAxis.addLimitLine(middle);
        leftAxis.setTypeface(tfLight);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(600f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(12f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis =coChart.getAxisRight();
        rightAxis.setEnabled(false);

        coChart.getDescription().setEnabled(false);
        coChart.getLegend().setEnabled(false);
    }

    public void gaugeInit()
    {
        gaugeCo = findViewById(R.id.gaugeCo);
        gaugeCo.setMinMaxSpeed(0,150);
        gaugeCo.setWithTremble(false);
        gaugeCo.setUnit(" ppm");

        Section s1,s2,s3;
        ArrayList<Section> sections = new ArrayList<>();
        ArrayList<Float> ticks = new ArrayList<>();

        s1 = new Section(0f,.4f,Color.parseColor("#00CD66"),80);
        s2 = new Section(.4f,.8f,Color.parseColor("#FFFF33"),80);
        s3 = new Section(.8f,1f,Color.parseColor("#EE5C42"),80);
        sections.add(s1);
        sections.add(s2);
        sections.add(s3);
        gaugeCo.clearSections();
        gaugeCo.addSections(sections);

        gaugeCo.setMarksNumber(9);
        ticks.add(0.2f);
        ticks.add(0.4f);
        ticks.add(0.6f);
        ticks.add(0.8f);

        gaugeCo.setTicks(ticks);
    }

    private void addEntry()
    {

        LineData data = coChart.getData();

        if (data != null)
        {

            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null)
            {
                set = createSet();
                data.addDataSet(set);
            }
            data.addEntry(new Entry(set.getEntryCount()/100f, co), 0);
            data.notifyDataChanged();
            coChart.notifyDataSetChanged();
            coChart.setVisibleXRangeMaximum(6);
            coChart.moveViewToX(data.getEntryCount());

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
                            co = btService.getMq2();
                            addEntry();

                            n++;
                            average = average + ((co-average))/n;

                            textViewAverage.setText(String.format("%.0f",average)+" ppm");
                            gaugeCo.speedTo(co);
                        }
                    }
                });
            }
        }
    }

}