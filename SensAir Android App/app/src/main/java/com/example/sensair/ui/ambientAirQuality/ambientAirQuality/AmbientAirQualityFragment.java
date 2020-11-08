package com.example.sensair.ui.ambientAirQuality.ambientAirQuality;

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

public class AmbientAirQualityFragment extends Fragment {

    private AmbientAirQualityViewModel ambientAirQualityViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        ambientAirQualityViewModel =
                new ViewModelProvider(this).get(AmbientAirQualityViewModel.class);
        View root = inflater.inflate(R.layout.fragment_ambient_air_quality, container, false);
        final TextView textView = root.findViewById(R.id.text_ambient_air_quality);
        ambientAirQualityViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

}