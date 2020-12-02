package com.example.sensair.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sensair.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HistoryDetailAdapter extends ArrayAdapter<AirData>
{
    public ArrayList<AirData> data = new ArrayList<>();

    public HistoryDetailAdapter(@NonNull Context context, ArrayList<AirData> data)
    {
        super(context,0,data);
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View listItem = convertView;
        if(listItem==null)
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.item_history_detail,parent,false);

        AirData datum = getItem(position);

        TextView hour = listItem.findViewById(R.id.itemHour);
        TextView co2Text = listItem.findViewById(R.id.co2Avg);
        TextView vocText = listItem.findViewById(R.id.vocAvg);
        TextView smokeText = listItem.findViewById(R.id.smokeAvg);
        TextView humidityText = listItem.findViewById(R.id.humidityAvg);
        TextView pressureText = listItem.findViewById(R.id.pressureAvg);
        TextView tempText = listItem.findViewById(R.id.temperatureAvg);

        Date date = datum.getDateObject();

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, ", Locale.getDefault());
        String dateString = dateFormat.format(date);

        String dataHour = datum.getHOUR();
        dataHour.replace("0","");
        dataHour = dateString+dataHour+":00";

        hour.setText(dataHour);
        co2Text.setText(datum.getCO2()+" ppm");
        vocText.setText(datum.getTVOC()+ " ppb");
        smokeText.setText(datum.getGAS());
        humidityText.setText(datum.getHUMIDITY()+"%");
        pressureText.setText(datum.getPRESSURE()+" KPa");
        tempText.setText(datum.getTEMP()+" C");

        return listItem;
    }
}
