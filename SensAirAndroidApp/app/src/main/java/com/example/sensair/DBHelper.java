package com.example.sensair;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.example.sensair.AirData;

import java.util.ArrayList;
import java.util.List;

import static com.example.sensair.DBConfig.COLUMN_CO2;
import static com.example.sensair.DBConfig.COLUMN_DATE;
import static com.example.sensair.DBConfig.COLUMN_KEY;
import static com.example.sensair.DBConfig.COLUMN_OVERALL;
import static com.example.sensair.DBConfig.COLUMN_TVOC;
import static com.example.sensair.DBConfig.COLUMN_COMBUST_GAS;
import static com.example.sensair.DBConfig.COLUMN_HUMIDITY;
import static com.example.sensair.DBConfig.COLUMN_PRESSURE;
import static com.example.sensair.DBConfig.COLUMN_TEMPERATURE;
import static com.example.sensair.DBConfig.DATABASE_NAME;
import static com.example.sensair.DBConfig.TABLE_AIRDATA;

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "DdHelper";

    private Context context = null;

    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context){

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.context = context;

        Log.d(TAG, "DbHelper Constructor");

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_AIRDATA_QUERY = " CREATE TABLE "  + TABLE_AIRDATA + "(" +
                COLUMN_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT,  " +
                COLUMN_DATE + " TEXT NOT NULL, " +
                COLUMN_OVERALL + " TEXT NOT NULL, " +
                COLUMN_CO2 + " TEXT NOT NULL, " +
                COLUMN_TVOC + " TEXT NOT NULL, " +
                COLUMN_COMBUST_GAS + " TEXT NOT NULL, " +
                COLUMN_HUMIDITY + " TEXT NOT NULL, " +
                COLUMN_PRESSURE + " TEXT NOT NULL, " +
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
        contentValues.put(COLUMN_KEY, airData.getKey());
        contentValues.put(COLUMN_DATE, airData.getDATE());
        contentValues.put(COLUMN_OVERALL, airData.getOVERALL());
        contentValues.put(COLUMN_CO2, airData.getCO2());
        contentValues.put(COLUMN_TVOC, airData.getTVOC());
        contentValues.put(COLUMN_COMBUST_GAS, airData.getGAS());
        contentValues.put(COLUMN_HUMIDITY, airData.getHUMIDITY());
        contentValues.put(COLUMN_PRESSURE, airData.getPRESSURE());
        contentValues.put(COLUMN_TEMPERATURE, airData.getTEMP());


        try{
            id = db.insertOrThrow(TABLE_AIRDATA, null, contentValues);
            Log.d(TAG, "successfully inserted AirQaulity Data");
        } catch(SQLException e){
            Toast.makeText(context, "operation failed:" + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally
        {
            db.close();
        }
        return id;
    }

    public List<AirData> getAllData(){
        Log.d(TAG, "Get All Data");
        SQLiteDatabase db = this.getReadableDatabase();

        List<AirData> airDataList = new ArrayList<>();

        Cursor cursor = null;

        try{
            cursor = db.query(TABLE_AIRDATA, null, null,
                    null, null,null,null);

            if(cursor != null) {
                cursor.moveToFirst();

                do {
                    int id = cursor.getInt(cursor.getColumnIndex(COLUMN_KEY));
                    String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                    String overall = cursor.getString(cursor.getColumnIndex(COLUMN_OVERALL));
                    String CO2 = cursor.getString(cursor.getColumnIndex(COLUMN_CO2));
                    String TVOC = cursor.getString(cursor.getColumnIndex(COLUMN_TVOC));
                    String gas = cursor.getString(cursor.getColumnIndex(COLUMN_COMBUST_GAS));
                    String humidity = cursor.getString(cursor.getColumnIndex(COLUMN_HUMIDITY));
                    String pressure = cursor.getString(cursor.getColumnIndex(COLUMN_PRESSURE));
                    String temperature = cursor.getString(cursor.getColumnIndex(COLUMN_TEMPERATURE));


                    airDataList.add(new AirData(date, overall, CO2, TVOC, gas, humidity, pressure, temperature));
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
    public boolean deleteItem(String ID){
        SQLiteDatabase db = this.getReadableDatabase();

        return db.delete(TABLE_AIRDATA, COLUMN_KEY + "=" + ID, null) > 0;
    }
}