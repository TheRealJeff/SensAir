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
    private ArrayList<String> metrics;

    public RealTimeListAdapter(@NonNull Context context, ArrayList<String> metrics)
    {
        super(context, 0);
        this.metrics = metrics;
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
        return listItem;
    }
}
