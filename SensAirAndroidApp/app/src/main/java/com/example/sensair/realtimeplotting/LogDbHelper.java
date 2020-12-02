package com.example.sensair.realtimeplotting;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class LogDbHelper extends SQLiteOpenHelper
{
    public static final String DB_NAME = "logged-db";
    public static final int DB_VERSION = 1;
    public static final String TABLE_LOGGED_DATA = "loggedData" ;
    public static final String COLUMN_KEY = "key";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_DATA_TYPE = "data_type";
    public static final String COLUMN_DATA_UNIT = "data_unit";
    public static final String COLUMN_DATA_VALUE = "data_value";

    public LogDbHelper(Context context)
    {
        super(context,DB_NAME,null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        String CREATE_LOGDATA_QUERY = " CREATE TABLE "  + TABLE_LOGGED_DATA + "( " +
                COLUMN_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT NOT NULL, " +
                COLUMN_TIME + " TEXT NOT NULL, " +
                COLUMN_DATA_TYPE + " TEXT NOT NULL, " +
                COLUMN_DATA_VALUE + " TEXT NOT NULL, " +
                COLUMN_DATA_UNIT+ " TEXT NOT NULL)";

        sqLiteDatabase.execSQL(CREATE_LOGDATA_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {

    }

    public long insertLogData(LogDataModel logDataModel)
    {
        if(logDataModel == null)
            return -1;

        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_DATE, logDataModel.getDATE());
        contentValues.put(COLUMN_TIME, logDataModel.getTIME());
        contentValues.put(COLUMN_DATA_TYPE, logDataModel.getTYPE());
        contentValues.put(COLUMN_DATA_VALUE, logDataModel.getVALUE());
        contentValues.put(COLUMN_DATA_UNIT, logDataModel.getUNIT());

        try
        {
            id = db.insertOrThrow(TABLE_LOGGED_DATA, null, contentValues);
        } catch(SQLException e)
        {
            e.printStackTrace();
        } finally
        {
            db.close();
        }
        return id;
    }

    public ArrayList<LogDataModel> getAllData()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<LogDataModel> airDataList = new ArrayList<>();

        Cursor cursor = null;
        try
        {
            cursor = db.query(TABLE_LOGGED_DATA, null, null,
                    null, null,null,null);

            if(cursor != null)
            {
                cursor.moveToFirst();

                do {
                    String key = cursor.getString(cursor.getColumnIndex(COLUMN_KEY));
                    String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                    String time = cursor.getString(cursor.getColumnIndex(COLUMN_TIME));
                    String type = cursor.getString(cursor.getColumnIndex(COLUMN_DATA_TYPE));
                    String value = cursor.getString(cursor.getColumnIndex(COLUMN_DATA_VALUE));
                    String unit = cursor.getString(cursor.getColumnIndex(COLUMN_DATA_UNIT));

                    airDataList.add(new LogDataModel(key, date, time, type, value, unit));
                } while (cursor.moveToNext());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(cursor != null)
            {
                cursor.close();
            }
            db.close();
        }
        return airDataList;
    }


    public boolean deleteItem(LogDataModel datum)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String ID = datum.getKEY();

        return db.delete(TABLE_LOGGED_DATA, COLUMN_KEY + "=" + ID, null) > 0;
    }
}
