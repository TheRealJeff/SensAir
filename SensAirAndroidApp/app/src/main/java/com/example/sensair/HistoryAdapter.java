package com.example.sensair;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kotlin.collections.ArrayDeque;


public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.myViewHolder> {

    private Context context;
    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    public List<AirData> airList;

    public HistoryAdapter(List<AirData> airList){
        this.airList = airList;
    }


    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType ){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.historyactivity_list_content, parent, false);
        HistoryAdapter.myViewHolder holder = new HistoryAdapter.myViewHolder(view,airList);
        context = parent.getContext();
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {

        AirData airPiece = airList.get(position);
        holder.Date.setText("Date: " + airPiece.getDATE());
        Log.i("DayView", "Date: " + airPiece.getDATE());
        holder.Overall.setText("Overall: " + airPiece.getOVERALL());
        Log.i("DayView", "Date: " + airPiece.getOVERALL());
        holder.CO2.setText("CO2: " + airPiece.getCO2());
        holder.TVOC.setText("TVOC: " + airPiece.getTVOC());
        holder.gas.setText("Combustible Gas: " + airPiece.getGAS());
        holder.humidity.setText("Humidity: " + airPiece.getHUMIDITY());
        holder.pressure.setText("Pressure: " + airPiece.getPRESSURE());
        holder.temperature.setText("Temperature: " +  airPiece.getTEMP());

    }

    @Override
    public int getItemCount() { return airList.size(); }

    protected static class myViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //Initialize data types and nested recyclerview
        public List<AirData> OnairList  ;

        private TextView Date,Overall,CO2,TVOC,gas,humidity,pressure,temperature;

        public myViewHolder(View v, List<AirData> airPass){
            super(v);
            OnairList = airPass;
            v.setOnClickListener(this);
            Date = itemView.findViewById(R.id.gen_Date);
            Overall = itemView.findViewById(R.id.gen_overall);
            CO2 =  itemView.findViewById(R.id.gen_CO2);
            TVOC = itemView.findViewById(R.id.gen_TVOC);
            gas = itemView.findViewById(R.id.gen_gas);
            humidity = itemView.findViewById(R.id.gen_Humidity);
            pressure = itemView.findViewById(R.id.gen_Pressure);
            temperature = itemView.findViewById(R.id.gen_Temperature);

        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), HistoryDetailActivity.class);
            int day_pass = OnairList.get(this.getPosition()).getDay();
            intent.putExtra("Day_Sampler",String.valueOf(day_pass));
            v.getContext().startActivity(intent);
        }
    }





}
