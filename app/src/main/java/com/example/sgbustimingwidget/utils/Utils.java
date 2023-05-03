package com.example.sgbustimingwidget.utils;

import android.content.SharedPreferences;

import com.example.sgbustimingwidget.database.DBHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

public class Utils {
    public static String[] ExtractArrivalTime(JSONObject busServiceJo) throws JSONException, ParseException {
        String[] arrivalTimeArr = new String[3];
        TimeZone sg_tz = TimeZone.getTimeZone("GMT+08:00");
        Long currentTime = Calendar.getInstance(sg_tz).getTime().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+08:00'");
        format.setTimeZone(sg_tz);
        arrivalTimeArr[0] = FormatTimeDiff(format, busServiceJo.getJSONObject("NextBus").getString("EstimatedArrival"), currentTime);
        arrivalTimeArr[1] = FormatTimeDiff(format, busServiceJo.getJSONObject("NextBus2").getString("EstimatedArrival"), currentTime);
        arrivalTimeArr[2] = FormatTimeDiff(format, busServiceJo.getJSONObject("NextBus3").getString("EstimatedArrival"), currentTime);

        return arrivalTimeArr;
    }

    public static String FormatTimeDiff(SimpleDateFormat format, String timeString, Long currentTime){
        try{
            if(timeString.length() > 0){
                long timeDiff = ((format.parse(timeString).getTime() - currentTime) / (1000 * 60)) % 60;
                if(timeDiff < 0){
                    return "0";
                }
                return String.valueOf(((format.parse(timeString).getTime() - currentTime) / (1000 * 60)) % 60) ;
            }
        } catch (ParseException e) {
            System.out.println("EEROR " + timeString.length());
            e.printStackTrace();
            return "?";
        }
        return "";
    }

    public static Boolean CheckMetadataUpdaterequired(SharedPreferences sharedPref){
//        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        Long lastMetadataUpdate = sharedPref.getLong("lastMetadataUpdate", 0);
        Long currentTime = Calendar.getInstance().getTime().getTime();
        Long threshold = Long.valueOf(24 * 3600000); //24 hours
        if (currentTime - lastMetadataUpdate > threshold){
            return true;
        }else{
            return false;
        }
    }

    public static JSONObject GetBusStopsMetadata(SharedPreferences sharedPref){
        String strJson = sharedPref.getString("busStops",null);
        try {
            if (strJson != null) {
                return new JSONObject(strJson);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("ERROR");
        }
        return null;
    }

    public static JSONObject ProcessBusStopsMetadata(JSONObject busStopsJo){
        try {
            JSONArray busStopsJa = busStopsJo.getJSONArray("value");
            JSONObject outJo = new JSONObject();
            for (int i = 0; i < busStopsJa.length(); i++) {
                JSONObject jo = busStopsJa.getJSONObject(i);
                outJo.put(jo.getString("BusStopCode"), jo);
            }
            return outJo;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String ConcatJsonStrings(String s1, String s2){
        return s1.replaceAll(".$", ",") + s2.replaceAll("^.", "");
    }

    public static JSONObject ConcatJo(JSONObject jo1, JSONObject jo2){
        Iterator itr = jo2.keys();
        while(itr.hasNext()) {
            String key = (String) itr.next();
            try {
                jo1.put(key, jo2.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jo1;
    }

    public static JSONObject GetStoredBusStopMetadata(SharedPreferences sharedPref, String busStopCode){
        try {
            JSONObject busStopsJo = GetBusStopsMetadata(sharedPref);
            return busStopsJo.getJSONObject(busStopCode);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
