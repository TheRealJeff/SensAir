package com.example.sensair;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sensair.R;

import java.util.ArrayList;

public class RealTimeListAdapter extends ArrayAdapter<String>
{
    private ArrayList<String> metrics = new ArrayList<String>();


    public RealTimeListAdapter(@NonNull Context context)
    {
        super(context, 0);
        metrics.add("Carbon Monoxide (CO)");
        metrics.add("Carbon Dioxide (CO2)");
        metrics.add("Volatile Organic Compounds (VOC)");
        metrics.add("Humidity");
        metrics.add("Pressure");
        metrics.add("Temperature");
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View listItem = convertView;
        if(listItem==null)
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.item_metric,parent,false);

        String metric = getItem(position);

        TextView textViewMetric = listItem.findViewById(R.id.airQualityMetric);
        textViewMetric.setText(metric);

        return super.getView(position, convertView, parent);
    }
}
