package com.example.sensair;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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


    public AirData(String key, String overall, String co2, String tvoc, String gas, String humidity, String pressure, String temperature){

        Date c = Calendar.getInstance().getTime();

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);

        this.key = key;
        DATE = formattedDate;
        OVERALL = overall;
        CO2 = co2;
        TVOC = tvoc;
        GAS = gas;
        HUMIDITY = humidity;
        PRESSURE = pressure;
        TEMP = temperature;
    }
    public AirData(String key, String date, String overall, String co2, String tvoc, String gas, String humidity, String pressure, String temperature){

        this.key = key;
        DATE = date;
        OVERALL = overall;
        CO2 = co2;
        TVOC = tvoc;
        GAS = gas;
        HUMIDITY = humidity;
        PRESSURE = pressure;
        TEMP = temperature;
    }

    @Override
    public String toString(){
        return "Date" + DATE + ": Overall" + OVERALL + ": CO2" + CO2 + ": TVOC" + TVOC + ": GAS" + GAS +
                ": Humidity"+ HUMIDITY + ": Pressure" + PRESSURE + ": TEMP" + TEMP;
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

    public void setKey(String key) { this.key = key; }
}