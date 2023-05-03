package com.example.sgbustimingwidget.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DBHandler extends SQLiteOpenHelper {

    // creating a constant variables for our database.
    // below variable is for our database name.
    private static final String DB_NAME = "sgbuswidgetdb";

    // below int is our database version
    private static final int DB_VERSION = 2;

    // below variable is for our table name.
    private static final String BUS_STOP_TABLE = "busstopmetadata";
    private static final String SAVED_BUS_ARV_TABLE = "savedbusarrival";

    private static final String CODE_COL = "code";
    private static final String ROADNAME_COL = "roadname";
    private static final String DESC_COL = "description";
    private static final String LAT_COL = "lat";
    private static final String LONG_COL = "long";
    private static final String BUS_NO_COL = "busno";

    // below variable is for our course tracks column.

    // creating a constructor for our database handler.
    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // below method is for creating a database by running a sqlite query
    @Override
    public void onCreate(SQLiteDatabase db) {
        // on below line we are creating
        // an sqlite query and we are
        // setting our column names
        // along with their data types.
        String create_query1 = "CREATE TABLE " + BUS_STOP_TABLE + " ("
                + CODE_COL + " TEXT PRIMARY KEY,"
                + ROADNAME_COL + " TEXT,"
                + DESC_COL + " TEXT,"
                + LAT_COL + " TEXT,"
                + LONG_COL + " TEXT)";

        String create_query2 = "CREATE TABLE " + SAVED_BUS_ARV_TABLE + " ("
                + CODE_COL + " TEXT,"
                + BUS_NO_COL + " TEXT,"
                + "PRIMARY KEY ("
                + CODE_COL + ", "
                + BUS_NO_COL + "))";

        // at last we are calling a exec sql
        // method to execute above sql query
        db.execSQL(create_query2);
        db.execSQL(create_query1);
    }

    // this method is use to add new course to our sqlite database.
    public void addNewBusStopMetadata(String busStopCode, String roadName, String description, String lattitude, String longtitude) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(CODE_COL, busStopCode);
        values.put(ROADNAME_COL, roadName);
        values.put(DESC_COL, description);
        values.put(LAT_COL, lattitude);
        values.put(LONG_COL, longtitude);

        int id = (int) db.insertWithOnConflict(BUS_STOP_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
//        db.close();
    }

    public void addNewSavedBusArrival(String busStopCode, String busNo){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(CODE_COL, busStopCode);
        values.put(BUS_NO_COL, busNo);

        int id = (int) db.insertWithOnConflict(SAVED_BUS_ARV_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
//        db.close();
    }

    public void removeSavedBusArrival(String busStopCode, String busNo){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String delete_query = "DELETE FROM " + SAVED_BUS_ARV_TABLE + " WHERE "
                + CODE_COL + " = '" + busStopCode
                + "' AND "
                + BUS_NO_COL + " = '" + busNo + "'";

        values.put(CODE_COL, busStopCode);
        values.put(BUS_NO_COL, busNo);

        db.execSQL(delete_query);
//        db.close();
    }

    public Boolean checkSavedBusArrival(String busStopCode, String busNo){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectSavedBusArrivalQuery = "SELECT * FROM " + SAVED_BUS_ARV_TABLE + " WHERE " + CODE_COL + " = '" + busStopCode + "' AND " + BUS_NO_COL + " = '" + busNo + "'";
        Cursor cursor = db.rawQuery(selectSavedBusArrivalQuery,null);
        int rows = cursor.getCount();
        cursor.close();
//        db.close();
        return (rows > 0);
    }

    public void DeleteTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ BUS_STOP_TABLE);
//        db.close();
    }

    public Map<String, String>[] Query(String query){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        Map<String, String>[] out_arr = new Map[cursor.getCount()];
        int row =0;
        while (cursor.moveToNext()) {
            Map<String, String> out = new HashMap<String, String>();
            out_arr[row] = out;
            for(int i = 0; i < cursor.getColumnCount(); i++){
                out.put(cursor.getColumnName(i), cursor.getString(i));
            }
            row++;
        }
        cursor.moveToFirst();
//        db.close();
        return out_arr;
    }

    public Map<String, String>[] GetTable(String table_name){
        String query = "SELECT * FROM " + table_name;
        return Query(query);
    }

    public Map<String, String>[] FindBusStop(String input){
        input = input.trim();
        if (input.matches("-?\\d+(\\.\\d+)?")){
            return Query("SELECT * FROM busstopmetadata WHERE code like '%" + input + "%'");
        }else{
            return Query("SELECT * FROM busstopmetadata WHERE lower(description) like lower('%" + input + "%')");
        }
    }

    public int CountDbRows(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + BUS_STOP_TABLE,null);
        int rows = cursor.getCount();
        cursor.close();
//        db.close();
        return rows;
    }

    public void PushBusStopMetadataToDB(JSONArray busStopsJa) throws JSONException {
        for (int i = 0; i < busStopsJa.length(); i++) {
            JSONObject jo = busStopsJa.getJSONObject(i);
            addNewBusStopMetadata(jo.getString("BusStopCode"), jo.getString("RoadName"), jo.getString("Description"), jo.getString("Latitude"), jo.getString("Longitude"));
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + BUS_STOP_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SAVED_BUS_ARV_TABLE );
        onCreate(db);
    }
}
