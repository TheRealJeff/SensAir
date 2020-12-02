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
<<<<<<< HEAD
    private String KEY;

    public LogDataModel(String KEY, String TYPE, String VALUE)
    {
        this.TYPE = TYPE;
        this.VALUE = VALUE;
=======
    private String UNIT;
    private String KEY;
    private String TIME;

    public LogDataModel(String KEY, String TYPE, String VALUE, String UNIT)
    {
        this.TYPE = TYPE;
        this.VALUE = VALUE;
        this.UNIT = UNIT;
>>>>>>> d091fd0a4a2f69cc49a76e5bc66cb57a487f3f8a
        this.KEY = KEY;

        Date date = Calendar.getInstance().getTime();

<<<<<<< HEAD
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(date);

        this.DATE = formattedDate;
    }

    public LogDataModel(String KEY, String DATE, String TYPE, String VALUE)
=======
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(date);
        this.DATE = formattedDate;

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.getDefault());
        String formattedTime = timeFormat.format(date);
        this.TIME = formattedTime;
    }

    public LogDataModel(String KEY, String DATE, String TYPE, String VALUE, String UNIT)
>>>>>>> d091fd0a4a2f69cc49a76e5bc66cb57a487f3f8a
    {
        this.KEY = KEY;
        this.DATE = DATE;
        this.TYPE = TYPE;
        this.VALUE = VALUE;
<<<<<<< HEAD
=======
        this.UNIT = UNIT;

        Date date = Calendar.getInstance().getTime();

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.getDefault());
        String formattedTime = timeFormat.format(date);
        this.TIME = formattedTime;
>>>>>>> d091fd0a4a2f69cc49a76e5bc66cb57a487f3f8a
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

<<<<<<< HEAD
=======
    public String getUNIT()
    {
        return UNIT;
    }

    public String getTIME()
    {
        return TIME;
    }
>>>>>>> d091fd0a4a2f69cc49a76e5bc66cb57a487f3f8a
}
