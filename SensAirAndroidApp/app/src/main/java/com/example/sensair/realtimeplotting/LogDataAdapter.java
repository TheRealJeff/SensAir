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
        super(context, 0,data);
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View listItem = convertView;
        if(listItem==null)
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.item_log_data,parent,false);

        LogDataModel data = getItem(position);

        TextView time = listItem.findViewById(R.id.time);
        TextView value = listItem.findViewById(R.id.value);
        TextView type = listItem.findViewById(R.id.type);
        TextView unit = listItem.findViewById(R.id.unit);

        time.setText(data.getDATE());
        value.setText(data.getVALUE());
        type.setText(data.getTYPE());
        unit.setText(data.getUNIT());

        return listItem;
    }
}