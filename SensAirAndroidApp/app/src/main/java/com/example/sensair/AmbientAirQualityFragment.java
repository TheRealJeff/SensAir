package com.example.sensair;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.sensair.R;
import com.example.sensair.RealTimeActivity;

import java.io.FileDescriptor;
import java.io.PrintWriter;

public class AmbientAirQualityFragment extends Fragment {

    protected TextView humidityData, pressureData, temperatureData, altitudeData;
    protected Thread thread;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        View view = inflater.inflate(R.layout.fragment_ambient_air_quality, container, false);
        uiInit(view);

        startBluetoothThreading();

        return view;
    }

    public void uiInit(View view)
    {
        humidityData = view.findViewById(R.id.humidityData);
        pressureData = view.findViewById(R.id.pressureData);
        temperatureData = view.findViewById(R.id.temperatureData);
        altitudeData = view.findViewById(R.id.altitudeData);
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
                                updateHumidity(((RealTimeActivity) getActivity()).getHumidity());
                                updatePressure(((RealTimeActivity) getActivity()).getPressure());
                                updateTemperature(((RealTimeActivity) getActivity()).getTemperature());
                                updateAltitudeData(((RealTimeActivity) getActivity()).getAltitude());
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

    @Override
    public void dump(@NonNull String prefix, @Nullable FileDescriptor fd, @NonNull PrintWriter writer, @Nullable String[] args)
    {
        super.dump(prefix, fd, writer, args);
    }

    public void updateHumidity(Float humidity)
    {
        humidityData.setText(String.format("%.0f",humidity));
    }

    public void updatePressure(Float pressure)
    {
        pressureData.setText(String.format("%.0f",pressure));
    }

    public void updateTemperature(Float temperature)
    {
        temperatureData.setText(String.format("%.0f",temperature));
    }

    public void updateAltitudeData(Float altitude)
    {
        altitudeData.setText(String.format("%.0f",altitude));
    }


    public void print(String message)
    {
        System.out.println(message);
    }

}