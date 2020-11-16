package com.example.sensair;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.sensair.R;
import com.example.sensair.RealTimeActivity;

public class HazardousGasFragment extends Fragment
{

    protected TextView co2Data,tvocData,mq2Data;
    protected Thread thread;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        View view = inflater.inflate(R.layout.fragment_hazardous_gasses, container, false);
        uiInit(view);

        startBluetoothThreading();

        return view;
    }

    public void uiInit(View view)
    {
        co2Data = view.findViewById(R.id.co2Data);
        tvocData = view.findViewById(R.id.tvocData);
        mq2Data = view.findViewById(R.id.mq2Data);
    }

    public void startBluetoothThreading()
    {
        thread =new Thread()
        {

            @Override
            public void run () {
                while (!thread.isInterrupted())
                {
                    try
                    {
                        Thread.sleep(100);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    RealTimeActivity.runOnUI(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(isAdded())
                            {
                                updateCo2(((RealTimeActivity) getActivity()).getCo2());
                                updateTvoc(((RealTimeActivity) getActivity()).getTvoc());
                                updateMq2(((RealTimeActivity) getActivity()).getMq2());
                            }
                            else
                                interrupt();
                        }
                    });
                }
            }
        };
        thread.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        thread.interrupt();
    }

    public void updateCo2(Float co2)
    {
        co2Data.setText(String.format("%.0f",co2));
    }

    public void updateTvoc(Float tvoc)
    {
        tvocData.setText(String.format("%.0f",tvoc));
    }

    public void updateMq2(Float mq2)
    {
        mq2Data.setText(String.format("%.0f",mq2));
    }

    public void print(String message)
    {
        System.out.println(message);
    }

}