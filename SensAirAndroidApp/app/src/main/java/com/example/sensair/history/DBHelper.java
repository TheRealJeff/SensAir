package com.example.sensair.history;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.example.sensair.history.AirData;

import java.util.ArrayList;
import java.util.List;

import static com.example.sensair.history.DBConfig.COLUMN_CO2;
import static com.example.sensair.history.DBConfig.COLUMN_DATE;
import static com.example.sensair.history.DBConfig.COLUMN_HOUR;
import static com.example.sensair.history.DBConfig.COLUMN_KEY;
import static com.example.sensair.history.DBConfig.COLUMN_OVERALL;
import static com.example.sensair.history.DBConfig.COLUMN_TVOC;
import static com.example.sensair.history.DBConfig.COLUMN_COMBUST_GAS;
import static com.example.sensair.history.DBConfig.COLUMN_HUMIDITY;
import static com.example.sensair.history.DBConfig.COLUMN_PRESSURE;
import static com.example.sensair.history.DBConfig.COLUMN_TEMPERATURE;
import static com.example.sensair.history.DBConfig.NAME_DATABASE;
import static com.example.sensair.history.DBConfig.TABLE_AIRSETS;
import static com.example.sensair.history.DBConfig.COLUMN_LATITUDE;
import static com.example.sensair.history.DBConfig.COLUMN_LONGITUDE;


public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "DdHelper";

    private Context context = null;

    private static final int DATABASE_VERSION = 2;

    public DBHelper(Context context){

        super(context, NAME_DATABASE, null, DATABASE_VERSION);

        this.context = context;

        Log.d(TAG, "DbHelper Constructor");

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    String CREATE_AIRDATA_QUERY = " CREATE TABLE "  + TABLE_AIRSETS + "(" +
                COLUMN_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT NOT NULL, " +
                COLUMN_HOUR + " TEXT NOT NULL, " +
                COLUMN_OVERALL + " TEXT NOT NULL, " +
                COLUMN_CO2 + " TEXT NOT NULL, " +
                COLUMN_TVOC + " TEXT NOT NULL, " +
                COLUMN_COMBUST_GAS + " TEXT NOT NULL, " +
                COLUMN_HUMIDITY + " TEXT NOT NULL, " +
                COLUMN_PRESSURE + " TEXT NOT NULL, " +
                COLUMN_LATITUDE + " TEXT NOT NULL, " +
                COLUMN_LONGITUDE + " TEXT NOT NULL, " +
                COLUMN_TEMPERATURE + " TEXT NOT NULL)";



        Log.d(TAG, CREATE_AIRDATA_QUERY);
        db.execSQL(CREATE_AIRDATA_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insertAirData(AirData airData){

        if(airData == null) return -1;

        Log.d(TAG, "attempt to insert AirQuality Data: " + airData.toString());

        SQLiteDatabase db = this.getWritableDatabase();

        long id = -1;

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_DATE, airData.getDate());
        contentValues.put(COLUMN_HOUR, airData.getHour());
        contentValues.put(COLUMN_OVERALL, airData.getOVERALL());
        contentValues.put(COLUMN_CO2, airData.getCO2());
        contentValues.put(COLUMN_TVOC, airData.getTVOC());
        contentValues.put(COLUMN_COMBUST_GAS, airData.getGAS());
        contentValues.put(COLUMN_HUMIDITY, airData.getHUMIDITY());
        contentValues.put(COLUMN_PRESSURE, airData.getPRESSURE());
        contentValues.put(COLUMN_LATITUDE, String.valueOf(airData.getLattidude()));
        contentValues.put(COLUMN_LONGITUDE, String.valueOf(airData.getLongitude()));
        contentValues.put(COLUMN_TEMPERATURE, airData.getTEMP());

        try{
            id = db.insertOrThrow(TABLE_AIRSETS, null, contentValues);
            Log.d(TAG, "successfully inserted AirQaulity Data");
        } catch(SQLException e){
            e.printStackTrace();
        } finally
        {
            db.close();
        }
        return id;
    }

    public ArrayList<AirData> getAllData(){
        Log.d(TAG, "Get All Data");
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<AirData> airDataList = new ArrayList<>();

        Cursor cursor = null;

        try{

            cursor = db.query(TABLE_AIRSETS, null, null, null, null, null, null);

            if(cursor != null) {
                cursor.moveToFirst();

                do {
                    String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                    String hour = cursor.getString(cursor.getColumnIndex(COLUMN_HOUR));
                    String overall = cursor.getString(cursor.getColumnIndex(COLUMN_OVERALL));
                    String CO2 = cursor.getString(cursor.getColumnIndex(COLUMN_CO2));
                    String TVOC = cursor.getString(cursor.getColumnIndex(COLUMN_TVOC));
                    String gas = cursor.getString(cursor.getColumnIndex(COLUMN_COMBUST_GAS));
                    String humidity = cursor.getString(cursor.getColumnIndex(COLUMN_HUMIDITY));
                    String pressure = cursor.getString(cursor.getColumnIndex(COLUMN_PRESSURE));
                    String temperature = cursor.getString(cursor.getColumnIndex(COLUMN_TEMPERATURE));
                    String latitude = cursor.getString(cursor.getColumnIndex(COLUMN_LATITUDE));
                    Double lat = Double.valueOf(latitude);
                    String longitude = cursor.getString(cursor.getColumnIndex(COLUMN_LONGITUDE));
                    Double longe = Double.valueOf(longitude);

                    airDataList.add(new AirData(date,hour,overall, CO2, TVOC, gas, humidity, pressure, temperature, lat, longe));
                    Log.i("Data", "returing data");
                } while (cursor.moveToNext());
            }
        }
        catch (Exception e){ }

        finally{
            if(cursor != null){
                cursor.close();
            }
            db.close();
        }
        return airDataList;
    }
    public boolean deleteItem(String Date){
        SQLiteDatabase db = this.getReadableDatabase();

        return db.delete(TABLE_AIRSETS, COLUMN_DATE + "=" + Date, null) > 0;
    }
}