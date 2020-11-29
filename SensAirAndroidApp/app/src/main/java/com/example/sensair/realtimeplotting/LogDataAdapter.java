package com.example.sensair.realtimeplotting;

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

public class LogDataAdapter extends ArrayAdapter<LogDataModel>
{
    private ArrayList<LogDataModel> data;

    public LogDataAdapter(@NonNull Context context, ArrayList<LogDataModel> data)
    {
        super(context, 0);
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View listItem = convertView;
        if(listItem==null)
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.item_metric,parent,false);

       // TODO set data to adapter
        return listItem;
    }
}