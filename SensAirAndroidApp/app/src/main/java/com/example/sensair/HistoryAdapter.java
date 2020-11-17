package com.example.sensair;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.myViewHolder> {

    private Context context;
    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    public List<AirData> airList;
    protected String cDate;
    protected RecyclerView LayoutManager ;

    public HistoryAdapter(List<AirData> airList){
        this.airList = airList;
    }


    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType ){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.historyactivity_list_content, parent, false);
        HistoryAdapter.myViewHolder holder = new HistoryAdapter.myViewHolder(view);
        context = parent.getContext();
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {

        AirData airPiece = airList.get(position);
        holder.date.setText(airPiece.getDATE());
        holder.overall.setText(airPiece.getCO2()); // Change to overall using functions.
    }

    @Override
    public int getItemCount() { return airList.size(); }

    protected static class myViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //Initialize data types and nested recyclerview
        public List<AirData> airList;
        public DBHelper airData;

        private TextView date;
        private TextView overall;
        public myViewHolder(View v){
            super(v);
            v.setOnClickListener(this);
            date = itemView.findViewById(R.id.date_text);
            overall = itemView.findViewById(R.id.overall);

        }

        @Override
        public void onClick(View v) {
            airData = new DBHelper(v.getContext());
            airList  = airData.getAllData();
            Intent intent = new Intent(v.getContext(), HistoryDetailActivity.class);
            int position = getAdapterPosition();
            final String key_c = airList.get(position).getKey();
            intent.putExtra("Key",key_c);
            v.getContext().startActivity(intent);
        }
    }



}
