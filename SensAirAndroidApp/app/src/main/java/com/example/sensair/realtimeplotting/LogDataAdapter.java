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
<<<<<<< HEAD
        super(context, 0);
=======
        super(context, 0,data);
>>>>>>> d091fd0a4a2f69cc49a76e5bc66cb57a487f3f8a
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View listItem = convertView;
        if(listItem==null)
<<<<<<< HEAD
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.item_metric,parent,false);

       // TODO set data to adapter
=======
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.item_log_data,parent,false);

        LogDataModel data = getItem(position);

        TextView time = listItem.findViewById(R.id.time);
        TextView value = listItem.findViewById(R.id.value);
        TextView type = listItem.findViewById(R.id.type);
        TextView unit = listItem.findViewById(R.id.unit);

        time.setText(data.getTIME());
        value.setText(data.getVALUE());
        type.setText(data.getTYPE());
        unit.setText(data.getUNIT());

>>>>>>> d091fd0a4a2f69cc49a76e5bc66cb57a487f3f8a
        return listItem;
    }
}