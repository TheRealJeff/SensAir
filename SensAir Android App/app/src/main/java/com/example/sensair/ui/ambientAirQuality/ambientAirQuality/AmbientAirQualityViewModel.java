package com.example.sensair.ui.ambientAirQuality.ambientAirQuality;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AmbientAirQualityViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AmbientAirQualityViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Here we will display Ambient Air quality. \n\nE.g. Pressure, temperature, humidity");
    }

    public LiveData<String> getText() {
        return mText;
    }
}