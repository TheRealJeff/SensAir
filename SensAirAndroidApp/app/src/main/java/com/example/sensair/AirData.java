package com.example.sensair;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AirData {
    private String DATE;
    private String OVERALL;
    private String CO2;
    private String TVOC;
    private String GAS;
    private String HUMIDITY;
    private String PRESSURE;
    private String TEMP;
    private String key;
    private double Longitude;
    private double Lattidude;
    private int day;
    private int hour;
    private int month;
    private int year;



    public AirData(String overall, String co2, String tvoc, String gas, String humidity, String pressure, String temperature, Double longe, Double latt){

        Calendar cal = Calendar.getInstance();
        day = cal.get(Calendar.DAY_OF_MONTH);
        hour = cal.get(Calendar.HOUR_OF_DAY);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);




        DATE = hour + ":" + day + ":" + month + ":" + year;
        Longitude = longe ;
        Lattidude = latt;
        OVERALL = overall;
        CO2 = co2;
        TVOC = tvoc;
        GAS = gas;
        HUMIDITY = humidity;
        PRESSURE = pressure;
        TEMP = temperature;

    }
    public AirData(String Date, String overall, String co2, String tvoc, String gas, String humidity, String pressure, String temperature, Double longe, Double latt){
        DATE = Date;
        Log.i("Air Time" , Date);
        int hour_count = 0;
        int day_count = 0;
        int transition_detect = 0;
        String hour = "";
        String day = "";
        for(int i = 0; i < Date.length();  i++ ){
            char c = Date.charAt(i);
            if(transition_detect == 0) {
                if (c != ':') {
                    Log.i("Air Time", "Saving int hour" + c);

                    hour += c;
                    hour_count++;
                    continue;
                } else {
                    transition_detect++;
                    continue;
                }
            }
            if(transition_detect == 1){
                if( c != ':'){
                    Log.i("Air Time", "Saving int day" + c);
                    day += c;
                    day_count++;
                    continue;
                }
                else{
                    transition_detect++;
                    continue;
                }
            }
        }


        this.hour = Integer.valueOf(hour);
        this.day = Integer.valueOf(day);
        Log.i("Air Time", "The day int is " + this.day);
        Log.i("Air Time", "The hour int is " + this.hour);

        OVERALL = overall;
        CO2 = co2;
        TVOC = tvoc;
        GAS = gas;
        HUMIDITY = humidity;
        PRESSURE = pressure;
        TEMP = temperature;
        Longitude = longe;
        Lattidude = latt;
    }

    @Override
    public String toString(){
        return "Overall" + OVERALL + ": CO2" + CO2 + ": TVOC" + TVOC + ": GAS" + GAS +
                ": Humidity"+ HUMIDITY + ": Pressure" + PRESSURE + ": TEMP" + TEMP + Lattidude + Longitude;
    }

    public String getDATE() {
        return DATE;
    }

    public String getOVERALL() {
        return OVERALL;
    }

    public String getCO2() {
        return CO2;
    }

    public String getTVOC() {
        return TVOC;
    }

    public String getGAS() {
        return GAS;
    }

    public String getHUMIDITY() {
        return HUMIDITY;
    }

    public String getPRESSURE() {
        return PRESSURE;
    }

    public String getTEMP() {
        return TEMP;
    }

    public String getKey() { return key; }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public double getLongitude() { return Longitude; }

    public double getLattidude() { return Lattidude; }

    public void setDATE(String DATE) {
        this.DATE = DATE;
    }

    public void setCO2(String CO2) {
        this.CO2 = CO2;
    }

    public void setTVOC(String TVOC) {
        this.TVOC = TVOC;
    }

    public void setGAS(String GAS) {
        this.GAS = GAS;
    }

    public void setHUMIDITY(String HUMIDITY) {
        this.HUMIDITY = HUMIDITY;
    }

    public void setPRESSURE(String PRESSURE) {
        this.PRESSURE = PRESSURE;
    }

    public void setOVERALL(String OVERALL) {
        this.OVERALL = OVERALL;
    }

    public void setTEMP(String TEMP) {
        this.TEMP = TEMP;
    }


}