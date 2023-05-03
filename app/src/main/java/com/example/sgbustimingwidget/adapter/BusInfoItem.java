package com.example.sgbustimingwidget.adapter;

import com.example.sgbustimingwidget.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

//public class BusInfoItem {
//    private String busNo;
//    private String busStopCode;
//    private String busStopName;
//    private String [] arrivalTimeArr;
//    private Map<String, String> busStopMetadata;
//
//    public BusInfoItem(String busNo, String[] arrivalTimeArr, String busStopCode,  Map<String, String> busStopMetadata) throws JSONException {
//        this.busNo = busNo;
//        this.arrivalTimeArr = arrivalTimeArr;
//        this.busStopCode = busStopCode;
//        this.busStopMetadata = busStopMetadata;
//        System.out.println("AHHH: " + busStopMetadata.toString());
//        busStopName = busStopMetadata.get("description");
//    }
//
//    public String getBusNo(){
//        return busNo;
//    }
//
//    public String[] getArrivalTimeArr(){
//        return arrivalTimeArr;
//    }
//
//    public String getBusStopCode(){
//        return busStopCode;
//    }
//
//    public String getBusStopName(){
//        return busStopName;
//    }
//
//    public Map<String, String> getBusStopMetadata(){
//        return busStopMetadata;
//    }
//
//
//}

public class BusInfoItem {
    private String busNo;
    private String busStopCode;
    private String busStopName;
    private String [] arrivalTimeArr;
    private Map<String, String> busStopMetadata;

    public BusInfoItem(String busNo, String[] arrivalTimeArr, String busStopCode,  Map<String, String> busStopMetadata) throws JSONException {
        this.busNo = busNo;
        this.arrivalTimeArr = arrivalTimeArr;
        this.busStopCode = busStopCode;
        this.busStopMetadata = busStopMetadata;
        System.out.println("AHHH: " + busStopMetadata.toString());
        busStopName = busStopMetadata.get("description");
    }

    public String getBusNo(){
        return busNo;
    }

    public String[] getArrivalTimeArr(){
        return arrivalTimeArr;
    }

    public String getBusStopCode(){
        return busStopCode;
    }

    public String getBusStopName(){
        return busStopName;
    }

    public Map<String, String> getBusStopMetadata(){
        return busStopMetadata;
    }


}
