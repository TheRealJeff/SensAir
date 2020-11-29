package com.example.sensair;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class DailyAdapter extends RecyclerView.Adapter<DailyAdapter.DailyViewHolder> {

    public ArrayList<AirData> airDataArrayList = new ArrayList<AirData>();

    public DailyAdapter(ArrayList<AirData> airsets) {
        this.airDataArrayList = airsets;

    }

    @NonNull
    @Override
    public DailyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_daily_element,parent, false);
        DailyViewHolder holder = new DailyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DailyViewHolder holder, int position) {
        AirData airPiece =  airDataArrayList.get(position);
        holder.Date.setText("Date: " + airPiece.getDATE());
        holder.Overall.setText("Overall: " + airPiece.getOVERALL());
        holder.CO2.setText("CO2: " + airPiece.getCO2());
        holder.TVOC.setText("TVOC: " + airPiece.getTVOC());
        holder.gas.setText("Combustible Gas: " + airPiece.getGAS());
        holder.humidity.setText("Humidity: " + airPiece.getHUMIDITY());
        holder.pressure.setText("Pressure: " + airPiece.getPRESSURE());
        holder.temperature.setText("Temperature: " +  airPiece.getTEMP());

    }

    @Override
    public int getItemCount() {
        return airDataArrayList.size();
    }

    public static class DailyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView Date,Overall,CO2,TVOC,gas,humidity,pressure,temperature;
        public DailyViewHolder(View itemView){
            super(itemView);
            itemView.setOnClickListener(this);
            Date = itemView.findViewById(R.id.Date);
            Overall = itemView.findViewById(R.id.overall);
            CO2 =  itemView.findViewById(R.id.CO2);
            TVOC = itemView.findViewById(R.id.TVOC);
            gas = itemView.findViewById(R.id.gas);
            humidity = itemView.findViewById(R.id.Humidity);
            pressure = itemView.findViewById(R.id.Pressure);
            temperature = itemView.findViewById(R.id.Temperature);

        }

        @Override
        public void onClick(View v) {

        }
    }


}
