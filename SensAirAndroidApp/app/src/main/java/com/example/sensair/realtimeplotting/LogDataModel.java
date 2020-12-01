package com.example.sensair.realtimeplotting;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LogDataModel
{
    private String DATE;
    private String TYPE;
    private String VALUE;
    private String UNIT;
    private String KEY;

    public LogDataModel(String KEY, String TYPE, String VALUE, String UNIT)
    {
        this.TYPE = TYPE;
        this.VALUE = VALUE;
        this.UNIT = UNIT;
        this.KEY = KEY;

        Date date = Calendar.getInstance().getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String formattedDate = dateFormat.format(date);

        this.DATE = formattedDate;
    }

    public LogDataModel(String KEY, String DATE, String TYPE, String VALUE, String UNIT)
    {
        this.KEY = KEY;
        this.DATE = DATE;
        this.TYPE = TYPE;
        this.VALUE = VALUE;
        this.UNIT = UNIT;
    }

    public String getDATE()
    {
        return DATE;
    }

    public String getTYPE()
    {
        return TYPE;
    }

    public String getVALUE()
    {
        return VALUE;
    }

    public String getKEY()
    {
        return KEY;
    }

    public String getUNIT()
    {
        return UNIT;
    }

}
