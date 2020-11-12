package com.example.sensair.ui.hazardousGasses;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HazardousGasViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HazardousGasViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Here we will display Hazardous air quality. \n\nE.g. CO2, Toxic Volatile Organic Compounds, etc.");
    }

    public LiveData<String> getText() {
        return mText;
    }
}